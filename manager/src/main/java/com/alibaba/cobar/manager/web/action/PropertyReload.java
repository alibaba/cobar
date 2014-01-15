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

package com.alibaba.cobar.manager.web.action;

import static com.alibaba.cobar.manager.util.ConstantDefine.CONFIG_RELOAD;
import static com.alibaba.cobar.manager.util.ConstantDefine.CONFIG_ROLLBACK;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import com.alibaba.cobar.manager.dao.CobarAdapterDAO;
import com.alibaba.cobar.manager.dataobject.xml.CobarDO;
import com.alibaba.cobar.manager.dataobject.xml.UserDO;
import com.alibaba.cobar.manager.service.CobarAccesser;
import com.alibaba.cobar.manager.service.XmlAccesser;
import com.alibaba.cobar.manager.util.CobarStringUtil;
import com.alibaba.cobar.manager.util.ConstantDefine;

/**
 * @author haiqing.zhuhq 2011-6-27
 */
public class PropertyReload extends AbstractController implements InitializingBean {
    private static Map<String, Integer> typeMap;
    private XmlAccesser xmlAccesser;
    private CobarAccesser cobarAccesser;
    private static final ReentrantLock lock = new ReentrantLock();

    public void setXmlAccesser(XmlAccesser xmlAccesser) {
        this.xmlAccesser = xmlAccesser;
    }

    public void setCobarAccesser(CobarAccesser cobarAccesser) {
        this.cobarAccesser = cobarAccesser;
    }

    static {
        typeMap = new HashMap<String, Integer>();
        typeMap.put("configReload", CONFIG_RELOAD);
        typeMap.put("configRollback", CONFIG_ROLLBACK);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (xmlAccesser == null) {
            throw new IllegalArgumentException("property 'xmlAccesser' is null!");
        }
        if (null == cobarAccesser) {
            throw new IllegalArgumentException("property 'cobarAccesser' is null!");
        }
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        UserDO user = (UserDO) request.getSession().getAttribute("user");
        String types = request.getParameter("type");
        int type = typeMap.get(types);
        String list = request.getParameter("list");
        String temp[] = list.split(",");
        int index[] = new int[temp.length];
        for (int i = 0; i < temp.length; i++) {
            index[i] = Integer.parseInt(temp[i]);
        }
        if (logger.isWarnEnabled()) {
            StringBuilder log =
                    new StringBuilder(user.getUsername()).append(" | do ").append(types).append(" | cobar:");
            for (int i = 0; i < index.length; i++) {
                log.append(xmlAccesser.getCobarDAO().getCobarById(index[i])).append(" ");
            }
            logger.warn(log.toString());
        }
        List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();

        lock.lock();
        try {
            switch (type) {
            case CONFIG_RELOAD:
                for (int i = 0; i < index.length; i++) {
                    CobarDO c = xmlAccesser.getCobarDAO().getCobarById(index[i]);
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("name", CobarStringUtil.htmlEscapedString(c.getName()));
                    if (c.getStatus().equals(ConstantDefine.ACTIVE)) {
                        CobarAdapterDAO perf = cobarAccesser.getAccesser(index[i]);
                        if (perf.checkConnection()) {
                            if (perf.reloadConfig()) {
                                map.put("result", "success");
                            } else {
                                map.put("result", "config reload error");
                            }
                        } else {
                            map.put("result", "connection error");
                        }
                    } else {
                        map.put("result", "cobar InActive");
                    }
                    resultList.add(map);
                }
                break;
            case CONFIG_ROLLBACK:
                for (int i = 0; i < index.length; i++) {
                    CobarDO c = xmlAccesser.getCobarDAO().getCobarById(index[i]);
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("name", CobarStringUtil.htmlEscapedString(c.getName()));
                    if (c.getStatus().equals(ConstantDefine.ACTIVE)) {
                        CobarAdapterDAO perf = cobarAccesser.getAccesser(index[i]);
                        if (perf.checkConnection()) {
                            if (perf.rollbackConfig()) {
                                map.put("result", "success");
                            } else {
                                map.put("result", "config reload error");
                            }
                        } else {
                            map.put("result", "connection error");
                        }
                    } else {
                        map.put("result", "cobar InActive");
                    }
                    resultList.add(map);
                }
                break;
            default:
                logger.error("Wrong property control type!");
                break;

            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("name", "UNKNOWN ERROR");
            map.put("result", "unknown exception occurs when reloading");
            resultList.clear();
            resultList.add(map);
        } finally {
            lock.unlock();
        }

        return new ModelAndView("c_result", "resultList", resultList);
    }

}
