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

import com.alibaba.cobar.manager.dataobject.xml.UserDO;

public interface UserDAO {

    public UserDO validateUser(String username, String password);

    public boolean checkName(String username);

    public boolean checkName(String username, long userId);

    public List<UserDO> getUserList();

    public boolean addUser(UserDO user);

    public boolean modifyUser(UserDO user);

    public UserDO getUserById(long id);

}
