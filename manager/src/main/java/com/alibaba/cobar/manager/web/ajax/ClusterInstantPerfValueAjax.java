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

import static com.alibaba.cobar.manager.util.ConstantDefine.*;

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

import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.HttpRequestHandler;

import com.alibaba.cobar.manager.dao.CobarAdapterDAO;
import com.alibaba.cobar.manager.dataobject.cobarnode.CommandStatus;
import com.alibaba.cobar.manager.dataobject.cobarnode.ProcessorStatus;
import com.alibaba.cobar.manager.dataobject.cobarnode.ServerStatus;
import com.alibaba.cobar.manager.dataobject.xml.CobarDO;
import com.alibaba.cobar.manager.service.CobarAccesser;
import com.alibaba.cobar.manager.service.XmlAccesser;
import com.alibaba.cobar.manager.util.ConstantDefine;
import com.alibaba.cobar.manager.util.FormatUtil;
import com.alibaba.cobar.manager.util.MathUtil;
import com.alibaba.cobar.manager.util.Pair;

/**
 * @author wenfeng.cenwf 2011-3-8
 * @author haiqing.zhuhq 2011-7-14
 */
public class ClusterInstantPerfValueAjax implements HttpRequestHandler, InitializingBean {
    private XmlAccesser xmlAccesser;
    private CobarAccesser cobarAccesser;
    private static final Logger logger = Logger.getLogger(ClusterInstantPerfValueAjax.class);

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

    private long groupByPList(List<ProcessorStatus> list, int type) {
        long result = 0;
        switch (type) {
        case NET_IN:
            for (ProcessorStatus p : list) {
                result += p.getNetIn();
            }
            break;
        case NET_OUT:
            for (ProcessorStatus p : list) {
                result += p.getNetOut();
            }
            break;
        case CONNECTION:
            for (ProcessorStatus p : list) {
                result += p.getConnections();
            }
            break;
        case REQUEST_COUNT:
            for (ProcessorStatus p : list) {
                result += p.getRequestCount();
            }
            break;
        default:
            throw new IllegalArgumentException("invalid parameter");
        }
        return result;
    }

    private long groupByCList(List<CommandStatus> list, int type) {
        long result = 0;
        switch (type) {
        case REQUEST_COUNT:
            for (CommandStatus p : list) {
                result += p.getQuery();
            }
            break;
        default:
            throw new IllegalArgumentException("invalid parameter");
        }
        return result;
    }

    private List<Pair<Long, Integer>> listCobarMemoryUsage(AjaxParams params) {
        List<Pair<Long, Integer>> result = new ArrayList<Pair<Long, Integer>>();
        List<CobarDO> nodes = xmlAccesser.getCobarDAO().getCobarList(params.getClusterId(), ConstantDefine.ACTIVE);
        for (CobarDO node : nodes) {
            CobarAdapterDAO perfAccesser = cobarAccesser.getAccesser(node.getId());
            if (!perfAccesser.checkConnection()) {
                StringBuilder sb = new StringBuilder("listCobarMemoryUsage: cobar connect error for Name:");
                sb.append(node.getName()).append(" Host:").append(node.getHost());
                logger.error(sb.toString());
                continue;
            }
            ServerStatus ss = perfAccesser.getServerStatus();
            int memoryUsage = 0;
            if (ss.getTotalMemory() != 0) memoryUsage = Math.round(ss.getUsedMemory() * 100 / ss.getTotalMemory());
            result.add(new Pair<Long, Integer>(node.getId(), memoryUsage));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getClusterThroughput(AjaxParams params) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

        JSONArray array = params.getArray();
        JSONObject json = null;
        Map<Long, JSONObject> cobarRequest = new HashMap<Long, JSONObject>();

        for (int i = 0; i < array.size(); i++) {
            JSONObject js = array.getJSONObject(i);
            if ("cluster".equals(js.getString("flag"))) {
                json = js;
            } else if ("cobar".equals(js.getString("flag"))) {
                cobarRequest.put(js.getLong("id"), js);
            }
        }

        PropertyUtilsBean util = new PropertyUtilsBean();

        long clusterId = params.getClusterId();
        List<CobarDO> nodes = xmlAccesser.getCobarDAO().getCobarList(clusterId, ConstantDefine.ACTIVE);
        AjaxResult cluster = new AjaxResult();
        cluster.setId(clusterId);
        cluster.setFlag("cluster");

        long timestamp = 0;
        for (CobarDO node : nodes) {
            CobarAdapterDAO perfAccesser = cobarAccesser.getAccesser(node.getId());
            if (!perfAccesser.checkConnection()) {
                StringBuilder sb = new StringBuilder("getClusterThroughput: cobar connect error for Name:");
                sb.append(node.getName()).append(" Host:").append(node.getHost());
                logger.error(sb.toString());
                continue;
            }

            AjaxResult re = new AjaxResult();
            List<ProcessorStatus> list = perfAccesser.listProccessorStatus();
            List<CommandStatus> cmdList = perfAccesser.listCommandStatus();

            long cobarNetIn = groupByPList(list, NET_IN);
            long cobarNetOut = groupByPList(list, NET_OUT);
            long cobarRequestCount = groupByCList(cmdList, REQUEST_COUNT);

            cluster.addRequest(cobarRequestCount);
            cluster.addNetIn(cobarNetIn);
            cluster.addNetOut(cobarNetOut);

            re.setId(node.getId());
            re.setFlag("cobar");
            re.setNetIn(cobarNetIn);
            re.setNetOut(cobarNetOut);
            re.setConnection(groupByPList(list, CONNECTION));
            re.setRequest(cobarRequestCount);

            timestamp = list.get(list.size() - 1).getSampleTimeStamp();
            re.setTimestamp(timestamp);

            JSONObject jsonTmp = cobarRequest.get(node.getId());
            if (jsonTmp != null) {
                re.setNetIn_deriv(FormatUtil.formatNetwork(Math.round(MathUtil.getDerivate(cobarNetIn,
                                                                                           jsonTmp.getLong("netIn"),
                                                                                           timestamp,
                                                                                           jsonTmp.getLong("timestamp"),
                                                                                           1000.0))));
                re.setNetOut_deriv(FormatUtil.formatNetwork(Math.round(MathUtil.getDerivate(cobarNetOut,
                                                                                            jsonTmp.getLong("netOut"),
                                                                                            timestamp,
                                                                                            jsonTmp.getLong("timestamp"),
                                                                                            1000.0))));
                re.setRequest_deriv(FormatUtil.formatNumber(Math.round(MathUtil.getDerivate(cobarRequestCount,
                                                                                            jsonTmp.getLong("reCount"),
                                                                                            timestamp,
                                                                                            jsonTmp.getLong("timestamp"),
                                                                                            1000.0))));
            }

            Map<String, Object> map = null;
            try {
                map = util.describe(re);
            } catch (Exception e) {
                logger.error(e);
                throw new RuntimeException(e);
            }
            if (null != map) {
                result.add(map);
            }
        }
        cluster.setTimestamp(timestamp);
        if (null != json && json.getLong("netIn") != -1) {
            long o_tiemstamp = json.getLong("timestamp");
            cluster.setNetIn_deriv(FormatUtil.formatNetwork(Math.round(MathUtil.getDerivate(cluster.getNetIn(),
                                                                                            json.getLong("netIn"),
                                                                                            timestamp,
                                                                                            o_tiemstamp,
                                                                                            1000.0))));
            cluster.setNetOut_deriv(FormatUtil.formatNetwork(Math.round(MathUtil.getDerivate(cluster.getNetOut(),
                                                                                             json.getLong("netOut"),
                                                                                             timestamp,
                                                                                             o_tiemstamp,
                                                                                             1000.0))));
            cluster.setRequest_deriv(FormatUtil.formatNumber(Math.round(MathUtil.getDerivate(cluster.getRequest(),
                                                                                             json.getLong("reCount"),
                                                                                             timestamp,
                                                                                             o_tiemstamp,
                                                                                             1000.0))));
        }
        Map<String, Object> m = null;
        try {
            m = util.describe(cluster);
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException(e);
        }
        if (null != m) {
            result.add(m);
        }

        return result;
    }

    private AjaxResult getClusterInfo(AjaxParams params) {
        JSONArray array = params.getArray();
        long clusterId = params.getClusterId();

        JSONObject json = null;
        if (array.size() > 0) {
            json = array.getJSONObject(0);
        }

        AjaxResult rs = new AjaxResult();
        rs.setId(clusterId);

        List<CobarDO> nodes = xmlAccesser.getCobarDAO().getCobarList(clusterId);
        rs.setTotal(nodes.size());
        for (CobarDO cobar : nodes) {
            if (ConstantDefine.IN_ACTIVE.equals(cobar.getStatus())) {
                continue;
            }
            CobarAdapterDAO perfAccesser = cobarAccesser.getAccesser(cobar.getId());
            if (!perfAccesser.checkConnection()) {
                rs.addError(1);
                StringBuilder sb = new StringBuilder("getClusterInfo : cobar connect error for [ Name:");
                sb.append(cobar.getName()).append(" Host:").append(cobar.getHost()).append(" ]");
                logger.error(sb.toString());
                continue;
            }
            rs.addActive(1);
            rs.setSchema(perfAccesser.listDataBases().size());
            List<ProcessorStatus> list = perfAccesser.listProccessorStatus();
            rs.addNetIn(groupByPList(list, NET_IN));
            rs.addNetOut(groupByPList(list, NET_OUT));

            rs.addConnection(groupByPList(list, CONNECTION));
            rs.setTimestamp(list.get(list.size() - 1).getSampleTimeStamp());

            List<CommandStatus> commandList = perfAccesser.listCommandStatus();
            rs.addRequest(groupByCList(commandList, REQUEST_COUNT));
        }

        if (json != null && json.getLong("netIn") != -1) {
            long o_tiemstamp = json.getLong("timestamp");
            rs.setNetIn_deriv(FormatUtil.formatNetwork(Math.round(MathUtil.getDerivate(rs.getNetIn(),
                                                                                       json.getLong("netIn"),
                                                                                       rs.getTimestamp(),
                                                                                       o_tiemstamp,
                                                                                       1000.0))));
            rs.setNetOut_deriv(FormatUtil.formatNetwork(Math.round(MathUtil.getDerivate(rs.getNetOut(),
                                                                                        json.getLong("netOut"),
                                                                                        rs.getTimestamp(),
                                                                                        o_tiemstamp,
                                                                                        1000.0))));
            rs.setRequest_deriv(FormatUtil.formatNumber(Math.round(MathUtil.getDerivate(rs.getRequest(),
                                                                                        json.getLong("reCount"),
                                                                                        rs.getTimestamp(),
                                                                                        o_tiemstamp,
                                                                                        1000.0))));
        }

        return rs;
    }

    private List<Pair<Long, String>> getStatus(AjaxParams params) {
        List<Pair<Long, String>> result = new ArrayList<Pair<Long, String>>();
        List<CobarDO> nodes = xmlAccesser.getCobarDAO().getCobarList(params.getClusterId(), ConstantDefine.ACTIVE);
        for (CobarDO node : nodes) {
            if (ConstantDefine.IN_ACTIVE.equals(node.getStatus())) {
                result.add(new Pair<Long, String>(node.getId(), ConstantDefine.IN_ACTIVE));
                continue;
            }

            CobarAdapterDAO perfAccesser = cobarAccesser.getAccesser(node.getId());
            if (!perfAccesser.checkConnection()) {
                StringBuilder sb = new StringBuilder("getStatus: cobar connect error for Name:");
                sb.append(node.getName()).append(" Host:").append(node.getHost());
                logger.error(sb.toString());
                result.add(new Pair<Long, String>(node.getId(), ConstantDefine.ERROR));
            } else {
                result.add(new Pair<Long, String>(node.getId(), ConstantDefine.ACTIVE));
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        AjaxParams params = new AjaxParams(request);
        String jsonRst = null;
        String st = params.getValueType();
        if (null == st || st.equals("")) {
            throw new IllegalArgumentException("parameter 'cobarControlValueType' is unknown: " + st);
        }
        int type = valueTypeMap.get(st);
        PropertyUtilsBean util = new PropertyUtilsBean();

        switch (type) {
        case TYPE_COBAR_MEMORY_USAGE:
            List<Pair<Long, Integer>> mList = listCobarMemoryUsage(params);
            JSONArray mArray = JSONArray.fromObject(mList);
            jsonRst = mArray.toString(2);
            break;
        case TYPE_CLUSTER_THROUGHPUT_INFO:
            List<Map<String, Object>> list1 = getClusterThroughput(params);
            JSONArray arrayMap = JSONArray.fromObject(list1);
            jsonRst = arrayMap.toString(2);
            break;
        case TYPE_CLUSTER_INFO:
            AjaxResult rs = getClusterInfo(params);
            Map<String, Object> map = null;
            try {
                map = util.describe(rs);
            } catch (Exception e) {
                logger.error(e);
                throw new RuntimeException(e);
            }
            jsonRst = JSONObject.fromObject(map).toString(2);
            break;
        case TYPE_STATUS:
            List<Pair<Long, String>> sList = getStatus(params);
            JSONArray sArray = JSONArray.fromObject(sList);
            jsonRst = sArray.toString(2);
            break;
        default:
            throw new IllegalArgumentException("parameter 'ValueType' is known: " + params.getValueType());
        }
        response.setHeader("Content-Type", "text/json; charset=utf-8");
        OutputStream out = response.getOutputStream();
        out.write(jsonRst.getBytes("utf-8"));
        out.flush();
    }

    private static final Map<String, Integer> valueTypeMap = new HashMap<String, Integer>();
    static {
        valueTypeMap.put("cobarServerLevelMemoryUsage", TYPE_COBAR_MEMORY_USAGE);
        valueTypeMap.put("cobarClusterLevelThroughput", TYPE_CLUSTER_THROUGHPUT_INFO);
        valueTypeMap.put("indexInfo", TYPE_INDEX);
        valueTypeMap.put("clusterInfo", TYPE_CLUSTER_INFO);
        valueTypeMap.put("status", TYPE_STATUS);
    }

    private static final int NET_IN = 1;
    private static final int NET_OUT = 2;
    private static final int CONNECTION = 3;
    private static final int REQUEST_COUNT = 4;

}
