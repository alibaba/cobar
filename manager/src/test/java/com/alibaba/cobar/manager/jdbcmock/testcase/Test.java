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

package com.alibaba.cobar.manager.jdbcmock.testcase;

import java.sql.Connection;

import static com.alibaba.cobar.manager.util.SQLDefine.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


import com.alibaba.cobar.manager.jdbcmock.domain.JDBCMock;

import junit.framework.TestCase;

public class Test extends TestCase {
    public void test1() throws SQLException {

        JDBCMock mock = new JDBCMock();
        Connection conn = mock.mockDBUtility.getConnection();
        Statement stat = conn.createStatement();
        ResultSet rs;

       rs = stat.executeQuery("Show @@Datanodes");

        while (rs.next()) {
            System.out.println(rs.getString(POOL_NAME) + "   " + rs.getString(DS) + "   " + rs.getInt(INDEX));
        }
        mock.resetDataNodes();
        
        rs = stat.executeQuery("Show @@Datanodes");

        while (rs.next()) {
            System.out.println(rs.getString(POOL_NAME) + "   " + rs.getString(DS) + "   " + rs.getInt(INDEX));
        }
        mock.resetDataNodes();
        rs = stat.executeQuery("Show @@Datanodes");

        while (rs.next()) {
            System.out.println(rs.getString(POOL_NAME) + "   " + rs.getString(DS) + "   " + rs.getInt(INDEX));
        }
        mock.resetDataNodes();
        rs = stat.executeQuery("show @@time.current");
        while (rs.next()) {
            System.out.println(rs.getString(TIMESTAMP));
        }
        

        rs=stat.executeQuery("show @@time.startup");
        while(rs.next()){
            System.out.println(rs.getString(TIMESTAMP));
        }
        mock.resetTimeStartUp();
        rs=stat.executeQuery("show @@time.startup");
        while(rs.next()){
            System.out.println(rs.getString(TIMESTAMP));
        }
        
        mock.resetTimeStartUp();
        rs=stat.executeQuery("show @@time.startup");
        while(rs.next()){
            System.out.println(rs.getString(TIMESTAMP));
        }
        rs=stat.executeQuery("show @@databases");
        while(rs.next()){
            System.out.println(rs.getString(DATABASE));
        }

        
        System.out.println(stat.executeUpdate("kill @@connection 1909417519"));

         rs = stat.executeQuery("Show @@connection");

        while (rs.next()) {
            System.out.println(rs.getString(C_PROCESSOR) + "   " + rs.getLong(ID) + "   " + rs.getString(HOST));
        }

        
        System.out.println(stat.executeUpdate("reload @@config"));
        System.out.println(stat.executeUpdate("switch @@Datasource circe:1"));
        System.out.println(stat.executeUpdate("kill @@CONnection 111"));
        System.out.println(stat.executeUpdate("sTop @@HeartBeat circe:1"));
        System.out.println(stat.executeUpdate("switch @@Datasource circe:1"));
        System.out.println(stat.executeUpdate("kill @@CONnection 111"));
        System.out.println(stat.executeUpdate("sTop @@HeartBeat circe:1"));
        
        
        rs = stat.executeQuery("show @@sql.detail where id=1000");

        while (rs.next()) {
            System.out.println(rs.getString(D_LAST_EXECUTE_TIMESTAMP) + "   " + rs.getLong(D_EXECUTE) + "   " + rs.getDouble(D_TIME));
        }
        
      rs = stat.executeQuery("show @@sql where id=1000");

      while (rs.next()) {
          System.out.println(rs.getLong(SQL_ID) + "   " + rs.getString(SQL_DETAIL) );
      }
   }
}
