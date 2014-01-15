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
 * (created at 2010-8-9)
 * 
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 * @author wenfeng.cenwf 2011-4-15
 */
public class ProcessorStatus extends TimeStampedVO {
    private String processorId;
    private int rQueue;
    private long requestCount;
    private int wQueue;
    /** Byte */
    private long netIn;
    /** Byte */
    private long netOut;
    /** connection count */
    private int connections;

    private long freeBuffer;
    private long totalBuffer;
    private long bc_count;

    public String getProcessorId() {
        return processorId;
    }

    public void setProcessorId(String processorId) {
        this.processorId = processorId;
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

    public int getrQueue() {
        return rQueue;
    }

    public void setrQueue(int rQueue) {
        this.rQueue = rQueue;
    }

    public long getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(long requestCount) {
        this.requestCount = requestCount;
    }

    public long getFreeBuffer() {
        return freeBuffer;
    }

    public void setFreeBuffer(long freeBuffer) {
        this.freeBuffer = freeBuffer;
    }

    public long getTotalBuffer() {
        return totalBuffer;
    }

    public void setTotalBuffer(long totalBuffer) {
        this.totalBuffer = totalBuffer;
    }

    public int getwQueue() {
        return wQueue;
    }

    public void setwQueue(int wQueue) {
        this.wQueue = wQueue;
    }

    public int getConnections() {
        return connections;
    }

    public void setConnections(int connections) {
        this.connections = connections;
    }

    public long getBc_count() {
        return bc_count;
    }

    public void setBc_count(long bc_count) {
        this.bc_count = bc_count;
    }

}
