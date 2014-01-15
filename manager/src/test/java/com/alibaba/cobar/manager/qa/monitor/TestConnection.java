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

package com.alibaba.cobar.manager.qa.monitor;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.cobar.manager.dataobject.cobarnode.ConnectionStatus;

public class TestConnection extends TestCobarAdapter {
    private static final Logger logger = Logger.getLogger(TestConnection.class);
    List<Connection> connList = null;
    int connNum = 0;

    @Override
    @Before
    public void initData() {
        super.initData();
        if (null != connList) {
            for (Connection conn : connList) {
                Assert.assertTrue(sCobarNode.detoryConnection(conn));
            }
            connList.clear();
        } else {
            connList = new ArrayList<Connection>();
        }
        connNum = 0;

    }

    public void connStatisic(List<ConnectionStatus> connStatusList) {
        connNum = 0;
        if (null != connStatusList) {
            connNum = connStatusList.size();
        }
    }

    @Test(timeout = 60000)
    public void testActiveConnection() {
        //TestUtils.waitForMonment(50000);
        int listNum = 10;
        List<ConnectionStatus> connStatusList = null;
        try {
            //create connection
            for (int i = 0; i < listNum; i++) {
                Connection conn = sCobarNode.createDMLConnection("ddl_test");
                connList.add(conn);
            }

            //get connection num from manager
            connStatusList = cobarAdapter.listConnectionStatus();
            Assert.assertNotNull(connStatusList);
            connStatisic(connStatusList);
            Assert.assertEquals(connNum, listNum + 1);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Assert.fail();
        } finally {
            for (Connection conn : connList) {
                Assert.assertTrue(sCobarNode.detoryConnection(conn));
            }
        }
    }

    @Test(timeout = 60000)
    public void testClosedConnection() {
        //TestUtils.waitForMonment(50000);
        int listNum = 10;
        List<ConnectionStatus> connStatusList = null;
        try {
            //create connection
            for (int i = 0; i < listNum; i++) {
                Connection conn = sCobarNode.createDMLConnection("ddl_test");
                connList.add(conn);
            }
            //destroy all connections
            for (Connection conn : connList) {
                Assert.assertTrue(sCobarNode.detoryConnection(conn));
            }
            //get connection num from manager
            connStatusList = cobarAdapter.listConnectionStatus();
            Assert.assertNotNull(connStatusList);
            connStatisic(connStatusList);
            Assert.assertEquals(connNum, 1);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Assert.fail();
        }
    }

    /*
     * excute "show @@connnection" for several times, the connection num is 1
     */
    @Test(timeout = 60000)
    public void testConnection() {
        //TestUtils.waitForMonment(50000);
        int queryNum = 10;
        List<ConnectionStatus> connStatusList = null;
        for (int i = 0; i < queryNum; i++) {
            connStatusList = cobarAdapter.listConnectionStatus();
        }
        connStatusList = cobarAdapter.listConnectionStatus();
        cobarAdapter.listCommandStatus();
        cobarAdapter.listDataBases();
        connStatisic(connStatusList);
        Assert.assertEquals(connNum, 1);
    }

}
