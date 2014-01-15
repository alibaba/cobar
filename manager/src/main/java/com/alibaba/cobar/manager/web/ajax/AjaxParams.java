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

package com.alibaba.cobar.manager.web.ajax;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * (created at 2010-9-7)
 * 
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 * @author wenfeng.cenwf 2011-3-25
 * @author haiqing.zhuhq 2011-9-1
 */
public class AjaxParams extends AbstractAjaxParams {

    private long clusterId;
    private long cobarNodeId;
    private String valueType;
    private JSONArray array;
    private String keyword;
    private long userId;
    private long connectionId;

    public AjaxParams(HttpServletRequest request) {
        super(request);
        this.clusterId = getLong(request, "clusterId", false);
        this.cobarNodeId = getLong(request, "cobarId", false);
        this.valueType = getString(request, "valueType", true);
        this.array = JSONArray.fromObject(getString(request, "last", false));
        this.keyword = getString(request, "keyword", false);
        this.userId = getLong(request, "userId", false);
        this.connectionId = getLong(request, "connectionId", false);
    }

    @Override
    protected void appendToStringBuilder(ToStringBuilder builder) {
        builder.append("clusterId", clusterId)
               .append("cobarNodeId", cobarNodeId)
               .append("valueType", valueType)
               .append("array", array)
               .append("keyword", keyword)
               .append("userId", userId)
               .append("connectionId", connectionId);
    }

    public long getCobarNodeId() {
        return cobarNodeId;
    }

    public String getValueType() {
        return valueType;
    }

    public long getClusterId() {
        return clusterId;
    }

    public JSONArray getArray() {
        return array;
    }

    public String getKeyword() {
        return keyword;
    }

    public long getUserId() {
        return userId;
    }

    public long getConnectionId() {
        return connectionId;
    }

}
