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
package com.alibaba.cobar.net.handler;

import java.util.Set;

/**
 * 权限提供者
 * 
 * @author xianmao.hexm
 */
public interface FrontendPrivileges {

    /**
     * 检查schema是否存在
     */
    boolean schemaExists(String schema);

    /**
     * 检查用户是否存在，并且可以使用host实行隔离策略。
     */
    boolean userExists(String user, String host);

    /**
     * 提供用户的服务器端密码
     */
    String getPassword(String user);

    /**
     * 提供有效的用户schema集合
     */
    Set<String> getUserSchemas(String user);

}
