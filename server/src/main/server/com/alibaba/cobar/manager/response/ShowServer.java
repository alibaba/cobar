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
import com.alibaba.cobar.manager.ManagerConnection;
import com.alibaba.cobar.mysql.PacketUtil;
import com.alibaba.cobar.net.mysql.EOFPacket;
import com.alibaba.cobar.net.mysql.FieldPacket;
import com.alibaba.cobar.net.mysql.ResultSetHeaderPacket;
import com.alibaba.cobar.net.mysql.RowDataPacket;
import com.alibaba.cobar.util.FormatUtil;
import com.alibaba.cobar.util.LongUtil;
import com.alibaba.cobar.util.StringUtil;
import com.alibaba.cobar.util.TimeUtil;

/**
 * 服务器状态报告
 * 
 * @author xianmao.hexm 2010-1-25 下午01:11:00
 * @author wenfeng.cenwf 2011-4-25
 */
public final class ShowServer {

    private static final int FIELD_COUNT = 8;
    private static final ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
    private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
    private static final EOFPacket eof = new EOFPacket();
    static {
        int i = 0;
        byte packetId = 0;
        header.packetId = ++packetId;

        fields[i] = PacketUtil.getField("UPTIME", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("USED_MEMORY", Fields.FIELD_TYPE_LONGLONG);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("TOTAL_MEMORY", Fields.FIELD_TYPE_LONGLONG);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("MAX_MEMORY", Fields.FIELD_TYPE_LONGLONG);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("RELOAD_TIME", Fields.FIELD_TYPE_LONGLONG);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("ROLLBACK_TIME", Fields.FIELD_TYPE_LONGLONG);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("CHARSET", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("STATUS", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        eof.packetId = ++packetId;
    }

    public static void execute(ManagerConnection c) {
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
        RowDataPacket row = getRow(c.getCharset());
        row.packetId = ++packetId;
        buffer = row.write(buffer, c);

        // write last eof
        EOFPacket lastEof = new EOFPacket();
        lastEof.packetId = ++packetId;
        buffer = lastEof.write(buffer, c);

        // write buffer
        c.write(buffer);
    }

    private static RowDataPacket getRow(String charset) {
        CobarServer server = CobarServer.getInstance();
        long startupTime = server.getStartupTime();
        long now = TimeUtil.currentTimeMillis();
        long uptime = now - startupTime;
        Runtime rt = Runtime.getRuntime();
        long total = rt.totalMemory();
        long max = rt.maxMemory();
        long used = (total - rt.freeMemory());
        RowDataPacket row = new RowDataPacket(FIELD_COUNT);
        row.add(StringUtil.encode(FormatUtil.formatTime(uptime, 3), charset));
        row.add(LongUtil.toBytes(used));
        row.add(LongUtil.toBytes(total));
        row.add(LongUtil.toBytes(max));
        row.add(LongUtil.toBytes(server.getConfig().getReloadTime()));
        row.add(LongUtil.toBytes(server.getConfig().getRollbackTime()));
        row.add(StringUtil.encode(charset, charset));
        row.add(StringUtil.encode(CobarServer.getInstance().isOnline() ? "ON" : "OFF", charset));
        return row;
    }

}
