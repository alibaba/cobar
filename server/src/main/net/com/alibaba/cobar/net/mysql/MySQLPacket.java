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
package com.alibaba.cobar.net.mysql;

import java.nio.ByteBuffer;

import com.alibaba.cobar.net.BackendConnection;
import com.alibaba.cobar.net.FrontendConnection;

/**
 * @author xianmao.hexm
 */
public abstract class MySQLPacket {
    /**
     * none, this is an internal thread state
     */
    public static final byte COM_SLEEP = 0;

    /**
     * mysql_close
     */
    public static final byte COM_QUIT = 1;

    /**
     * mysql_select_db
     */
    public static final byte COM_INIT_DB = 2;

    /**
     * mysql_real_query
     */
    public static final byte COM_QUERY = 3;

    /**
     * mysql_list_fields
     */
    public static final byte COM_FIELD_LIST = 4;

    /**
     * mysql_create_db (deprecated)
     */
    public static final byte COM_CREATE_DB = 5;

    /**
     * mysql_drop_db (deprecated)
     */
    public static final byte COM_DROP_DB = 6;

    /**
     * mysql_refresh
     */
    public static final byte COM_REFRESH = 7;

    /**
     * mysql_shutdown
     */
    public static final byte COM_SHUTDOWN = 8;

    /**
     * mysql_stat
     */
    public static final byte COM_STATISTICS = 9;

    /**
     * mysql_list_processes
     */
    public static final byte COM_PROCESS_INFO = 10;

    /**
     * none, this is an internal thread state
     */
    public static final byte COM_CONNECT = 11;

    /**
     * mysql_kill
     */
    public static final byte COM_PROCESS_KILL = 12;

    /**
     * mysql_dump_debug_info
     */
    public static final byte COM_DEBUG = 13;

    /**
     * mysql_ping
     */
    public static final byte COM_PING = 14;

    /**
     * none, this is an internal thread state
     */
    public static final byte COM_TIME = 15;

    /**
     * none, this is an internal thread state
     */
    public static final byte COM_DELAYED_INSERT = 16;

    /**
     * mysql_change_user
     */
    public static final byte COM_CHANGE_USER = 17;

    /**
     * used by slave server mysqlbinlog
     */
    public static final byte COM_BINLOG_DUMP = 18;

    /**
     * used by slave server to get master table
     */
    public static final byte COM_TABLE_DUMP = 19;

    /**
     * used by slave to log connection to master
     */
    public static final byte COM_CONNECT_OUT = 20;

    /**
     * used by slave to register to master
     */
    public static final byte COM_REGISTER_SLAVE = 21;

    /**
     * mysql_stmt_prepare
     */
    public static final byte COM_STMT_PREPARE = 22;

    /**
     * mysql_stmt_execute
     */
    public static final byte COM_STMT_EXECUTE = 23;

    /**
     * mysql_stmt_send_long_data
     */
    public static final byte COM_STMT_SEND_LONG_DATA = 24;

    /**
     * mysql_stmt_close
     */
    public static final byte COM_STMT_CLOSE = 25;

    /**
     * mysql_stmt_reset
     */
    public static final byte COM_STMT_RESET = 26;

    /**
     * mysql_set_server_option
     */
    public static final byte COM_SET_OPTION = 27;

    /**
     * mysql_stmt_fetch
     */
    public static final byte COM_STMT_FETCH = 28;

    /**
     * cobar heartbeat
     */
    public static final byte COM_HEARTBEAT = 64;

    public int packetLength;
    public byte packetId;

    /**
     * 把数据包写到buffer中，如果buffer满了就把buffer通过前端连接写出。
     */
    public ByteBuffer write(ByteBuffer buffer, FrontendConnection c) {
        throw new UnsupportedOperationException();
    }

    /**
     * 把数据包通过后端连接写出，一般使用buffer机制来提高写的吞吐量。
     */
    public void write(BackendConnection c) {
        throw new UnsupportedOperationException();
    }

    /**
     * 计算数据包大小，不包含包头长度。
     */
    public abstract int calcPacketSize();

    /**
     * 取得数据包信息
     */
    protected abstract String getPacketInfo();

    @Override
    public String toString() {
        return new StringBuilder().append(getPacketInfo())
                                  .append("{length=")
                                  .append(packetLength)
                                  .append(",id=")
                                  .append(packetId)
                                  .append('}')
                                  .toString();
    }

}
