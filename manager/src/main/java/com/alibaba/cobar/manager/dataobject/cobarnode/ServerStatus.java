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
 * @author wenfeng.cenwf 2011-4-14
 * @author haiqing.zhuhq 2011-7-12
 */
public class ServerStatus {
    public static final String STATUS_ON = "RUNNING";
    public static final String STATUS_OFF = "off";
    public static final String STATUS_CLOSING = "closing";
    public static final String STATUS_OPENNING = "openning";

    private String uptime;
    private String status;
    private long rollbackTime;
    private long reloadTime;
    private long usedMemory;
    private long totalMemory;
    private long maxMemory;
    private String charSet;

    public boolean isOn() {
        return STATUS_ON.equalsIgnoreCase(status);
    }

    public String getUptime() {
        return uptime;
    }

    public void setUptime(String uptime) {
        this.uptime = uptime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getRollbackTime() {
        return rollbackTime;
    }

    public void setRollbackTime(long rollbackTime) {
        this.rollbackTime = rollbackTime;
    }

    public long getReloadTime() {
        return reloadTime;
    }

    public void setReloadTime(long reloadTime) {
        this.reloadTime = reloadTime;
    }

    public long getUsedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(long usedMemory) {
        this.usedMemory = usedMemory;
    }

    public long getTotalMemory() {
        return totalMemory;
    }

    public void setTotalMemory(long totalMemory) {
        this.totalMemory = totalMemory;
    }

    public long getMaxMemory() {
        return maxMemory;
    }

    public void setMaxMemory(long maxMemory) {
        this.maxMemory = maxMemory;
    }

    public String getCharSet() {
        return charSet;
    }

    public void setCharSet(String charSet) {
        this.charSet = charSet;
    }

}
