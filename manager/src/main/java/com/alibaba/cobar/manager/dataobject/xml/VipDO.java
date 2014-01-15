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

package com.alibaba.cobar.manager.dataobject.xml;

public class VipDO {
    private long id;
    private String sid;
    private long[] cobarIds;
    private String schema;
    private int[] weights;

    public VipDO() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    /**
     * @return length == length of {@link #getWeights()}
     */
    public long[] getCobarIds() {
        return cobarIds;
    }

    public void setCobarIds(long[] cobarIds) {
        this.cobarIds = cobarIds;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    /**
     * @return length == length of {@link #getCobarIds()}
     */
    public int[] getWeights() {
        return weights;
    }

    public void setWeights(int[] weights) {
        this.weights = weights;
    }

    public String idsString() {
        if (cobarIds.length < 1) {
            return "";
        }
        StringBuilder id = new StringBuilder();
        for (int i = 0; i < cobarIds.length - 1; i++) {
            id.append(String.valueOf(cobarIds[i])).append(",");
        }
        id.append(String.valueOf(cobarIds[cobarIds.length - 1]));
        return id.toString();
    }

    public String weightsString() {
        if (weights.length < 1) {
            return "";
        }
        StringBuilder id = new StringBuilder();
        for (int i = 0; i < weights.length - 1; i++) {
            id.append(String.valueOf(weights[i])).append(",");
        }
        id.append(String.valueOf(weights[weights.length - 1]));
        return id.toString();
    }
}
