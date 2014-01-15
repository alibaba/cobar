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
package com.alibaba.cobar.net.handler;

import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

import org.apache.log4j.Logger;

import com.alibaba.cobar.config.ErrorCode;
import com.alibaba.cobar.mysql.SecurityUtil;
import com.alibaba.cobar.net.FrontendConnection;
import com.alibaba.cobar.net.NIOHandler;
import com.alibaba.cobar.net.mysql.AuthPacket;
import com.alibaba.cobar.net.mysql.MySQLPacket;
import com.alibaba.cobar.net.mysql.QuitPacket;

/**
 * 前端认证处理器
 * 
 * @author xianmao.hexm
 */
public class FrontendAuthenticator implements NIOHandler {
    private static final Logger LOGGER = Logger.getLogger(FrontendAuthenticator.class);
    private static final byte[] AUTH_OK = new byte[] { 7, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0 };

    protected final FrontendConnection source;

    public FrontendAuthenticator(FrontendConnection source) {
        this.source = source;
    }

    @Override
    public void handle(byte[] data) {
        // check quit packet
        if (data.length == QuitPacket.QUIT.length && data[4] == MySQLPacket.COM_QUIT) {
            source.close();
            return;
        }

        AuthPacket auth = new AuthPacket();
        auth.read(data);

        // check user
        if (!checkUser(auth.user, source.getHost())) {
            failure(ErrorCode.ER_ACCESS_DENIED_ERROR, "Access denied for user '" + auth.user + "'");
            return;
        }

        // check password
        if (!checkPassword(auth.password, auth.user)) {
            failure(ErrorCode.ER_ACCESS_DENIED_ERROR, "Access denied for user '" + auth.user + "'");
            return;
        }

        // check schema
        switch (checkSchema(auth.database, auth.user)) {
        case ErrorCode.ER_BAD_DB_ERROR:
            failure(ErrorCode.ER_BAD_DB_ERROR, "Unknown database '" + auth.database + "'");
            break;
        case ErrorCode.ER_DBACCESS_DENIED_ERROR:
            String s = "Access denied for user '" + auth.user + "' to database '" + auth.database + "'";
            failure(ErrorCode.ER_DBACCESS_DENIED_ERROR, s);
            break;
        default:
            success(auth);
        }
    }

    protected boolean checkUser(String user, String host) {
        return source.getPrivileges().userExists(user, host);
    }

    protected boolean checkPassword(byte[] password, String user) {
        String pass = source.getPrivileges().getPassword(user);

        // check null
        if (pass == null || pass.length() == 0) {
            if (password == null || password.length == 0) {
                return true;
            } else {
                return false;
            }
        }
        if (password == null || password.length == 0) {
            return false;
        }

        // encrypt
        byte[] encryptPass = null;
        try {
            encryptPass = SecurityUtil.scramble411(pass.getBytes(), source.getSeed());
        } catch (NoSuchAlgorithmException e) {
            LOGGER.warn(source.toString(), e);
            return false;
        }
        if (encryptPass != null && (encryptPass.length == password.length)) {
            int i = encryptPass.length;
            while (i-- != 0) {
                if (encryptPass[i] != password[i]) {
                    return false;
                }
            }
        } else {
            return false;
        }

        return true;
    }

    protected int checkSchema(String schema, String user) {
        if (schema == null) {
            return 0;
        }
        FrontendPrivileges privileges = source.getPrivileges();
        if (!privileges.schemaExists(schema)) {
            return ErrorCode.ER_BAD_DB_ERROR;
        }
        Set<String> schemas = privileges.getUserSchemas(user);
        if (schemas == null || schemas.size() == 0 || schemas.contains(schema)) {
            return 0;
        } else {
            return ErrorCode.ER_DBACCESS_DENIED_ERROR;
        }
    }

    protected void success(AuthPacket auth) {
        source.setAuthenticated(true);
        source.setUser(auth.user);
        source.setSchema(auth.database);
        source.setCharsetIndex(auth.charsetIndex);
        source.setHandler(new FrontendCommandHandler(source));
        if (LOGGER.isInfoEnabled()) {
            StringBuilder s = new StringBuilder();
            s.append(source).append('\'').append(auth.user).append("' login success");
            byte[] extra = auth.extra;
            if (extra != null && extra.length > 0) {
                s.append(",extra:").append(new String(extra));
            }
            LOGGER.info(s.toString());
        }
        ByteBuffer buffer = source.allocate();
        source.write(source.writeToBuffer(AUTH_OK, buffer));
    }

    protected void failure(int errno, String info) {
        LOGGER.error(source.toString() + info);
        source.writeErrMessage((byte) 2, errno, info);
    }

}
