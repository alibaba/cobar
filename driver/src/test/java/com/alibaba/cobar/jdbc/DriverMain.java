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
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

/**
 * @author xianmao.hexm 2012-4-27
 */
public class DriverMain {

    public static void main(String[] args) throws Exception {
        //Class.forName("com.alibaba.cobar.jdbc.Driver");
        String url = "jdbc:cobar://localhost:8066/cndb";
        Properties info = new Properties();
        info.setProperty("user", "test");
        info.setProperty("password", "");
        Connection con = null;
        try {
            con = DriverManager.getConnection(url, info);
            Statement stmt = con.createStatement();
            String query = "select id,member_id from t1 limit 1";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                System.out.println("id:" + rs.getString(1) + ",member_id:" + rs.getString(2));
            }
            rs.close();
            stmt.close();
        } finally {
            if (con != null) {
                con.close();
            }
        }
    }

}
