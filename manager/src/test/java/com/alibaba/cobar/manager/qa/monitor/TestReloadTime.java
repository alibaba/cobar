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
import java.util.Arrays;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.alibaba.cobar.manager.qa.util.TestUtils;

@RunWith(Parameterized.class)
public class TestReloadTime extends TestCobarAdapter {
    public Connection managerConnection = null;
    private static final Logger logger = Logger.getLogger(TestReloadTime.class);
    private String reloadSql = null;

    @Override
    @Before
    public void initData() {
        super.initData();
        try {
            if (null != managerConnection) {
                managerConnection.close();
            }
            managerConnection = sCobarNode.createManagerConnection();
        } catch (Exception e) {
            logger.error("destroy adpter error");
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
        } catch (Exception e) {
            logger.error("destroy adpter error");
            Assert.fail();
        }
    }

    @SuppressWarnings("rawtypes")
    @Parameters
    public static Collection perpareData() {
        Object[][] objs = { { "reload @@config" }, { "reload @@user" } };
        return Arrays.asList(objs);
    }

    public TestReloadTime(String reloadSql) {
        this.reloadSql = reloadSql;
    }

    @Test
    public void testReloadTime() {
        try {
            //show @@server
            long timeBeforeReload = cobarAdapter.getServerStatus().getReloadTime();
            long sleepTime = 1000;
            TestUtils.waitForMonment(sleepTime);

            //reload @@config
            Assert.assertTrue(sCobarNode.excuteSQL(managerConnection, reloadSql));
            long timeAfterReload = cobarAdapter.getServerStatus().getReloadTime();
            Assert.assertFalse(timeBeforeReload == timeAfterReload);
            Assert.assertTrue(timeAfterReload - timeBeforeReload >= sleepTime);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            Assert.fail();
        }
    }
}
