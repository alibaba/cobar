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
package com.alibaba.cobar.config.model;

/**
 * 用于描述一个数据节点的配置
 * 
 * @author xianmao.hexm
 */
public final class DataNodeConfig {

    private static final int DEFAULT_POOL_SIZE = 128;
    private static final long DEFAULT_WAIT_TIMEOUT = 10 * 1000L;
    private static final long DEFAULT_IDLE_TIMEOUT = 10 * 60 * 1000L;
    private static final long DEFAULT_HEARTBEAT_TIMEOUT = 30 * 1000L;
    private static final int DEFAULT_HEARTBEAT_RETRY = 10;

    private String name;
    private String dataSource;
    private int poolSize = DEFAULT_POOL_SIZE;// 保持后端数据通道的默认最大值
    private long waitTimeout = DEFAULT_WAIT_TIMEOUT; // 取得新连接的等待超时时间
    private long idleTimeout = DEFAULT_IDLE_TIMEOUT; // 连接池中连接空闲超时时间

    // heartbeat config
    private long heartbeatTimeout = DEFAULT_HEARTBEAT_TIMEOUT; // 心跳超时时间
    private int heartbeatRetry = DEFAULT_HEARTBEAT_RETRY; // 检查连接发生异常到切换，重试次数
    private String heartbeatSQL;// 静态心跳语句

    public String getHeartbeatSQL() {
        return heartbeatSQL;
    }

    public void setHeartbeatSQL(String heartbeatSQL) {
        this.heartbeatSQL = heartbeatSQL;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public long getWaitTimeout() {
        return waitTimeout;
    }

    public void setWaitTimeout(long waitTimeout) {
        this.waitTimeout = waitTimeout;
    }

    public long getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(long idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public long getHeartbeatTimeout() {
        return heartbeatTimeout;
    }

    public void setHeartbeatTimeout(long heartbeatTimeout) {
        this.heartbeatTimeout = heartbeatTimeout;
    }

    public int getHeartbeatRetry() {
        return heartbeatRetry;
    }

    public void setHeartbeatRetry(int heartbeatRetry) {
        this.heartbeatRetry = heartbeatRetry;
    }

    public boolean isNeedHeartbeat() {
        return heartbeatSQL != null;
    }

}
