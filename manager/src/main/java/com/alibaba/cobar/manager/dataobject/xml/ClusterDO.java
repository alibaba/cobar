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

/**
 * @author haiqing.zhuhq 2011-6-14
 */
public class ClusterDO {
    private long id;
    private String name;
    private String deployContact;
    private String maintContact;
    private String deployDesc;
    private String onlineTime;
    private int sortId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeployContact() {
        return deployContact;
    }

    public void setDeployContact(String deployContact) {
        this.deployContact = deployContact;
    }

    public String getMaintContact() {
        return maintContact;
    }

    public void setMaintContact(String maintContact) {
        this.maintContact = maintContact;
    }

    public String getDeployDesc() {
        return deployDesc;
    }

    public void setDeployDesc(String deployDesc) {
        this.deployDesc = deployDesc;
    }

    public String getOnlineTime() {
        return onlineTime;
    }

    public void setOnlineTime(String onlineTime) {
        this.onlineTime = onlineTime;
    }

    public int getSortId() {
        return sortId;
    }

    public void setSortId(int sortId) {
        this.sortId = sortId;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("id:")
                                  .append(id)
                                  .append("|name:")
                                  .append(name)
                                  .append("|status:")
                                  .append("|deployContact:")
                                  .append(deployContact)
                                  .append("|maintContact:")
                                  .append(maintContact)
                                  .append("|onlineTime:")
                                  .append(onlineTime)
                                  .append("|deployDesc:")
                                  .append(deployDesc)
                                  .append("|sortId:")
                                  .append(sortId)
                                  .toString();
    }

}
