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
 * (created at 2011-1-4)
 */
package com.alibaba.cobar.parser.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public final class ListUtil {

    @SuppressWarnings("rawtypes")
    public static List<?> createList(Object... objs) {
        return createList(new ArrayList(), objs);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static List<?> createList(List list, Object... objs) {
        if (objs != null) {
            for (Object obj : objs) {
                list.add(obj);
            }
        }
        return list;
    }

    public static boolean isEquals(List<? extends Object> l1, List<? extends Object> l2) {
        if (l1 == l2)
            return true;
        if (l1 == null)
            return l2 == null;
        if (l2 == null)
            return false;
        if (l1.size() != l2.size())
            return false;
        Iterator<? extends Object> iter1 = l1.iterator();
        Iterator<? extends Object> iter2 = l2.iterator();
        while (iter1.hasNext()) {
            Object o1 = iter1.next();
            Object o2 = iter2.next();
            if (o1 == o2)
                continue;
            if (o1 == null && o2 != null)
                return false;
            if (!o1.equals(o2))
                return false;
        }
        return true;
    }

}
