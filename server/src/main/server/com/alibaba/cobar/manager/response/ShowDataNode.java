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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.alibaba.cobar.CobarConfig;
import com.alibaba.cobar.CobarServer;
import com.alibaba.cobar.config.Fields;
import com.alibaba.cobar.config.model.SchemaConfig;
import com.alibaba.cobar.manager.ManagerConnection;
import com.alibaba.cobar.mysql.MySQLDataNode;
import com.alibaba.cobar.mysql.MySQLDataSource;
import com.alibaba.cobar.mysql.PacketUtil;
import com.alibaba.cobar.net.mysql.EOFPacket;
import com.alibaba.cobar.net.mysql.FieldPacket;
import com.alibaba.cobar.net.mysql.ResultSetHeaderPacket;
import com.alibaba.cobar.net.mysql.RowDataPacket;
import com.alibaba.cobar.parser.util.Pair;
import com.alibaba.cobar.parser.util.PairUtil;
import com.alibaba.cobar.util.IntegerUtil;
import com.alibaba.cobar.util.LongUtil;
import com.alibaba.cobar.util.StringUtil;
import com.alibaba.cobar.util.TimeUtil;

/**
 * 查看数据节点信息
 * 
 * @author wenfeng.cenwf 2011-4-28
 * @author xianmao.hexm
 */
public final class ShowDataNode {

    private static final NumberFormat nf = DecimalFormat.getInstance();
    private static final int FIELD_COUNT = 12;
    private static final ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
    private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
    private static final EOFPacket eof = new EOFPacket();
    static {
        nf.setMaximumFractionDigits(3);

        int i = 0;
        byte packetId = 0;
        header.packetId = ++packetId;

        fields[i] = PacketUtil.getField("NAME", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("DATASOURCES", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("INDEX", Fields.FIELD_TYPE_LONG);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("TYPE", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("ACTIVE", Fields.FIELD_TYPE_LONG);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("IDLE", Fields.FIELD_TYPE_LONG);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("SIZE", Fields.FIELD_TYPE_LONG);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("EXECUTE", Fields.FIELD_TYPE_LONGLONG);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("TOTAL_TIME", Fields.FIELD_TYPE_DOUBLE);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("MAX_TIME", Fields.FIELD_TYPE_DOUBLE);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("MAX_SQL", Fields.FIELD_TYPE_LONGLONG);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("RECOVERY_TIME", Fields.FIELD_TYPE_LONGLONG);
        fields[i++].packetId = ++packetId;

        eof.packetId = ++packetId;
    }

    public static void execute(ManagerConnection c, String name) {
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
        CobarConfig conf = CobarServer.getInstance().getConfig();
        Map<String, MySQLDataNode> dataNodes = conf.getDataNodes();
        List<String> keys = new ArrayList<String>();
        if (StringUtil.isEmpty(name)) {
            keys.addAll(dataNodes.keySet());
        } else {
            SchemaConfig sc = conf.getSchemas().get(name);
            if (null != sc) {
                keys.addAll(sc.getAllDataNodes());
            }
        }
        Collections.sort(keys, new Comparators<String>());
        for (String key : keys) {
            RowDataPacket row = getRow(dataNodes.get(key), c.getCharset());
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

    private static RowDataPacket getRow(MySQLDataNode node, String charset) {
        RowDataPacket row = new RowDataPacket(FIELD_COUNT);
        row.add(StringUtil.encode(node.getName(), charset));
        row.add(StringUtil.encode(node.getConfig().getDataSource(), charset));
        MySQLDataSource ds = node.getSource();
        if (ds != null) {
            row.add(IntegerUtil.toBytes(ds.getIndex()));
            row.add(ds.getConfig().getType().getBytes());
            row.add(IntegerUtil.toBytes(ds.getActiveCount()));
            row.add(IntegerUtil.toBytes(ds.getIdleCount()));
            row.add(IntegerUtil.toBytes(ds.size()));
        } else {
            row.add(null);
            row.add(null);
            row.add(null);
            row.add(null);
            row.add(null);
        }
        row.add(LongUtil.toBytes(node.getExecuteCount()));
        row.add(StringUtil.encode(nf.format(0), charset));
        row.add(StringUtil.encode(nf.format(0), charset));
        row.add(LongUtil.toBytes(0));
        long recoveryTime = node.getHeartbeatRecoveryTime() - TimeUtil.currentTimeMillis();
        row.add(LongUtil.toBytes(recoveryTime > 0 ? recoveryTime / 1000L : -1L));
        return row;
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
