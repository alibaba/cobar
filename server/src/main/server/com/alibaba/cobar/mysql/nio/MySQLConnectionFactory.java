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
package com.alibaba.cobar.mysql.nio;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import com.alibaba.cobar.CobarServer;
import com.alibaba.cobar.config.model.DataSourceConfig;
import com.alibaba.cobar.mysql.nio.handler.ResponseHandler;
import com.alibaba.cobar.net.factory.BackendConnectionFactory;

/**
 * @author xianmao.hexm 2012-4-12
 */
public class MySQLConnectionFactory extends BackendConnectionFactory {

    public MySQLConnection make(MySQLConnectionPool pool, ResponseHandler handler) throws IOException {
        SocketChannel channel = openSocketChannel();
        DataSourceConfig dsc = pool.getConfig();
        MySQLConnection c = new MySQLConnection(channel);
        c.setHost(dsc.getHost());
        c.setPort(dsc.getPort());
        c.setUser(dsc.getUser());
        c.setPassword(dsc.getPassword());
        c.setSchema(dsc.getDatabase());
        c.setHandler(new MySQLConnectionAuthenticator(c, handler));
        c.setPool(pool);
        postConnect(c, CobarServer.getInstance().getConnector());
        return c;
    }

}
