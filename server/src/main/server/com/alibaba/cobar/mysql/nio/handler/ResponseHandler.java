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
 * @author xianmao.hexm
 */
public interface ResponseHandler {
    /**
     * 已获得有效连接的响应处理
     */
    void connectionAcquired(MySQLConnection conn);

    /**
     * 发生异常的响应处理
     */
    void connectionError(Throwable e, MySQLConnection conn);

    /**
     * 收到错误数据包的响应处理
     */
    void errorResponse(byte[] err, MySQLConnection conn);

    /**
     * 收到OK数据包的响应处理
     */
    void okResponse(byte[] ok, MySQLConnection conn);

    /**
     * 收到字段数据包结束的响应处理
     */
    void fieldEofResponse(byte[] header, List<byte[]> fields, byte[] eof, MySQLConnection conn);

    /**
     * 收到行数据包的响应处理
     */
    void rowResponse(byte[] row, MySQLConnection conn);

    /**
     * 收到行数据包结束的响应处理
     */
    void rowEofResponse(byte[] eof, MySQLConnection conn);

}
