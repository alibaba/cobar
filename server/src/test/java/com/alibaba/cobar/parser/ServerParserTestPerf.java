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

import com.alibaba.cobar.server.parser.ServerParseSet;

/**
 * @author xianmao.hexm
 */
public final class ServerParserTestPerf {

    private static void parseSetPerf() {
        // ServerParse.parse("show databases");
        // ServerParseSet.parse("set autocommit=1");
        // ServerParseSet.parse("set names=1");
        ServerParseSet.parse("SET character_set_results = NULL", 4);
        // ServerParse.parse("select id,name,value from t");
        // ServerParse.parse("select * from offer where member_id='abc'");
    }

    public static void main(String[] args) {
        parseSetPerf();
        int count = 10000000;

        System.currentTimeMillis();
        long t1 = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            parseSetPerf();
        }
        long t2 = System.currentTimeMillis();

        // print time
        System.out.println("take:" + ((t2 - t1) * 1000 * 1000) / count + " ns.");
    }

}
