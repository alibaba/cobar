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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.alibaba.cobar.manager.dataobject.cobarnode.ConnectionStatus;
import com.alibaba.cobar.manager.dataobject.cobarnode.DataNodesStatus;
import com.alibaba.cobar.manager.dataobject.xml.ClusterDO;
import com.alibaba.cobar.manager.dataobject.xml.CobarDO;

public class ListSortUtil {
    public static void sortClusterByName(List<ClusterDO> list) {
        Collections.sort(list, new Comparator<ClusterDO>() {
            @Override
            public int compare(ClusterDO c1, ClusterDO c2) {
                return (c1).getName().compareTo((c2).getName());
            }
        });
    }

    public static void sortClusterBySortId(List<ClusterDO> list) {
        Collections.sort(list, new Comparator<ClusterDO>() {
            @Override
            public int compare(ClusterDO c1, ClusterDO c2) {
                if (c1.getSortId() == c2.getSortId()) {
                    return (c1).getName().compareTo((c2).getName());
                }
                return c1.getSortId() - c2.getSortId();
            }
        });
    }

    public static void sortCobarByName(List<CobarDO> list) {
        Collections.sort(list, new Comparator<CobarDO>() {

            @Override
            public int compare(CobarDO o1, CobarDO o2) {
                return o1.getName().compareTo(o2.getName());
            }

        });
    }

    public static void sortDataNodesByPoolName(List<DataNodesStatus> list) {
        Collections.sort(list, new Comparator<DataNodesStatus>() {

            @Override
            public int compare(DataNodesStatus o1, DataNodesStatus o2) {
                return comparePoolName(o1.getPoolName(), o2.getPoolName());
            }

        });
    }

    public static void sortDataNodesMapByPoolName(List<Map<String, Object>> list) {
        Collections.sort(list, new Comparator<Map<String, Object>>() {

            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                return comparePoolName((String) o1.get("poolName"), (String) o2.get("poolName"));
            }

        });
    }

    public static int comparePoolName(String s1, String s2) {
        Pair<String, Integer> p1 = CobarStringUtil.splitIndex(s1, '[', ']');
        Pair<String, Integer> p2 = CobarStringUtil.splitIndex(s2, '[', ']');
        if (p1.getFirst().compareTo(p2.getFirst()) == 0) {
            return p1.getSecond() - p2.getSecond();
        } else {
            return p1.getFirst().compareTo(p2.getFirst());
        }
    }

    public static void sortConnections(List<ConnectionStatus> list) {
        Collections.sort(list, new Comparator<ConnectionStatus>() {

            @Override
            public int compare(ConnectionStatus o1, ConnectionStatus o2) {
                if (o1.getHost().equals(o2.getHost())) {
                    return (int) (o2.getNetOut() - o1.getNetOut());
                }
                return o1.getHost().compareTo(o2.getHost());
            }

        });
    }
}
