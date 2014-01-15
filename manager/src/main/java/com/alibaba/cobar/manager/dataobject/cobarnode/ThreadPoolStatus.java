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
 * (created at 2010-7-26)
 * 
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 * @author wenfeng.cenwf 2011-4-15
 */
public class ThreadPoolStatus {
    private String threadPoolName;
    private int poolSize;
    private int activeSize;
    private int taskQueue;
    private long completedTask;
    private long totalTask;

    public String getThreadPoolName() {
        return threadPoolName;
    }

    public void setThreadPoolName(String threadPoolName) {
        this.threadPoolName = threadPoolName;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public int getActiveSize() {
        return activeSize;
    }

    public void setActiveSize(int activeSize) {
        this.activeSize = activeSize;
    }

    public int getTaskQueue() {
        return taskQueue;
    }

    public void setTaskQueue(int taskQueue) {
        this.taskQueue = taskQueue;
    }

    public long getCompletedTask() {
        return completedTask;
    }

    public void setCompletedTask(long completedTask) {
        this.completedTask = completedTask;
    }

    public long getTotalTask() {
        return totalTask;
    }

    public void setTotalTask(long totalTask) {
        this.totalTask = totalTask;
    }
}
