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

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.cobar.manager.dao.xml.PropertyDAOImple;
import com.alibaba.cobar.manager.dao.xml.XMLFileLoaderPath;
import com.alibaba.cobar.manager.dataobject.xml.PropertyDO;

public class PropertyDaoTest extends SysAdminTest {
    private PropertyDAOImple propertyDao = null;
    private String propertyXmlPath = null;
    private static final Logger logger = Logger.getLogger(PropertyDaoTest.class);
    private XMLFileLoaderPath xmlFileLoader = null;

    @Before
    public void initData() {
        Assert.assertNotNull(xmlPath);
        propertyXmlPath = xmlPath + System.getProperty("file.separator") + "property.xml";
        xmlFileLoader = new XMLFileLoaderPath();
        xmlFileLoader.setXmlPath(xmlPath);
        propertyDao = new PropertyDAOImple();
        propertyDao.setXmlFileLoader(xmlFileLoader);
        XmlFile xmlFile = new XmlFile(propertyXmlPath, "pro");
        try {
            xmlFile.init();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Assert.fail();
        }

        read();
    }

    @Test
    public void testAddFirstTime() {
        int time = 10;
        Assert.assertTrue(propertyDao.addTime(time));
        PropertyDO propertyDO = propertyDao.getProperty();
        Assert.assertEquals(1, propertyDO.getStopTimes().size());
        Assert.assertEquals((Integer) time, propertyDO.getStopTimes().get(0));
    }

    @Test
    public void testAddManyTime() {
        int timeNum = 10;
        for (int i = 0; i < timeNum; i++) {
            Assert.assertTrue(propertyDao.addTime(i));
        }

        PropertyDO propertyDO = propertyDao.getProperty();
        Assert.assertEquals(timeNum, propertyDO.getStopTimes().size());
    }

    @Test
    public void testAddDuplicatedTime() {
        int time = 1;
        Assert.assertTrue(propertyDao.addTime(time));
        for (int i = 0; i < 10; i++) {
            Assert.assertFalse(propertyDao.addTime(time));
        }
        PropertyDO propertyDO = propertyDao.getProperty();
        Assert.assertEquals(1, propertyDO.getStopTimes().size());
        Assert.assertEquals((Integer) time, propertyDO.getStopTimes().get(0));
    }

    @Test
    public void testDeleteTime() {
        int timeNum = 10;
        for (int i = 0; i < timeNum; i++) {
            Assert.assertTrue(propertyDao.addTime(i));
        }

        PropertyDO propertyDO = propertyDao.getProperty();
        Assert.assertEquals(timeNum, propertyDO.getStopTimes().size());

        for (int i = 0; i < timeNum - 1; i++) {
            Assert.assertTrue(propertyDao.deleteTime(i));
        }
        propertyDO = propertyDao.getProperty();
        Assert.assertEquals(1, propertyDO.getStopTimes().size());

        Assert.assertTrue(propertyDao.deleteTime(timeNum - 1));
        propertyDO = propertyDao.getProperty();
        Assert.assertEquals(0, propertyDO.getStopTimes().size());
    }

    @Test
    public void testDeleteNonExistsTime() {
        int time = 10;
        PropertyDO propertyDO = propertyDao.getProperty();
        Assert.assertEquals(0, propertyDO.getStopTimes().size());

        Assert.assertFalse(propertyDao.deleteTime(time));
    }

    public void read() {
        Assert.assertNotNull(xmlPath);
        try {
            xmlFileLoader.setXmlPath(xmlPath);
            propertyDao.afterPropertiesSet();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Assert.fail();
        }
    }
}
