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

package com.alibaba.cobar.manager.web.screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.PropertyUtilsBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import com.alibaba.cobar.manager.dao.CobarAdapterDAO;
import com.alibaba.cobar.manager.dataobject.cobarnode.ServerStatus;
import com.alibaba.cobar.manager.dataobject.xml.ClusterDO;
import com.alibaba.cobar.manager.dataobject.xml.CobarDO;
import com.alibaba.cobar.manager.dataobject.xml.UserDO;
import com.alibaba.cobar.manager.service.CobarAccesser;
import com.alibaba.cobar.manager.service.XmlAccesser;
import com.alibaba.cobar.manager.util.CobarStringUtil;
import com.alibaba.cobar.manager.util.ConstantDefine;
import com.alibaba.cobar.manager.util.FluenceHashMap;
import com.alibaba.cobar.manager.util.FormatUtil;
import com.alibaba.cobar.manager.util.ListSortUtil;

/**
 * @author haiqing.zhuhq 2011-8-11
 */
public class PropertyReloadScreen extends AbstractController implements InitializingBean {
    private XmlAccesser xmlAccesser;
    private CobarAccesser cobarAccesser;

    public void setXmlAccesser(XmlAccesser xmlAccesser) {
        this.xmlAccesser = xmlAccesser;
    }

    public void setCobarAccesser(CobarAccesser cobarAccesser) {
        this.cobarAccesser = cobarAccesser;
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

    @SuppressWarnings("unchecked")
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        UserDO user = (UserDO) request.getSession().getAttribute("user");
        String id = request.getParameter("clusterId");
        long clusterId = -1;
        if (null != id) {
            clusterId = Long.parseLong(id);
        }

        List<ClusterDO> cList = xmlAccesser.getClusterDAO().listAllCluster();
        List<Map<String, Object>> clusterList = new ArrayList<Map<String, Object>>();
        ListSortUtil.sortClusterByName(cList);
        for (ClusterDO e : cList) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("id", e.getId());
            map.put("name", CobarStringUtil.htmlEscapedString(e.getName()));
            clusterList.add(map);
        }

        List<CobarDO> cobarList = null;
        if (null != cList && cList.size() > 0) {
            if (-1 == clusterId) {
                clusterId = cList.get(0).getId();
                cobarList = xmlAccesser.getCobarDAO().getCobarList(clusterId);
            } else {
                cobarList = xmlAccesser.getCobarDAO().getCobarList(clusterId);
            }
        }

        List<Map<String, Object>> cobarListMap = new ArrayList<Map<String, Object>>();
        PropertyUtilsBean util = new PropertyUtilsBean();
        if (null != cobarList) {
            ListSortUtil.sortCobarByName(cobarList);
            for (CobarDO c : cobarList) {
                CobarAdapterDAO perf = cobarAccesser.getAccesser(c.getId());
                Map<String, Object> map;
                try {
                    map = util.describe(c);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
                map.remove("class");
                map.remove("name");
                map.put("name", CobarStringUtil.htmlEscapedString(c.getName()));

                if (ConstantDefine.ACTIVE.equals(c.getStatus())) {
                    if (!perf.checkConnection()) {
                        map.remove("status");
                        map.put("status", ConstantDefine.ERROR);
                        map.put("reloadTime", "");
                        map.put("rollbackTime", "");
                    } else {
                        ServerStatus ss = perf.getServerStatus();
                        String rollbackTime = "NO";
                        String reloadTime = FormatUtil.fromMilliseconds2String(ss.getReloadTime());
                        if (ss.getRollbackTime() != -1) {
                            rollbackTime = FormatUtil.fromMilliseconds2String(ss.getRollbackTime());
                        }
                        map.put("reloadTime", reloadTime);
                        map.put("rollbackTime", rollbackTime);
                    }
                } else {
                    map.put("reloadTime", "");
                    map.put("rollbackTime", "");
                }

                cobarListMap.add(map);
            }
        }

        return new ModelAndView("c_propertyReload", new FluenceHashMap<String, Object>().putKeyValue("cList",
                                                                                                     clusterList)
                                                                                        .putKeyValue("cobarList",
                                                                                                     cobarListMap)
                                                                                        .putKeyValue("clusterId",
                                                                                                     clusterId)
                                                                                        .putKeyValue("user", user));

    }

}
