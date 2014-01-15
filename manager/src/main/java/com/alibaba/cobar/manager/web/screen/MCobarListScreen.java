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

import com.alibaba.cobar.manager.dataobject.xml.ClusterDO;
import com.alibaba.cobar.manager.dataobject.xml.CobarDO;
import com.alibaba.cobar.manager.dataobject.xml.UserDO;
import com.alibaba.cobar.manager.service.XmlAccesser;
import com.alibaba.cobar.manager.util.CobarStringUtil;
import com.alibaba.cobar.manager.util.FluenceHashMap;
import com.alibaba.cobar.manager.util.ListSortUtil;

/**
 * @author haiqing.zhuhq 2011-6-27
 */
public class MCobarListScreen extends AbstractController implements InitializingBean {
    private XmlAccesser xmlAccesser;

    public void setXmlAccesser(XmlAccesser xmlAccesser) {
        this.xmlAccesser = xmlAccesser;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (xmlAccesser == null) {
            throw new IllegalArgumentException("property 'xmlAccesser' is null!");
        }
    }

    @SuppressWarnings({ "unchecked" })
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        UserDO user = (UserDO) request.getSession().getAttribute("user");
        long clusterId = Long.parseLong(request.getParameter("clusterId"));
        ClusterDO cluster = xmlAccesser.getClusterDAO().getClusterById(clusterId);
        List<CobarDO> cobarList = xmlAccesser.getCobarDAO().getCobarList(clusterId);
        List<Map<String, Object>> cobarViewList = null;
        if (null != cobarList) {
            ListSortUtil.sortCobarByName(cobarList);
            cobarViewList = new ArrayList<Map<String, Object>>();
            PropertyUtilsBean util = new PropertyUtilsBean();
            for (CobarDO c : cobarList) {
                Map<String, Object> map = util.describe(c);
                map.remove("class");
                map.remove("name");
                map.put("name", CobarStringUtil.htmlEscapedString(c.getName()));
                cobarViewList.add(map);
            }
        }
        Map<String, Object> clusterView = new HashMap<String, Object>();
        clusterView.put("id", cluster.getId());
        clusterView.put("name", CobarStringUtil.htmlEscapedString(cluster.getName()));

        return new ModelAndView("m_cobarList", new FluenceHashMap<String, Object>().putKeyValue("cobarList",
                                                                                                cobarViewList)
                                                                                   .putKeyValue("user", user)
                                                                                   .putKeyValue("cluster", clusterView));

    }

}
