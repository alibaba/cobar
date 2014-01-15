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

package com.alibaba.cobar.manager.qa.modle;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;

import com.alibaba.cobar.manager.dao.delegate.CobarAdapter;

public class CobarFactory {

    public static CobarAdapter getCobarAdapter(String cobarNodeName) throws IOException {
        CobarAdapter cAdapter = new CobarAdapter();
        Properties prop = new Properties();
        prop.load(CobarFactory.class.getClassLoader().getResourceAsStream("cobarNode.properties"));
        BasicDataSource ds = new BasicDataSource();
        String user = prop.getProperty(cobarNodeName + ".user").trim();
        String password = prop.getProperty(cobarNodeName + ".password").trim();
        String ip = prop.getProperty(cobarNodeName + ".ip").trim();
        int managerPort = Integer.parseInt(prop.getProperty(cobarNodeName + ".manager.port").trim());
        int maxActive = -1;
        int minIdle = 0;
        long timeBetweenEvictionRunsMillis = 10 * 60 * 1000;
        int numTestsPerEvictionRun = Integer.MAX_VALUE;
        long minEvictableIdleTimeMillis = GenericObjectPool.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS;
        ds.setUsername(user);
        ds.setPassword(password);
        ds.setUrl(new StringBuilder().append("jdbc:mysql://")
                                     .append(ip)
                                     .append(":")
                                     .append(managerPort)
                                     .append("/")
                                     .toString());
        ds.setDriverClassName("com.mysql.jdbc.Driver");
        ds.setMaxActive(maxActive);
        ds.setMinIdle(minIdle);
        ds.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        ds.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
        ds.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);

        cAdapter.setDataSource(ds);
        return cAdapter;
    }

    public static SimpleCobarNode getSimpleCobarNode(String cobarNodeName) throws Exception {
        Properties prop = new Properties();
        prop.load(CobarFactory.class.getClassLoader().getResourceAsStream("cobarNode.properties"));
        String user = prop.getProperty(cobarNodeName + ".user").trim();
        String password = prop.getProperty(cobarNodeName + ".password").trim();
        String ip = prop.getProperty(cobarNodeName + ".ip").trim();
        int dmlPort = Integer.parseInt(prop.getProperty(cobarNodeName + ".dml.port").trim());
        int managerPort = Integer.parseInt(prop.getProperty(cobarNodeName + ".manager.port").trim());
        SimpleCobarNode sCobarNode = new SimpleCobarNode(ip, dmlPort, managerPort, user, password);
        return sCobarNode;
    }
}
