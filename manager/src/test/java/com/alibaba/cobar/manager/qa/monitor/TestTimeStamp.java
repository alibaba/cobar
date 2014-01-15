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

import org.junit.Assert;
import org.junit.Test;

import com.alibaba.cobar.manager.qa.util.TestUtils;

public class TestTimeStamp extends TestCobarAdapter {

    @Test
    public void testGetVersion() {
        Assert.assertEquals("5.1.48-cobar-1.2.0", cobarAdapter.getVersion());
    }

    @Test
    public void testGetTimeCurrent() {
        com.alibaba.cobar.manager.dataobject.cobarnode.TimeStamp timeStamp = null;
        timeStamp = cobarAdapter.getCurrentTime();
        Assert.assertNotNull(timeStamp);
        long sleepTime = 1000L;
        long startTime = cobarAdapter.getCurrentTime().getTimestamp();
        TestUtils.waitForMonment(sleepTime);
        long endTime = cobarAdapter.getCurrentTime().getTimestamp();
        Assert.assertTrue(startTime + sleepTime <= endTime);
    }

    @Test
    public void testGetTimeStartUp() {
        String startTime = cobarAdapter.getServerStatus().getUptime();
        Assert.assertNotNull(startTime);
        long sleepTime = 1000L;
        TestUtils.waitForMonment(sleepTime);
        String endTime = cobarAdapter.getServerStatus().getUptime();
        Assert.assertNotNull(endTime);
    }

}
