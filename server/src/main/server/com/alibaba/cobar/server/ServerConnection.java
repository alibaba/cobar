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
package com.alibaba.cobar.server;

import java.io.EOFException;
import java.nio.channels.SocketChannel;
import java.sql.SQLNonTransientException;

import org.apache.log4j.Logger;

import com.alibaba.cobar.CobarServer;
import com.alibaba.cobar.config.ErrorCode;
import com.alibaba.cobar.config.model.SchemaConfig;
import com.alibaba.cobar.net.FrontendConnection;
import com.alibaba.cobar.route.RouteResultset;
import com.alibaba.cobar.route.ServerRouter;
import com.alibaba.cobar.server.response.Heartbeat;
import com.alibaba.cobar.server.response.Ping;
import com.alibaba.cobar.server.session.BlockingSession;
import com.alibaba.cobar.server.session.NonBlockingSession;
import com.alibaba.cobar.util.TimeUtil;

/**
 * @author xianmao.hexm 2011-4-21 上午11:22:57
 */
public class ServerConnection extends FrontendConnection {
    private static final Logger LOGGER = Logger.getLogger(ServerConnection.class);
    private static final long AUTH_TIMEOUT = 15 * 1000L;

    private volatile int txIsolation;
    private volatile boolean autocommit;
    private volatile boolean txInterrupted;
    private long lastInsertId;
    private BlockingSession session;
    private NonBlockingSession session2;

    public ServerConnection(SocketChannel channel) {
        super(channel);
        this.txInterrupted = false;
        this.autocommit = true;
    }

    @Override
    public boolean isIdleTimeout() {
        if (isAuthenticated) {
            return super.isIdleTimeout();
        } else {
            return TimeUtil.currentTimeMillis() > Math.max(lastWriteTime, lastReadTime) + AUTH_TIMEOUT;
        }
    }

    public int getTxIsolation() {
        return txIsolation;
    }

    public void setTxIsolation(int txIsolation) {
        this.txIsolation = txIsolation;
    }

    public boolean isAutocommit() {
        return autocommit;
    }

    public void setAutocommit(boolean autocommit) {
        this.autocommit = autocommit;
    }

    public long getLastInsertId() {
        return lastInsertId;
    }

    public void setLastInsertId(long lastInsertId) {
        this.lastInsertId = lastInsertId;
    }

    /**
     * 设置是否需要中断当前事务
     */
    public void setTxInterrupt() {
        if (!autocommit && !txInterrupted) {
            txInterrupted = true;
        }
    }

    public BlockingSession getSession() {
        return session;
    }

    public void setSession(BlockingSession session) {
        this.session = session;
    }

    public NonBlockingSession getSession2() {
        return session2;
    }

    public void setSession2(NonBlockingSession session2) {
        this.session2 = session2;
    }

    @Override
    public void ping() {
        Ping.response(this);
    }

    @Override
    public void heartbeat(byte[] data) {
        Heartbeat.response(this, data);
    }

    public void execute(String sql, int type) {
        // 状态检查
        if (txInterrupted) {
            writeErrMessage(ErrorCode.ER_YES, "Transaction error, need to rollback.");
            return;
        }

        // 检查当前使用的DB
        String db = this.schema;
        if (db == null) {
            writeErrMessage(ErrorCode.ER_NO_DB_ERROR, "No database selected");
            return;
        }
        SchemaConfig schema = CobarServer.getInstance().getConfig().getSchemas().get(db);
        if (schema == null) {
            writeErrMessage(ErrorCode.ER_BAD_DB_ERROR, "Unknown database '" + db + "'");
            return;
        }

        // 路由计算
        RouteResultset rrs = null;
        try {
            rrs = ServerRouter.route(schema, sql, this.charset, this);
        } catch (SQLNonTransientException e) {
            StringBuilder s = new StringBuilder();
            LOGGER.warn(s.append(this).append(sql).toString(), e);
            String msg = e.getMessage();
            writeErrMessage(ErrorCode.ER_PARSE_ERROR, msg == null ? e.getClass().getSimpleName() : msg);
            return;
        }

        // session执行
        session.execute(rrs, type);
    }

    /**
     * 提交事务
     */
    public void commit() {
        if (txInterrupted) {
            writeErrMessage(ErrorCode.ER_YES, "Transaction error, need to rollback.");
        } else {
            session.commit();
        }
    }

    /**
     * 回滚事务
     */
    public void rollback() {
        // 状态检查
        if (txInterrupted) {
            txInterrupted = false;
        }

        // 执行回滚
        session.rollback();
    }

    /**
     * 撤销执行中的语句
     * 
     * @param sponsor 发起者为null表示是自己
     */
    public void cancel(final FrontendConnection sponsor) {
        processor.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                session.cancel(sponsor);
            }
        });
    }

    @Override
    public void error(int errCode, Throwable t) {
        // 根据异常类型和信息，选择日志输出级别。
        if (t instanceof EOFException) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(toString(), t);
            }
        } else if (isConnectionReset(t)) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(toString(), t);
            }
        } else {
            LOGGER.warn(toString(), t);
        }

        // 异常返回码处理
        switch (errCode) {
        case ErrorCode.ERR_HANDLE_DATA:
            String msg = t.getMessage();
            writeErrMessage(ErrorCode.ER_YES, msg == null ? t.getClass().getSimpleName() : msg);
            break;
        default:
            close();
        }
    }

    @Override
    public boolean close() {
        if (super.close()) {
            processor.getExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    session.terminate();
                }
            });
            return true;
        } else {
            return false;
        }
    }

}
