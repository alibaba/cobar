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
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.cobar.manager.dataobject.cobarnode.DataNodesStatus;
import com.alibaba.cobar.manager.qa.util.TestUtils;

public class TestDataNodes extends TestCobarAdapter {
    private Connection managerConnection = null;
    private Connection dmlConnection = null;
    private static final Logger logger = Logger.getLogger(TestDataNodes.class);
    List<DataNodesStatus> dataNodeStatusList = null;

    @Override
    @Before
    public void initData() {
        super.initData();
        try {
            if (null != managerConnection) {
                managerConnection.close();
            }
            managerConnection = sCobarNode.createManagerConnection();
            if (null != dmlConnection){
                dmlConnection.close();
            }
            dmlConnection = sCobarNode.createDMLConnection("ddl_test");
            if (null != dataNodeStatusList) {
                dataNodeStatusList.clear();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Assert.fail();
        }
    }

    @Override
    @After
    public void end() {
        super.end();
        try {
            if (null != managerConnection) {
                managerConnection.close();
            }
            if (null != dmlConnection){
                dmlConnection.close();
            }
            if (null != dataNodeStatusList) {
                dataNodeStatusList.clear();
            }
        } catch (Exception e) {
            logger.error("destroy adpter error");
            Assert.fail();
        }
    }

    public DataNodesStatus getDataNodeStatus(String poolName) {
        DataNodesStatus dataNodeStatus = null;
        dataNodeStatusList = cobarAdapter.listDataNodes();
        for (DataNodesStatus data : dataNodeStatusList) {
            String dataNode = data.getPoolName();
            if (poolName.equals(dataNode)) {
                dataNodeStatus = data;
                break;
            }
        }
        return dataNodeStatus;
    }

    /*
     * test has suspect that there are no dataNodes with duplicated poolName
     * first test switch @@dataSource poolName, then test switch @@dataSource poolName:index
     */
    @Test
    public void testSwitchIndex() {
        String testDataNode = null;
        boolean resultSwitch = false;
        int indexBeforeSwitch = 0;
        int indexAfterSwitch = 0;
        int dataSourceNumOfTestDataNode = 0;
        dataNodeStatusList = cobarAdapter.listDataNodes();

        //test first dataNode with multi-dataSources or last dataNode with single dataSource
        for (DataNodesStatus dataNodeStatus : dataNodeStatusList) {
            testDataNode = dataNodeStatus.getPoolName();
            indexBeforeSwitch = dataNodeStatus.getIndex();
            String[] dataSources = dataNodeStatus.getDataSource().split(",");
            dataSourceNumOfTestDataNode = dataSources.length;
            if (dataSourceNumOfTestDataNode > 1) {
                resultSwitch = true;
                break;
            }
        }
        Assert.assertNotNull(testDataNode);

        //switch @@dataSource poolName
        try {
            sCobarNode.excuteSQL(managerConnection, "switch @@dataSource " + testDataNode);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            Assert.fail();
        }

        DataNodesStatus testDataNodeStatus = this.getDataNodeStatus(testDataNode);
        Assert.assertNotNull(testDataNodeStatus);
        indexAfterSwitch = testDataNodeStatus.getIndex();
        if (resultSwitch) {
            Assert.assertTrue(indexBeforeSwitch != indexAfterSwitch);
        } else {
            Assert.assertTrue(indexBeforeSwitch == indexAfterSwitch);
        }

        //switch @@dataSource poolName:index
        try {
            sCobarNode.excuteSQL(managerConnection, "switch @@dataSource " + testDataNode + " : " + indexBeforeSwitch);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            Assert.fail();
        }
        testDataNodeStatus = this.getDataNodeStatus(testDataNode);
        Assert.assertNotNull(testDataNodeStatus);
        indexAfterSwitch = testDataNodeStatus.getIndex();
        Assert.assertTrue(indexBeforeSwitch == indexAfterSwitch);

    }

    @Test
    public void testRecoverTime() {
        int recoverTime = 10;
        dataNodeStatusList = cobarAdapter.listDataNodes();
        Assert.assertTrue(dataNodeStatusList.size() >= 1);
        String testDataNode = dataNodeStatusList.get(0).getPoolName();
        try {
            sCobarNode.excuteSQL(managerConnection, "stop @@heartbeat " + testDataNode + " : " + recoverTime);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            Assert.fail();
        }

        DataNodesStatus testDataNodeStatus = this.getDataNodeStatus(testDataNode);
        Assert.assertNotNull(testDataNodeStatus);
        double remainTime = testDataNodeStatus.getRecoveryTime();
        Assert.assertTrue((remainTime > 0) && (remainTime <= recoverTime));

        TestUtils.waitForMonment(recoverTime * 1000);
        testDataNodeStatus = this.getDataNodeStatus(testDataNode);
        Assert.assertNotNull(testDataNodeStatus);
        remainTime = testDataNodeStatus.getRecoveryTime();
        Assert.assertTrue(remainTime == -1);
    }
    
    @Test
    public void testExcute(){
        String testDataNode = "ddl_test";
        dataNodeStatusList = cobarAdapter.listDataNodes();
        DataNodesStatus testDNStatusBefore= getDataNodeStatus(testDataNode);
        Assert.assertNotNull(testDNStatusBefore);
        try {
            sCobarNode.excuteSQL(dmlConnection, "show tables");
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            Assert.fail();
        }
        DataNodesStatus testDNStatusAfter= getDataNodeStatus(testDataNode);
        Assert.assertNotNull(testDNStatusAfter);
        
        Assert.assertEquals(testDNStatusBefore.getExecuteCount() + 1, testDNStatusAfter.getExecuteCount());
    }
    
    /*
     * excute num is not affacted by switch index
     */
    @Test
    public void testExcuteAfterSwitchIndex(){
        String testDataNode = "ddl_test";
        dataNodeStatusList = cobarAdapter.listDataNodes();
        DataNodesStatus testDNStatusBefore= getDataNodeStatus(testDataNode);
        Assert.assertNotNull(testDNStatusBefore);
        try {
            sCobarNode.excuteSQL(managerConnection, "switch @@dataSource " + testDataNode);
            sCobarNode.excuteSQL(dmlConnection, "show tables");
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            Assert.fail();
        }
        DataNodesStatus testDNStatusAfter= getDataNodeStatus(testDataNode);
        Assert.assertNotNull(testDNStatusAfter);
        
        Assert.assertEquals(testDNStatusBefore.getExecuteCount() + 1, testDNStatusAfter.getExecuteCount());
    }

}
