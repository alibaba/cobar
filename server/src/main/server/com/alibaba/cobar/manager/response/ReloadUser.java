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

import org.apache.log4j.Logger;

import com.alibaba.cobar.config.ErrorCode;
import com.alibaba.cobar.manager.ManagerConnection;
import com.alibaba.cobar.net.mysql.OkPacket;

/**
 * @author xianmao.hexm
 */
public final class ReloadUser {

    private static final Logger logger = Logger.getLogger(ReloadUser.class);

    public static void execute(ManagerConnection c) {
        boolean status = false;
        if (status) {
            StringBuilder s = new StringBuilder();
            s.append(c).append("Reload userConfig success by manager");
            logger.warn(s.toString());
            OkPacket ok = new OkPacket();
            ok.packetId = 1;
            ok.affectedRows = 1;
            ok.serverStatus = 2;
            ok.message = "Reload userConfig success".getBytes();
            ok.write(c);
        } else {
            c.writeErrMessage(ErrorCode.ER_YES, "Unsupported statement");
        }
    }

}
