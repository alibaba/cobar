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
import java.util.LinkedList;
import java.util.List;

import com.alibaba.cobar.CobarServer;
import com.alibaba.cobar.config.Fields;
import com.alibaba.cobar.manager.ManagerConnection;
import com.alibaba.cobar.mysql.PacketUtil;
import com.alibaba.cobar.net.NIOProcessor;
import com.alibaba.cobar.net.mysql.EOFPacket;
import com.alibaba.cobar.net.mysql.FieldPacket;
import com.alibaba.cobar.net.mysql.ResultSetHeaderPacket;
import com.alibaba.cobar.net.mysql.RowDataPacket;
import com.alibaba.cobar.util.IntegerUtil;
import com.alibaba.cobar.util.LongUtil;
import com.alibaba.cobar.util.NameableExecutor;
import com.alibaba.cobar.util.StringUtil;

/**
 * 查看线程池状态
 * 
 * @author xianmao.hexm 2010-9-26 下午04:08:39
 * @author wenfeng.cenwf 2011-4-25
 */
public final class ShowThreadPool {

    private static final int FIELD_COUNT = 6;
    private static final ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
    private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
    private static final EOFPacket eof = new EOFPacket();
    static {
        int i = 0;
        byte packetId = 0;
        header.packetId = ++packetId;

        fields[i] = PacketUtil.getField("NAME", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("POOL_SIZE", Fields.FIELD_TYPE_LONG);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("ACTIVE_COUNT", Fields.FIELD_TYPE_LONG);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("TASK_QUEUE_SIZE", Fields.FIELD_TYPE_LONG);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("COMPLETED_TASK", Fields.FIELD_TYPE_LONGLONG);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("TOTAL_TASK", Fields.FIELD_TYPE_LONGLONG);
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
        List<NameableExecutor> executors = getExecutors();
        for (NameableExecutor exec : executors) {
            if (exec != null) {
                RowDataPacket row = getRow(exec, c.getCharset());
                row.packetId = ++packetId;
                buffer = row.write(buffer, c);
            }
        }

        // write last eof
        EOFPacket lastEof = new EOFPacket();
        lastEof.packetId = ++packetId;
        buffer = lastEof.write(buffer, c);

        // write buffer
        c.write(buffer);
    }

    private static RowDataPacket getRow(NameableExecutor exec, String charset) {
        RowDataPacket row = new RowDataPacket(FIELD_COUNT);
        row.add(StringUtil.encode(exec.getName(), charset));
        row.add(IntegerUtil.toBytes(exec.getPoolSize()));
        row.add(IntegerUtil.toBytes(exec.getActiveCount()));
        row.add(IntegerUtil.toBytes(exec.getQueue().size()));
        row.add(LongUtil.toBytes(exec.getCompletedTaskCount()));
        row.add(LongUtil.toBytes(exec.getTaskCount()));
        return row;
    }

    private static List<NameableExecutor> getExecutors() {
        List<NameableExecutor> list = new LinkedList<NameableExecutor>();
        CobarServer server = CobarServer.getInstance();
        list.add(server.getInitExecutor());
        list.add(server.getTimerExecutor());
        list.add(server.getManagerExecutor());
        for (NIOProcessor p : server.getProcessors()) {
            list.add(p.getHandler());
            list.add(p.getExecutor());
        }
        return list;
    }
}
