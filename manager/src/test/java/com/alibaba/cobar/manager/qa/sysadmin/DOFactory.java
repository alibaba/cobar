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

package com.alibaba.cobar.manager.qa.sysadmin;

import com.alibaba.cobar.manager.dataobject.xml.ClusterDO;
import com.alibaba.cobar.manager.dataobject.xml.CobarDO;
import com.alibaba.cobar.manager.dataobject.xml.UserDO;
import com.alibaba.cobar.manager.util.ConstantDefine;

/**
 * @author xiaowen.guoxw
 * @version ???????2011-6-27 ????10:46:05
 */

public class DOFactory {

    public static ClusterDO getCluster() {
        ClusterDO cluster = new ClusterDO();
        cluster.setDeployContact("deployContact");
        cluster.setDeployDesc("deployDesc");
        cluster.setMaintContact("13456789123");
        cluster.setName("name");
        cluster.setOnlineTime("OnlineTime");
        return cluster;
    }

    public static CobarDO getCobar() {
        CobarDO cobar = new CobarDO();
        cobar.setClusterId(1);
        cobar.setHost("1.1.1.1");
        cobar.setName("gxw");
        cobar.setPassword("gxw");
        cobar.setStatus(ConstantDefine.ACTIVE);
        cobar.setPort(9090);
        cobar.setTime_diff("time_diff");
        cobar.setUser("gxw");
        return cobar;
    }

    public static UserDO getUser() {
        UserDO user = new UserDO();
        user.setPassword("gxw");
        user.setRealname("gxw");
        user.setStatus(ConstantDefine.ACTIVE);
        user.setUser_role(ConstantDefine.SYSTEM_ADMIN);
        user.setUsername("wenjun");
        return user;
    }

}
