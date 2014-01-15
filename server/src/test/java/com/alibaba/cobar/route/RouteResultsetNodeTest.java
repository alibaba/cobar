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
package com.alibaba.cobar.route;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author xianmao.hexm
 */
public class RouteResultsetNodeTest {

    @Test
    public void testMapKeyValue() {
        Map<RouteResultsetNode, String> map = new HashMap<RouteResultsetNode, String>();
        RouteResultsetNode rrn = new RouteResultsetNode("test", "select * from t1 limit 1");
        RouteResultsetNode rrn2 = new RouteResultsetNode("test", 1, "select * from t2 limit 1");
        map.put(rrn, rrn.getStatement());
        map.put(rrn2, rrn2.getStatement());
        Assert.assertEquals(2, map.size());
        for (int i = 0; i < 100; i++) {
            Assert.assertEquals("select * from t1 limit 1", map.get(rrn));
            Assert.assertEquals("select * from t2 limit 1", map.get(rrn2));
        }
    }

}
