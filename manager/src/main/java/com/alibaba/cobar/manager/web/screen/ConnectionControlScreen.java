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

import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import com.alibaba.cobar.manager.dao.CobarAdapterDAO;
import com.alibaba.cobar.manager.dataobject.cobarnode.ConnectionStatus;
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
 * @author haiqing.zhuhq 2011-6-27
 */
public class ConnectionControlScreen extends AbstractController implements InitializingBean {
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

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        UserDO user = (UserDO) request.getSession().getAttribute("user");
        String id = request.getParameter("clusterId");
        long clusterId = -1;
        if (null != id) {
            clusterId = Long.parseLong(id);
        }

        String cobarNodeId = request.getParameter("cobarNodeId");
        long cobarId = -1;
        if (null != cobarNodeId) {
            cobarId = Long.parseLong(cobarNodeId);
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
                cobarList = xmlAccesser.getCobarDAO().getCobarList(clusterId, ConstantDefine.ACTIVE);
            } else {
                cobarList = xmlAccesser.getCobarDAO().getCobarList(clusterId, ConstantDefine.ACTIVE);
            }
        }

        List<Map<String, Object>> cobarViewList = null;
        if (null != cobarList && cobarList.size() > 0) {
            ListSortUtil.sortCobarByName(cobarList);
            cobarViewList = new ArrayList<Map<String, Object>>();
            for (CobarDO c : cobarList) {
                CobarAdapterDAO perf = cobarAccesser.getAccesser(c.getId());
                if (perf.checkConnection()) {
                    Map<String, Object> cobarMap = new HashMap<String, Object>();
                    cobarMap.put("id", c.getId());
                    cobarMap.put("name", CobarStringUtil.htmlEscapedString(c.getName()));
                    cobarViewList.add(cobarMap);
                }
            }
        }

        /* cobarId=-2, cobar InActive; cobarId=-3, connection error */
        List<ConnectionStatus> connecList = null;
        if (cobarId > 0) {
            CobarDO cobar = xmlAccesser.getCobarDAO().getCobarById(cobarId);
            if (cobar.getStatus().equals(ConstantDefine.ACTIVE)) {
                CobarAdapterDAO perf = cobarAccesser.getAccesser(cobarId);
                if (!perf.checkConnection()) {
                    cobarId = -3;
                } else {
                    connecList = perf.listConnectionStatus();
                }
            } else {
                cobarId = -2;
            }
        }

        List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();

        if (null != connecList) {
            ListSortUtil.sortConnections(connecList);
            for (ConnectionStatus c : connecList) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("processor", c.getProcessor());
                map.put("id", c.getId());
                map.put("host", c.getHost());
                map.put("port", c.getPort());
                map.put("local_port", c.getLocal_port());
                map.put("schema", c.getSchema());
                map.put("charset", c.getCharset());
                map.put("netIn", FormatUtil.formatStore(c.getNetIn()));
                map.put("netOut", FormatUtil.formatStore(c.getNetOut()));
                map.put("aliveTime", FormatUtil.formatTime(c.getAliveTime() * 1000, 2));
                map.put("attempsCount", FormatUtil.formatNumber(c.getAttempsCount()));
                map.put("recvBuffer", FormatUtil.formatStore(c.getRecvBuffer()));
                map.put("sendQueue", c.getSendQueue());
                map.put("channel", c.getChannel());
                returnList.add(map);
            }
        }

        return new ModelAndView(
                "c_connection",
                new FluenceHashMap<String, Object>().putKeyValue("cList", clusterList)
                                                    .putKeyValue("cobarList", cobarViewList)
                                                    .putKeyValue("clusterId", clusterId)
                                                    .putKeyValue("cobarId", cobarId)
                                                    .putKeyValue("connecList", returnList)
                                                    .putKeyValue("user", user));

    }

}
