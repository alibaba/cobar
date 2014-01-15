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

import java.io.File;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

public class SysAdminTest {
    protected static String xmlPath = null;
    private static final String tmpdir = System.getProperty("java.io.tmpdir");
    private static final Logger logger = Logger.getLogger(SysAdminTest.class);

    @BeforeClass
    public static void init() {
        Assert.assertNotNull(tmpdir);
        String xmlFolderPath = tmpdir;
        if (!xmlFolderPath.endsWith(System.getProperty("file.separator"))) {
            xmlFolderPath = new StringBuilder(xmlFolderPath).append(System.getProperty("file.separator")).toString();
        }
        xmlFolderPath = xmlFolderPath + "cobarSysAdminUT";
        File xmlFolder = new File(xmlFolderPath);
        try {
            if (xmlFolder.exists()) {
                if (!(xmlFolder.isDirectory())) {
                    if (!(xmlFolder.delete())) {
                        logger.error("A none directory file exists and is deleted error!");
                        Assert.fail();
                    }
                } else {
                    xmlPath = xmlFolderPath;
                }
            } else if (xmlFolder.mkdir()) {
                //xmlPath is set after folder has created
                xmlPath = xmlFolderPath;
            } else {
                logger.error("mkdir for SystemAdmin test failed");
                Assert.fail();
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Assert.fail();
        }
    }

    @AfterClass
    public static void end() {
        File xmlFolder = null;
        if (null != xmlPath) {
            xmlFolder = new File(xmlPath);
            if (!deleteFile(xmlFolder)) {
                Assert.fail();
            }
        }
    }

    //delete folder
    public static boolean deleteFile(File file) {
        boolean success = true;
        String filePath = file.getPath();
        if (file.isDirectory()) {
            String[] children = file.list();
            if (!(null == children || 0 >= children.length)) {
                for (String child : children) {
                    String childFilePath = filePath + System.getProperty("file.separator") + child;
                    File childFile = new File(childFilePath);
                    if (!(deleteFile(childFile))) {
                        success = false;
                    }
                }
            }
        }

        if (success && file.delete()) {
            success = true;
        } else {
            logger.error(filePath + " delete error");
            success = false;
        }
        return success;
    }

}
