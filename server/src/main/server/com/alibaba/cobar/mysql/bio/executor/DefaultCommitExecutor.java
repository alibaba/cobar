/*
 * Copyright 1999-2012 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * (created at 2011-12-19)
 */
package com.alibaba.cobar.mysql.bio.executor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.alibaba.cobar.config.ErrorCode;
import com.alibaba.cobar.exception.UnknownPacketException;
import com.alibaba.cobar.mysql.bio.Channel;
import com.alibaba.cobar.mysql.bio.MySQLChannel;
import com.alibaba.cobar.net.mysql.BinaryPacket;
import com.alibaba.cobar.net.mysql.ErrorPacket;
import com.alibaba.cobar.net.mysql.OkPacket;
import com.alibaba.cobar.route.RouteResultsetNode;
import com.alibaba.cobar.server.ServerConnection;
import com.alibaba.cobar.server.session.BlockingSession;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class DefaultCommitExecutor extends NodeExecutor {
    private static final Logger LOGGER = Logger.getLogger(DefaultCommitExecutor.class);

    private AtomicBoolean isFail = new AtomicBoolean(false);
    private int nodeCount;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition taskFinished = lock.newCondition();
    private volatile OkPacket indicatedOK;

    protected Logger getLogger() {
        return LOGGER;
    }

    protected String getErrorMessage() {
        return "commit";
    }

    @Override
    public void terminate() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            while (nodeCount > 0) {
                taskFinished.await();
            }
        } finally {
            lock.unlock();
        }
    }

    private void decrementCountToZero() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            nodeCount = 0;
            taskFinished.signalAll();
        } finally {
            lock.unlock();
        }
    }

    /**
     * @param finish how many tasks finished
     * @return is this last task
     */
    private boolean decrementCountBy(int finished) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            boolean last = (nodeCount -= finished) <= 0;
            taskFinished.signalAll();
            return last;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 提交事务
     */
    public void commit(final OkPacket packet, final BlockingSession session, final int initCount) {
        // 初始化
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            this.isFail.set(false);
            this.nodeCount = initCount;
            this.indicatedOK = packet;
        } finally {
            lock.unlock();
        }

        if (session.getSource().isClosed()) {
            decrementCountToZero();
            return;
        }

        // 执行
        final ConcurrentMap<RouteResultsetNode, Channel> target = session.getTarget();
        Executor executor = session.getSource().getProcessor().getExecutor();
        int started = 0;
        for (RouteResultsetNode rrn : target.keySet()) {
            if (rrn == null) {
                try {
                    getLogger().error(
                            "null is contained in RoutResultsetNodes, source = " + session.getSource()
                                    + ", bindChannel = " + target);
                } catch (Exception e) {
                }
                continue;
            }
            final MySQLChannel mc = (MySQLChannel) target.get(rrn);
            if (mc != null) {
                mc.setRunning(true);
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        _commit(mc, session);
                    }
                });
                ++started;
            }
        }

        if (started < initCount && decrementCountBy(initCount - started)) {
            /**
             * assumption: only caused by front-end connection close. <br/>
             * Otherwise, packet must be returned to front-end
             */
            session.clear();
        }
    }

    private void _commit(MySQLChannel mc, BlockingSession session) {
        ServerConnection source = session.getSource();
        if (isFail.get() || source.isClosed()) {
            mc.setRunning(false);
            try {
                throw new Exception("other task fails, commit cancel");
            } catch (Exception e) {
                handleException(mc, session, e);
            }
            return;
        }

        try {
            BinaryPacket bin = mc.commit();
            switch (bin.data[0]) {
            case OkPacket.FIELD_COUNT:
                mc.setRunning(false);
                if (decrementCountBy(1)) {
                    try {
                        if (isFail.get()) { // some other tasks failed
                            session.clear();
                            source.writeErrMessage(ErrorCode.ER_YES, getErrorMessage() + " error!");
                        } else { // all tasks are successful
                            session.release();
                            if (indicatedOK != null) {
                                indicatedOK.write(source);
                            } else {
                                ByteBuffer buffer = source.allocate();
                                source.write(bin.write(buffer, source));
                            }
                        }
                    } catch (Exception e) {
                        getLogger().warn("exception happens in success notification: " + source, e);
                    }
                }
                break;
            case ErrorPacket.FIELD_COUNT:
                mc.setRunning(false);
                isFail.set(true);
                if (decrementCountBy(1)) {
                    try {
                        session.clear();
                        getLogger().warn(mc.getErrLog(getErrorMessage(), mc.getErrMessage(bin), source));
                        ByteBuffer buffer = source.allocate();
                        source.write(bin.write(buffer, source));
                    } catch (Exception e) {
                        getLogger().warn("exception happens in failure notification: " + source, e);
                    }
                }
                break;
            default:
                throw new UnknownPacketException(bin.toString());
            }
        } catch (IOException e) {
            mc.close();
            handleException(mc, session, e);
        } catch (RuntimeException e) {
            mc.close();
            handleException(mc, session, e);
        }
    }

    private void handleException(Channel mc, BlockingSession session, Exception e) {
        isFail.set(true);
        if (decrementCountBy(1)) {
            try {
                session.clear();
                ServerConnection sc = session.getSource();
                getLogger().warn(new StringBuilder().append(sc).append(mc).append(getErrorMessage()).toString(), e);
                String msg = e.getMessage();
                sc.writeErrMessage(ErrorCode.ER_YES, msg == null ? e.getClass().getSimpleName() : msg);
            } catch (Exception e2) {
                getLogger().warn("exception happens in failure notification: " + session.getSource(), e2);
            }
        }
    }

}
