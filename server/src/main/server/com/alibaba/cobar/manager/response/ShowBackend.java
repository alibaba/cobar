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

import com.alibaba.cobar.CobarServer;
import com.alibaba.cobar.config.Fields;
import com.alibaba.cobar.heartbeat.CobarDetector;
import com.alibaba.cobar.heartbeat.CobarHeartbeat;
import com.alibaba.cobar.heartbeat.MySQLDetector;
import com.alibaba.cobar.heartbeat.MySQLHeartbeat;
import com.alibaba.cobar.manager.ManagerConnection;
import com.alibaba.cobar.mysql.PacketUtil;
import com.alibaba.cobar.net.BackendConnection;
import com.alibaba.cobar.net.NIOProcessor;
import com.alibaba.cobar.net.mysql.EOFPacket;
import com.alibaba.cobar.net.mysql.FieldPacket;
import com.alibaba.cobar.net.mysql.ResultSetHeaderPacket;
import com.alibaba.cobar.net.mysql.RowDataPacket;
import com.alibaba.cobar.util.IntegerUtil;
import com.alibaba.cobar.util.LongUtil;
import com.alibaba.cobar.util.StringUtil;
import com.alibaba.cobar.util.TimeUtil;

/**
 * 查询后端连接
 * 
 * @author xianmao.hexm 2012-5-10
 */
public class ShowBackend {

    private static final int FIELD_COUNT = 14;
    private static final ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
    private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
    private static final EOFPacket eof = new EOFPacket();
    static {
        int i = 0;
        byte packetId = 0;
        header.packetId = ++packetId;
        fields[i] = PacketUtil.getField("processor", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("id", Fields.FIELD_TYPE_LONG);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("host", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("port", Fields.FIELD_TYPE_LONG);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("l_port", Fields.FIELD_TYPE_LONG);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("net_in", Fields.FIELD_TYPE_LONGLONG);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("net_out", Fields.FIELD_TYPE_LONGLONG);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("life", Fields.FIELD_TYPE_LONGLONG);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("closed", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("auth", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("quit", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("checking", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("stop", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("status", Fields.FIELD_TYPE_LONGLONG);
        fields[i++].packetId = ++packetId;
        eof.packetId = ++packetId;
    }

    public static void execute(ManagerConnection c) {
        ByteBuffer buffer = c.allocate();
        buffer = header.write(buffer, c);
        for (FieldPacket field : fields) {
            buffer = field.write(buffer, c);
        }
        buffer = eof.write(buffer, c);
        byte packetId = eof.packetId;
        String charset = c.getCharset();
        for (NIOProcessor p : CobarServer.getInstance().getProcessors()) {
            for (BackendConnection bc : p.getBackends().values()) {
                if (bc != null) {
                    RowDataPacket row = getRow(bc, charset);
                    row.packetId = ++packetId;
                    buffer = row.write(buffer, c);
                }
            }
        }
        EOFPacket lastEof = new EOFPacket();
        lastEof.packetId = ++packetId;
        buffer = lastEof.write(buffer, c);
        c.write(buffer);
    }

    private static RowDataPacket getRow(BackendConnection c, String charset) {
        RowDataPacket row = new RowDataPacket(FIELD_COUNT);
        row.add(c.getProcessor().getName().getBytes());
        row.add(LongUtil.toBytes(c.getId()));
        row.add(StringUtil.encode(c.getHost(), charset));
        row.add(IntegerUtil.toBytes(c.getPort()));
        row.add(IntegerUtil.toBytes(c.getLocalPort()));
        row.add(LongUtil.toBytes(c.getNetInBytes()));
        row.add(LongUtil.toBytes(c.getNetOutBytes()));
        row.add(LongUtil.toBytes((TimeUtil.currentTimeMillis() - c.getStartupTime()) / 1000L));
        row.add(c.isClosed() ? "true".getBytes() : "false".getBytes());
        if (c instanceof CobarDetector) {
            CobarDetector detector = (CobarDetector) c;
            CobarHeartbeat heartbeat = detector.getHeartbeat();
            row.add(detector.isAuthenticated() ? "true".getBytes() : "false".getBytes());
            row.add(detector.isQuit() ? "true".getBytes() : "false".getBytes());
            row.add(heartbeat.isChecking() ? "true".getBytes() : "false".getBytes());
            row.add(heartbeat.isStop() ? "true".getBytes() : "false".getBytes());
            row.add(LongUtil.toBytes(heartbeat.getStatus()));
        } else if (c instanceof MySQLDetector) {
            MySQLDetector detector = (MySQLDetector) c;
            MySQLHeartbeat heartbeat = detector.getHeartbeat();
            row.add(detector.isAuthenticated() ? "true".getBytes() : "false".getBytes());
            row.add(detector.isQuit() ? "true".getBytes() : "false".getBytes());
            row.add(heartbeat.isChecking() ? "true".getBytes() : "false".getBytes());
            row.add(heartbeat.isStop() ? "true".getBytes() : "false".getBytes());
            row.add(LongUtil.toBytes(heartbeat.getStatus()));
        } else {
            row.add(null);
            row.add(null);
            row.add(null);
            row.add(null);
            row.add(null);
        }
        return row;
    }

}
