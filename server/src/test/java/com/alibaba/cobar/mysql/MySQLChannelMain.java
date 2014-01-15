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
package com.alibaba.cobar.mysql;

import com.alibaba.cobar.config.model.DataSourceConfig;
import com.alibaba.cobar.mysql.bio.Channel;

/**
 * @author xianmao.hexm 2011-5-6 下午08:59:07
 */
public class MySQLChannelMain {

    public Channel getChannel() throws Exception {
        DataSourceConfig config = new DataSourceConfig();
        config.setHost("10.20.153.177");
        config.setPort(3306);
        config.setUser("offer");
        config.setPassword("offer");
        config.setDatabase("offer1");
        MySQLDataSource ds = new MySQLDataSource(null, 0, config, 1);
        return ds.getChannel();
    }

    public static void main(String[] args) throws Exception {
        MySQLChannelMain test = new MySQLChannelMain();
        Channel channel = test.getChannel();
        channel.close();
    }

}
