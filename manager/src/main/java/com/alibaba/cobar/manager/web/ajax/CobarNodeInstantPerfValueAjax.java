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

import static com.alibaba.cobar.manager.util.ConstantDefine.TYPE_COMMAND;
import static com.alibaba.cobar.manager.util.ConstantDefine.TYPE_CONNECTION;
import static com.alibaba.cobar.manager.util.ConstantDefine.TYPE_DATABASES;
import static com.alibaba.cobar.manager.util.ConstantDefine.TYPE_DATANODES;
import static com.alibaba.cobar.manager.util.ConstantDefine.TYPE_DATASOURCES;
import static com.alibaba.cobar.manager.util.ConstantDefine.TYPE_PROCESSOR_STATUS;
import static com.alibaba.cobar.manager.util.ConstantDefine.TYPE_SERVER_STATUS;
import static com.alibaba.cobar.manager.util.ConstantDefine.TYPE_THREAD_POOL;

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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.HttpRequestHandler;

import com.alibaba.cobar.manager.dao.CobarAdapterDAO;
import com.alibaba.cobar.manager.dataobject.cobarnode.CommandStatus;
import com.alibaba.cobar.manager.dataobject.cobarnode.ConnectionStatus;
import com.alibaba.cobar.manager.dataobject.cobarnode.DataNodesStatus;
import com.alibaba.cobar.manager.dataobject.cobarnode.DataSources;
import com.alibaba.cobar.manager.dataobject.cobarnode.ProcessorStatus;
import com.alibaba.cobar.manager.dataobject.cobarnode.ServerStatus;
import com.alibaba.cobar.manager.dataobject.cobarnode.ThreadPoolStatus;
import com.alibaba.cobar.manager.service.CobarAccesser;
import com.alibaba.cobar.manager.service.XmlAccesser;
import com.alibaba.cobar.manager.util.FormatUtil;
import com.alibaba.cobar.manager.util.ListSortUtil;
import com.alibaba.cobar.manager.util.MathUtil;

/**
 * (created at 2010-8-27)
 * 
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 * @author haiqing.zhuhq 2011-9-1
 */
public class CobarNodeInstantPerfValueAjax implements HttpRequestHandler, InitializingBean {

    private XmlAccesser xmlAccesser;
    private CobarAccesser cobarAccesser;

    private static final int NET_IN = 1;
    private static final int NET_OUT = 2;
    private static final int CONNECTION = 3;
    private static final int REQUEST_COUNT = 4;

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

    private long groupBy(List<ProcessorStatus> list, int type) {
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

    /**
     * Processor Tab
     * 
     * @param params
     * @return
     */
    private List<Map<String, Object>> listProcessorStatus(AjaxParams params) {
        long timestamp = 0;

        CobarAdapterDAO perfAccesser = cobarAccesser.getAccesser(params.getCobarNodeId());
        if (!perfAccesser.checkConnection()) {
            return null;
        }

        List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();

        List<ProcessorStatus> list = perfAccesser.listProccessorStatus();
        long a[] = new long[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        for (ProcessorStatus e : list) {
            a[0] += e.getNetIn();
            a[1] += e.getNetOut();
            a[2] += e.getRequestCount();
            a[3] += e.getrQueue();
            a[4] += e.getwQueue();
            a[5] += e.getFreeBuffer();
            a[6] += e.getTotalBuffer();
            a[7] += e.getConnections();
            a[8] += e.getBc_count();
            timestamp = e.getSampleTimeStamp();

            Map<String, Object> map = new HashMap<String, Object>();
            map.put("processorId", e.getProcessorId());
            map.put("netIn", FormatUtil.formatStore(e.getNetIn()));
            map.put("netOut", FormatUtil.formatStore(e.getNetOut()));
            map.put("requestCount", FormatUtil.formatNumber(e.getRequestCount()));
            map.put("rQueue", e.getrQueue());
            map.put("wQueue", e.getwQueue());
            map.put("freeBuffer", e.getFreeBuffer());
            map.put("totalBuffer", e.getTotalBuffer());
            map.put("connections", e.getConnections());
            map.put("bc_count", e.getBc_count());
            returnList.add(map);
        }

        Map<String, Object> total = new HashMap<String, Object>();
        total.put("processorId", "TOTAL");
        total.put("netIn", FormatUtil.formatStore(a[0]));
        total.put("netOut", FormatUtil.formatStore(a[1]));
        total.put("requestCount", FormatUtil.formatNumber(a[2]));
        total.put("rQueue", a[3]);
        total.put("wQueue", a[4]);
        total.put("freeBuffer", a[5]);
        total.put("totalBuffer", a[6]);
        total.put("connections", a[7]);
        total.put("bc_count", a[8]);
        total.put("sampleTimeStamp", timestamp);
        total.put("netInC", a[0]);
        total.put("netOutC", a[1]);
        total.put("requestCountC", a[2]);
        returnList.add(total);

        return returnList;
    }

    /**
     * ThreadPool Tab
     * 
     * @param params
     * @return
     */
    private List<Map<String, Object>> listThreadPool(AjaxParams params) {
        CobarAdapterDAO perfAccesser = cobarAccesser.getAccesser(params.getCobarNodeId());
        if (!perfAccesser.checkConnection()) {
            return null;
        }

        List<ThreadPoolStatus> pools = perfAccesser.listThreadPoolStatus();
        List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();
        for (ThreadPoolStatus t : pools) {
            Map<String, Object> map = new HashMap<String, Object>();

            map.put("threadPoolName", t.getThreadPoolName());
            map.put("poolSize", t.getPoolSize());
            map.put("activeSize", t.getActiveSize());
            map.put("taskQueue", t.getTaskQueue());
            map.put("completedTask", FormatUtil.formatNumber(t.getCompletedTask()));
            map.put("totalTask", FormatUtil.formatNumber(t.getTotalTask()));

            returnList.add(map);
        }

        return returnList;
    }

    /**
     * Command Tab
     * 
     * @param params
     * @return
     */
    private List<Map<String, Object>> listCommand(AjaxParams params) {
        CobarAdapterDAO perfAccesser = cobarAccesser.getAccesser(params.getCobarNodeId());
        if (!perfAccesser.checkConnection()) {
            return null;
        }
        List<CommandStatus> list = perfAccesser.listCommandStatus();
        List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();

        // the last element is total count
        long a[] = new long[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        for (CommandStatus l : list) {
            a[0] += l.getQuery();
            a[1] += l.getStmtPrepared();
            a[2] += l.getStmtExecute();
            a[3] += l.getStmtClose();
            a[4] += l.getPing();
            a[5] += l.getQuit();
            a[6] += l.getOther();
            a[7] += l.getInitDB();
            a[8] += l.getKill();

            Map<String, Object> map = new HashMap<String, Object>();
            map.put("processorId", l.getProcessorId());
            map.put("stmtPrepared", FormatUtil.formatNumber(l.getStmtPrepared()));
            map.put("stmtExecute", FormatUtil.formatNumber(l.getStmtExecute()));
            map.put("query", FormatUtil.formatNumber(l.getQuery()));
            map.put("stmtClose", FormatUtil.formatNumber(l.getStmtClose()));
            map.put("ping", FormatUtil.formatNumber(l.getPing()));
            map.put("quit", FormatUtil.formatNumber(l.getQuit()));
            map.put("other", FormatUtil.formatNumber(l.getOther()));
            map.put("kill", FormatUtil.formatNumber(l.getKill()));
            map.put("initDB", FormatUtil.formatNumber(l.getInitDB()));
            returnList.add(map);
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("processorId", "Total");
        map.put("stmtPrepared", FormatUtil.formatNumber(a[1]));
        map.put("stmtExecute", FormatUtil.formatNumber(a[2]));
        map.put("query", FormatUtil.formatNumber(a[0]));
        map.put("stmtClose", FormatUtil.formatNumber(a[3]));
        map.put("ping", FormatUtil.formatNumber(a[4]));
        map.put("quit", FormatUtil.formatNumber(a[5]));
        map.put("other", FormatUtil.formatNumber(a[6]));
        map.put("kill", FormatUtil.formatNumber(a[8]));
        map.put("initDB", FormatUtil.formatNumber(a[7]));
        returnList.add(map);

        return returnList;
    }

    /**
     * Connection Tab
     * 
     * @param params
     * @return
     */
    private List<Map<String, Object>> listConnection(AjaxParams params) {
        CobarAdapterDAO perfAccesser = cobarAccesser.getAccesser(params.getCobarNodeId());
        if (!perfAccesser.checkConnection()) {
            return null;
        }
        List<ConnectionStatus> list = perfAccesser.listConnectionStatus();
        if (null != list) {
            ListSortUtil.sortConnections(list);
        }
        List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();
        for (ConnectionStatus c : list) {
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
        return returnList;
    }

    @SuppressWarnings({ "unchecked" })
    private List<Map<String, Object>> listDatanode(AjaxParams params) {
        CobarAdapterDAO perfAccesser = cobarAccesser.getAccesser(params.getCobarNodeId());
        if (!perfAccesser.checkConnection()) {
            return null;
        }
        PropertyUtilsBean util = new PropertyUtilsBean();
        List<DataNodesStatus> list = perfAccesser.listDataNodes();;
        if (null != list) {
            ListSortUtil.sortDataNodesByPoolName(list);
        }
        List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();
        for (DataNodesStatus c : list) {
            Map<String, Object> map = null;
            try {
                map = util.describe(c);
            } catch (Exception e1) {
                throw new RuntimeException(e1);
            }
            map.remove("class");
            map.remove("executeCount");
            map.put("executeCount", FormatUtil.formatNumber(c.getExecuteCount()));
            map.remove("recoveryTime");
            if (-1 != c.getRecoveryTime()) {
                map.put("recoveryTime", FormatUtil.formatTime(c.getRecoveryTime() * 1000, 2));
            } else {
                map.put("recoveryTime", c.getRecoveryTime());
            }
            returnList.add(map);
        }
        return returnList;
    }

    private Map<String, Object> getServerStatus(AjaxParams params) {
        JSONArray array = params.getArray();

        JSONObject jobject = array.getJSONObject(0);

        CobarAdapterDAO perfAccesser = cobarAccesser.getAccesser(params.getCobarNodeId());
        if (!perfAccesser.checkConnection()) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("uptime", 0);
            map.put("usedMemory", 0);
            map.put("maxMemory", 0);
            map.put("totalMemory", 0);
            map.put("connectionCount", 0);
            map.put("status", "ERROR");
            map.put("version", "UNKNOWN");
            map.put("starttime", 0);

            map.put("netInC", 0);
            map.put("netOutC", 0);
            map.put("requestCountC", 0);
            map.put("sampleTimeStamp", System.currentTimeMillis());
            map.put("netIn_deriv", 0);
            map.put("netOut_deriv", 0);
            map.put("reCount_deriv", 0);

            return map;
        }
        ServerStatus ss = perfAccesser.getServerStatus();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("uptime", ss.getUptime());
        map.put("usedMemory", FormatUtil.formatStore(ss.getUsedMemory()));
        map.put("maxMemory", FormatUtil.formatStore(ss.getMaxMemory()));
        map.put("totalMemory", FormatUtil.formatStore(ss.getTotalMemory()));

        List<ProcessorStatus> list = perfAccesser.listProccessorStatus();
        List<CommandStatus> cmdList = perfAccesser.listCommandStatus();

        long netIn = groupBy(list, NET_IN);
        long netOut = groupBy(list, NET_OUT);
        long requestCount = groupByCList(cmdList, REQUEST_COUNT);
        long connectionCount = groupBy(list, CONNECTION);
        long timestamp = list.get(list.size() - 1).getSampleTimeStamp();

        long o_netIn = jobject.getLong("netIn");
        long o_netOut = jobject.getLong("netOut");
        long o_requestCount = jobject.getLong("requestCount");
        long o_timestamp = jobject.getLong("sampleTimeStamp");

        map.put("netInC", netIn);
        map.put("netOutC", netOut);
        map.put("requestCountC", requestCount);
        map.put("sampleTimeStamp", timestamp);

        map.put("netIn_deriv",
                FormatUtil.formatNetwork((long) MathUtil.getDerivate(netIn, o_netIn, timestamp, o_timestamp, 1000.0)));
        map.put("netOut_deriv",
                FormatUtil.formatNetwork((long) MathUtil.getDerivate(netOut, o_netOut, timestamp, o_timestamp, 1000.0)));
        map.put("reCount_deriv", FormatUtil.formatNumber((long) MathUtil.getDerivate(requestCount,
                                                                                     o_requestCount,
                                                                                     timestamp,
                                                                                     o_timestamp,
                                                                                     1000.0)));

        map.put("version", FormatUtil.formatVersion(perfAccesser.getVersion()));
        map.put("starttime", perfAccesser.getStartUpTime().getFormatTime());

        map.put("connectionCount", connectionCount);
        map.put("status", ss.getStatus());

        return map;
    }

    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        AjaxParams params = new AjaxParams(request);
        CobarAdapterDAO perfAccesser = cobarAccesser.getAccesser(params.getCobarNodeId());
        JSONArray array = null;
        String jsonRst = null;
        String st = params.getValueType();
        if (null == st || st.equals("")) {
            throw new IllegalArgumentException("parameter 'cobarControlValueType' is unknown: " + st);
        }
        int type = typeMap.get(st);
        switch (type) {
        case TYPE_SERVER_STATUS:
            jsonRst = JSONObject.fromObject(getServerStatus(params)).toString(2);
            break;
        case TYPE_PROCESSOR_STATUS:
            List<Map<String, Object>> listProcessor = listProcessorStatus(params);
            array = JSONArray.fromObject(listProcessor);
            jsonRst = array.toString(2);
            break;
        case TYPE_THREAD_POOL:
            List<Map<String, Object>> listThreadpool = listThreadPool(params);
            array = JSONArray.fromObject(listThreadpool);
            jsonRst = array.toString(2);
            break;
        case TYPE_COMMAND:
            List<Map<String, Object>> listCommand = listCommand(params);
            array = JSONArray.fromObject(listCommand);
            jsonRst = array.toString(2);
            break;
        case TYPE_DATANODES:
            List<Map<String, Object>> listDatanode = listDatanode(params);
            array = JSONArray.fromObject(listDatanode);
            jsonRst = array.toString(2);
            break;
        case TYPE_DATABASES:
            List<String> listDatabase = null;
            if (perfAccesser.checkConnection()) {
                listDatabase = perfAccesser.listDataBases();
            }
            array = JSONArray.fromObject(listDatabase);
            jsonRst = array.toString(2);
            break;
        case TYPE_DATASOURCES:
            List<DataSources> listDatasource = null;
            if (perfAccesser.checkConnection()) {
                listDatasource = perfAccesser.listDataSources();
            }
            array = JSONArray.fromObject(listDatasource);
            jsonRst = array.toString(2);
            break;
        case TYPE_CONNECTION:
            List<Map<String, Object>> listConnection = listConnection(params);
            array = JSONArray.fromObject(listConnection);
            jsonRst = array.toString(2);
            break;
        default:
            throw new IllegalArgumentException("parameter 'cobarNodeInstantPerfValueType' is known: "
                                               + params.getValueType());
        }
        perfAccesser = null;

        response.setHeader("Content-Type", "text/json; charset=utf-8");
        OutputStream out = response.getOutputStream();
        out.write(jsonRst.getBytes("utf-8"));
        out.flush();
    }

    private static final Map<String, Integer> typeMap = new HashMap<String, Integer>();
    static {
        typeMap.put("serverStatus", TYPE_SERVER_STATUS);
        typeMap.put("processorStatus", TYPE_PROCESSOR_STATUS);
        typeMap.put("threadPoolStatus", TYPE_THREAD_POOL);
        typeMap.put("command", TYPE_COMMAND);
        typeMap.put("datanodes", TYPE_DATANODES);
        typeMap.put("connection", TYPE_CONNECTION);
        typeMap.put("databases", TYPE_DATABASES);
        typeMap.put("datasources", TYPE_DATASOURCES);
    }
}
