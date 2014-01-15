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

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.alibaba.cobar.manager.qa.modle.CobarFactory;
import com.alibaba.cobar.manager.qa.modle.SimpleCobarNode;

public class TestCobarNode {
    private SimpleCobarNode cobarNode;
    private static final Logger logger = Logger.getLogger(TestCobarNode.class);
    private Connection dmlConnection = null;

    @BeforeClass
    public static void init() {

    }

    @Before
    public void initConnection() {
        try {
            cobarNode = CobarFactory.getSimpleCobarNode("cobar");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Assert.fail();
        }
    }

    public void getDMLConnection(String schema) {
        if (null != dmlConnection) {
            try {
                dmlConnection.close();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                Assert.fail();
            }
        }
        try {
            dmlConnection = cobarNode.createDMLConnection(schema);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Assert.fail();
        }
    }

    @Ignore
    @Test
    public void testAddConnections() {
        Connection conn = null;
        try {
            try {
                conn = cobarNode.createManagerConnection();
            } finally {
                Assert.assertTrue(cobarNode.detoryConnection(conn));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Assert.fail();
        }
    }

    @Ignore
    @Test
    public void testExcuteReadSql() {
        Connection conn = null;
        try {
            try {
                conn = cobarNode.createDMLConnection("ddl_test");
                cobarNode.executeSQLRead(conn, "select * from animals");
            } finally {
                Assert.assertTrue(cobarNode.detoryConnection(conn));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Assert.fail();
        }
    }

    @Ignore
    @Test
    public void testExcuteWriteSql() {
        getDMLConnection("ddl_test");
        try {
            try {
                cobarNode.excuteSQWrite(dmlConnection, "insert into animals (name) values('name')");
            } finally {
                Assert.assertTrue(cobarNode.detoryConnection(dmlConnection));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Assert.fail();
        }
    }

}
