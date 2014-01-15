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

package com.alibaba.cobar.manager.dao;

import java.util.List;

import com.alibaba.cobar.manager.dataobject.xml.CobarDO;

/**
 * @author haiqing.zhuhq 2011-6-14
 */
public interface CobarDAO {

    public CobarDO getCobarById(long id);

    //    public int getCobarCountByStatus(long clusterId, String status);

    public boolean checkName(String name, long clusterId);

    public boolean addCobar(CobarDO cobar);

    public List<CobarDO> getCobarList(long clusterId);

    public List<CobarDO> listCobarById(long[] cobarIds);

    public List<CobarDO> listAllCobar();

    public List<CobarDO> getCobarList(long clusterId, String status);

    public boolean modifyCobar(CobarDO cobar);

    public boolean checkName(String name, long clusterId, long cobarId);
}
