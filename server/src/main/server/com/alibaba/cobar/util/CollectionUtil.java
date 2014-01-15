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
/**
 * (created at 2011-10-24)
 */
package com.alibaba.cobar.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class CollectionUtil {
    /**
     * @param orig if null, return intersect
     */
    public static Set<? extends Object> intersectSet(Set<? extends Object> orig, Set<? extends Object> intersect) {
        if (orig == null)
            return intersect;
        if (intersect == null || orig.isEmpty())
            return Collections.emptySet();
        Set<Object> set = new HashSet<Object>(orig.size());
        for (Object p : orig) {
            if (intersect.contains(p))
                set.add(p);
        }
        return set;
    }
}
