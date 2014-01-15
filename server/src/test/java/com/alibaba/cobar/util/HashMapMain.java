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

import java.util.HashMap;
import java.util.Map;

/**
 * @author xianmao.hexm
 */
public class HashMapMain {

    public void t() {
        String[] keys = new String[] { "a", "b", "c", "d", "e" };
        long t = System.currentTimeMillis();
        int count = 1000000;
        Map<String, String> m = new HashMap<String, String>();
        t = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            for (String key : keys) {
                m.put(key, "String.value");
            }
            for (String key : keys) {
                m.remove(key);
            }
        }
        System.out.println((System.currentTimeMillis() - t) * 1000 * 1000 / (count * keys.length * 2) + " ns");
    }

}
