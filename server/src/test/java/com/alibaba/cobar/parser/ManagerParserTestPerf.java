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
package com.alibaba.cobar.parser;

import com.alibaba.cobar.manager.parser.ManagerParse;

/**
 * @author xianmao.hexm
 */
public class ManagerParserTestPerf {

    public void testPerformance() {
        for (int i = 0; i < 250000; i++) {
            ManagerParse.parse("show databases");
            ManagerParse.parse("set autocommit=1");
            ManagerParse.parse(" show  @@datasource ");
            ManagerParse.parse("select id,name,value from t");
        }
    }

    public void testPerformanceWhere() {
        for (int i = 0; i < 500000; i++) {
            ManagerParse.parse(" show  @@datasource where datanode = 1");
            ManagerParse.parse(" show  @@datanode where schema = 1");
        }
    }

}
