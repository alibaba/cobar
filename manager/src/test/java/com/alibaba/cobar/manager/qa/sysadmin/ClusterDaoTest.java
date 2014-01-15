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

package com.alibaba.cobar.manager.qa.sysadmin;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.alibaba.cobar.manager.dao.xml.ClusterDAOImple;
import com.alibaba.cobar.manager.dao.xml.XMLFileLoaderPath;
import com.alibaba.cobar.manager.dataobject.xml.ClusterDO;

/**
 * @author xiaowen.guoxw
 * @version ???????2011-6-23 ????02:11:55
 */

public class ClusterDaoTest extends SysAdminTest {
    private ClusterDAOImple clusterDao = null;
    private static String clusterXmlPath = null;
    private static final Logger logger = Logger.getLogger(ClusterDaoTest.class);
    private XMLFileLoaderPath xmlFileLoader = null;

    @Before
    public void initData() {
        Assert.assertNotNull(xmlPath);
        xmlFileLoader = new XMLFileLoaderPath();
        xmlFileLoader.setXmlPath(xmlPath);
        clusterDao = new ClusterDAOImple();
        clusterDao.setXmlFileLoader(xmlFileLoader);
        clusterXmlPath = xmlPath + System.getProperty("file.separator") + "cluster.xml";
        XmlFile xmlFile = new XmlFile(clusterXmlPath, "clusters");
        try {
            xmlFile.init();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            Assert.fail();
        }
        read();
    }

    @Test
    public void testAddFristCluster() {
        long testId = 1L;
        ClusterDO clusterTemp = clusterDao.getClusterById(testId);
        Assert.assertNull(clusterTemp);

        ClusterDO cluster = DOFactory.getCluster();

        clusterDao.addCluster(cluster);

        Assert.assertNotNull(clusterDao.getClusterById(testId));
        Assert.assertSame(cluster, clusterDao.getClusterById(testId));

        read();
        Assert.assertNotNull(clusterDao.getClusterById(testId));

    }

    @Test
    public void testAddManyCluster() {
        int insertClusterNum = 3;

        for (int i = 0; i < insertClusterNum; i++) {
            ClusterDO cluster = DOFactory.getCluster();
            cluster.setName("test" + i);
            clusterDao.addCluster(cluster);
        }

        read();
        List<ClusterDO> activeClusterList = clusterDao.listAllCluster();
        Assert.assertEquals(insertClusterNum, activeClusterList.size());
    }

    @Test
    public void testAddDuplicatedCluster() {
        ClusterDO cluster = DOFactory.getCluster();
        clusterDao.addCluster(cluster);

        int duplicatedClusterNum = 2;
        for (int i = 0; i < duplicatedClusterNum; i++) {
            ClusterDO clusterDuplicate = DOFactory.getCluster();
            Assert.assertFalse(clusterDao.addCluster(clusterDuplicate));
        }

        read();
        Assert.assertEquals(1, clusterDao.listAllCluster().size());

    }

    @Test
    public void testModifyClusterWithoutId() {
        ClusterDO cluster = DOFactory.getCluster();
        clusterDao.addCluster(cluster);

        long testId = 1L;
        ClusterDO clusterTemp = clusterDao.getClusterById(testId);
        Assert.assertNotNull(clusterTemp);
        String destDeployContact = clusterTemp.getDeployContact() + "testModifyCluster";
        String destDeployDesc = clusterTemp.getDeployDesc() + "testModifyCluster";
        String destMainContact = clusterTemp.getMaintContact() + "11111";
        String destName = clusterTemp.getName() + "testModifyCluster";
        String destOnlineTime = clusterTemp.getOnlineTime() + "testModifyCluster";

        clusterTemp.setDeployContact(destDeployContact);
        clusterTemp.setDeployDesc(destDeployDesc);
        clusterTemp.setMaintContact(destMainContact);
        clusterTemp.setName(destName);
        clusterTemp.setOnlineTime(destOnlineTime);

        clusterDao.modifyCluster(clusterTemp);

        read();
        Assert.assertEquals(clusterDao.getClusterById(testId).getDeployContact(), destDeployContact);
        Assert.assertEquals(clusterDao.getClusterById(testId).getDeployDesc(), destDeployDesc);
        Assert.assertEquals(clusterDao.getClusterById(testId).getMaintContact(), destMainContact);
        Assert.assertEquals(clusterDao.getClusterById(testId).getName(), destName);
        Assert.assertEquals(clusterDao.getClusterById(testId).getOnlineTime(), destOnlineTime);

    }

    @Test
    public void testModifyClusterNameToExistName() {
        int insertClusterNum = 3;
        for (int i = 0; i < insertClusterNum; i++) {
            ClusterDO cluster = DOFactory.getCluster();
            cluster.setName("test" + i);
            clusterDao.addCluster(cluster);
        }

        ClusterDO clusterTemp = clusterDao.getClusterById(1);
        clusterTemp.setName("test1");
        Assert.assertFalse(clusterDao.modifyCluster(clusterTemp));

        read();
        clusterTemp = clusterDao.getClusterById(1);
        Assert.assertEquals("test0", clusterTemp.getName());

    }

    @Ignore
    @Test
    public void testXmlFileNotFound() {
        String testXmlPath = "./src/resources/";
        xmlFileLoader.setXmlPath(testXmlPath);
        try {
            clusterDao.afterPropertiesSet();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ClusterDO cluster = DOFactory.getCluster();
        clusterDao.addCluster(cluster);
        Assert.assertNull(clusterDao.getClusterById(1L));
        xmlFileLoader.setXmlPath(testXmlPath);
        try {
            clusterDao.afterPropertiesSet();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertNull(clusterDao.getClusterById(1L));
    }

    @Test
    public void testCheckName() {
        int clusterNum = 10;
        for (int i = 0; i < clusterNum; i++) {
            ClusterDO cluster = DOFactory.getCluster();
            cluster.setName("test" + i);
            clusterDao.addCluster(cluster);
        }
        ClusterDO cluster = DOFactory.getCluster();
        cluster.setName("test" + 0);
        clusterDao.addCluster(cluster);

        Assert.assertFalse(clusterDao.checkName("test1"));
        Assert.assertTrue(clusterDao.checkName("test10"));
        Assert.assertTrue(clusterDao.checkName("test2", 3));
        Assert.assertFalse(clusterDao.checkName("test0", 3));
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testPropertiesNotSet() throws Exception {
        xmlFileLoader.setXmlPath(null);
        clusterDao.afterPropertiesSet();
    }

    public void read() {
        try {
            xmlFileLoader.setXmlPath(xmlPath);
            clusterDao.afterPropertiesSet();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
