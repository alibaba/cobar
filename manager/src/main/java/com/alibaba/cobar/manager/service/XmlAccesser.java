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

package com.alibaba.cobar.manager.service;

import org.springframework.beans.factory.InitializingBean;

import com.alibaba.cobar.manager.dao.xml.ClusterDAOImple;
import com.alibaba.cobar.manager.dao.xml.CobarDAOImple;
import com.alibaba.cobar.manager.dao.xml.PropertyDAOImple;
import com.alibaba.cobar.manager.dao.xml.UserDAOImple;

/**
 * @author haiqing.zhuhq 2011-6-15
 */
public class XmlAccesser implements InitializingBean {
    private ClusterDAOImple clusterDAO;
    private CobarDAOImple cobarDAO;
    private UserDAOImple userDAO;
    private PropertyDAOImple propertyDAO;

    public ClusterDAOImple getClusterDAO() {
        return clusterDAO;
    }

    public void setClusterDAO(ClusterDAOImple clusterDAO) {
        this.clusterDAO = clusterDAO;
    }

    public CobarDAOImple getCobarDAO() {
        return cobarDAO;
    }

    public void setCobarDAO(CobarDAOImple cobarDAO) {
        this.cobarDAO = cobarDAO;
    }

    public UserDAOImple getUserDAO() {
        return userDAO;
    }

    public void setUserDAO(UserDAOImple userDAO) {
        this.userDAO = userDAO;
    }

    public PropertyDAOImple getPropertyDAO() {
        return propertyDAO;
    }

    public void setPropertyDAO(PropertyDAOImple propertyDAO) {
        this.propertyDAO = propertyDAO;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (clusterDAO == null) {
            throw new IllegalArgumentException("property 'clusterDAO' is not set!");
        }
        if (cobarDAO == null) {
            throw new IllegalArgumentException("property 'cobarDAO' is not set!");
        }
        if (userDAO == null) {
            throw new IllegalArgumentException("property 'userDAO' is not set!");
        }
        if (propertyDAO == null) {
            throw new IllegalArgumentException("property 'propertyDAO' is not set!");
        }
    }

}
