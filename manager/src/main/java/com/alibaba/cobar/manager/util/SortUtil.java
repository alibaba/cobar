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

import java.util.Comparator;
import java.util.Map;

import com.alibaba.cobar.manager.dataobject.cobarnode.ConnectionStatus;
import com.alibaba.cobar.manager.dataobject.cobarnode.DataNodesStatus;
import com.alibaba.cobar.manager.dataobject.xml.ClusterDO;
import com.alibaba.cobar.manager.dataobject.xml.CobarDO;

public class SortUtil<T> implements Comparator<T> {

    public int compareDatanodes(DataNodesStatus arg0, DataNodesStatus arg1) {
        return comparePoolName((arg0).getPoolName(), (arg1).getPoolName());
    }

    /**
     * Host first, second NetOut decrease;
     * 
     * @param arg0
     * @param arg1
     * @return
     */
    public int compareConnection(ConnectionStatus arg0, ConnectionStatus arg1) {
        if ((arg0).getHost().equals((arg1).getHost())) {
            return (int) ((arg1).getNetOut() - (arg0).getNetOut());
        }
        return (arg0).getHost().compareTo((arg1).getHost());
    }

    /**
     * sortId increase first, then cluster name
     * 
     * @param c0
     * @param c1
     * @return
     */
    public int compareCluster(ClusterDO c0, ClusterDO c1) {
        if (c0.getSortId() == c1.getSortId()) {
            return c0.getName().compareTo(c1.getName());
        }
        return c0.getSortId() - c1.getSortId();
    }

    public int comparePoolName(String s1, String s2) {
        Pair<String, Integer> p1 = CobarStringUtil.splitIndex(s1, '[', ']');
        Pair<String, Integer> p2 = CobarStringUtil.splitIndex(s2, '[', ']');
        if (p1.getFirst().compareTo(p2.getFirst()) == 0) {
            return p1.getSecond() - p2.getSecond();
        } else {
            return p1.getFirst().compareTo(p2.getFirst());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public int compare(T arg0, T arg1) {
        if (arg0 instanceof DataNodesStatus) {
            return compareDatanodes((DataNodesStatus) arg0, (DataNodesStatus) arg1);
        } else if (arg0 instanceof ConnectionStatus) {
            return compareConnection((ConnectionStatus) arg0, (ConnectionStatus) arg1);
        } else if (arg0 instanceof CobarDO) {
            //cobar sort by name
            return ((CobarDO) arg0).getName().compareTo(((CobarDO) arg1).getName());
        } else if (arg0 instanceof ClusterDO) {
            //cluster sort by name
            return compareCluster((ClusterDO) arg0, (ClusterDO) arg1);
        } else if (arg0 instanceof Map) {
            //compare datanodes  map
            return comparePoolName((String) ((Map<String, Object>) arg0).get("poolName"),
                                   (String) ((Map<String, Object>) arg1).get("poolName"));
        }

        return 0;
    }
}
