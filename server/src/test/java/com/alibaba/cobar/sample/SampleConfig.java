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
package com.alibaba.cobar.sample;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 服务器配置信息示例
 * 
 * @author xianmao.hexm
 */
public class SampleConfig {

    /**
     * 服务器名
     */
    private String serverName;

    /**
     * 可登录的用户和密码
     */
    private Map<String, String> users;

    /**
     * 可使用的schemas
     */
    private Set<String> schemas;

    /**
     * 指定用户可使用的schemas
     */
    private Map<String, Set<String>> userSchemas;

    public SampleConfig() {
        this.serverName = "Sample";

        // add user/password
        this.users = new HashMap<String, String>();
        this.users.put("root", null);
        this.users.put("test", "12345");

        // add schema
        this.schemas = new HashSet<String>();
        this.schemas.add("schema1");
        this.schemas.add("schema2");
        this.schemas.add("schema3");

        // add user/schema
        this.userSchemas = new HashMap<String, Set<String>>();
        Set<String> schemaSet = new HashSet<String>();
        schemaSet.add("schema1");
        schemaSet.add("schema3");
        this.userSchemas.put("test", schemaSet);
    }

    public String getServerName() {
        return serverName;
    }

    public Map<String, String> getUsers() {
        return users;
    }

    public Set<String> getSchemas() {
        return schemas;
    }

    public Map<String, Set<String>> getUserSchemas() {
        return userSchemas;
    }

}
