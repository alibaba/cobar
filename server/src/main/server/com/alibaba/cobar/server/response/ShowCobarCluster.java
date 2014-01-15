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
package com.alibaba.cobar.server.response;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.alibaba.cobar.CobarCluster;
import com.alibaba.cobar.CobarConfig;
import com.alibaba.cobar.CobarNode;
import com.alibaba.cobar.CobarServer;
import com.alibaba.cobar.config.Alarms;
import com.alibaba.cobar.config.Fields;
import com.alibaba.cobar.config.model.CobarNodeConfig;
import com.alibaba.cobar.config.model.SchemaConfig;
import com.alibaba.cobar.mysql.PacketUtil;
import com.alibaba.cobar.net.mysql.EOFPacket;
import com.alibaba.cobar.net.mysql.FieldPacket;
import com.alibaba.cobar.net.mysql.ResultSetHeaderPacket;
import com.alibaba.cobar.net.mysql.RowDataPacket;
import com.alibaba.cobar.server.ServerConnection;
import com.alibaba.cobar.util.IntegerUtil;
import com.alibaba.cobar.util.StringUtil;

/**
 * @author xianmao.hexm
 */
public class ShowCobarCluster {

    private static final Logger alarm = Logger.getLogger("alarm");

    private static final int FIELD_COUNT = 2;
    private static final ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
    private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
    private static final EOFPacket eof = new EOFPacket();
    static {
        int i = 0;
        byte packetId = 0;
        header.packetId = ++packetId;
        fields[i] = PacketUtil.getField("HOST", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("WEIGHT", Fields.FIELD_TYPE_LONG);
        fields[i++].packetId = ++packetId;
        eof.packetId = ++packetId;
    }

    public static void response(ServerConnection c) {
        ByteBuffer buffer = c.allocate();

        // write header
        buffer = header.write(buffer, c);

        // write field
        for (FieldPacket field : fields) {
            buffer = field.write(buffer, c);
        }

        // write eof
        buffer = eof.write(buffer, c);

        // write rows
        byte packetId = eof.packetId;
        for (RowDataPacket row : getRows(c)) {
            row.packetId = ++packetId;
            buffer = row.write(buffer, c);
        }

        // last eof
        EOFPacket lastEof = new EOFPacket();
        lastEof.packetId = ++packetId;
        buffer = lastEof.write(buffer, c);

        // post write
        c.write(buffer);
    }

    private static List<RowDataPacket> getRows(ServerConnection c) {
        List<RowDataPacket> rows = new LinkedList<RowDataPacket>();
        CobarConfig config = CobarServer.getInstance().getConfig();
        CobarCluster cluster = config.getCluster();
        Map<String, SchemaConfig> schemas = config.getSchemas();
        SchemaConfig schema = (c.getSchema() == null) ? null : schemas.get(c.getSchema());

        // 如果没有指定schema或者schema为null，则使用全部集群。
        if (schema == null) {
            Map<String, CobarNode> nodes = cluster.getNodes();
            for (CobarNode n : nodes.values()) {
                if (n != null && n.isOnline()) {
                    rows.add(getRow(n, c.getCharset()));
                }
            }
        } else {
            String group = (schema.getGroup() == null) ? "default" : schema.getGroup();
            List<String> nodeList = cluster.getGroups().get(group);
            if (nodeList != null && nodeList.size() > 0) {
                Map<String, CobarNode> nodes = cluster.getNodes();
                for (String id : nodeList) {
                    CobarNode n = nodes.get(id);
                    if (n != null && n.isOnline()) {
                        rows.add(getRow(n, c.getCharset()));
                    }
                }
            }
            // 如果schema对应的group或者默认group都没有有效的节点，则使用全部集群。
            if (rows.size() == 0) {
                Map<String, CobarNode> nodes = cluster.getNodes();
                for (CobarNode n : nodes.values()) {
                    if (n != null && n.isOnline()) {
                        rows.add(getRow(n, c.getCharset()));
                    }
                }
            }
        }

        if (rows.size() == 0) {
            alarm.error(Alarms.CLUSTER_EMPTY + c.toString());
        }

        return rows;
    }

    private static RowDataPacket getRow(CobarNode node, String charset) {
        CobarNodeConfig conf = node.getConfig();
        RowDataPacket row = new RowDataPacket(FIELD_COUNT);
        row.add(StringUtil.encode(conf.getHost(), charset));
        row.add(IntegerUtil.toBytes(conf.getWeight()));
        return row;
    }

}
