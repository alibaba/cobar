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

package com.alibaba.cobar.manager.web.ajax;

public class AjaxResult {
    private long id;
    private long netIn;
    private long netOut;
    private long request;
    private long connection;
    private long timestamp;
    private int total;
    private int active;
    private int error;
    private int schema;
    private String netIn_deriv;
    private String netOut_deriv;
    private String request_deriv;

    private String flag;

    public AjaxResult() {
        id = -1;
        netIn = 0L;
        netOut = 0L;
        request = 0L;
        connection = 0L;
        timestamp = 0;
        total = 0;
        active = 0;
        error = 0;
        schema = 0;
        netIn_deriv = "0";
        netOut_deriv = "0";
        request_deriv = "0";
        flag = null;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getNetIn() {
        return netIn;
    }

    public void setNetIn(long netIn) {
        this.netIn = netIn;
    }

    public void addNetIn(long netIn) {
        this.netIn += netIn;
    }

    public long getNetOut() {
        return netOut;
    }

    public void setNetOut(long netOut) {
        this.netOut = netOut;
    }

    public void addNetOut(long netOut) {
        this.netOut += netOut;
    }

    public long getRequest() {
        return request;
    }

    public void setRequest(long request) {
        this.request = request;
    }

    public void addRequest(long request) {
        this.request += request;
    }

    public long getConnection() {
        return connection;
    }

    public void setConnection(long connection) {
        this.connection = connection;
    }

    public void addConnection(long connection) {
        this.connection += connection;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int totalCount) {
        this.total = totalCount;
    }

    public int getActive() {
        return active;
    }

    public void setActive(int activeCount) {
        this.active = activeCount;
    }

    public void addActive(int activeCount) {
        this.active += activeCount;
    }

    public int getError() {
        return error;
    }

    public void setError(int errorCount) {
        this.error = errorCount;
    }

    public void addError(int errorCount) {
        this.error += errorCount;
    }

    public int getSchema() {
        return schema;
    }

    public void setSchema(int schemaCount) {
        this.schema = schemaCount;
    }

    public String getNetIn_deriv() {
        return netIn_deriv;
    }

    public void setNetIn_deriv(String netIn_deriv) {
        this.netIn_deriv = netIn_deriv;
    }

    public String getNetOut_deriv() {
        return netOut_deriv;
    }

    public void setNetOut_deriv(String netOut_deriv) {
        this.netOut_deriv = netOut_deriv;
    }

    public String getRequest_deriv() {
        return request_deriv;
    }

    public void setRequest_deriv(String reCount_deriv) {
        this.request_deriv = reCount_deriv;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

}
