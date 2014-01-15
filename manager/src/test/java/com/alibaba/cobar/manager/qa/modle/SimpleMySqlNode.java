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

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.mysql.jdbc.Connection;

public class SimpleMySqlNode {
    private static String Driver = "com.mysql.jdbc.Driver";
    private String ip = null;
    private static final Logger logger = Logger.getLogger(SimpleMySqlNode.class);

    static {
        try {
            Class.forName(Driver);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            System.exit(-1);
        }
    }

    public SimpleMySqlNode(String ip) {
        this.ip = ip;
    }

    public Connection createConnection(int port, String user, String password, String schema) throws Exception {
        Connection conn = null;
        conn =
                (Connection) DriverManager.getConnection("jdbc:mysql://" + ip + ":" + port + "/" + schema,
                                                         user,
                                                         password);
        return conn;
    }

    public boolean detoryConnection(java.sql.Connection conn) {
        boolean success = false;
        if (null == conn) {
            success = true;
        } else {
            try {
                conn.close();
                success = true;
            } catch (Exception e2) {
                logger.error(e2.getMessage(), e2);
            }
        }
        return success;
    }

    public void executeSQLRead(java.sql.Connection conn, String sql) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery(sql);
        } finally {
            //only catch exceptions when stmt and rs closed
            try {
                rs.close();
                stmt.close();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public int excuteSQWrite(java.sql.Connection dmlConnection, String sql) throws SQLException {
        Statement stmt = dmlConnection.createStatement();
        int result = 0;
        try {
            result = stmt.executeUpdate(sql);
        } finally {
            //only catch exceptions when stmt closed
            try {
                stmt.close();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return result;
    }

    public boolean excuteSQL(java.sql.Connection managerConnection, String sql) throws SQLException {
        Statement stmt = managerConnection.createStatement();
        boolean success = false;
        try {
            stmt.execute(sql);
            success = true;
        } finally {
            try {
                stmt.close();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return success;
    }
}
