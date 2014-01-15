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

package com.alibaba.cobar.manager.dataobject.xml;

/**
 * @author haiqing.zhuhq 2011-6-14
 */
public class CobarDO {
    private long id;
    private String name;
    private long clusterId;
    private String host;
    private int port;
    private int serverPort;
    private String user;
    private String password;
    private String status;
    private String time_diff;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getClusterId() {
        return clusterId;
    }

    public void setClusterId(long clusterId) {
        this.clusterId = clusterId;
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

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTime_diff() {
        return time_diff;
    }

    public void setTime_diff(String time_diff) {
        this.time_diff = time_diff;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("id:")
                                  .append(id)
                                  .append("|clusterId:")
                                  .append(clusterId)
                                  .append("|name:")
                                  .append(name)
                                  .append("|status:")
                                  .append(status)
                                  .append("|host:")
                                  .append(host)
                                  .append("|port:")
                                  .append(port)
                                  .append("|user:")
                                  .append(user)
                                  .append("|password:")
                                  .append(password)
                                  .append("|time_diff:")
                                  .append(time_diff)
                                  .toString();
    }

}
