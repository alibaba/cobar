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

import java.util.Set;

import com.alibaba.cobar.net.handler.FrontendPrivileges;

/**
 * @author xianmao.hexm
 */
public class SamplePrivileges implements FrontendPrivileges {

    @Override
    public boolean schemaExists(String schema) {
        SampleConfig conf = SampleServer.getInstance().getConfig();
        return conf.getSchemas().contains(schema);
    }

    @Override
    public boolean userExists(String user, String host) {
        SampleConfig conf = SampleServer.getInstance().getConfig();
        return conf.getUsers().containsKey(user);
    }

    @Override
    public String getPassword(String user) {
        SampleConfig conf = SampleServer.getInstance().getConfig();
        return conf.getUsers().get(user);
    }

    @Override
    public Set<String> getUserSchemas(String user) {
        SampleConfig conf = SampleServer.getInstance().getConfig();
        return conf.getUserSchemas().get(user);
    }

}
