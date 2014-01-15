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
package com.alibaba.cobar.manager.response;

import java.util.Map;

import com.alibaba.cobar.CobarConfig;
import com.alibaba.cobar.CobarServer;
import com.alibaba.cobar.config.ErrorCode;
import com.alibaba.cobar.config.model.SchemaConfig;
import com.alibaba.cobar.manager.ManagerConnection;
import com.alibaba.cobar.mysql.MySQLDataNode;
import com.alibaba.cobar.mysql.MySQLDataSource;
import com.alibaba.cobar.net.mysql.OkPacket;

/**
 * @author xianmao.hexm 2012-4-16
 */
public class ClearSlow {

    public static void dataNode(ManagerConnection c, String name) {
        MySQLDataNode dn = CobarServer.getInstance().getConfig().getDataNodes().get(name);
        MySQLDataSource ds = null;
        if (dn != null && (ds = dn.getSource()) != null) {
            ds.getSqlRecorder().clear();
            c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
        } else {
            c.writeErrMessage(ErrorCode.ER_YES, "Invalid DataNode:" + name);
        }
    }

    public static void schema(ManagerConnection c, String name) {
        CobarConfig conf = CobarServer.getInstance().getConfig();
        SchemaConfig schema = conf.getSchemas().get(name);
        if (schema != null) {
            Map<String, MySQLDataNode> dataNodes = conf.getDataNodes();
            for (String n : schema.getAllDataNodes()) {
                MySQLDataNode dn = dataNodes.get(n);
                MySQLDataSource ds = null;
                if (dn != null && (ds = dn.getSource()) != null) {
                    ds.getSqlRecorder().clear();
                }
            }
            c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
        } else {
            c.writeErrMessage(ErrorCode.ER_YES, "Invalid Schema:" + name);
        }
    }

}
