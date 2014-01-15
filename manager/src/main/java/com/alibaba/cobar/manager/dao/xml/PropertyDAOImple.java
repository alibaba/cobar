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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.xmlpull.mxp1.MXParser;
import org.xmlpull.mxp1_serializer.MXSerializer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.alibaba.cobar.manager.dao.PropertyDAO;
import com.alibaba.cobar.manager.dataobject.xml.PropertyDO;

/**
 * @author haiqing.zhuhq 2011-6-17
 */
public class PropertyDAOImple extends AbstractDAOImple implements PropertyDAO, InitializingBean {
    private static final Logger logger = Logger.getLogger(PropertyDAOImple.class);
    private static final ReentrantLock lock = new ReentrantLock();
    private PropertyDO property;

    public PropertyDAOImple() {
        this.xpp = new MXParser();
        this.xsl = new MXSerializer();
        this.property = new PropertyDO();
    }

    public PropertyDO getProperty() {
        return property;
    }

    public void setProperty(PropertyDO property) {
        this.property = property;
    }

    private boolean read() {
        FileInputStream is = null;
        lock.lock();
        try {
            property.getStopTimes().clear();
            is = new FileInputStream(xmlPath);
            xpp.setInput(is, "UTF-8");
            while (!(xpp.getEventType() == XmlPullParser.END_TAG && "pro".equals(xpp.getName()))) {
                if (xpp.getEventType() == XmlPullParser.START_TAG && "property".equals(xpp.getName())) {
                    if ("stop_times".equals(xpp.getAttributeValue(0))) {
                        String value = xpp.nextText().trim();
                        String[] times = value.split(",");
                        for (int i = 0; i < times.length; i++) {
                            property.getStopTimes().add(Integer.parseInt(times[i].trim()));
                        }
                        Collections.sort(property.getStopTimes());
                    }
                }
                xpp.next();
            }
            is.close();
            return true;
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(), e);
        } catch (XmlPullParserException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            lock.unlock();
        }
        if (null != is) {
            try {
                is.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return false;
    }

    private boolean write() {
        FileOutputStream os = null;
        lock.lock();
        try {
            if (!backup(xmlPath)) {
                logger.error("property backup fail!");
            }
            os = new FileOutputStream(xmlPath);
            xsl.setOutput(os, "UTF-8");
            xsl.startDocument("UTF-8", null);
            xsl.text("\n");
            xsl.startTag(null, "pro");
            xsl.text("\n");
            String st = property.toString();
            writeProperty("stop_times", st);
            xsl.endTag(null, "pro");
            xsl.endDocument();
            os.close();
            return true;
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            lock.unlock();
        }
        if (null != os) {
            try {
                os.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return false;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        xmlPath = xmlFileLoader.getFilePath();
        if (null == xmlPath) {
            logger.error("property xmlpath doesn't set!");
            throw new IllegalArgumentException("property xmlpath doesn't set!");
        } else {
            if (xmlPath.endsWith(System.getProperty("file.separator"))) {
                xmlPath = new StringBuilder(xmlPath).append("property.xml").toString();
            } else {
                xmlPath =
                        new StringBuilder(xmlPath).append(System.getProperty("file.separator"))
                                                  .append("property.xml")
                                                  .toString();
            }
            read();
        }
    }

    @Override
    public boolean addTime(int time) {
        lock.lock();
        try {
            if (property.getStopTimes().contains(time)) {
                return false;
            }
            property.getStopTimes().add(time);
            Collections.sort(property.getStopTimes());
            if (!write()) {
                logger.error("Fail to add property!");
                recovery(xmlPath);
                read();
                return false;
            }
        } finally {
            lock.unlock();
        }
        return true;
    }

    @Override
    public boolean deleteTime(int time) {
        if (!property.getStopTimes().contains(time)) {
            logger.warn("Time (" + time + ") to delete does not exist!");
            return false;
        }
        property.getStopTimes().remove((Integer) time);
        if (!write()) {
            logger.error("Fail to delete property!");
            recovery(xmlPath);
            read();
            return false;
        }
        return true;
    }

}
