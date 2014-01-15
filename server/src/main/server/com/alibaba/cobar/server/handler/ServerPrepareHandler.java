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
package com.alibaba.cobar.server.handler;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.cobar.config.ErrorCode;
import com.alibaba.cobar.mysql.ByteUtil;
import com.alibaba.cobar.mysql.PreparedStatement;
import com.alibaba.cobar.net.handler.FrontendPrepareHandler;
import com.alibaba.cobar.net.mysql.ExecutePacket;
import com.alibaba.cobar.server.ServerConnection;
import com.alibaba.cobar.server.response.PreparedStmtResponse;

/**
 * @author xianmao.hexm 2012-8-28
 */
public class ServerPrepareHandler implements FrontendPrepareHandler {

    private ServerConnection source;
    private volatile long pstmtId;
    private Map<String, PreparedStatement> pstmtForSql;
    private Map<Long, PreparedStatement> pstmtForId;

    public ServerPrepareHandler(ServerConnection source) {
        this.source = source;
        this.pstmtId = 0L;
        this.pstmtForSql = new HashMap<String, PreparedStatement>();
        this.pstmtForId = new HashMap<Long, PreparedStatement>();
    }

    @Override
    public void prepare(String sql) {
        PreparedStatement pstmt = null;
        if ((pstmt = pstmtForSql.get(sql)) == null) {
            pstmt = new PreparedStatement(++pstmtId, sql, 0, 0);
            pstmtForSql.put(pstmt.getStatement(), pstmt);
            pstmtForId.put(pstmt.getId(), pstmt);
        }
        PreparedStmtResponse.response(pstmt, source);
    }

    @Override
    public void execute(byte[] data) {
        long pstmtId = ByteUtil.readUB4(data, 5);
        PreparedStatement pstmt = null;
        if ((pstmt = pstmtForSql.get(pstmtId)) == null) {
            source.writeErrMessage(ErrorCode.ER_ERROR_WHEN_EXECUTING_COMMAND, "Unknown pstmtId when executing.");
        } else {
            ExecutePacket packet = new ExecutePacket(pstmt);
            try {
                packet.read(data, source.getCharset());
            } catch (UnsupportedEncodingException e) {
                source.writeErrMessage(ErrorCode.ER_ERROR_WHEN_EXECUTING_COMMAND, e.getMessage());
                return;
            }
            // TODO ...
        }
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub

    }

}
