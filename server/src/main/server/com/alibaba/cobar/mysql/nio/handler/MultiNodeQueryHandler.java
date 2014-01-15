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
 * (created at 2012-4-19)
 */
package com.alibaba.cobar.mysql.nio.handler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.alibaba.cobar.CobarConfig;
import com.alibaba.cobar.CobarServer;
import com.alibaba.cobar.mysql.MySQLDataNode;
import com.alibaba.cobar.mysql.nio.MySQLConnection;
import com.alibaba.cobar.net.mysql.ErrorPacket;
import com.alibaba.cobar.net.mysql.OkPacket;
import com.alibaba.cobar.route.RouteResultsetNode;
import com.alibaba.cobar.server.ServerConnection;
import com.alibaba.cobar.server.session.NonBlockingSession;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class MultiNodeQueryHandler extends MultiNodeHandler {
    private static final Logger LOGGER = Logger.getLogger(MultiNodeQueryHandler.class);

    private final RouteResultsetNode[] route;
    private final NonBlockingSession session;
    private final boolean autocommit;
    private final CommitNodeHandler icHandler;

    public MultiNodeQueryHandler(RouteResultsetNode[] route, boolean autocommit, NonBlockingSession session) {
        super(session);
        if (route == null) {
            throw new IllegalArgumentException("routeNode is null!");
        }
        this.session = session;
        this.route = route;
        this.autocommit = autocommit;
        this.lock = new ReentrantLock();
        this.icHandler = new CommitNodeHandler(session);
    }

    private final ReentrantLock lock;
    private long affectedRows;
    private long insertId;
    private ByteBuffer buffer;
    private boolean fieldsReturned;

    public void execute() throws Exception {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            this.reset(route.length);
            this.fieldsReturned = false;
            this.affectedRows = 0L;
            this.insertId = 0L;
            this.buffer = session.getSource().allocate();
        } finally {
            lock.unlock();
        }

        if (session.closed()) {
            decrementCountToZero();
            recycleResources();
            return;
        }

        session.setConnectionRunning(route);

        ThreadPoolExecutor executor = session.getSource().getProcessor().getExecutor();
        for (final RouteResultsetNode node : route) {
            final MySQLConnection conn = session.getTarget(node);
            if (conn != null) {
                conn.setAttachment(node);
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        _execute(conn, node);
                    }
                });
            } else {
                CobarConfig conf = CobarServer.getInstance().getConfig();
                MySQLDataNode dn = conf.getDataNodes().get(node.getName());
                dn.getConnection(this, node);
            }
        }
    }

    private void _execute(MySQLConnection conn, RouteResultsetNode node) {
        conn.setResponseHandler(this);

        if (session.closed()) {
            backendConnError(conn, "failed or cancelled by other thread");
            return;
        }

        try {
            conn.execute(node, session.getSource(), autocommit);
        } catch (IOException e) {
            connectionError(e, conn);
        }
    }

    @Override
    protected void recycleResources() {
        ByteBuffer buf;
        lock.lock();
        try {
            buf = buffer;
            if (buf != null) {
                buffer = null;
            }
        } finally {
            lock.unlock();
        }
        if (buf != null) {
            session.getSource().recycle(buf);
        }
    }

    @Override
    public void connectionAcquired(final MySQLConnection conn) {
        Object attachment = conn.getAttachment();
        if (!(attachment instanceof RouteResultsetNode)) {
            backendConnError(conn, new StringBuilder().append("wrong attachement from connection build: ")
                                                      .append(conn)
                                                      .append(" bound by ")
                                                      .append(session.getSource())
                                                      .toString());
            conn.release();
            return;
        }
        final RouteResultsetNode node = (RouteResultsetNode) attachment;
        conn.setRunning(true);
        session.bindConnection(node, conn);
        session.getSource().getProcessor().getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                _execute(conn, node);
            }
        });
    }

    @Override
    public void connectionError(Throwable e, MySQLConnection conn) {
        backendConnError(conn, "connection err!");
    }

    @Override
    public void errorResponse(byte[] data, MySQLConnection conn) {
        ErrorPacket err = new ErrorPacket();
        err.read(data);
        backendConnError(conn, err);
    }

    @Override
    public void okResponse(byte[] data, MySQLConnection conn) {
        boolean executeResponse = false;
        try {
            executeResponse = conn.syncAndExcute();
        } catch (UnsupportedEncodingException e) {
            connectionError(e, conn);
        }
        if (executeResponse) {
            ServerConnection source = session.getSource();
            conn.setRunning(false);
            Object attachment = conn.getAttachment();
            if (attachment instanceof RouteResultsetNode) {
                RouteResultsetNode node = (RouteResultsetNode) attachment;
                conn.recordSql(source.getHost(), source.getSchema(), node.getStatement());
            } else {
                LOGGER.warn(new StringBuilder().append("back-end conn: ")
                                               .append(conn)
                                               .append(" has wrong attachment: ")
                                               .append(attachment)
                                               .append(", for front-end conn: ")
                                               .append(source));
            }
            OkPacket ok = new OkPacket();
            ok.read(data);
            lock.lock();
            try {
                affectedRows += ok.affectedRows;
                if (ok.insertId > 0) {
                    insertId = (insertId == 0) ? ok.insertId : Math.min(insertId, ok.insertId);
                }
            } finally {
                lock.unlock();
            }
            if (decrementCountBy(1)) {
                if (isFail.get()) {
                    notifyError();
                    return;
                }
                try {
                    recycleResources();
                    ok.packetId = ++packetId;// OK_PACKET
                    ok.affectedRows = affectedRows;
                    if (insertId > 0) {
                        ok.insertId = insertId;
                        source.setLastInsertId(insertId);
                    }

                    if (source.isAutocommit()) {
                        if (!autocommit) { // 前端非事务模式，后端事务模式，则需要自动递交后端事务。
                            icHandler.commit();
                        } else {
                            session.releaseConnections();
                            ok.write(source);
                        }
                    } else {
                        ok.write(source);
                    }
                } catch (Exception e) {
                    LOGGER.warn("exception happens in success notification: " + session.getSource(), e);
                }
            }
        }
    }

    @Override
    public void rowEofResponse(byte[] eof, MySQLConnection conn) {
        conn.setRunning(false);
        ServerConnection source = session.getSource();
        RouteResultsetNode node = null;
        Object attachment = conn.getAttachment();
        if (attachment instanceof RouteResultsetNode) {
            node = (RouteResultsetNode) attachment;
            conn.recordSql(source.getHost(), source.getSchema(), node.getStatement());
        } else {
            LOGGER.warn(new StringBuilder().append("back-end conn: ")
                                           .append(conn)
                                           .append(" has wrong attachment: ")
                                           .append(attachment)
                                           .append(", for front-end conn: ")
                                           .append(source));
        }
        if (source.isAutocommit()) {
            if (node != null) {
                conn = session.removeTarget(node);
                if (conn != null) {
                    if (isFail.get() || session.closed()) {
                        conn.quit();
                    } else {
                        conn.release();
                    }
                }
            }
        }
        if (decrementCountBy(1)) {
            if (isFail.get()) {
                notifyError();
                recycleResources();
                return;
            }
            try {
                if (source.isAutocommit()) {
                    session.releaseConnections();
                }
                eof[3] = ++packetId;

                source.write(source.writeToBuffer(eof, buffer));
            } catch (Exception e) {
                LOGGER.warn("exception happens in success notification: " + session.getSource(), e);
            }
        }
    }

    @Override
    public void fieldEofResponse(byte[] header, List<byte[]> fields, byte[] eof, MySQLConnection conn) {
        lock.lock();
        try {
            if (fieldsReturned) {
                return;
            }
            fieldsReturned = true;
            header[3] = ++packetId;
            ServerConnection source = session.getSource();
            buffer = source.writeToBuffer(header, buffer);
            for (int i = 0, len = fields.size(); i < len; ++i) {
                byte[] field = fields.get(i);
                field[3] = ++packetId;
                buffer = source.writeToBuffer(field, buffer);
            }
            eof[3] = ++packetId;
            buffer = source.writeToBuffer(eof, buffer);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void rowResponse(byte[] row, MySQLConnection conn) {
        lock.lock();
        try {
            row[3] = ++packetId;
            buffer = session.getSource().writeToBuffer(row, buffer);
        } finally {
            lock.unlock();
        }
    }

}
