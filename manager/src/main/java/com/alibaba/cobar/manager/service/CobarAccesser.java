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

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import com.alibaba.cobar.manager.dao.CobarAdapterDAO;
import com.alibaba.cobar.manager.dao.delegate.AdapterDelegate;
import com.alibaba.cobar.manager.dataobject.xml.CobarDO;

/**
 * @author haiqing.zhuhq 2011-6-15
 */
public class CobarAccesser implements InitializingBean {
    private static final Logger logger = Logger.getLogger(CobarAccesser.class);
    private AdapterDelegate cobarAdapterDelegate;
    private XmlAccesser xmlAccesser;

    public AdapterDelegate getCobarAdapterDelegate() {
        return cobarAdapterDelegate;
    }

    public void setCobarAdapterDelegate(AdapterDelegate cobarAdapterDelegate) {
        this.cobarAdapterDelegate = cobarAdapterDelegate;
    }

    public XmlAccesser getXmlAccesser() {
        return xmlAccesser;
    }

    public void setXmlAccesser(XmlAccesser xmlAccesser) {
        this.xmlAccesser = xmlAccesser;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (cobarAdapterDelegate == null) {
            throw new IllegalArgumentException("property 'cobarAdapterDelegate' is null!");
        }
        if (xmlAccesser == null) {
            throw new IllegalArgumentException("property 'xmlAccesser' is null!");
        }
    }

    public CobarAdapterDAO getAccesser(long cobarId) {
        final CobarDO cobar = xmlAccesser.getCobarDAO().getCobarById(cobarId);
        if (cobar == null) {
            logger.error(new StringBuilder("Fail to get cobar information which id = ").append(cobarId).toString());
        }
        CobarAdapterDAO accesser =
                cobarAdapterDelegate.getCobarNodeAccesser(cobar.getHost(),
                                                          cobar.getPort(),
                                                          cobar.getUser(),
                                                          cobar.getPassword());
        return accesser;
    }

    public CobarAdapterDAO getAccesser(CobarDO cobar) {
        CobarAdapterDAO accesser =
                cobarAdapterDelegate.getCobarNodeAccesser(cobar.getHost(),
                                                          cobar.getPort(),
                                                          cobar.getUser(),
                                                          cobar.getPassword());
        return accesser;
    }
}
