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

import static com.alibaba.cobar.manager.util.ConstantDefine.ACTIVE;
import static com.alibaba.cobar.manager.util.ConstantDefine.CLUSTER_ID;
import static com.alibaba.cobar.manager.util.ConstantDefine.DELETE;
import static com.alibaba.cobar.manager.util.ConstantDefine.HOST;
import static com.alibaba.cobar.manager.util.ConstantDefine.ID;
import static com.alibaba.cobar.manager.util.ConstantDefine.IN_ACTIVE;
import static com.alibaba.cobar.manager.util.ConstantDefine.NAME;
import static com.alibaba.cobar.manager.util.ConstantDefine.PASSWORD;
import static com.alibaba.cobar.manager.util.ConstantDefine.PORT;
import static com.alibaba.cobar.manager.util.ConstantDefine.SERVER_PORT;
import static com.alibaba.cobar.manager.util.ConstantDefine.STATUS;
import static com.alibaba.cobar.manager.util.ConstantDefine.TIME_DIFF;
import static com.alibaba.cobar.manager.util.ConstantDefine.USER;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
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

import com.alibaba.cobar.manager.dao.CobarDAO;
import com.alibaba.cobar.manager.dataobject.xml.CobarDO;
import com.alibaba.cobar.manager.util.EncryptUtil;

/**
 * @author haiqing.zhuhq 2011-6-15
 */
public class CobarDAOImple extends AbstractDAOImple implements CobarDAO, InitializingBean {
    private static final Logger logger = Logger.getLogger(CobarDAOImple.class);
    private Map<Long, CobarDO> map;
    private static long maxId;
    private static final ReentrantLock lock = new ReentrantLock();
    private static final Map<String, Integer> typeMap = new HashMap<String, Integer>();

    static {
        typeMap.put("id", ID);
        typeMap.put("name", NAME);
        typeMap.put("status", STATUS);
        typeMap.put("clusterId", CLUSTER_ID);
        typeMap.put("host", HOST);
        typeMap.put("port", PORT);
        typeMap.put("password", PASSWORD);
        typeMap.put("time_diff", TIME_DIFF);
        typeMap.put("user", USER);
        typeMap.put("serverPort", SERVER_PORT);
    }

    public CobarDAOImple() {
        map = new HashMap<Long, CobarDO>();
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
            while (!(xpp.getEventType() == XmlPullParser.END_TAG && "cobars".equals(xpp.getName()))) {
                if (xpp.getEventType() == XmlPullParser.START_TAG && "cobar".equals(xpp.getName())) {
                    CobarDO cobar = read(xpp);
                    if (null == cobar) {
                        throw new XmlPullParserException("Cobar read error");
                    }
                    maxId = (maxId < cobar.getId()) ? cobar.getId() : maxId;
                    map.put(cobar.getId(), cobar);
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private boolean write() {
        FileOutputStream os = null;
        lock.lock();
        try {
            if (!backup(xmlPath)) {
                logger.error("cobar backup fail!");
            }
            os = new FileOutputStream(xmlPath);
            xsl.setOutput(os, "UTF-8");
            xsl.startDocument("UTF-8", null);
            xsl.text("\n");
            xsl.startTag(null, "cobars");
            xsl.text("\n");
            Iterator it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Long, CobarDO> entry = (Entry<Long, CobarDO>) it.next();
                CobarDO cobar = entry.getValue();
                if (!write(cobar)) {
                    throw new IOException("Cobar write error!");
                }
            }
            xsl.endTag(null, "cobars");
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

    private boolean write(CobarDO cobar) {
        try {
            writePrefix(false);
            xsl.startTag(null, "cobar");
            xsl.text("\n");
            writeProperty("id", String.valueOf(cobar.getId()));
            writeProperty("clusterId", String.valueOf(cobar.getClusterId()));
            writeProperty("name", cobar.getName());
            writeProperty("status", cobar.getStatus());
            writeProperty("host", cobar.getHost());
            writeProperty("serverPort", String.valueOf(cobar.getServerPort()));
            writeProperty("port", String.valueOf(cobar.getPort()));
            writeProperty("user", cobar.getUser());
            String password = EncryptUtil.encrypt(cobar.getPassword());
            writeProperty("password", password);
            writeProperty("time_diff", cobar.getTime_diff());
            writePrefix(true);
            xsl.endTag(null, "cobar");
            xsl.text("\n");
            return true;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    private CobarDO read(XmlPullParser xpp) {
        CobarDO cobar = new CobarDO();
        cobar.setServerPort(8066);
        try {
            while (!(xpp.getEventType() == XmlPullParser.END_TAG && "cobar".equals(xpp.getName()))) {
                if (xpp.getEventType() == XmlPullParser.START_TAG && "property".equals(xpp.getName())) {
                    int type = typeMap.get(xpp.getAttributeValue(0).trim());
                    switch (type) {
                    case ID:
                        cobar.setId(Long.parseLong(xpp.nextText().trim()));
                        break;
                    case NAME:
                        cobar.setName(xpp.nextText().trim());
                        break;
                    case CLUSTER_ID:
                        cobar.setClusterId(Long.parseLong(xpp.nextText().trim()));
                        break;
                    case STATUS:
                        String value = xpp.nextText().trim();
                        if (ACTIVE.equals(value)) {
                            cobar.setStatus(ACTIVE);
                        } else if (DELETE.equals(value)) {
                            cobar.setStatus(DELETE);
                        } else {
                            cobar.setStatus(IN_ACTIVE);
                        }
                        break;
                    case HOST:
                        cobar.setHost(xpp.nextText().trim());
                        break;
                    case PORT:
                        cobar.setPort(Integer.parseInt(xpp.nextText().trim()));
                        break;
                    case SERVER_PORT:
                        cobar.setServerPort(Integer.parseInt(xpp.nextText().trim()));
                        break;
                    case USER:
                        cobar.setUser(xpp.nextText().trim());
                        break;
                    case PASSWORD:
                        String password = EncryptUtil.decrypt(xpp.nextText().trim());
                        cobar.setPassword(password);
                        break;
                    case TIME_DIFF:
                        cobar.setTime_diff(xpp.nextText().trim());
                        break;
                    default:
                        break;
                    }
                }
                xpp.next();
            }
            return cobar;
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
    public CobarDO getCobarById(long id) {
        return map.get(id);
    }

    //    @SuppressWarnings({ "unchecked", "rawtypes" })
    //    @Override
    //    public int getCobarCountByStatus(long clusterId, String status) {
    //        int count = 0;
    //        Iterator it = map.entrySet().iterator();
    //        while (it.hasNext()) {
    //            Map.Entry<Long, CobarDO> entry = (Entry<Long, CobarDO>) it.next();
    //            CobarDO cobar = entry.getValue();
    //            if (clusterId == cobar.getClusterId() && status.equals(cobar.getStatus())) {
    //                count++;
    //            }
    //        }
    //        return count;
    //    }

    @Override
    public void afterPropertiesSet() throws Exception {
        xmlPath = xmlFileLoader.getFilePath();
        if (null == xmlPath) {
            logger.error("cobar xmlpath doesn't set!");
            throw new IllegalArgumentException("cobar xmlpath doesn't set!");
        } else {
            if (xmlPath.endsWith(System.getProperty("file.separator"))) {
                xmlPath = new StringBuilder(xmlPath).append("cobar.xml").toString();
            } else {
                xmlPath =
                        new StringBuilder(xmlPath).append(System.getProperty("file.separator"))
                                                  .append("cobar.xml")
                                                  .toString();
            }
            read();
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public boolean checkName(String name, long clusterId) {
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, CobarDO> entry = (Entry<Long, CobarDO>) it.next();
            CobarDO cobar = entry.getValue();
            if (clusterId == cobar.getClusterId() && cobar.getName().equals(name)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addCobar(CobarDO cobar) {
        lock.lock();
        try {
            if (!checkName(cobar.getName(), cobar.getClusterId())) {
                return false;
            }
            cobar.setId(++maxId);
            map.put(cobar.getId(), cobar);
            if (!write()) {
                logger.error("Fail to add cobar!");
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
    public List<CobarDO> getCobarList(long clusterId) {
        List<CobarDO> list = new ArrayList<CobarDO>();
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, CobarDO> entry = (Entry<Long, CobarDO>) it.next();
            CobarDO cobar = entry.getValue();
            if (clusterId == cobar.getClusterId()) {
                list.add(cobar);
            }
        }
        return list;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public List<CobarDO> getCobarList(long clusterId, String status) {
        List<CobarDO> list = new ArrayList<CobarDO>();
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, CobarDO> entry = (Entry<Long, CobarDO>) it.next();
            CobarDO cobar = entry.getValue();
            if (clusterId == cobar.getClusterId() && status.equals(cobar.getStatus())) {
                list.add(cobar);
            }
        }
        return list;
    }

    @Override
    public List<CobarDO> listCobarById(long[] cobarIds) {
        List<CobarDO> list = new LinkedList<CobarDO>();
        for (int i = 0; i < cobarIds.length; i++) {
            CobarDO cobar = map.get(cobarIds[i]);
            if (cobar != null) {
                list.add(cobar);
            }
        }
        return list;
    }

    @Override
    public boolean modifyCobar(CobarDO cobar) {
        lock.lock();
        try {
            if (!checkName(cobar.getName(), cobar.getClusterId(), cobar.getId())) {
                return false;
            }
            map.put(cobar.getId(), cobar);
            if (!write()) {
                logger.error("Fail to modify cobar!");
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
    public boolean checkName(String name, long clusterId, long cobarId) {
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, CobarDO> entry = (Entry<Long, CobarDO>) it.next();
            CobarDO cobar = entry.getValue();
            if (cobar.getId() == cobarId) {
                continue;
            }
            if (clusterId == cobar.getClusterId() && cobar.getName().equals(name)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<CobarDO> listAllCobar() {
        return new ArrayList<CobarDO>(map.values());
    }

}
