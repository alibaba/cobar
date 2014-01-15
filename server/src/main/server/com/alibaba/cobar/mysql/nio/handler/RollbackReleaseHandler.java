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
 * (created at 2012-5-11)
 */
package com.alibaba.cobar.mysql.nio.handler;

import java.util.List;

import org.apache.log4j.Logger;

import com.alibaba.cobar.mysql.nio.MySQLConnection;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class RollbackReleaseHandler implements ResponseHandler {
    private static final Logger logger = Logger.getLogger(RollbackReleaseHandler.class);

    public RollbackReleaseHandler() {
    }

    @Override
    public void connectionAcquired(MySQLConnection conn) {
        logger.error("unexpected invocation: connectionAcquired from rollback-release");
        conn.close();
    }

    @Override
    public void connectionError(Throwable e, MySQLConnection conn) {
        logger.error("unexpected invocation: connectionError from rollback-release");
        conn.close();
    }

    @Override
    public void errorResponse(byte[] err, MySQLConnection conn) {
        conn.quit();
    }

    @Override
    public void okResponse(byte[] ok, MySQLConnection conn) {
        conn.release();
    }

    @Override
    public void fieldEofResponse(byte[] header, List<byte[]> fields, byte[] eof, MySQLConnection conn) {
    }

    @Override
    public void rowResponse(byte[] row, MySQLConnection conn) {
    }

    @Override
    public void rowEofResponse(byte[] eof, MySQLConnection conn) {
        logger.error("unexpected packet: EOF of resultSet from rollback-release");
        conn.close();
    }

}
