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
 * (created at 2012-5-3)
 */
package com.alibaba.cobar.mysql.nio.handler;

import java.util.List;
import java.util.concurrent.Executor;

import org.apache.log4j.Logger;

import com.alibaba.cobar.mysql.nio.MySQLConnection;
import com.alibaba.cobar.net.mysql.ErrorPacket;
import com.alibaba.cobar.route.RouteResultsetNode;
import com.alibaba.cobar.server.session.NonBlockingSession;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class RollbackNodeHandler extends MultiNodeHandler {
    private static final Logger logger = Logger.getLogger(RollbackNodeHandler.class);

    public RollbackNodeHandler(NonBlockingSession session) {
        super(session);
    }

    public void rollback() {
        final int initCount = session.getTargetCount();
        lock.lock();
        try {
            reset(initCount);
        } finally {
            lock.unlock();
        }
        if (session.closed()) {
            decrementCountToZero();
            return;
        }

        // 执行
        Executor executor = session.getSource().getProcessor().getExecutor();
        int started = 0;
        for (final RouteResultsetNode node : session.getTargetKeys()) {
            if (node == null) {
                try {
                    logger.error("null is contained in RoutResultsetNodes, source = " + session.getSource());
                } catch (Exception e) {
                }
                continue;
            }
            final MySQLConnection conn = session.getTarget(node);
            if (conn != null) {
                conn.setRunning(true);
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (isFail.get() || session.closed()) {
                            backendConnError(conn, "cancelled by other thread");
                            return;
                        }
                        conn.setResponseHandler(RollbackNodeHandler.this);
                        conn.rollback();
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
            session.clearConnections();
        }
    }

    @Override
    public void okResponse(byte[] ok, MySQLConnection conn) {
        conn.setRunning(false);
        if (decrementCountBy(1)) {
            if (isFail.get() || session.closed()) {
                notifyError((byte) 1);
            } else {
                session.getSource().write(ok);
            }
        }
    }

    @Override
    public void errorResponse(byte[] data, MySQLConnection conn) {
        ErrorPacket err = new ErrorPacket();
        err.read(data);
        backendConnError(conn, err);
    }

    @Override
    public void rowEofResponse(byte[] eof, MySQLConnection conn) {
        backendConnError(conn, "Unknown response packet for back-end rollback");
    }

    @Override
    public void connectionError(Throwable e, MySQLConnection conn) {
        backendConnError(conn, "connection err for " + conn);
    }

    @Override
    public void connectionAcquired(MySQLConnection conn) {
        logger.error("unexpected invocation: connectionAcquired from rollback");
        conn.release();
    }

    @Override
    public void fieldEofResponse(byte[] header, List<byte[]> fields, byte[] eof, MySQLConnection conn) {
        logger.error(new StringBuilder().append("unexpected packet for ")
                                        .append(conn)
                                        .append(" bound by ")
                                        .append(session.getSource())
                                        .append(": field's eof")
                                        .toString());
    }

    @Override
    public void rowResponse(byte[] row, MySQLConnection conn) {
        logger.error(new StringBuilder().append("unexpected packet for ")
                                        .append(conn)
                                        .append(" bound by ")
                                        .append(session.getSource())
                                        .append(": field's eof")
                                        .toString());
    }

}
