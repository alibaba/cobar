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
package com.alibaba.cobar.heartbeat;

import org.junit.Test;

/**
 * @author xianmao.hexm
 */
public class HeartbeatConfigForTest {
    @Test
    public void testNoop() {
    }
    // public static DataNodeConfig[] getOfferNodes(int offset, int length) {
    // DataNodeConfig[] nodes = new DataNodeConfig[length];
    // for (int i = 0; i < length; i++) {
    // DataNodeConfig node = new DataNodeConfig();
    // node.name = "offer" + (offset + i);
    // node.activedIndex = 0;
    // node.dataSource = getOfferDataSource(node.name);
    // nodes[i] = node;
    // }
    // return nodes;
    // }
    //
    // private static DataSourceConfig[] getOfferDataSource(String schema) {
    // DataSourceConfig ds1 = new DataSourceConfig();
    // ds1.host = "10.20.132.17";
    // ds1.port = 3306;
    // ds1.schema = schema;
    // ds1.user = "offer";
    // ds1.password = "offer";
    // ds1.statement = "update xdual set x=now()";
    //
    // DataSourceConfig ds2 = new DataSourceConfig();
    // ds2.host = "10.20.153.177";
    // ds2.port = 3316;
    // ds2.schema = schema;
    // ds2.user = "offer";
    // ds2.password = "offer";
    // ds2.statement = "update xdual set x=now()";
    //
    // return new DataSourceConfig[] { ds1, ds2 };
    // }
    //
    // public static DataNodeConfig getNodeErrorConfig() {
    // // 数据源1（IP错误）
    // DataSourceConfig ds1 = new DataSourceConfig();
    // ds1.host = "100.20.132.17";
    // ds1.port = 3306;
    // ds1.schema = "offer1";
    // ds1.user = "offer";
    // ds1.password = "offer";
    // ds1.statement = "update xdual set x=now()";
    //
    // // 数据源2（端口错误）
    // DataSourceConfig ds2 = new DataSourceConfig();
    // ds2.host = "10.20.132.17";
    // ds2.port = 3316;
    // ds2.schema = "offer1";
    // ds2.user = "offer";
    // ds2.password = "offer";
    // ds2.statement = "update xdual set x=now()";
    //
    // // 数据源3（SCHEMA错误）
    // DataSourceConfig ds3 = new DataSourceConfig();
    // ds3.host = "10.20.132.17";
    // ds3.port = 3306;
    // ds3.schema = "offer1_x";
    // ds3.user = "offer";
    // ds3.password = "offer";
    // ds3.statement = "update xdual set x=now()";
    //
    // // 数据源4（用户错误）
    // DataSourceConfig ds4 = new DataSourceConfig();
    // ds4.host = "10.20.132.17";
    // ds4.port = 3306;
    // ds4.schema = "offer1";
    // ds4.user = "offer_x";
    // ds4.password = "offer";
    // ds4.statement = "update xdual set x=now()";
    //
    // // 数据源5（密码错误）
    // DataSourceConfig ds5 = new DataSourceConfig();
    // ds5.host = "10.20.132.17";
    // ds5.port = 3306;
    // ds5.schema = "offer1";
    // ds5.user = "offer";
    // ds5.password = "offer_x";
    // ds5.statement = "update xdual set x=now()";
    //
    // // 数据源6（语句错误）
    // DataSourceConfig ds6 = new DataSourceConfig();
    // ds6.host = "10.20.132.17";
    // ds6.port = 3306;
    // ds6.schema = "offer1";
    // ds6.user = "offer";
    // ds6.password = "offer";
    // ds6.statement = "update xdual_x set x=now()";
    //
    // // 数据源（正确配置）
    // DataSourceConfig ds = new DataSourceConfig();
    // ds.host = "10.20.132.17";
    // ds.port = 3306;
    // ds.schema = "offer1";
    // ds.user = "offer";
    // ds.password = "offer";
    // ds.statement = "update xdual set x=now()";
    //
    // DataNodeConfig node = new DataNodeConfig();
    // node.name = "offer1";
    // node.activedIndex = 0;
    // node.dataSource = new DataSourceConfig[] { ds1, ds2, ds3, ds4, ds5, ds6,
    // ds };
    // return node;
    // }

}
