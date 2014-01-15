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

package com.alibaba.cobar.manager.web.commons;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.junit.Assert;

import com.alibaba.cobar.manager.dao.xml.ClusterDAOImple;
import com.alibaba.cobar.manager.dao.xml.CobarDAOImple;
import com.alibaba.cobar.manager.dao.xml.PropertyDAOImple;
import com.alibaba.cobar.manager.dao.xml.UserDAOImple;
import com.alibaba.cobar.manager.dao.xml.XMLFileLoaderPath;
import com.alibaba.cobar.manager.dataobject.xml.ClusterDO;
import com.alibaba.cobar.manager.dataobject.xml.CobarDO;
import com.alibaba.cobar.manager.dataobject.xml.UserDO;

public class XmlFile {
    private String xmlPath = null;
    private final Logger logger = Logger.getLogger(XmlFile.class);
    private static final ReentrantLock lock = new ReentrantLock();
    private XMLFileLoaderPath xmlFileLoader = null;

    public XmlFile(String path) {
        xmlPath = path;
        Assert.assertNotNull(xmlPath);
        xmlFileLoader = new XMLFileLoaderPath();
        xmlFileLoader.setXmlPath(xmlPath);
    }

    public void init(String xmlPath, String propertyName) {
        Assert.assertNotNull(xmlPath);
        Assert.assertNotNull(propertyName);
        BufferedWriter bf = null;

        try {
            bf = new BufferedWriter(new FileWriter(new File(xmlPath)));
            bf.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
            bf.write("<" + propertyName + ">\r\n");
            bf.write("</" + propertyName + ">");
            bf.flush();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            Assert.fail();
        } finally {
            if (null != bf) {
                try {
                    bf.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                    Assert.fail();
                }
            }
        }
    }

    private void initCluster() {
        String path = this.xmlPath + System.getProperty("file.separator") + "cluster.xml";
        init(path, "clusters");
        ClusterDAOImple clusterDAO = new ClusterDAOImple();
        clusterDAO.setXmlFileLoader(xmlFileLoader);
        try {
            clusterDAO.afterPropertiesSet();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Assert.fail();
        }
        ClusterDO cluster = new ClusterDO();
        cluster.setDeployContact("utest");
        cluster.setDeployDesc("utest");
        cluster.setMaintContact("utest");
        cluster.setName("cluster");
        cluster.setOnlineTime("2011-01-01");
        clusterDAO.addCluster(cluster);
    }

    private void initCobar() {
        String path = this.xmlPath + System.getProperty("file.separator") + "cobar.xml";
        init(path, "cobars");
        CobarDAOImple cobarDAO = new CobarDAOImple();
        cobarDAO.setXmlFileLoader(xmlFileLoader);
        try {
            cobarDAO.afterPropertiesSet();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Assert.fail();
        }
        CobarDO cobar = new CobarDO();
        cobar.setClusterId(1);
        cobar.setHost("10.20.10.100");
        cobar.setName("cobar");
        cobar.setPassword("");
        cobar.setPort(9066);
        cobar.setStatus("Active");
        cobar.setTime_diff("no");
        cobar.setUser("test");
        cobarDAO.addCobar(cobar);
    }

    private void initUser() {
        String path = this.xmlPath + System.getProperty("file.separator") + "user.xml";
        init(path, "users");
        UserDAOImple userDAO = new UserDAOImple();
        userDAO.setXmlFileLoader(xmlFileLoader);
        try {
            userDAO.afterPropertiesSet();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Assert.fail();
        }
        UserDO user = new UserDO();
        user.setPassword("utest");
        user.setRealname("utest");
        user.setStatus("Normal");
        user.setUser_role("SystemAdmin");
        user.setUsername("utest");
        userDAO.addUser(user);
    }

    private void initProperty() {
        String path = this.xmlPath + System.getProperty("file.separator") + "property.xml";
        init(path, "pro");
        PropertyDAOImple propertyDAO = new PropertyDAOImple();
        propertyDAO.setXmlFileLoader(xmlFileLoader);
        try {
            propertyDAO.afterPropertiesSet();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Assert.fail();
        }
        propertyDAO.addTime(1);
    }

    public void initData() {
        lock.lock();
        try {
            initCluster();
            initCobar();
            initUser();
            initProperty();
        } finally {
            lock.unlock();
        }
    }

    public void delete() {
        File xmlFolder = null;
        lock.lock();
        try {
            if (null != xmlPath) {
                xmlFolder = new File(xmlPath);
                if (!deleteFile(xmlFolder)) {
                    Assert.fail();
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean deleteFile(File file) {
        boolean success = true;
        String filePath = file.getPath();
        if (file.isDirectory()) {
            String[] children = file.list();
            if ((null != children) && (children.length > 0)) {
                for (String child : children) {
                    String childFilePath = filePath + System.getProperty("file.separator") + child;
                    File childFile = new File(childFilePath);
                    if (!(deleteFile(childFile))) {
                        success = false;
                    }
                }
            }
        }

        if (success && file.delete()) {
            success = true;
        } else {
            logger.error(filePath + " delete error");
            success = false;
        }
        return success;
    }
}
