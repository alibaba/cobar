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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.alibaba.cobar.CobarConfig;
import com.alibaba.cobar.CobarNode;
import com.alibaba.cobar.CobarServer;
import com.alibaba.cobar.config.Fields;
import com.alibaba.cobar.heartbeat.CobarHeartbeat;
import com.alibaba.cobar.heartbeat.MySQLHeartbeat;
import com.alibaba.cobar.manager.ManagerConnection;
import com.alibaba.cobar.mysql.MySQLDataNode;
import com.alibaba.cobar.mysql.PacketUtil;
import com.alibaba.cobar.net.mysql.EOFPacket;
import com.alibaba.cobar.net.mysql.FieldPacket;
import com.alibaba.cobar.net.mysql.ResultSetHeaderPacket;
import com.alibaba.cobar.net.mysql.RowDataPacket;
import com.alibaba.cobar.parser.util.Pair;
import com.alibaba.cobar.parser.util.PairUtil;
import com.alibaba.cobar.util.IntegerUtil;
import com.alibaba.cobar.util.LongUtil;

/**
 * @author xianmao.hexm
 */
public class ShowHeartbeat {

    private static final int FIELD_COUNT = 11;
    private static final ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
    private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
    private static final EOFPacket eof = new EOFPacket();
    static {
        int i = 0;
        byte packetId = 0;
        header.packetId = ++packetId;

        fields[i] = PacketUtil.getField("NAME", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("TYPE", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("HOST", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("PORT", Fields.FIELD_TYPE_LONG);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("RS_CODE", Fields.FIELD_TYPE_LONG);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("RETRY", Fields.FIELD_TYPE_LONG);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("STATUS", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("TIMEOUT", Fields.FIELD_TYPE_LONGLONG);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("EXECUTE_TIME", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("LAST_ACTIVE_TIME", Fields.FIELD_TYPE_DATETIME);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("STOP", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        eof.packetId = ++packetId;
    }

    public static void response(ManagerConnection c) {
        ByteBuffer buffer = c.allocate();

        // write header
        buffer = header.write(buffer, c);

        // write fields
        for (FieldPacket field : fields) {
            buffer = field.write(buffer, c);
        }

        // write eof
        buffer = eof.write(buffer, c);

        // write rows
        byte packetId = eof.packetId;
        for (RowDataPacket row : getRows()) {
            row.packetId = ++packetId;
            buffer = row.write(buffer, c);
        }

        // write last eof
        EOFPacket lastEof = new EOFPacket();
        lastEof.packetId = ++packetId;
        buffer = lastEof.write(buffer, c);

        // post write
        c.write(buffer);
    }

    private static List<RowDataPacket> getRows() {
        List<RowDataPacket> list = new LinkedList<RowDataPacket>();
        CobarConfig conf = CobarServer.getInstance().getConfig();

        // cobar nodes
        Map<String, CobarNode> cobarNodes = conf.getCluster().getNodes();
        List<String> cobarNodeKeys = new ArrayList<String>(cobarNodes.size());
        cobarNodeKeys.addAll(cobarNodes.keySet());
        Collections.sort(cobarNodeKeys);
        for (String key : cobarNodeKeys) {
            CobarNode node = cobarNodes.get(key);
            if (node != null) {
                CobarHeartbeat hb = node.getHeartbeat();
                RowDataPacket row = new RowDataPacket(FIELD_COUNT);
                row.add(node.getName().getBytes());
                row.add("COBAR".getBytes());
                row.add(node.getConfig().getHost().getBytes());
                row.add(IntegerUtil.toBytes(node.getConfig().getPort()));
                row.add(IntegerUtil.toBytes(hb.getStatus()));
                row.add(IntegerUtil.toBytes(hb.getErrorCount()));
                row.add(hb.isChecking() ? "checking".getBytes() : "idle".getBytes());
                row.add(LongUtil.toBytes(hb.getTimeout()));
                row.add(hb.getRecorder().get().getBytes());
                String at = hb.lastActiveTime();
                row.add(at == null ? null : at.getBytes());
                row.add(hb.isStop() ? "true".getBytes() : "false".getBytes());
                list.add(row);
            }
        }

        // data nodes
        Map<String, MySQLDataNode> dataNodes = conf.getDataNodes();
        List<String> dataNodeKeys = new ArrayList<String>(dataNodes.size());
        dataNodeKeys.addAll(dataNodes.keySet());
        Collections.sort(dataNodeKeys, new Comparators<String>());
        for (String key : dataNodeKeys) {
            MySQLDataNode node = dataNodes.get(key);
            if (node != null) {
                MySQLHeartbeat hb = node.getHeartbeat();
                RowDataPacket row = new RowDataPacket(FIELD_COUNT);
                row.add(node.getName().getBytes());
                row.add("MYSQL".getBytes());
                if (hb != null) {
                    row.add(hb.getSource().getConfig().getHost().getBytes());
                    row.add(IntegerUtil.toBytes(hb.getSource().getConfig().getPort()));
                    row.add(IntegerUtil.toBytes(hb.getStatus()));
                    row.add(IntegerUtil.toBytes(hb.getErrorCount()));
                    row.add(hb.isChecking() ? "checking".getBytes() : "idle".getBytes());
                    row.add(LongUtil.toBytes(hb.getTimeout()));
                    row.add(hb.getRecorder().get().getBytes());
                    String lat = hb.getLastActiveTime();
                    row.add(lat == null ? null : lat.getBytes());
                    row.add(hb.isStop() ? "true".getBytes() : "false".getBytes());
                } else {
                    row.add(null);
                    row.add(null);
                    row.add(null);
                    row.add(null);
                    row.add(null);
                    row.add(null);
                    row.add(null);
                    row.add(null);
                    row.add(null);
                }
                list.add(row);
            }
        }
        return list;
    }

    private static final class Comparators<T> implements Comparator<String> {
        @Override
        public int compare(String s1, String s2) {
            Pair<String, Integer> p1 = PairUtil.splitIndex(s1, '[', ']');
            Pair<String, Integer> p2 = PairUtil.splitIndex(s2, '[', ']');
            if (p1.getKey().compareTo(p2.getKey()) == 0) {
                return p1.getValue() - p2.getValue();
            } else {
                return p1.getKey().compareTo(p2.getKey());
            }
        }
    }

}
