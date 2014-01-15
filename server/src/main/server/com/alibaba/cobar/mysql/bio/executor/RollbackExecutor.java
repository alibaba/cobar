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
 * 事务回滚执行器
 * 
 * @author xianmao.hexm
 */
public final class RollbackExecutor extends NodeExecutor {
    private static final Logger LOGGER = Logger.getLogger(RollbackExecutor.class);

    private int nodeCount;
    private AtomicBoolean isFail = new AtomicBoolean(false);
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition taskFinished = lock.newCondition();

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
     * 事务回滚
     */
    public void rollback(final BlockingSession session) {
        final ServerConnection source = session.getSource();
        final ConcurrentMap<RouteResultsetNode, Channel> target = session.getTarget();
        final int initNodeCount = target.size();
        if (initNodeCount <= 0) {
            ByteBuffer buffer = source.allocate();
            source.write(source.writeToBuffer(OkPacket.OK, buffer));
            return;
        }

        // 初始化
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            this.isFail.set(false);
            this.nodeCount = initNodeCount;
        } finally {
            lock.unlock();
        }

        if (source.isClosed()) {
            decrementCountToZero();
            return;
        }

        // 执行
        Executor exec = source.getProcessor().getExecutor();

        int started = 0;
        for (RouteResultsetNode rrn : target.keySet()) {
            final MySQLChannel mc = (MySQLChannel) target.get(rrn);
            if (mc != null) {
                mc.setRunning(true);
                exec.execute(new Runnable() {
                    @Override
                    public void run() {
                        _rollback(mc, session);
                    }
                });
                ++started;
            }
        }

        if (started < initNodeCount) {
            decrementCountBy(initNodeCount - started);
        }
    }

    private void _rollback(MySQLChannel mc, BlockingSession session) {
        final ServerConnection source = session.getSource();
        if (isFail.get() || source.isClosed()) {
            mc.setRunning(false);
            try {
                throw new Exception("other task fails, rollback failed channel");
            } catch (Exception e) {
                handleException(mc, session, e);
            }
            return;
        }
        try {
            BinaryPacket bin = mc.rollback();
            switch (bin.data[0]) {
            case OkPacket.FIELD_COUNT:
                mc.setRunning(false);
                if (decrementCountBy(1)) {
                    try {
                        if (isFail.get()) { // some other tasks failed
                            session.clear();
                            source.writeErrMessage(ErrorCode.ER_YES, "rollback error!");
                        } else { // all tasks are successful
                            session.release();
                            ByteBuffer buffer = source.allocate();
                            source.write(bin.write(buffer, source));
                        }
                    } catch (Exception e) {
                        LOGGER.warn("exception happens in success notification: " + source, e);
                    }
                }
                break;
            case ErrorPacket.FIELD_COUNT:
                isFail.set(true);
                if (decrementCountBy(1)) {
                    try {
                        session.clear();
                        LOGGER.warn(mc.getErrLog("rollback", mc.getErrMessage(bin), source));
                        ByteBuffer buffer = source.allocate();
                        source.write(bin.write(buffer, source));
                    } catch (Exception e) {
                        LOGGER.warn("exception happens in failure notification: " + source, e);
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
                ServerConnection source = session.getSource();
                LOGGER.warn(new StringBuilder().append(source).append(mc).append("rollback").toString(), e);
                String msg = e.getMessage();
                source.writeErrMessage(ErrorCode.ER_YES, msg == null ? e.getClass().getSimpleName() : msg);
            } catch (Exception e2) {
                LOGGER.warn("exception happens in failure notification: " + session.getSource(), e2);
            }
        }
    }

}
