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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author xianmao.hexm 2012-7-18
 */
public class FetchSizeMain {

    public static void main(String[] args) throws SQLException {
        String url = "jdbc:mysql://localhost:3306/cndb?defaultFetchSize=5&useCursorFetch=true";
        Properties info = new Properties();
        info.setProperty("user", "test");
        info.setProperty("password", "");
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, info);
            long t1 = System.currentTimeMillis();
            PreparedStatement pstmt = conn.prepareStatement("select id from t1");
            System.out.println("set fetch size:" + pstmt.getFetchSize());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                System.out.println("id:" + rs.getString(1));
                System.out.println((System.currentTimeMillis() - t1) + " ms.");
                System.out.println("OK");
            }
            rs.close();
            pstmt.close();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

}
