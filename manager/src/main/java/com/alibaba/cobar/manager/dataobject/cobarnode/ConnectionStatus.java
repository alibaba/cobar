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

package com.alibaba.cobar.manager.dataobject.cobarnode;

/**
 * (created at 2010-8-26)
 * 
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 * @author wenfeng.cenwf 2011-4-15
 */
public class ConnectionStatus extends TimeStampedVO {
    private String processor;
    private long id;
    private String host;
    private int port;
    private int local_port;
    private String schema;
    private String charset;
    private long netIn;
    private long netOut;
    private long aliveTime;
    private int attempsCount;
    private int recvBuffer;
    private int sendQueue;
    private int channel;

    public String getProcessor() {
        return processor;
    }

    public void setProcessor(String processor) {
        this.processor = processor;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public long getNetIn() {
        return netIn;
    }

    public void setNetIn(long netIn) {
        this.netIn = netIn;
    }

    public long getNetOut() {
        return netOut;
    }

    public void setNetOut(long netOut) {
        this.netOut = netOut;
    }

    public long getAliveTime() {
        return aliveTime;
    }

    public void setAliveTime(long aliveTime) {
        this.aliveTime = aliveTime;
    }

    public int getAttempsCount() {
        return attempsCount;
    }

    public void setAttempsCount(int attempsCount) {
        this.attempsCount = attempsCount;
    }

    public int getRecvBuffer() {
        return recvBuffer;
    }

    public void setRecvBuffer(int recvBuffer) {
        this.recvBuffer = recvBuffer;
    }

    public int getSendQueue() {
        return sendQueue;
    }

    public void setSendQueue(int sendQueue) {
        this.sendQueue = sendQueue;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getLocal_port() {
        return local_port;
    }

    public void setLocal_port(int local_port) {
        this.local_port = local_port;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

}
