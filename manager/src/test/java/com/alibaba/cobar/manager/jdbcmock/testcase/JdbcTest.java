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
import java.sql.ResultSet;
import java.sql.Statement;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;

import com.alibaba.cobar.manager.jdbcmock.domain.DBUtility;
import com.alibaba.cobar.manager.jdbcmock.domain.SalesOrder;
import com.alibaba.cobar.manager.jdbcmock.domain.SalesOrderImpl;
import com.alibaba.cobar.manager.jdbcmock.macher.SQLEquals;

import junit.framework.TestCase;

public class JdbcTest extends TestCase {
    public void test() {
        IMocksControl control = EasyMock.createControl();

        DBUtility mockDBUtility = control.createMock(DBUtility.class);
        Connection mockConnection = control.createMock(Connection.class);
        Statement mockStatement = control.createMock(Statement.class);
        ResultSet mockResultSet = control.createMock(ResultSet.class);

        try {
            mockDBUtility.getConnection();
            EasyMock.expectLastCall().andStubReturn(mockConnection);

            mockConnection.createStatement();
            EasyMock.expectLastCall().andStubReturn(mockStatement);

            mockStatement.executeQuery(SQLEquals.sqlEquals("SELECT * FROM sales_order_table"));
            EasyMock.expectLastCall().andStubReturn(mockResultSet);

            mockResultSet.next();
            EasyMock.expectLastCall().andReturn(true).times(3);
            EasyMock.expectLastCall().andReturn(false).times(1);

            mockResultSet.getString(1);
            EasyMock.expectLastCall().andReturn("DEMO_ORDER_001").times(1);
            EasyMock.expectLastCall().andReturn("DEMO_ORDER_002").times(1);
            EasyMock.expectLastCall().andReturn("DEMO_ORDER_003").times(1);

            mockResultSet.getString(2);
            EasyMock.expectLastCall().andReturn("Asia Pacific").times(1);
            EasyMock.expectLastCall().andReturn("Europe").times(1);
            EasyMock.expectLastCall().andReturn("America").times(1);

            mockResultSet.getDouble(3);
            EasyMock.expectLastCall().andReturn(350.0).times(1);
            EasyMock.expectLastCall().andReturn(1350.0).times(1);
            EasyMock.expectLastCall().andReturn(5350.0).times(1);

            control.replay();

            Connection conn = mockDBUtility.getConnection();
            Statement stat = conn.createStatement();
            ResultSet rs = stat.executeQuery("select * from sales_order_table");

            int i = 0;
            String[] priceLevels = { "Level_A", "Level_C", "Level_E" };
            while (rs.next()) {
                SalesOrder order = new SalesOrderImpl();
                order.loadDataFromDB(rs);
                assertEquals(order.getPriceLevel(), priceLevels[i]);
                i++;
            }

            control.verify();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
