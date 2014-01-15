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
 * (created at 2012-4-17)
 */
package com.alibaba.cobar.mysql.nio;

import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.alibaba.cobar.config.Alarms;
import com.alibaba.cobar.config.model.DataSourceConfig;
import com.alibaba.cobar.mysql.MySQLDataNode;
import com.alibaba.cobar.mysql.nio.handler.DelegateResponseHandler;
import com.alibaba.cobar.mysql.nio.handler.ResponseHandler;
import com.alibaba.cobar.statistic.SQLRecorder;
import com.alibaba.cobar.util.TimeUtil;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class MySQLConnectionPool {
    private static final Logger alarm = Logger.getLogger("alarm");

    private final MySQLDataNode dataNode;
    private final int index;
    private final String name;
    private final ReentrantLock lock = new ReentrantLock();
    private final MySQLConnectionFactory factory;
    private final DataSourceConfig config;
    private final int size;

    private final MySQLConnection[] items;
    private int activeCount;
    private int idleCount;
    private final SQLRecorder sqlRecorder;

    public MySQLConnectionPool(MySQLDataNode node, int index, DataSourceConfig config, int size) {
        this.dataNode = node;
        this.size = size;
        this.items = new MySQLConnection[size];
        this.config = config;
        this.name = config.getName();
        this.index = index;
        this.factory = new MySQLConnectionFactory();
        this.sqlRecorder = new SQLRecorder(config.getSqlRecordCount());
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public void getConnection(final ResponseHandler handler, final Object attachment) throws Exception {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            // too many active connections
            if (activeCount >= size) {
                StringBuilder s = new StringBuilder();
                s.append(Alarms.DEFAULT).append("[name=").append(name).append(",active=");
                s.append(activeCount).append(",size=").append(size).append(']');
                alarm.error(s.toString());
            }

            // get connection from pool
            final MySQLConnection[] items = this.items;
            for (int i = 0, len = items.length; idleCount > 0 && i < len; ++i) {
                if (items[i] != null) {
                    MySQLConnection conn = items[i];
                    items[i] = null;
                    --idleCount;
                    if (conn.isClosedOrQuit()) {
                        continue;
                    } else {
                        ++activeCount;
                        conn.setAttachment(attachment);
                        handler.connectionAcquired(conn);
                        return;
                    }
                }
            }

            ++activeCount;
        } finally {
            lock.unlock();
        }

        // create connection
        factory.make(this, new DelegateResponseHandler(handler) {
            private boolean deactived;

            @Override
            public void connectionError(Throwable e, MySQLConnection conn) {
                lock.lock();
                try {
                    if (!deactived) {
                        --activeCount;
                        deactived = true;
                    }
                } finally {
                    lock.unlock();
                }
                handler.connectionError(e, conn);
            }

            @Override
            public void connectionAcquired(MySQLConnection conn) {
                conn.setAttachment(attachment);
                handler.connectionAcquired(conn);
            }
        });
    }

    public void releaseChannel(MySQLConnection c) {
        if (c == null || c.isClosedOrQuit()) {
            return;
        }

        // release connection
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            final MySQLConnection[] items = this.items;
            for (int i = 0; i < items.length; i++) {
                if (items[i] == null) {
                    ++idleCount;
                    --activeCount;
                    c.setLastTime(TimeUtil.currentTimeMillis());
                    items[i] = c;
                    return;
                }
            }
        } finally {
            lock.unlock();
        }

        // close excess connection
        c.quit();
    }

    public void deActive() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            --activeCount;
        } finally {
            lock.unlock();
        }
    }

    public SQLRecorder getSqlRecorder() {
        return sqlRecorder;
    }

    public DataSourceConfig getConfig() {
        return config;
    }

}
