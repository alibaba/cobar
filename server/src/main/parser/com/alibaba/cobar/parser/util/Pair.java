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
package com.alibaba.cobar.parser.util;

/**
 * (created at 2010-7-21)
 * 
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public final class Pair<K, V> {

    private final K key;
    private final V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(key).append(", ").append(value).append(")");
        return sb.toString();
    }

    private static final int HASH_CONST = 37;

    @Override
    public int hashCode() {
        int hash = 17;
        if (key == null) {
            hash += HASH_CONST;
        } else {
            hash = hash << 5 + hash << 1 + hash + key.hashCode();
        }
        if (value == null) {
            hash += HASH_CONST;
        } else {
            hash = hash << 5 + hash << 1 + hash + value.hashCode();
        }
        return hash;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Pair))
            return false;
        Pair that = (Pair) obj;
        return isEquals(this.key, that.key) && isEquals(this.value, that.value);
    }

    private boolean isEquals(Object o1, Object o2) {
        if (o1 == o2)
            return true;
        if (o1 == null)
            return o2 == null;
        return o1.equals(o2);
    }

}
