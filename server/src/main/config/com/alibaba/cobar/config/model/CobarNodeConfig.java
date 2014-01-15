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
 * @author haiqing.zhuhq 2012-3-21
 * @author xianmao.hexm
 */
public final class CobarNodeConfig {

    private String name;
    private String host;
    private int port;
    private int weight;

    public CobarNodeConfig(String name, String host, int port, int weight) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("[name=")
                                  .append(name)
                                  .append(",host=")
                                  .append(host)
                                  .append(",port=")
                                  .append(port)
                                  .append(",weight=")
                                  .append(weight)
                                  .append(']')
                                  .toString();
    }

}
