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

package com.alibaba.cobar.manager.dao.xml;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

public class XMLFileLoaderPath implements XMLFileLoader, InitializingBean {
    private static final Logger logger = Logger.getLogger(XMLFileLoaderPath.class);
    private String xmlPath;

    public void setXmlPath(String xmlPath) {
        this.xmlPath = xmlPath;
    }

    @Override
    public String getFilePath() {
        return this.xmlPath;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (null == xmlPath) {
            logger.error("xmlpath doesn't set!");
            throw new IllegalArgumentException("xmlPath doesn't set!");
        } else if (!xmlPath.endsWith(System.getProperty("file.separator"))) {
            xmlPath = new StringBuilder(xmlPath).append(System.getProperty("file.separator")).toString();
        }
    }

}
