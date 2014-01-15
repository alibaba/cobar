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

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.cobar.manager.dao.xml.CobarDAOImple;
import com.alibaba.cobar.manager.dao.xml.XMLFileLoaderPath;
import com.alibaba.cobar.manager.dataobject.xml.CobarDO;
import com.alibaba.cobar.manager.util.ConstantDefine;

/**
 * @author xiaowen.guoxw
 * @version ???????2011-6-27 ????03:20:37
 */

public class CobarDaoTest extends SysAdminTest {
    private CobarDAOImple cobarDao = null;
    private String cobarXmlPath = null;
    private static final Logger logger = Logger.getLogger(CobarDaoTest.class);
    private XMLFileLoaderPath xmlFileLoader = null;

    @Before
    public void initData() {
        Assert.assertNotNull(xmlPath);

        xmlFileLoader = new XMLFileLoaderPath();
        xmlFileLoader.setXmlPath(xmlPath);
        cobarDao = new CobarDAOImple();
        cobarDao.setXmlFileLoader(xmlFileLoader);
        cobarXmlPath = xmlPath + System.getProperty("file.separator") + "cobar.xml";
        XmlFile xmlFile = new XmlFile(cobarXmlPath, "cobars");
        try {
            xmlFile.init();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            Assert.fail();
        }
        read();
    }

    @Test
    public void testAddFirstCobar() {
        long testId = 1L;
        CobarDO cobarTemp = cobarDao.getCobarById(testId);
        Assert.assertNull(cobarTemp);

        CobarDO cobar = DOFactory.getCobar();
        cobarDao.addCobar(cobar);

        Assert.assertNotNull(cobarDao.getCobarById(testId));
        Assert.assertSame(cobar, cobarDao.getCobarById(testId));

        read();
        Assert.assertEquals(cobar.getClusterId(), cobarDao.getCobarById(testId).getClusterId());
    }

    @Test
    public void testAddManyCobar() {
        int cobarNum = 10;
        for (int i = 0; i < cobarNum; i++) {
            CobarDO cobar = DOFactory.getCobar();
            cobar.setName("" + i);
            cobarDao.addCobar(cobar);
        }

        //        int activeCobarNum = cobarDao.getCobarCountByStatus(1L, ConstantDefine.ACTIVE);
        int activeCobarNum = cobarDao.getCobarList(1L).size();
        Assert.assertEquals(cobarNum, activeCobarNum);
    }

    @Test
    public void testModifyCobar() {
        CobarDO cobar = DOFactory.getCobar();
        cobarDao.addCobar(cobar);
        CobarDO destCobar = DOFactory.getCobar();
        long destClusterId = cobar.getClusterId() + 1L;
        String destHost = cobar.getHost() + "1.1.1";
        String destName = cobar.getName() + "test";
        int destPort = cobar.getPort() + 2;
        String destUser = cobar.getUser() + "test";
        String destPassword = cobar.getPassword() + "test";
        String destStatus =
                ConstantDefine.ACTIVE.equals(cobar.getStatus()) ? ConstantDefine.IN_ACTIVE : ConstantDefine.ACTIVE;
        String destTimeDiff = cobar.getTime_diff() + "test";
        destCobar.setClusterId(destClusterId);
        destCobar.setHost(destHost);
        destCobar.setName(destName);
        destCobar.setPassword(destPassword);
        destCobar.setPort(destPort);
        destCobar.setStatus(destStatus);
        destCobar.setTime_diff(destTimeDiff);
        destCobar.setUser(destUser);
        destCobar.setId(cobar.getId());

        Assert.assertTrue(cobarDao.modifyCobar(destCobar));

        read();
        CobarDO cobarTemp = cobarDao.getCobarById(1L);
        Assert.assertEquals(cobarTemp.getClusterId(), destClusterId);
        Assert.assertEquals(cobarTemp.getHost(), destHost);
        Assert.assertEquals(cobarTemp.getPort(), destPort);
        Assert.assertEquals(cobarTemp.getPassword(), destPassword);
        Assert.assertEquals(cobarTemp.getStatus(), destStatus);
        Assert.assertEquals(cobarTemp.getTime_diff(), destTimeDiff);
    }

    @Test
    public void testModifyCobarToExistName() {
        int insertCobarNum = 3;
        for (int i = 0; i < insertCobarNum; i++) {
            CobarDO cobar = DOFactory.getCobar();
            cobar.setName("test" + i);
            cobarDao.addCobar(cobar);
        }

        CobarDO cobarTemp = cobarDao.getCobarById(1);
        cobarTemp.setName("test1");
        Assert.assertFalse(cobarDao.modifyCobar(cobarTemp));

        read();
        cobarTemp = cobarDao.getCobarById(1);
        Assert.assertEquals("test0", cobarTemp.getName());
    }

    // same as add cluster
    @Test
    public void testModifyNotExistCobarId() {
        CobarDO cobar = DOFactory.getCobar();
        cobar.setId(3);

        cobarDao.modifyCobar(cobar);

        read();
        Assert.assertNotNull(cobarDao.getCobarById(3));
        Assert.assertNull(cobarDao.getCobarById(1));
    }

    @Test
    public void testGetCobarList() {
        long testClusterId = 1L;
        int testCobarNum = 3;
        int otherCobarNum = 4;
        for (int i = 0; i < testCobarNum; i++) {
            CobarDO cobar = DOFactory.getCobar();
            cobar.setName("test" + i);
            cobar.setClusterId(testClusterId);
            cobarDao.addCobar(cobar);
        }
        for (int i = testCobarNum; i < testCobarNum + otherCobarNum; i++) {
            CobarDO cobar = DOFactory.getCobar();
            cobar.setName("test" + i);
            cobar.setClusterId(testClusterId + 1L);
            cobarDao.addCobar(cobar);
        }

        Assert.assertEquals(testCobarNum, cobarDao.getCobarList(testClusterId).size());
        Assert.assertEquals(0, cobarDao.getCobarList(testClusterId + 3).size());
    }

    @Test
    public void testGetCobarListbyStatus() {
        long testClusterId = 1L;
        int activeCobarNum = 3;
        int inActiveCobarNum = 4;
        for (int i = 0; i < activeCobarNum; i++) {
            CobarDO cobar = DOFactory.getCobar();
            cobar.setName("test" + i);
            cobar.setStatus(ConstantDefine.ACTIVE);
            cobarDao.addCobar(cobar);
        }
        for (int i = activeCobarNum; i < activeCobarNum + inActiveCobarNum; i++) {
            CobarDO cobar = DOFactory.getCobar();
            cobar.setName("test" + i);
            cobar.setStatus(ConstantDefine.IN_ACTIVE);
            cobarDao.addCobar(cobar);
        }

        //        Assert.assertEquals(activeCobarNum, cobarDao.getCobarCountByStatus(testClusterId, ConstantDefine.ACTIVE));
        //        Assert.assertEquals(inActiveCobarNum, cobarDao.getCobarCountByStatus(testClusterId, ConstantDefine.IN_ACTIVE));
        //        Assert.assertEquals(0, cobarDao.getCobarCountByStatus(testClusterId + 1, ConstantDefine.ACTIVE));
        //        Assert.assertEquals(0, cobarDao.getCobarCountByStatus(testClusterId + 1, ConstantDefine.IN_ACTIVE));

        Assert.assertEquals(activeCobarNum, cobarDao.getCobarList(testClusterId, ConstantDefine.ACTIVE).size());
        Assert.assertEquals(inActiveCobarNum, cobarDao.getCobarList(testClusterId, ConstantDefine.IN_ACTIVE).size());
        Assert.assertEquals(0, cobarDao.getCobarList(testClusterId + 1, ConstantDefine.ACTIVE).size());
        Assert.assertEquals(0, cobarDao.getCobarList(testClusterId + 1, ConstantDefine.IN_ACTIVE).size());
    }

    public void read() {
        try {
            xmlFileLoader.setXmlPath(xmlPath);
            cobarDao.afterPropertiesSet();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Assert.fail();
        }
    }
}
