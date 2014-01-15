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

import com.alibaba.cobar.CobarServer;
import com.alibaba.cobar.manager.ManagerConnection;
import com.alibaba.cobar.manager.parser.ManagerParseSwitch;
import com.alibaba.cobar.mysql.MySQLDataNode;
import com.alibaba.cobar.net.mysql.OkPacket;
import com.alibaba.cobar.parser.util.Pair;

/**
 * 切换数据节点的数据源
 * 
 * @author xianmao.hexm 2011-5-31 下午01:19:36
 */
public final class SwitchDataSource {

    public static void response(String stmt, ManagerConnection c) {
        int count = 0;
        Pair<String[], Integer> pair = ManagerParseSwitch.getPair(stmt);
        Map<String, MySQLDataNode> dns = CobarServer.getInstance().getConfig().getDataNodes();
        Integer idx = pair.getValue();
        for (String key : pair.getKey()) {
            MySQLDataNode dn = dns.get(key);
            if (dn != null) {
                int m = dn.getActivedIndex();
                int n = (idx == null) ? dn.next(m) : idx.intValue();
                if (dn.switchSource(n, false, "MANAGER")) {
                    ++count;
                }
            }
        }
        OkPacket packet = new OkPacket();
        packet.packetId = 1;
        packet.affectedRows = count;
        packet.serverStatus = 2;
        packet.write(c);
    }

}
