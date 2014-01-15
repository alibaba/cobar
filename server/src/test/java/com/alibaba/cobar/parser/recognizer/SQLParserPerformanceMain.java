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
package com.alibaba.cobar.parser.recognizer;

import java.sql.SQLSyntaxErrorException;

/**
 * @author xianmao.hexm
 */
public class SQLParserPerformanceMain {

    public static void performance() throws SQLSyntaxErrorException {
        String sql = "select id,member_id,gmt_create from offer where member_id in ('1','22','333','1124','4525')";
        for (int i = 0; i < 1000000; i++) {
            SQLParserDelegate.parse(sql);
        }
    }

    public static void main(String[] args) throws SQLSyntaxErrorException {
        performance();
    }

}
