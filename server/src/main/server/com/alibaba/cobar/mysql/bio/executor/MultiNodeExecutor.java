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

import static com.alibaba.cobar.route.RouteResultsetNode.DEFAULT_REPLICA_INDEX;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.alibaba.cobar.CobarConfig;
import com.alibaba.cobar.CobarServer;
import com.alibaba.cobar.config.ErrorCode;
import com.alibaba.cobar.exception.UnknownDataNodeException;
import com.alibaba.cobar.mysql.MySQLDataNode;
import com.alibaba.cobar.mysql.PacketUtil;
import com.alibaba.cobar.mysql.bio.Channel;
import com.alibaba.cobar.mysql.bio.MySQLChannel;
import com.alibaba.cobar.net.mysql.BinaryPacket;
import com.alibaba.cobar.net.mysql.EOFPacket;
import com.alibaba.cobar.net.mysql.ErrorPacket;
import com.alibaba.cobar.net.mysql.FieldPacket;
import com.alibaba.cobar.net.mysql.MySQLPacket;
import com.alibaba.cobar.net.mysql.OkPacket;
import com.alibaba.cobar.route.RouteResultset;
import com.alibaba.cobar.route.RouteResultsetNode;
import com.alibaba.cobar.server.ServerConnection;
import com.alibaba.cobar.server.session.BlockingSession;
import com.alibaba.cobar.util.StringUtil;

/**
 * 多数据节点执行器
 * 
 * @author xianmao.hexm
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public final class MultiNodeExecutor extends NodeExecutor {
    private static final Logger LOGGER = Logger.getLogger(MultiNodeExecutor.class);
    private static final int RECEIVE_CHUNK_SIZE = 16 * 1024;

    private AtomicBoolean isFail = new AtomicBoolean(false);
    private int unfinishedNodeCount;
    private int errno;
    private String errMessage;
    private boolean fieldEOF;
    private byte packetId;
    private long affectedRows;
    private long insertId;
    private ByteBuffer buffer;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition taskFinished = lock.newCondition();
    private final DefaultCommitExecutor icExecutor = new DefaultCommitExecutor() {
        @Override
        protected String getErrorMessage() {
            return "Internal commit";
        }

        @Override
        protected Logger getLogger() {
            return MultiNodeExecutor.LOGGER;
        }

    };

    @Override
    public void terminate() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            while (unfinishedNodeCount > 0) {
                taskFinished.await();
            }
        } finally {
            lock.unlock();
        }
        icExecutor.terminate();
    }

    private void decrementCountToZero() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            unfinishedNodeCount = 0;
            taskFinished.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private boolean decrementCountAndIsZero() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int ufc = --unfinishedNodeCount;
            taskFinished.signalAll();
            return ufc <= 0;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 多数据节点执行
     * 
     * @param nodes never null
     */
    public void execute(RouteResultsetNode[] nodes, final boolean autocommit, final BlockingSession ss, final int flag) {
        // 初始化
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            this.isFail.set(false);
            this.unfinishedNodeCount = nodes.length;
            this.errno = 0;
            this.errMessage = null;
            this.fieldEOF = false;
            this.packetId = 0;
            this.affectedRows = 0L;
            this.insertId = 0L;
            this.buffer = ss.getSource().allocate();
        } finally {
            lock.unlock();
        }

        if (ss.getSource().isClosed()) {
            decrementCountToZero();
            ss.getSource().recycle(this.buffer);
            return;
        }

        // 多节点处理
        ConcurrentMap<RouteResultsetNode, Channel> target = ss.getTarget();
        for (RouteResultsetNode rrn : nodes) {
            Channel c = target.get(rrn);
            if (c != null) {
                c.setRunning(true);
            }
        }

        ThreadPoolExecutor exec = ss.getSource().getProcessor().getExecutor();
        for (final RouteResultsetNode rrn : nodes) {
            final Channel c = target.get(rrn);
            if (c != null) {
                exec.execute(new Runnable() {
                    @Override
                    public void run() {
                        execute0(rrn, c, autocommit, ss, flag);
                    }
                });
            } else {
                newExecute(rrn, autocommit, ss, flag);
            }
        }
    }

    /**
     * 新通道的执行
     */
    private void newExecute(final RouteResultsetNode rrn, final boolean autocommit, final BlockingSession ss,
                            final int flag) {
        final ServerConnection sc = ss.getSource();

        // 检查数据节点是否存在
        CobarConfig conf = CobarServer.getInstance().getConfig();
        final MySQLDataNode dn = conf.getDataNodes().get(rrn.getName());
        if (dn == null) {
            handleFailure(ss, rrn, new SimpleErrInfo(new UnknownDataNodeException("Unknown dataNode '" + rrn.getName()
                    + "'"), ErrorCode.ER_BAD_DB_ERROR, sc, rrn));
            return;
        }

        // 提交执行任务
        sc.getProcessor().getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                // 取得数据通道
                int i = rrn.getReplicaIndex();
                Channel c = null;
                try {
                    c = (i == DEFAULT_REPLICA_INDEX) ? dn.getChannel() : dn.getChannel(i);
                } catch (final Exception e) {
                    handleFailure(ss, rrn, new SimpleErrInfo(e, ErrorCode.ER_BAD_DB_ERROR, sc, rrn));
                    return;
                }

                c.setRunning(true);
                Channel old = ss.getTarget().put(rrn, c);
                if (old != null && c != old) {
                    old.close();
                }

                // 执行
                execute0(rrn, c, autocommit, ss, flag);
            }
        });
    }

    /**
     * 执行
     */
    private void execute0(RouteResultsetNode rrn, Channel c, boolean autocommit, BlockingSession ss, int flag) {
        ServerConnection sc = ss.getSource();
        if (isFail.get() || sc.isClosed()) {
            c.setRunning(false);
            handleFailure(ss, rrn, null);
            return;
        }

        try {
            // 执行并等待返回
            BinaryPacket bin = ((MySQLChannel) c).execute(rrn, sc, autocommit);

            // 接收和处理数据
            final ReentrantLock lock = MultiNodeExecutor.this.lock;
            lock.lock();
            try {
                switch (bin.data[0]) {
                case ErrorPacket.FIELD_COUNT:
                    c.setRunning(false);
                    handleFailure(ss, rrn, new BinaryErrInfo((MySQLChannel) c, bin, sc, rrn));
                    break;
                case OkPacket.FIELD_COUNT:
                    OkPacket ok = new OkPacket();
                    ok.read(bin);
                    affectedRows += ok.affectedRows;
                    // set lastInsertId
                    if (ok.insertId > 0) {
                        insertId = (insertId == 0) ? ok.insertId : Math.min(insertId, ok.insertId);
                    }
                    c.setRunning(false);
                    handleSuccessOK(ss, rrn, autocommit, ok);
                    break;
                default: // HEADER|FIELDS|FIELD_EOF|ROWS|LAST_EOF
                    final MySQLChannel mc = (MySQLChannel) c;
                    if (fieldEOF) {
                        for (;;) {
                            bin = mc.receive();
                            switch (bin.data[0]) {
                            case ErrorPacket.FIELD_COUNT:
                                c.setRunning(false);
                                handleFailure(ss, rrn, new BinaryErrInfo(mc, bin, sc, rrn));
                                return;
                            case EOFPacket.FIELD_COUNT:
                                handleRowData(rrn, c, ss);
                                return;
                            default:
                                continue;
                            }
                        }
                    } else {
                        bin.packetId = ++packetId;// HEADER
                        List<MySQLPacket> headerList = new LinkedList<MySQLPacket>();
                        headerList.add(bin);
                        for (;;) {
                            bin = mc.receive();
                            switch (bin.data[0]) {
                            case ErrorPacket.FIELD_COUNT:
                                c.setRunning(false);
                                handleFailure(ss, rrn, new BinaryErrInfo(mc, bin, sc, rrn));
                                return;
                            case EOFPacket.FIELD_COUNT:
                                bin.packetId = ++packetId;// FIELD_EOF
                                for (MySQLPacket packet : headerList) {
                                    buffer = packet.write(buffer, sc);
                                }
                                headerList = null;
                                buffer = bin.write(buffer, sc);
                                fieldEOF = true;
                                handleRowData(rrn, c, ss);
                                return;
                            default:
                                bin.packetId = ++packetId;// FIELDS
                                switch (flag) {
                                case RouteResultset.REWRITE_FIELD:
                                    StringBuilder fieldName = new StringBuilder();
                                    fieldName.append("Tables_in_").append(ss.getSource().getSchema());
                                    FieldPacket field = PacketUtil.getField(bin, fieldName.toString());
                                    headerList.add(field);
                                    break;
                                default:
                                    headerList.add(bin);
                                }
                            }
                        }
                    }
                }
            } finally {
                lock.unlock();
            }
        } catch (final IOException e) {
            c.close();
            handleFailure(ss, rrn, new SimpleErrInfo(e, ErrorCode.ER_YES, sc, rrn));
        } catch (final RuntimeException e) {
            c.close();
            handleFailure(ss, rrn, new SimpleErrInfo(e, ErrorCode.ER_YES, sc, rrn));
        }
    }

    /**
     * 处理RowData数据
     */
    private void handleRowData(final RouteResultsetNode rrn, Channel c, BlockingSession ss) throws IOException {
        final ServerConnection source = ss.getSource();
        BinaryPacket bin = null;
        int size = 0;
        for (;;) {
            bin = ((MySQLChannel) c).receive();
            switch (bin.data[0]) {
            case ErrorPacket.FIELD_COUNT:
                c.setRunning(false);
                handleFailure(ss, rrn, new BinaryErrInfo(((MySQLChannel) c), bin, source, rrn));
                return;
            case EOFPacket.FIELD_COUNT:
                c.setRunning(false);
                if (source.isAutocommit()) {
                    c = ss.getTarget().remove(rrn);
                    if (c != null) {
                        if (isFail.get() || source.isClosed()) {
                            /**
                             * this {@link Channel} might be closed by other
                             * thread in this condition, so that do not release
                             * this channel
                             */
                            c.close();
                        } else {
                            c.release();
                        }
                    }
                }
                handleSuccessEOF(ss, bin);
                return;
            default:
                bin.packetId = ++packetId;// ROWS
                buffer = bin.write(buffer, source);
                size += bin.packetLength;
                if (size > RECEIVE_CHUNK_SIZE) {
                    handleNext(rrn, c, ss);
                    return;
                }
            }
        }
    }

    /**
     * 处理下一个任务
     */
    private void handleNext(final RouteResultsetNode rrn, final Channel c, final BlockingSession ss) {
        final ServerConnection sc = ss.getSource();
        sc.getProcessor().getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                final ReentrantLock lock = MultiNodeExecutor.this.lock;
                lock.lock();
                try {
                    handleRowData(rrn, c, ss);
                } catch (final IOException e) {
                    c.close();
                    handleFailure(ss, rrn, new SimpleErrInfo(e, ErrorCode.ER_YES, sc, rrn));
                } catch (final RuntimeException e) {
                    c.close();
                    handleFailure(ss, rrn, new SimpleErrInfo(e, ErrorCode.ER_YES, sc, rrn));
                } finally {
                    lock.unlock();
                }
            }
        });
    }

    /**
     * @throws nothing never throws any exception
     */
    private void handleSuccessEOF(BlockingSession ss, BinaryPacket bin) {
        if (decrementCountAndIsZero()) {
            if (isFail.get()) {
                notifyFailure(ss);
                return;
            }
            try {
                ServerConnection source = ss.getSource();
                if (source.isAutocommit()) {
                    ss.release();
                }

                bin.packetId = ++packetId;// LAST_EOF
                source.write(bin.write(buffer, source));
            } catch (Exception e) {
                LOGGER.warn("exception happens in success notification: " + ss.getSource(), e);
            }
        }
    }

    /**
     * @throws nothing never throws any exception
     */
    private void handleSuccessOK(BlockingSession ss, RouteResultsetNode rrn, boolean autocommit, OkPacket ok) {
        if (decrementCountAndIsZero()) {
            if (isFail.get()) {
                notifyFailure(ss);
                return;
            }
            try {
                ServerConnection source = ss.getSource();
                ok.packetId = ++packetId;// OK_PACKET
                ok.affectedRows = affectedRows;
                if (insertId > 0) {
                    ok.insertId = insertId;
                    source.setLastInsertId(insertId);
                }

                if (source.isAutocommit()) {
                    if (!autocommit) { // 前端非事务模式，后端事务模式，则需要自动递交后端事务。
                        icExecutor.commit(ok, ss, ss.getTarget().size());
                    } else {
                        ss.release();
                        ok.write(source);
                    }
                } else {
                    ok.write(source);
                }

                source.recycle(buffer);
            } catch (Exception e) {
                LOGGER.warn("exception happens in success notification: " + ss.getSource(), e);
            }
        }
    }

    private void handleFailure(BlockingSession ss, RouteResultsetNode rrn, ErrInfo errInfo) {
        try {
            // 标记为执行失败，并记录第一次异常信息。
            if (!isFail.getAndSet(true) && errInfo != null) {
                errno = errInfo.getErrNo();
                errMessage = errInfo.getErrMsg();
                errInfo.logErr();
            }
        } catch (Exception e) {
            LOGGER.warn("handleFailure failed in " + getClass().getSimpleName() + ", source = " + ss.getSource(), e);
        }
        if (decrementCountAndIsZero()) {
            notifyFailure(ss);
        }
    }

    /**
     * 通知，执行异常
     * 
     * @throws nothing never throws any exception
     */
    private void notifyFailure(BlockingSession ss) {
        try {
            // 清理
            ss.clear();

            ServerConnection sc = ss.getSource();
            sc.setTxInterrupt();

            // 通知
            ErrorPacket err = new ErrorPacket();
            err.packetId = ++packetId;// ERROR_PACKET
            err.errno = errno;
            err.message = StringUtil.encode(errMessage, sc.getCharset());
            sc.write(err.write(buffer, sc));
        } catch (Exception e) {
            LOGGER.warn("exception happens in failure notification: " + ss.getSource(), e);
        }
    }

    protected static interface ErrInfo {
        int getErrNo();

        String getErrMsg();

        void logErr();
    }

    protected static class BinaryErrInfo implements ErrInfo {
        private String errMsg;
        private int errNo;
        private ServerConnection source;
        private RouteResultsetNode rrn;
        private MySQLChannel mc;

        public BinaryErrInfo(MySQLChannel mc, BinaryPacket bin, ServerConnection sc, RouteResultsetNode rrn) {
            this.mc = mc;
            this.source = sc;
            this.rrn = rrn;
            ErrorPacket err = new ErrorPacket();
            err.read(bin);
            this.errMsg = (err.message == null) ? null : StringUtil.decode(err.message, mc.getCharset());
            this.errNo = err.errno;
        }

        @Override
        public int getErrNo() {
            return errNo;
        }

        @Override
        public String getErrMsg() {
            return errMsg;
        }

        @Override
        public void logErr() {
            try {
                LOGGER.warn(mc.getErrLog(rrn.getStatement(), errMsg, source));
            } catch (Exception e) {
            }
        }
    }

    protected static class SimpleErrInfo implements ErrInfo {
        private Exception e;
        private int errNo;
        private ServerConnection source;
        private RouteResultsetNode rrn;

        public SimpleErrInfo(Exception e, int errNo, ServerConnection sc, RouteResultsetNode rrn) {
            this.e = e;
            this.errNo = errNo;
            this.source = sc;
            this.rrn = rrn;
        }

        @Override
        public int getErrNo() {
            return errNo;
        }

        @Override
        public String getErrMsg() {
            String msg = e.getMessage();
            return msg == null ? e.getClass().getSimpleName() : msg;
        }

        @Override
        public void logErr() {
            try {
                LOGGER.warn(new StringBuilder().append(source).append(rrn).toString(), e);
            } catch (Exception e) {
            }
        }
    }

}
