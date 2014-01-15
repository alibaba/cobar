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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * (created at 2010-8-4)
 * 
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class Tuple {
    private final Object[] objects;

    public Tuple(Object... objects) {
        if (objects == null) throw new IllegalArgumentException("no argument!");
        this.objects = objects;
    }

    /**
     * @param index start from 0
     */
    public Object getElement(int index) {
        return objects[index];
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        for (Object obj : objects) {
            builder.append(obj);
        }
        return builder.toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Tuple)) return false;
        Tuple that = (Tuple) obj;
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(this.objects, that.objects);
        return builder.isEquals();
    }
}
