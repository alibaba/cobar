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
 * (created at 2012-4-19)
 */
package com.alibaba.cobar.mysql.nio.handler;

import java.util.List;

import com.alibaba.cobar.mysql.nio.MySQLConnection;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class DelegateResponseHandler implements ResponseHandler {
    private final ResponseHandler target;

    public DelegateResponseHandler(ResponseHandler target) {
        if (target == null) {
            throw new IllegalArgumentException("delegate is null!");
        }
        this.target = target;
    }

    @Override
    public void connectionAcquired(MySQLConnection conn) {
        target.connectionAcquired(conn);
    }

    @Override
    public void connectionError(Throwable e, MySQLConnection conn) {
        target.connectionError(e, conn);
    }

    @Override
    public void okResponse(byte[] ok, MySQLConnection conn) {
        target.okResponse(ok, conn);
    }

    @Override
    public void errorResponse(byte[] err, MySQLConnection conn) {
        target.errorResponse(err, conn);
    }

    @Override
    public void fieldEofResponse(byte[] header, List<byte[]> fields, byte[] eof, MySQLConnection conn) {
        target.fieldEofResponse(header, fields, eof, conn);
    }

    @Override
    public void rowResponse(byte[] row, MySQLConnection conn) {
        target.rowResponse(row, conn);
    }

    @Override
    public void rowEofResponse(byte[] eof, MySQLConnection conn) {
        target.rowEofResponse(eof, conn);
    }

}
