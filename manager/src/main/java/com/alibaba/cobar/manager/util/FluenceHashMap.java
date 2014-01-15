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

package com.alibaba.cobar.manager.util;

import java.util.HashMap;

/**
 * (created at 2010-7-22)
 * 
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class FluenceHashMap<K, V> extends HashMap<K, V> {

    private static final long serialVersionUID = 1L;

    public FluenceHashMap() {
        super();
    }

    public FluenceHashMap(int initCap) {
        super(initCap);
    }

    public FluenceHashMap<K, V> putKeyValue(K key, V value) {
        put(key, value);
        return this;
    }

    public FluenceHashMap<K, V> putKeyValue(Pair<K, V> pair) {
        if (pair != null) put(pair.getFirst(), pair.getSecond());
        return this;
    }
}
