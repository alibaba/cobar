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

package com.alibaba.cobar.manager.web.ajax;

import static com.alibaba.cobar.manager.util.ConstantDefine.COBAR_LIST;
import static com.alibaba.cobar.manager.util.ConstantDefine.KILL_CONNECTION;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.HttpRequestHandler;

import com.alibaba.cobar.manager.dao.CobarAdapterDAO;
import com.alibaba.cobar.manager.dataobject.xml.CobarDO;
import com.alibaba.cobar.manager.service.CobarAccesser;
import com.alibaba.cobar.manager.service.XmlAccesser;
import com.alibaba.cobar.manager.util.ConstantDefine;
import com.alibaba.cobar.manager.util.ListSortUtil;
import com.alibaba.cobar.manager.util.Pair;

/**
 * @author haiqing.zhuhq 2011-8-11
 */
public class CobarControlAjax implements HttpRequestHandler, InitializingBean {

    private XmlAccesser xmlAccesser;
    private CobarAccesser cobarAccesser;

    public void setCobarAccesser(CobarAccesser cobarAccesser) {
        this.cobarAccesser = cobarAccesser;
    }

    public void setXmlAccesser(XmlAccesser xmlAccesser) {
        this.xmlAccesser = xmlAccesser;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (null == xmlAccesser) {
            throw new IllegalArgumentException("property 'xmlAccesser' is null!");
        }
        if (null == cobarAccesser) {
            throw new IllegalArgumentException("property 'cobarAccesser' is null!");
        }
    }

    private boolean killConnections(AjaxParams params) {
        long cobarId = params.getCobarNodeId();
        long connecId = params.getConnectionId();
        CobarDO cobar = xmlAccesser.getCobarDAO().getCobarById(cobarId);
        if (!cobar.getStatus().equals(ConstantDefine.ACTIVE)) {
            return false;
        }
        CobarAdapterDAO control = cobarAccesser.getAccesser(cobarId);
        if (control.checkConnection()) {
            control.killConnection(connecId);
            return true;
        }
        return false;
    }

    private List<Map<String, Object>> getCobarList(AjaxParams params) {
        long clusterId = params.getClusterId();
        List<CobarDO> cobarList = xmlAccesser.getCobarDAO().getCobarList(clusterId, ConstantDefine.ACTIVE);
        ListSortUtil.sortCobarByName(cobarList);
        List<Map<String, Object>> cobarViewList = new ArrayList<Map<String, Object>>();
        for (CobarDO c : cobarList) {
            CobarAdapterDAO perf = cobarAccesser.getAccesser(c.getId());
            if (perf.checkConnection()) {
                Map<String, Object> cobarMap = new HashMap<String, Object>();
                cobarMap.put("id", c.getId());
                cobarMap.put("name", c.getName());
                cobarViewList.add(cobarMap);
            }
        }
        return cobarViewList;
    }

    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        AjaxParams params = new AjaxParams(request);
        JSONArray array = null;
        String jsonRst = null;
        String st = params.getValueType();
        if (null == st || st.equals("")) {
            throw new IllegalArgumentException("parameter 'cobarControlValueType' is unknown: " + st);
        }
        int type = typeMap.get(st);
        switch (type) {
        case COBAR_LIST:
            List<Map<String, Object>> cobarList = getCobarList(params);
            array = JSONArray.fromObject(cobarList);
            jsonRst = array.toString(2);
            break;
        case KILL_CONNECTION:
            Pair<String, Boolean> kill = new Pair<String, Boolean>("result", killConnections(params));
            jsonRst = JSONObject.fromObject(kill).toString(2);
            break;
        default:
            throw new IllegalArgumentException("parameter 'cobarControlValueType' is unknown: " + params.getValueType());
        }

        response.setHeader("Content-Type", "text/json; charset=utf-8");
        OutputStream out = response.getOutputStream();
        out.write(jsonRst.getBytes("utf-8"));
        out.flush();
    }

    private static final Map<String, Integer> typeMap = new HashMap<String, Integer>();
    static {
        typeMap.put("cobarList", COBAR_LIST);
        typeMap.put("killconnection", KILL_CONNECTION);
    }
}
