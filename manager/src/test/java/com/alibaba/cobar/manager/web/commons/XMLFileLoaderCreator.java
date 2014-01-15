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

package com.alibaba.cobar.manager.web.commons;

import java.io.File;

import org.apache.log4j.Logger;
import org.junit.Assert;

import com.alibaba.cobar.manager.dao.xml.XMLFileLoader;

public class XMLFileLoaderCreator implements XMLFileLoader {

    private final String tmpdir = System.getProperty("java.io.tmpdir");
    private final Logger logger = Logger.getLogger(XMLFileLoaderCreator.class);
    private String xmlPath = null;

    @Override
    public String getFilePath() {
        return null == xmlPath ? createFile() : xmlPath;
    }

    public String createFile() {
        Assert.assertNotNull(tmpdir);
        String xmlFolderPath = null;
        if (!tmpdir.endsWith(System.getProperty("file.separator"))) {
            xmlFolderPath =
                    new StringBuilder(tmpdir).append(System.getProperty("file.separator"))
                                             .append("cobarManagerWebUT")
                                             .toString();
        } else {
            xmlFolderPath = new StringBuilder(tmpdir).append("cobarManagerWebUT").toString();
        }
        File xmlFolder = new File(xmlFolderPath);
        try {
            if (xmlFolder.exists()) {
                if (!(xmlFolder.isDirectory())) {
                    if (!(xmlFolder.delete())) {
                        logger.error("A none directory file name \"cobarManagerWebUT\" exists and delete error!");
                        Assert.fail();
                    }
                } else {
                    xmlPath = xmlFolderPath;
                    new XmlFile(xmlPath).delete();
                }
            }
            if (xmlFolder.mkdir()) {
                //xmlPath is set after folder has created
                xmlPath = xmlFolderPath;
            } else {
                logger.error("mkdir for cobarManagerWebUT test failed");
                Assert.fail();
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Assert.fail();
        }
        new XmlFile(xmlPath).initData();
        return xmlPath;
    }

}
