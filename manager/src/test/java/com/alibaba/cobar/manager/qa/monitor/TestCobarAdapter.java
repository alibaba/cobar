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

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

import com.alibaba.cobar.manager.dao.delegate.CobarAdapter;
import com.alibaba.cobar.manager.qa.modle.CobarFactory;
import com.alibaba.cobar.manager.qa.modle.SimpleCobarNode;

public class TestCobarAdapter {
    public static CobarAdapter cobarAdapter = null;
    public static SimpleCobarNode sCobarNode = null;
    private static final Logger logger = Logger.getLogger(TestCobarAdapter.class);

    @BeforeClass
    public static void init() {
        try {
            cobarAdapter = CobarFactory.getCobarAdapter("cobar");
            sCobarNode = CobarFactory.getSimpleCobarNode("cobar");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Assert.fail();
        }
    }

    @Before
    public void initData() {
        if (null != cobarAdapter && null != cobarAdapter.getDataSource()) {
            try {
                cobarAdapter.destroy();
            } catch (Exception e) {
                logger.error("destroy adpter error");
                Assert.fail();
            }
        }
    }

    @After
    public void end() {
        try {
            if (null != cobarAdapter && null != cobarAdapter.getDataSource()) {
                cobarAdapter.destroy();
            }

        } catch (Exception e) {
            logger.error("destroy adpter error");
            Assert.fail();
        }

    }

}
