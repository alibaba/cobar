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
package com.alibaba.cobar.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;

/**
 * @author xianmao.hexm
 */
public class MapPerfMain {

    public void t1() {
        Map<String, Date> m = new HashMap<String, Date>();
        for (int i = 0; i < 100000; i++) {
            m.put(UUID.randomUUID().toString(), new Date());
        }
        remove1(m);
        Assert.assertEquals(0, m.size());
    }

    public void t2() {
        Map<String, Date> m = new HashMap<String, Date>();
        for (int i = 0; i < 100000; i++) {
            m.put(UUID.randomUUID().toString(), new Date());
        }
        remove2(m);
        Assert.assertEquals(0, m.size());
    }

    void remove1(Map<String, Date> m) {
        Iterator<Map.Entry<String, Date>> it = m.entrySet().iterator();
        while (it.hasNext()) {
            it.next().getValue();
            it.remove();
        }
    }

    void remove2(Map<String, Date> m) {
        Iterator<Map.Entry<String, Date>> it = m.entrySet().iterator();
        while (it.hasNext()) {
            it.next().getValue();
        }
        m.clear();
    }

}
