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
package com.alibaba.cobar.jdbc;

import java.sql.Connection;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

import com.mysql.jdbc.NonRegisteringDriver;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * 在使用集群时提供负载均衡的功能，其他情况和MySQLDriver一样。
 *
 * <pre>
 * 使用方法：
 *   Class.forName("com.alibaba.cobar.jdbc.Driver");
 *   String url = "jdbc:cobar://host:port/dbname?user=xxx&password=xxx";
 *   ...
 * </pre>
 *
 * @author xianmao.hexm 2012-4-27
 */
public class Driver extends NonRegisteringDriver implements java.sql.Driver {

    public static final String VERSION = "1.0.0";

    /**
     * Register ourselves with the DriverManager
     */
    static {
        try {
            java.sql.DriverManager.registerDriver(new Driver());
        } catch (SQLException E) {
            throw new RuntimeException("Can't register driver!");
        }
    }

    /**
     * Construct a new driver and register it with DriverManager
     *
     * @throws SQLException if a database error occurs.
     */
    public Driver() throws SQLException {
        // Required for Class.forName().newInstance()
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        return super.connect(UrlProvider.getUrl(url, info), info);
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return super.acceptsURL(UrlProvider.getMySQLUrl(url));
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return super.getPropertyInfo(UrlProvider.getMySQLUrl(url), info);
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("Not supported yet.");
    }

}
