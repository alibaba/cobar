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

import org.apache.log4j.Logger;

import com.alibaba.cobar.CobarServer;
import com.alibaba.cobar.manager.ManagerConnection;
import com.alibaba.cobar.manager.parser.ManagerParseStop;
import com.alibaba.cobar.mysql.MySQLDataNode;
import com.alibaba.cobar.net.mysql.OkPacket;
import com.alibaba.cobar.parser.util.Pair;
import com.alibaba.cobar.util.FormatUtil;
import com.alibaba.cobar.util.TimeUtil;

/**
 * 暂停数据节点心跳检测
 * 
 * @author xianmao.hexm
 */
public final class StopHeartbeat {

    private static final Logger logger = Logger.getLogger(StopHeartbeat.class);

    public static void execute(String stmt, ManagerConnection c) {
        int count = 0;
        Pair<String[], Integer> keys = ManagerParseStop.getPair(stmt);
        if (keys.getKey() != null && keys.getValue() != null) {
            long time = keys.getValue().intValue() * 1000L;
            Map<String, MySQLDataNode> dns = CobarServer.getInstance().getConfig().getDataNodes();
            for (String key : keys.getKey()) {
                MySQLDataNode dn = dns.get(key);
                if (dn != null) {
                    dn.setHeartbeatRecoveryTime(TimeUtil.currentTimeMillis() + time);
                    ++count;
                    StringBuilder s = new StringBuilder();
                    s.append(dn.getName()).append(" stop heartbeat '");
                    logger.warn(s.append(FormatUtil.formatTime(time, 3)).append("' by manager."));
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
