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
package com.alibaba.cobar.server.session;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.alibaba.cobar.config.ErrorCode;
import com.alibaba.cobar.exception.UnknownPacketException;
import com.alibaba.cobar.mysql.bio.Channel;
import com.alibaba.cobar.mysql.bio.MySQLChannel;
import com.alibaba.cobar.mysql.bio.executor.DefaultCommitExecutor;
import com.alibaba.cobar.mysql.bio.executor.MultiNodeExecutor;
import com.alibaba.cobar.mysql.bio.executor.NodeExecutor;
import com.alibaba.cobar.mysql.bio.executor.RollbackExecutor;
import com.alibaba.cobar.mysql.bio.executor.SingleNodeExecutor;
import com.alibaba.cobar.net.FrontendConnection;
import com.alibaba.cobar.net.mysql.BinaryPacket;
import com.alibaba.cobar.net.mysql.ErrorPacket;
import com.alibaba.cobar.net.mysql.OkPacket;
import com.alibaba.cobar.route.RouteResultset;
import com.alibaba.cobar.route.RouteResultsetNode;
import com.alibaba.cobar.server.ServerConnection;
import com.alibaba.cobar.server.parser.ServerParse;

/**
 * 由前后端参与的一次执行会话过程
 * 
 * @author xianmao.hexm
 */
public class BlockingSession implements Session {
    private static final Logger LOGGER = Logger.getLogger(BlockingSession.class);

    private final ServerConnection source;
    private final ConcurrentHashMap<RouteResultsetNode, Channel> target;
    private final SingleNodeExecutor singleNodeExecutor;
    private final MultiNodeExecutor multiNodeExecutor;
    private final DefaultCommitExecutor commitExecutor;
    private final RollbackExecutor rollbackExecutor;

    public BlockingSession(ServerConnection source) {
        this.source = source;
        this.target = new ConcurrentHashMap<RouteResultsetNode, Channel>();
        this.singleNodeExecutor = new SingleNodeExecutor();
        this.multiNodeExecutor = new MultiNodeExecutor();
        this.commitExecutor = new DefaultCommitExecutor();
        this.rollbackExecutor = new RollbackExecutor();
    }

    @Override
    public ServerConnection getSource() {
        return source;
    }

    @Override
    public int getTargetCount() {
        return target.size();
    }

    public ConcurrentHashMap<RouteResultsetNode, Channel> getTarget() {
        return target;
    }

    @Override
    public void execute(RouteResultset rrs, int type) {
        if (LOGGER.isDebugEnabled()) {
            StringBuilder s = new StringBuilder();
            LOGGER.debug(s.append(source).append(rrs).toString());
        }

        // 检查路由结果是否为空
        RouteResultsetNode[] nodes = rrs.getNodes();
        if (nodes == null || nodes.length == 0) {
            source.writeErrMessage(ErrorCode.ER_NO_DB_ERROR, "No dataNode selected");
            return;
        }

        // 选择执行方式
        if (nodes.length == 1) {
            singleNodeExecutor.execute(nodes[0], this, rrs.getFlag());
        } else {
            // 多数据节点，非事务模式下，执行的是可修改数据的SQL，则后端为事务模式。
            boolean autocommit = source.isAutocommit();
            if (autocommit && isModifySQL(type)) {
                autocommit = false;
            }
            multiNodeExecutor.execute(nodes, autocommit, this, rrs.getFlag());
        }
    }

    @Override
    public void commit() {
        final int initCount = target.size();
        if (initCount <= 0) {
            ByteBuffer buffer = source.allocate();
            buffer = source.writeToBuffer(OkPacket.OK, buffer);
            source.write(buffer);
            return;
        }
        commitExecutor.commit(null, this, initCount);
    }

    @Override
    public void rollback() {
        rollbackExecutor.rollback(this);
    }

    @Override
    public void cancel(FrontendConnection sponsor) {
        // TODO terminate session
        source.writeErrMessage(ErrorCode.ER_QUERY_INTERRUPTED, "Query execution was interrupted");
        if (sponsor != null) {
            OkPacket packet = new OkPacket();
            packet.packetId = 1;
            packet.affectedRows = 0;
            packet.serverStatus = 2;
            packet.write(sponsor);
        }
    }

    @Override
    public void terminate() {
        // 终止所有正在执行的任务
        kill();

        // 等待所有任务结束，包括还未执行的，执行中的，执行完的。
        try {
            singleNodeExecutor.terminate();
            multiNodeExecutor.terminate();
            commitExecutor.terminate();
            rollbackExecutor.terminate();
        } catch (InterruptedException e) {
            for (RouteResultsetNode rrn : target.keySet()) {
                Channel c = target.remove(rrn);
                if (c != null) {
                    c.close();
                }
            }
            LOGGER.warn("termination interrupted: " + source, e);
        }

        // 清理绑定的资源
        clear(false);
    }

    /**
     * 释放session关联的资源
     */
    public void release() {
        for (RouteResultsetNode rrn : target.keySet()) {
            Channel c = target.remove(rrn);
            if (c != null) {
                if (c.isRunning()) {
                    c.close();
                    try {
                        throw new IllegalStateException("running connection is found: " + c);
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                } else if (!c.isClosed()) {
                    if (source.isClosed()) {
                        c.close();
                    } else {
                        c.release();
                    }
                }
            }
        }
    }

    public void clear() {
        clear(true);
    }

    /**
     * MUST be called at the end of {@link NodeExecutor}
     * 
     * @param pessimisticRelease true if this method might be invoked
     *            concurrently with {@link #kill()}
     */
    private void clear(boolean pessimisticRelease) {
        for (RouteResultsetNode rrn : target.keySet()) {
            Channel c = target.remove(rrn);

            // 通道不存在或者已被关闭
            if (c == null || c.isClosed()) {
                continue;
            }

            // 如果通道正在运行中，则关闭当前通道。
            if (c.isRunning() || (pessimisticRelease && source.isClosed())) {
                c.close();
                continue;
            }

            // 非事务中的通道，直接释放资源。
            if (c.isAutocommit()) {
                c.release();
                continue;
            }

            // 事务中的通道，需要先回滚后再释放资源。
            MySQLChannel mc = (MySQLChannel) c;
            try {
                BinaryPacket bin = mc.rollback();
                switch (bin.data[0]) {
                case OkPacket.FIELD_COUNT:
                    mc.release();
                    break;
                case ErrorPacket.FIELD_COUNT:
                    mc.close();
                    break;
                default:
                    throw new UnknownPacketException(bin.toString());
                }
            } catch (IOException e) {
                StringBuilder s = new StringBuilder();
                LOGGER.warn(s.append(mc).append("rollback").toString(), e);
                mc.close();
            } catch (RuntimeException e) {
                StringBuilder s = new StringBuilder();
                LOGGER.warn(s.append(mc).append("rollback").toString(), e);
                mc.close();
            }
        }
    }

    /**
     * 终止执行中的通道
     */
    private void kill() {
        for (RouteResultsetNode rrn : target.keySet()) {
            Channel c = target.get(rrn);
            if (c != null && c.isRunning()) {
                c.kill();
            }
        }
    }

    /**
     * 检查是否会引起数据变更的语句
     */
    private static boolean isModifySQL(int type) {
        switch (type) {
        case ServerParse.INSERT:
        case ServerParse.DELETE:
        case ServerParse.UPDATE:
        case ServerParse.REPLACE:
            return true;
        default:
            return false;
        }
    }

}
