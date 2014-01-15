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

import com.alibaba.cobar.config.Isolations;

/**
 * 系统基础配置项
 * 
 * @author xianmao.hexm 2011-1-11 下午02:14:04
 */
public final class SystemConfig {

    private static final int DEFAULT_PORT = 8066;
    private static final int DEFAULT_MANAGER_PORT = 9066;
    private static final String DEFAULT_CHARSET = "UTF-8";
    private static final int DEFAULT_PROCESSORS = Runtime.getRuntime().availableProcessors();
    private static final long DEFAULT_IDLE_TIMEOUT = 8 * 3600 * 1000L;
    private static final long DEFAULT_PROCESSOR_CHECK_PERIOD = 15 * 1000L;
    private static final long DEFAULT_DATANODE_IDLE_CHECK_PERIOD = 60 * 1000L;
    private static final long DEFAULT_DATANODE_HEARTBEAT_PERIOD = 10 * 1000L;
    private static final long DEFAULT_CLUSTER_HEARTBEAT_PERIOD = 5 * 1000L;
    private static final long DEFAULT_CLUSTER_HEARTBEAT_TIMEOUT = 10 * 1000L;
    private static final int DEFAULT_CLUSTER_HEARTBEAT_RETRY = 10;
    private static final String DEFAULT_CLUSTER_HEARTBEAT_USER = "_HEARTBEAT_USER_";
    private static final String DEFAULT_CLUSTER_HEARTBEAT_PASS = "_HEARTBEAT_PASS_";
    private static final int DEFAULT_PARSER_COMMENT_VERSION = 50148;
    private static final int DEFAULT_SQL_RECORD_COUNT = 10;

    private int serverPort;
    private int managerPort;
    private String charset;
    private int processors;
    private int processorHandler;
    private int processorExecutor;
    private int initExecutor;
    private int timerExecutor;
    private int managerExecutor;
    private long idleTimeout;
    private long processorCheckPeriod;
    private long dataNodeIdleCheckPeriod;
    private long dataNodeHeartbeatPeriod;
    private String clusterHeartbeatUser;
    private String clusterHeartbeatPass;
    private long clusterHeartbeatPeriod;
    private long clusterHeartbeatTimeout;
    private int clusterHeartbeatRetry;
    private int txIsolation;
    private int parserCommentVersion;
    private int sqlRecordCount;

    public SystemConfig() {
        this.serverPort = DEFAULT_PORT;
        this.managerPort = DEFAULT_MANAGER_PORT;
        this.charset = DEFAULT_CHARSET;
        this.processors = DEFAULT_PROCESSORS;
        this.processorHandler = DEFAULT_PROCESSORS;
        this.processorExecutor = DEFAULT_PROCESSORS;
        this.managerExecutor = DEFAULT_PROCESSORS;
        this.timerExecutor = DEFAULT_PROCESSORS;
        this.initExecutor = DEFAULT_PROCESSORS;
        this.idleTimeout = DEFAULT_IDLE_TIMEOUT;
        this.processorCheckPeriod = DEFAULT_PROCESSOR_CHECK_PERIOD;
        this.dataNodeIdleCheckPeriod = DEFAULT_DATANODE_IDLE_CHECK_PERIOD;
        this.dataNodeHeartbeatPeriod = DEFAULT_DATANODE_HEARTBEAT_PERIOD;
        this.clusterHeartbeatUser = DEFAULT_CLUSTER_HEARTBEAT_USER;
        this.clusterHeartbeatPass = DEFAULT_CLUSTER_HEARTBEAT_PASS;
        this.clusterHeartbeatPeriod = DEFAULT_CLUSTER_HEARTBEAT_PERIOD;
        this.clusterHeartbeatTimeout = DEFAULT_CLUSTER_HEARTBEAT_TIMEOUT;
        this.clusterHeartbeatRetry = DEFAULT_CLUSTER_HEARTBEAT_RETRY;
        this.txIsolation = Isolations.REPEATED_READ;
        this.parserCommentVersion = DEFAULT_PARSER_COMMENT_VERSION;
        this.sqlRecordCount = DEFAULT_SQL_RECORD_COUNT;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public int getManagerPort() {
        return managerPort;
    }

    public void setManagerPort(int managerPort) {
        this.managerPort = managerPort;
    }

    public int getProcessors() {
        return processors;
    }

    public void setProcessors(int processors) {
        this.processors = processors;
    }

    public int getProcessorHandler() {
        return processorHandler;
    }

    public void setProcessorHandler(int processorExecutor) {
        this.processorHandler = processorExecutor;
    }

    public int getProcessorExecutor() {
        return processorExecutor;
    }

    public void setProcessorExecutor(int processorExecutor) {
        this.processorExecutor = processorExecutor;
    }

    public int getManagerExecutor() {
        return managerExecutor;
    }

    public void setManagerExecutor(int managerExecutor) {
        this.managerExecutor = managerExecutor;
    }

    public int getTimerExecutor() {
        return timerExecutor;
    }

    public void setTimerExecutor(int timerExecutor) {
        this.timerExecutor = timerExecutor;
    }

    public int getInitExecutor() {
        return initExecutor;
    }

    public void setInitExecutor(int initExecutor) {
        this.initExecutor = initExecutor;
    }

    public long getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(long idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public long getProcessorCheckPeriod() {
        return processorCheckPeriod;
    }

    public void setProcessorCheckPeriod(long processorCheckPeriod) {
        this.processorCheckPeriod = processorCheckPeriod;
    }

    public long getDataNodeIdleCheckPeriod() {
        return dataNodeIdleCheckPeriod;
    }

    public void setDataNodeIdleCheckPeriod(long dataNodeIdleCheckPeriod) {
        this.dataNodeIdleCheckPeriod = dataNodeIdleCheckPeriod;
    }

    public long getDataNodeHeartbeatPeriod() {
        return dataNodeHeartbeatPeriod;
    }

    public void setDataNodeHeartbeatPeriod(long dataNodeHeartbeatPeriod) {
        this.dataNodeHeartbeatPeriod = dataNodeHeartbeatPeriod;
    }

    public String getClusterHeartbeatUser() {
        return clusterHeartbeatUser;
    }

    public void setClusterHeartbeatUser(String clusterHeartbeatUser) {
        this.clusterHeartbeatUser = clusterHeartbeatUser;
    }

    public String getClusterHeartbeatPass() {
        return clusterHeartbeatPass;
    }

    public void setClusterHeartbeatPass(String clusterHeartbeatPass) {
        this.clusterHeartbeatPass = clusterHeartbeatPass;
    }

    public long getClusterHeartbeatPeriod() {
        return clusterHeartbeatPeriod;
    }

    public void setClusterHeartbeatPeriod(long clusterHeartbeatPeriod) {
        this.clusterHeartbeatPeriod = clusterHeartbeatPeriod;
    }

    public long getClusterHeartbeatTimeout() {
        return clusterHeartbeatTimeout;
    }

    public void setClusterHeartbeatTimeout(long clusterHeartbeatTimeout) {
        this.clusterHeartbeatTimeout = clusterHeartbeatTimeout;
    }

    public int getClusterHeartbeatRetry() {
        return clusterHeartbeatRetry;
    }

    public void setClusterHeartbeatRetry(int clusterHeartbeatRetry) {
        this.clusterHeartbeatRetry = clusterHeartbeatRetry;
    }

    public int getTxIsolation() {
        return txIsolation;
    }

    public void setTxIsolation(int txIsolation) {
        this.txIsolation = txIsolation;
    }

    public int getParserCommentVersion() {
        return parserCommentVersion;
    }

    public void setParserCommentVersion(int parserCommentVersion) {
        this.parserCommentVersion = parserCommentVersion;
    }

    public int getSqlRecordCount() {
        return sqlRecordCount;
    }

    public void setSqlRecordCount(int sqlRecordCount) {
        this.sqlRecordCount = sqlRecordCount;
    }

}
