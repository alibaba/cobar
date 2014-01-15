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
package com.alibaba.cobar.route;

import com.alibaba.cobar.util.FormatUtil;

/**
 * @author xianmao.hexm
 */
public final class RouteResultset {
    public static final int SUM_FLAG = 1;
    public static final int MIN_FLAG = 2;
    public static final int MAX_FLAG = 3;
    public static final int REWRITE_FIELD = 4;

    private final String statement; // 原始语句
    private RouteResultsetNode[] nodes; // 路由结果节点
    private int flag; // 结果集的处理标识，比如：合并，相加等。
    private long limitSize;

    public RouteResultset(String stmt) {
        this.statement = stmt;
        this.limitSize = -1;
    }

    public String getStatement() {
        return statement;
    }

    public RouteResultsetNode[] getNodes() {
        return nodes;
    }

    public void setNodes(RouteResultsetNode[] nodes) {
        this.nodes = nodes;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    /**
     * @return -1 if no limit
     */
    public long getLimitSize() {
        return limitSize;
    }

    public void setLimitSize(long limitSize) {
        this.limitSize = limitSize;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(statement).append(", route={");
        if (nodes != null) {
            for (int i = 0; i < nodes.length; ++i) {
                s.append("\n ").append(FormatUtil.format(i + 1, 3));
                s.append(" -> ").append(nodes[i]);
            }
        }
        s.append("\n}");
        return s.toString();
    }

}
