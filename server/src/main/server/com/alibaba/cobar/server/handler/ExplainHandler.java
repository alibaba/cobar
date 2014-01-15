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
/**
 * (created at 2011-11-25)
 */
package com.alibaba.cobar.server.handler;

import java.nio.ByteBuffer;
import java.sql.SQLNonTransientException;

import org.apache.log4j.Logger;

import com.alibaba.cobar.CobarServer;
import com.alibaba.cobar.config.ErrorCode;
import com.alibaba.cobar.config.Fields;
import com.alibaba.cobar.config.model.SchemaConfig;
import com.alibaba.cobar.mysql.PacketUtil;
import com.alibaba.cobar.net.mysql.EOFPacket;
import com.alibaba.cobar.net.mysql.FieldPacket;
import com.alibaba.cobar.net.mysql.ResultSetHeaderPacket;
import com.alibaba.cobar.net.mysql.RowDataPacket;
import com.alibaba.cobar.route.RouteResultset;
import com.alibaba.cobar.route.RouteResultsetNode;
import com.alibaba.cobar.route.ServerRouter;
import com.alibaba.cobar.server.ServerConnection;
import com.alibaba.cobar.util.StringUtil;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class ExplainHandler {

    private static final Logger logger = Logger.getLogger(ExplainHandler.class);
    private static final RouteResultsetNode[] EMPTY_ARRAY = new RouteResultsetNode[0];
    private static final int FIELD_COUNT = 2;
    private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
    static {
        fields[0] = PacketUtil.getField("DATA_NODE", Fields.FIELD_TYPE_VAR_STRING);
        fields[1] = PacketUtil.getField("SQL", Fields.FIELD_TYPE_VAR_STRING);
    }

    public static void handle(String stmt, ServerConnection c, int offset) {
        stmt = stmt.substring(offset);

        RouteResultset rrs = getRouteResultset(c, stmt);
        if (rrs == null)
            return;

        ByteBuffer buffer = c.allocate();

        // write header
        ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
        byte packetId = header.packetId;
        buffer = header.write(buffer, c);

        // write fields
        for (FieldPacket field : fields) {
            field.packetId = ++packetId;
            buffer = field.write(buffer, c);
        }

        // write eof
        EOFPacket eof = new EOFPacket();
        eof.packetId = ++packetId;
        buffer = eof.write(buffer, c);

        // write rows
        RouteResultsetNode[] rrsn = (rrs != null) ? rrs.getNodes() : EMPTY_ARRAY;
        for (RouteResultsetNode node : rrsn) {
            RowDataPacket row = getRow(node, c.getCharset());
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

    private static RowDataPacket getRow(RouteResultsetNode node, String charset) {
        RowDataPacket row = new RowDataPacket(FIELD_COUNT);
        row.add(StringUtil.encode(node.getName(), charset));
        row.add(StringUtil.encode(node.getStatement(), charset));
        return row;
    }

    private static RouteResultset getRouteResultset(ServerConnection c, String stmt) {
        String db = c.getSchema();
        if (db == null) {
            c.writeErrMessage(ErrorCode.ER_NO_DB_ERROR, "No database selected");
            return null;
        }
        SchemaConfig schema = CobarServer.getInstance().getConfig().getSchemas().get(db);
        if (schema == null) {
            c.writeErrMessage(ErrorCode.ER_BAD_DB_ERROR, "Unknown database '" + db + "'");
            return null;
        }
        try {
            return ServerRouter.route(schema, stmt, c.getCharset(), c);
        } catch (SQLNonTransientException e) {
            StringBuilder s = new StringBuilder();
            logger.warn(s.append(c).append(stmt).toString(), e);
            String msg = e.getMessage();
            c.writeErrMessage(ErrorCode.ER_PARSE_ERROR, msg == null ? e.getClass().getSimpleName() : msg);
            return null;
        }
    }

}
