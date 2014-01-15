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

import static com.alibaba.cobar.manager.util.ConstantDefine.CLUSTER_ADMIN;
import static com.alibaba.cobar.manager.util.ConstantDefine.FORBIDDEN;
import static com.alibaba.cobar.manager.util.ConstantDefine.ID;
import static com.alibaba.cobar.manager.util.ConstantDefine.NORMAL;
import static com.alibaba.cobar.manager.util.ConstantDefine.PASSWORD;
import static com.alibaba.cobar.manager.util.ConstantDefine.REALNAME;
import static com.alibaba.cobar.manager.util.ConstantDefine.STATUS;
import static com.alibaba.cobar.manager.util.ConstantDefine.SYSTEM_ADMIN;
import static com.alibaba.cobar.manager.util.ConstantDefine.USERNAME;
import static com.alibaba.cobar.manager.util.ConstantDefine.USER_ROLE;

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

import com.alibaba.cobar.manager.dao.UserDAO;
import com.alibaba.cobar.manager.dataobject.xml.UserDO;
import com.alibaba.cobar.manager.util.EncryptUtil;

/**
 * @author haiqing.zhuhq 2011-6-17
 */
public class UserDAOImple extends AbstractDAOImple implements UserDAO, InitializingBean {

    private static final Logger logger = Logger.getLogger(UserDAOImple.class);

    private Map<Long, UserDO> map;
    private static long maxId;
    private static final ReentrantLock lock = new ReentrantLock();
    private static final Map<String, Integer> typeMap = new HashMap<String, Integer>();

    static {
        typeMap.put("id", ID);
        typeMap.put("user_role", USER_ROLE);
        typeMap.put("realname", REALNAME);
        typeMap.put("password", PASSWORD);
        typeMap.put("username", USERNAME);
        typeMap.put("status", STATUS);
    }

    @SuppressWarnings("static-access")
    public UserDAOImple() {
        this.map = new HashMap<Long, UserDO>();
        this.xpp = new MXParser();
        this.xsl = new MXSerializer();
        this.maxId = Long.MIN_VALUE;
    }

    private boolean read() {
        FileInputStream is = null;
        lock.lock();
        try {
            map.clear();
            is = new FileInputStream(xmlPath);
            xpp.setInput(is, "UTF-8");
            while (!(xpp.getEventType() == XmlPullParser.END_TAG && "users".equals(xpp.getName()))) {
                if (xpp.getEventType() == XmlPullParser.START_TAG && "user".equals(xpp.getName())) {
                    UserDO user = read(xpp);
                    if (null == user) {
                        throw new XmlPullParserException("User read error");
                    }
                    maxId = (maxId < user.getId()) ? user.getId() : maxId;
                    map.put(user.getId(), user);
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
                logger.error("user backup fail!");
            }
            os = new FileOutputStream(xmlPath);
            xsl.setOutput(os, "UTF-8");
            xsl.startDocument("UTF-8", null);
            xsl.text("\n");
            xsl.startTag(null, "users");
            xsl.text("\n");
            Iterator it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Long, UserDO> entry = (Entry<Long, UserDO>) it.next();
                UserDO user = entry.getValue();
                if (!write(user)) {
                    throw new IOException("User write error!");
                }
            }
            xsl.endTag(null, "users");
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

    private UserDO read(XmlPullParser xpp) {
        UserDO user = new UserDO();
        try {
            while (!(xpp.getEventType() == XmlPullParser.END_TAG && "user".equals(xpp.getName()))) {
                if (xpp.getEventType() == XmlPullParser.START_TAG && "property".equals(xpp.getName())) {
                    int type = typeMap.get(xpp.getAttributeValue(0).trim());
                    switch (type) {
                    case ID:
                        user.setId(Long.parseLong(xpp.nextText().trim()));
                        break;
                    case USER_ROLE:
                        String value = xpp.nextText().trim();
                        if (SYSTEM_ADMIN.equals(value)) {
                            user.setUser_role(SYSTEM_ADMIN);
                        } else {
                            user.setUser_role(CLUSTER_ADMIN);
                        }
                        break;
                    case STATUS:
                        String value1 = xpp.nextText().trim();
                        if (NORMAL.equals(value1)) {
                            user.setStatus(NORMAL);
                        } else {
                            user.setStatus(FORBIDDEN);
                        }
                        break;
                    case PASSWORD:
                        user.setPassword(xpp.nextText().trim());
                        break;
                    case REALNAME:
                        user.setRealname(xpp.nextText().trim());
                        break;
                    case USERNAME:
                        user.setUsername(xpp.nextText().trim());
                        break;
                    default:
                        break;
                    }
                }
                xpp.next();
            }
            return user;
        } catch (NumberFormatException e) {
            logger.error(e.getMessage(), e);
        } catch (XmlPullParserException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    private boolean write(UserDO user) {
        try {
            writePrefix(false);
            xsl.startTag(null, "user");
            xsl.text("\n");
            writeProperty("id", String.valueOf(user.getId()));
            writeProperty("realname", user.getRealname());
            writeProperty("username", user.getUsername());
            writeProperty("password", user.getPassword());
            writeProperty("user_role", user.getUser_role());
            writeProperty("status", user.getStatus());
            writePrefix(true);
            xsl.endTag(null, "user");
            xsl.text("\n");
            return true;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public boolean checkName(String username) {
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, UserDO> entry = (Entry<Long, UserDO>) it.next();
            UserDO user = entry.getValue();
            if (username.equals(user.getUsername())) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public UserDO validateUser(String username, String password) {
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, UserDO> entry = (Entry<Long, UserDO>) it.next();
            UserDO user = entry.getValue();
            if (username.equals(user.getUsername()) && EncryptUtil.encrypt(password).equals(user.getPassword())) {
                //encrypt check success
                return user;
            } else if (username.equals(user.getUsername()) && password.equals(user.getPassword())) {
                //no encrypt check success,then change pwd to encrypted
                user.setPassword(EncryptUtil.encrypt(password));
                modifyUser(user);
                return user;
            }
        }
        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        xmlPath = xmlFileLoader.getFilePath();
        if (null == xmlPath) {
            logger.error("user xmlpath doesn't set!");
            throw new IllegalArgumentException("user xmlpath doesn't set!");
        } else {
            if (xmlPath.endsWith(System.getProperty("file.separator"))) {
                xmlPath = new StringBuilder(xmlPath).append("user.xml").toString();
            } else {
                xmlPath =
                        new StringBuilder(xmlPath).append(System.getProperty("file.separator"))
                                                  .append("user.xml")
                                                  .toString();
            }
            read();
        }
    }

    @Override
    public List<UserDO> getUserList() {
        List<UserDO> list = new ArrayList<UserDO>(map.values());
        //        Iterator it = map.entrySet().iterator();
        //        while (it.hasNext()) {
        //            Map.Entry<Long, UserDO> entry = (Entry<Long, UserDO>) it.next();
        //            UserDO user = entry.getValue();
        //            list.add(user);
        //        }
        return list;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public boolean checkName(String username, long userId) {
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, UserDO> entry = (Entry<Long, UserDO>) it.next();
            UserDO user = entry.getValue();
            if (userId == user.getId()) {
                continue;
            }
            if (username.equals(user.getUsername())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addUser(UserDO user) {
        lock.lock();
        try {
            if (!checkName(user.getUsername())) {
                return false;
            }
            user.setId(++maxId);
            map.put(user.getId(), user);
            if (!write()) {
                logger.error("Fail to add user!");
                recovery(xmlPath);
                read();
                return false;
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean modifyUser(UserDO user) {
        lock.lock();
        try {
            if (!checkName(user.getUsername(), user.getId())) {
                return false;
            }
            map.put(user.getId(), user);
            if (!write()) {
                logger.error("Fail to modify user!");
                recovery(xmlPath);
                read();
                return false;
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public UserDO getUserById(long id) {
        return map.get(id);
    }

}
