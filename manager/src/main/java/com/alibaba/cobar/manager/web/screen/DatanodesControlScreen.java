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
import com.alibaba.cobar.manager.dataobject.cobarnode.DataNodesStatus;
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
public class DatanodesControlScreen extends AbstractController implements InitializingBean {
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

    /*
     * 对每个cobar数据节点列表排序，依次取出每个列表的对应节点值进行比较
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        UserDO user = (UserDO) request.getSession().getAttribute("user");
        String id = request.getParameter("clusterId");
        long clusterId = -1;
        if (null != id) {
            clusterId = Long.parseLong(id);
        }

        /* 集群下拉列表显示处理 */
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
        List<Map<String, Object>> dataList = null;
        boolean hasDatanode = false;//if some cobar has datanodeList which is not null
        boolean isUniform = true;//all cobar has same datanodeList
        boolean connectionFlag = true;

        if (null != cList && cList.size() > 0) {
            if (-1 == clusterId) {
                clusterId = cList.get(0).getId();
                cobarList = xmlAccesser.getCobarDAO().getCobarList(clusterId, ConstantDefine.ACTIVE);
            } else {
                cobarList = xmlAccesser.getCobarDAO().getCobarList(clusterId, ConstantDefine.ACTIVE);
            }
        }

        if (null != cobarList && cobarList.size() > 0) {
            List<String> cobarNameList = new ArrayList<String>();
            Map<String, List<DataNodesStatus>> datanodeListMap = new HashMap<String, List<DataNodesStatus>>();
            int listLength = -1;//datanodeList length
            int maxListLength = -1;

            /* 获取每个cobar的数据节点列表，保存在Map<cobarName,List>中 */
            for (CobarDO cobar : cobarList) {
                CobarAdapterDAO control = cobarAccesser.getAccesser(cobar.getId());
                if (!control.checkConnection()) {
                    connectionFlag = false;
                    break;
                }
                List<DataNodesStatus> dList = control.listDataNodes();
                if (null != dList) {
                    hasDatanode = true;
                    ListSortUtil.sortDataNodesByPoolName(dList);
                    // first time to init listLength
                    if (listLength < 0) {
                        listLength = dList.size();
                    }
                    if (listLength != dList.size()) {
                        isUniform = false;
                    }
                    maxListLength = (maxListLength < dList.size()) ? dList.size() : maxListLength;
                } else {
                    if (listLength < 0) {
                        listLength = 0;
                    } else if (listLength > 0) {
                        isUniform = false;
                    }
                }

                cobarNameList.add(cobar.getName());
                datanodeListMap.put(cobar.getName(), dList);

            }

            /* all cobar has same datanodeList */
            if (connectionFlag && hasDatanode && isUniform) {
                dataList = new ArrayList<Map<String, Object>>();
                for (int i = 0; isUniform && i < listLength; i++) {
                    DataNodesStatus dnode = datanodeListMap.get(cobarNameList.get(0)).get(i);
                    int indexlength = dnode.getDataSource().split(",").length;
                    long recoveryTime = dnode.getRecoveryTime();
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("poolName", dnode.getPoolName());
                    map.put("dataSource", dnode.getDataSource());

                    boolean statusFlag = true;
                    StringBuilder sb = new StringBuilder();
                    StringBuilder time = new StringBuilder();
                    for (String name : cobarNameList) {
                        DataNodesStatus tmp = datanodeListMap.get(name).get(i);
                        if (0 != ListSortUtil.comparePoolName(tmp.getPoolName(), dnode.getPoolName())) {
                            isUniform = false;
                            break;
                        }
                        sb.append(CobarStringUtil.htmlEscapedString(name))
                          .append(":")
                          .append(tmp.getIndex())
                          .append(";");
                        time.append(CobarStringUtil.htmlEscapedString(name))
                            .append(":")
                            .append(tmp.getRecoveryTime())
                            .append(";");
                        if (tmp.getIndex() != dnode.getIndex()) {
                            statusFlag = false;
                        }
                        recoveryTime = (recoveryTime < tmp.getRecoveryTime()) ? tmp.getRecoveryTime() : recoveryTime;
                    }

                    if (recoveryTime == -1) {
                        map.put("recoveryTime", recoveryTime);
                    } else {
                        map.put("recoveryTime", FormatUtil.formatTime(recoveryTime * 1000, 2));
                    }

                    /* index = -1 说明节点不一致，否则为对应节点index； */
                    if (statusFlag) {
                        map.put("index", dnode.getIndex());
                    } else {
                        map.put("index", -1);
                    }
                    map.put("status", sb.toString());
                    map.put("indexlength", indexlength);
                    map.put("time", time);
                    dataList.add(map);
                }
                ListSortUtil.sortDataNodesMapByPoolName(dataList);
            }

            /*
             * all datanodeList is not the same O(3MN)
             */
            if (connectionFlag && !isUniform) {
                dataList = new ArrayList<Map<String, Object>>();
                boolean choose = false; // should choose for whatever reason
                boolean allsame = true; // if all cobar has the same pool
                boolean nullList = false;// if there is null dataList

                for (;;) {
                    // all list has finish
                    if (cobarNameList.size() <= 0) {
                        break;
                    }

                    // clear the null dataList
                    nullList = false;
                    for (String name : cobarNameList) {
                        List<DataNodesStatus> dList = datanodeListMap.get(name);
                        if (null == dList || dList.size() == 0) {
                            choose = true; // then ever datanode should be choosen
                            nullList = true; // one dataList is null
                            datanodeListMap.remove(name);//remove
                            cobarNameList.remove(name);//remove
                            break;
                        }
                    }
                    if (nullList) {
                        continue;
                    }

                    // get the mini datanode
                    DataNodesStatus dnode = null;
                    allsame = true;
                    for (String name : cobarNameList) {
                        DataNodesStatus tmp = datanodeListMap.get(name).get(0);
                        if (null == dnode) {
                            dnode = tmp;
                        } else if (ListSortUtil.comparePoolName(dnode.getPoolName(), tmp.getPoolName()) < 0) {
                            // dnode.poolname < tmp.poolname
                            allsame = false;
                        } else if (ListSortUtil.comparePoolName(dnode.getPoolName(), tmp.getPoolName()) > 0) {
                            // dnode.poolname > tmp.poolname
                            dnode = tmp;
                            allsame = false;
                        }
                    }

                    //remove allsame datanode
                    if (!choose && allsame) {
                        for (String name : cobarNameList) {
                            datanodeListMap.get(name).remove(0);
                        }
                        continue;
                    }

                    Map<String, Object> map = new HashMap<String, Object>();
                    StringBuilder sb = new StringBuilder();
                    StringBuilder time = new StringBuilder();

                    boolean statusFlag = true; // if every datanode index is the same
                    long recoveryTime = dnode.getRecoveryTime();

                    for (String name : cobarNameList) {
                        List<DataNodesStatus> dList = datanodeListMap.get(name);
                        DataNodesStatus tmp = dList.get(0); // get the first datanode
                        if (ListSortUtil.comparePoolName(dnode.getPoolName(), tmp.getPoolName()) < 0) {
                            continue;
                        } else {
                            sb.append(CobarStringUtil.htmlEscapedString(name))
                              .append(":")
                              .append(tmp.getIndex())
                              .append(";");
                            time.append(CobarStringUtil.htmlEscapedString(name))
                                .append(":")
                                .append(tmp.getRecoveryTime())
                                .append(";");
                            if (tmp.getIndex() != dnode.getIndex()) {
                                statusFlag = false;
                            }
                            recoveryTime =
                                    (recoveryTime < tmp.getRecoveryTime()) ? tmp.getRecoveryTime() : recoveryTime;
                            dList.remove(0);
                        }
                    }
                    if (dnode != null && (choose || !allsame)) {
                        map.put("poolName", dnode.getPoolName());
                        map.put("dataSource", dnode.getDataSource());
                        map.put("recoveryTime", recoveryTime);
                        if (statusFlag) {
                            map.put("index", dnode.getIndex());
                        } else {
                            map.put("index", -1);
                        }
                        map.put("status", sb.toString());
                        map.put("indexlength", -1);
                        map.put("time", time);
                        dataList.add(map);
                    }
                }
                ListSortUtil.sortDataNodesMapByPoolName(dataList);
            }
        }

        return new ModelAndView("c_datanodes", new FluenceHashMap<String, Object>().putKeyValue("cList", clusterList)
                                                                                   .putKeyValue("clusterId", clusterId)
                                                                                   .putKeyValue("user", user)
                                                                                   .putKeyValue("datanodes", dataList)
                                                                                   .putKeyValue("uniform", isUniform)
                                                                                   .putKeyValue("connecitonFlag",
                                                                                                connectionFlag));

    }
}
