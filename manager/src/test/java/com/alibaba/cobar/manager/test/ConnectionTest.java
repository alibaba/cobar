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

package com.alibaba.cobar.manager.test;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;

public class ConnectionTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            BasicDataSource ds = new BasicDataSource();
            ds.setUsername("test");
            ds.setPassword("");
            ds.setUrl("jdbc:mysql://10.20.153.178:9066/");
            ds.setDriverClassName("com.mysql.jdbc.Driver");
            ds.setMaxActive(-1);
            ds.setMinIdle(0);
            ds.setTimeBetweenEvictionRunsMillis(600000);
            ds.setNumTestsPerEvictionRun(Integer.MAX_VALUE);
            ds.setMinEvictableIdleTimeMillis(GenericObjectPool.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS);
            Connection conn = ds.getConnection();
            
            Statement stm = conn.createStatement();
            stm.execute("show @@version");
            
            ResultSet rst = stm.getResultSet();
            rst.next();
            String version = rst.getString("VERSION");
            
            System.out.println(version);

        } catch (Exception exception) {
            System.out.println("10.20.153.178:9066   " + exception.getMessage() +exception);
        }
    }
}
