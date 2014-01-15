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

import static com.alibaba.cobar.manager.util.ConstantDefine.DEPLOY_CONTACT;
import static com.alibaba.cobar.manager.util.ConstantDefine.DEPLOY_DESC;
import static com.alibaba.cobar.manager.util.ConstantDefine.ID;
import static com.alibaba.cobar.manager.util.ConstantDefine.MAINT_CONTACT;
import static com.alibaba.cobar.manager.util.ConstantDefine.NAME;
import static com.alibaba.cobar.manager.util.ConstantDefine.ONLINE_TIME;
import static com.alibaba.cobar.manager.util.ConstantDefine.SORT_ID;
import static com.alibaba.cobar.manager.util.ConstantDefine.STATUS;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.xmlpull.mxp1.MXParser;
import org.xmlpull.mxp1_serializer.MXSerializer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.alibaba.cobar.manager.dao.ClusterDAO;
import com.alibaba.cobar.manager.dataobject.xml.ClusterDO;

/**
 * @author haiqing.zhuhq 2011-6-14
 */
public class ClusterDAOImple extends AbstractDAOImple implements ClusterDAO, InitializingBean {
    private static final Logger logger = Logger.getLogger(ClusterDAOImple.class);
    private Map<Long, ClusterDO> map;
    private static long maxId;
    private static final ReentrantLock lock = new ReentrantLock();
    private static final Map<String, Integer> typeMap = new HashMap<String, Integer>();

    static {
        typeMap.put("id", ID);
        typeMap.put("name", NAME);
        typeMap.put("status", STATUS);
        typeMap.put("deployContact", DEPLOY_CONTACT);
        typeMap.put("maintContact", MAINT_CONTACT);
        typeMap.put("onlineTime", ONLINE_TIME);
        typeMap.put("deployDesc", DEPLOY_DESC);
        typeMap.put("sortId", SORT_ID);
    }

    public ClusterDAOImple() {
        map = new HashMap<Long, ClusterDO>();
        xpp = new MXParser();
        xsl = new MXSerializer();
        maxId = Long.MIN_VALUE;
    }

    private boolean read() {
        FileInputStream is = null;
        lock.lock();
        try {
            map.clear();
            is = new FileInputStream(xmlPath);
            xpp.setInput(is, "UTF-8");
            while (!(xpp.getEventType() == XmlPullParser.END_TAG && "clusters".equals(xpp.getName()))) {
                if (xpp.getEventType() == XmlPullParser.START_TAG && "cluster".equals(xpp.getName())) {
                    ClusterDO cluster = read(xpp);
                    if (null == cluster) {
                        throw new XmlPullParserException("Cluster read error");
                    }
                    maxId = (maxId < cluster.getId()) ? cluster.getId() : maxId;
                    map.put(cluster.getId(), cluster);
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
            maxId = maxId < 0 ? 0 : maxId;
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

    @SuppressWarnings("rawtypes")
    private boolean write() {
        FileOutputStream os = null;
        lock.lock();
        try {
            if (!backup(xmlPath)) {
                logger.error("cluster backup fail!");
            }
            os = new FileOutputStream(xmlPath);
            xsl.setOutput(os, "UTF-8");
            xsl.startDocument("UTF-8", null);
            xsl.text("\n");
            xsl.startTag(null, "clusters");
            xsl.text("\n");
            Iterator it = map.entrySet().iterator();
            while (it.hasNext()) {
                @SuppressWarnings("unchecked")
                Map.Entry<Long, ClusterDO> entry = (Entry<Long, ClusterDO>) it.next();
                ClusterDO cluster = entry.getValue();
                if (!write(cluster)) {
                    throw new IOException("cluster write error!");
                }
            }
            xsl.endTag(null, "clusters");
            xsl.endDocument();
            os.close();
            return true;
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

    private boolean write(ClusterDO cluster) {
        try {
            writePrefix(false);
            xsl.startTag(null, "cluster");
            xsl.text("\n");
            writeProperty("id", String.valueOf(cluster.getId()));
            writeProperty("sortId", String.valueOf(cluster.getSortId()));
            writeProperty("name", cluster.getName());
            writeProperty("deployContact", cluster.getDeployContact());
            writeProperty("maintContact", cluster.getMaintContact());
            writeProperty("onlineTime", cluster.getOnlineTime());
            writeProperty("deployDesc", cluster.getDeployDesc());
            writePrefix(true);
            xsl.endTag(null, "cluster");
            xsl.text("\n");
            return true;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    private ClusterDO read(XmlPullParser xpp) {
        ClusterDO cluster = new ClusterDO();
        cluster.setSortId(0);
        try {
            while (!(xpp.getEventType() == XmlPullParser.END_TAG && "cluster".equals(xpp.getName()))) {
                if (xpp.getEventType() == XmlPullParser.START_TAG && "property".equals(xpp.getName())) {
                    int type = typeMap.get(xpp.getAttributeValue(0).trim());
                    switch (type) {
                    case ID:
                        cluster.setId(Long.parseLong(xpp.nextText().trim()));
                        break;
                    case SORT_ID:
                        cluster.setSortId(Integer.parseInt(xpp.nextText().trim()));
                        break;
                    case NAME:
                        cluster.setName(xpp.nextText().trim());
                        break;
                    case DEPLOY_CONTACT:
                        cluster.setDeployContact(xpp.nextText().trim());
                        break;
                    case MAINT_CONTACT:
                        cluster.setMaintContact(xpp.nextText().trim());
                        break;
                    case DEPLOY_DESC:
                        cluster.setDeployDesc(xpp.nextText().trim());
                        break;
                    case ONLINE_TIME:
                        cluster.setOnlineTime(xpp.nextText().trim());
                        break;
                    default:
                        break;
                    }
                }
                xpp.next();
            }
            return cluster;
        } catch (NumberFormatException e) {
            logger.error(e.getMessage(), e);
        } catch (XmlPullParserException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        xmlPath = xmlFileLoader.getFilePath();
        if (null == xmlPath) {
            logger.error("cluster xmlpath doesn't set!");
            throw new IllegalArgumentException("cluster xmlpath doesn't set!");
        } else {
            if (xmlPath.endsWith(System.getProperty("file.separator"))) {
                xmlPath = new StringBuilder(xmlPath).append("cluster.xml").toString();
            } else {
                xmlPath =
                        new StringBuilder(xmlPath).append(System.getProperty("file.separator"))
                                                  .append("cluster.xml")
                                                  .toString();
            }
            read();
        }
    }

    @Override
    public ClusterDO getClusterById(long id) {
        return map.get(id);
    }

    @Override
    public boolean modifyCluster(ClusterDO cluster) {
        lock.lock();
        try {
            if (!checkName(cluster.getName(), cluster.getId())) {
                return false;
            }
            map.put(cluster.getId(), cluster);
            if (!write()) {
                logger.error("Fail to modify cluster!");
                recovery(xmlPath);
                read();
                return false;
            }
        } finally {
            lock.unlock();
        }
        return true;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public boolean checkName(String name) {
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, ClusterDO> entry = (Entry<Long, ClusterDO>) it.next();
            ClusterDO cluster = entry.getValue();
            if (name.equals(cluster.getName())) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public boolean checkName(String name, long id) {
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, ClusterDO> entry = (Entry<Long, ClusterDO>) it.next();
            ClusterDO cluster = entry.getValue();
            if (cluster.getId() == id) {
                continue;
            }
            if (name.equals(cluster.getName())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addCluster(ClusterDO cluster) {
        lock.lock();
        try {
            if (!checkName(cluster.getName())) {
                return false;
            }
            cluster.setId(++maxId);
            map.put(cluster.getId(), cluster);
            if (!write()) {
                logger.error("Fail to add cluster!");
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
    public List<ClusterDO> listAllCluster() {
        return new ArrayList<ClusterDO>(map.values());
    }

}
