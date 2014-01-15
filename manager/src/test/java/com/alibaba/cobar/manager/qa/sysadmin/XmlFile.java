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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.Assert;


/**
 * @author xiaowen.guoxw
 * @version ???????2011-6-29 ????09:18:23
 */

public class XmlFile {
    private String xmlPath;
    private String propertyName;

    public XmlFile(String xmlPath, String propertyName) {
        Assert.assertNotNull(xmlPath);
        Assert.assertNotNull(propertyName);

        this.xmlPath = xmlPath;
        this.propertyName = propertyName;
    }

    public void init() throws IOException {
        BufferedWriter bf = null;
      
        try {
            bf = new BufferedWriter(new FileWriter(new File(this.xmlPath)));
            bf.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
            bf.write("<" + this.propertyName + ">\r\n");
            bf.write("</" + this.propertyName + ">");
            bf.flush();
        } finally {
            if (null != bf) {
                bf.close();
            }
        }
    }
}
