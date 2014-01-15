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

package com.alibaba.cobar.manager.qa.modle;

import com.mysql.jdbc.Connection;

public class SimpleCobarNode extends SimpleMySqlNode {
    private int dmlPort = 0;
    private int managerPort = 0;
    private String user;
    private String password;

    public SimpleCobarNode(String cobarIP, int dmlPort, int managerPort, String user, String password) throws Exception {
        super(cobarIP);
        this.dmlPort = dmlPort;
        this.managerPort = managerPort;
        this.user = user;
        this.password = password;
    }

    public Connection createDMLConnection(String schema) throws Exception {
        return createConnection(dmlPort, user, password, schema);
    }

    public Connection createManagerConnection() throws Exception {
        return createConnection(managerPort, user, password, "");
    }

}
