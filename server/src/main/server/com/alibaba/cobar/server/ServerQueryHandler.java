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
package com.alibaba.cobar.server;

import org.apache.log4j.Logger;

import com.alibaba.cobar.config.ErrorCode;
import com.alibaba.cobar.net.handler.FrontendQueryHandler;
import com.alibaba.cobar.server.handler.BeginHandler;
import com.alibaba.cobar.server.handler.ExplainHandler;
import com.alibaba.cobar.server.handler.KillHandler;
import com.alibaba.cobar.server.handler.SavepointHandler;
import com.alibaba.cobar.server.handler.SelectHandler;
import com.alibaba.cobar.server.handler.SetHandler;
import com.alibaba.cobar.server.handler.ShowHandler;
import com.alibaba.cobar.server.handler.StartHandler;
import com.alibaba.cobar.server.handler.UseHandler;
import com.alibaba.cobar.server.parser.ServerParse;

/**
 * @author xianmao.hexm
 */
public class ServerQueryHandler implements FrontendQueryHandler {
    private static final Logger LOGGER = Logger.getLogger(ServerQueryHandler.class);

    private final ServerConnection source;

    public ServerQueryHandler(ServerConnection source) {
        this.source = source;
    }

    @Override
    public void query(String sql) {
        ServerConnection c = this.source;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(new StringBuilder().append(c).append(sql).toString());
        }
        int rs = ServerParse.parse(sql);
        switch (rs & 0xff) {
        case ServerParse.EXPLAIN:
            ExplainHandler.handle(sql, c, rs >>> 8);
            break;
        case ServerParse.SET:
            SetHandler.handle(sql, c, rs >>> 8);
            break;
        case ServerParse.SHOW:
            ShowHandler.handle(sql, c, rs >>> 8);
            break;
        case ServerParse.SELECT:
            SelectHandler.handle(sql, c, rs >>> 8);
            break;
        case ServerParse.START:
            StartHandler.handle(sql, c, rs >>> 8);
            break;
        case ServerParse.BEGIN:
            BeginHandler.handle(sql, c);
            break;
        case ServerParse.SAVEPOINT:
            SavepointHandler.handle(sql, c);
            break;
        case ServerParse.KILL:
            KillHandler.handle(sql, rs >>> 8, c);
            break;
        case ServerParse.KILL_QUERY:
            c.writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Unsupported command");
            break;
        case ServerParse.USE:
            UseHandler.handle(sql, c, rs >>> 8);
            break;
        case ServerParse.COMMIT:
            c.commit();
            break;
        case ServerParse.ROLLBACK:
            c.rollback();
            break;
        default:
            c.execute(sql, rs);
        }
    }

}
