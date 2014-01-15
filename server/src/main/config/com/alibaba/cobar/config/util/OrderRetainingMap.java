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
package com.alibaba.cobar.config.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class OrderRetainingMap<K, V> extends HashMap<K, V> {
    private static final long serialVersionUID = 1L;

    private Set<K> keyOrder = new ArraySet<K>();
    private List<V> valueOrder = new ArrayList<V>();

    @Override
    public V put(K key, V value) {
        keyOrder.add(key);
        valueOrder.add(value);
        return super.put(key, value);
    }

    @Override
    public Collection<V> values() {
        return Collections.unmodifiableList(valueOrder);
    }

    @Override
    public Set<K> keySet() {
        return Collections.unmodifiableSet(keyOrder);
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException();
    }

    private static class ArraySet<T> extends ArrayList<T> implements Set<T> {

        private static final long serialVersionUID = 1L;
    }

}
