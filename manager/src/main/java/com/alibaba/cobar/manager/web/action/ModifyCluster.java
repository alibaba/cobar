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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import com.alibaba.cobar.manager.dataobject.xml.ClusterDO;
import com.alibaba.cobar.manager.service.XmlAccesser;

/**
 * @author haiqing.zhuhq 2011-6-27
 */
public class ModifyCluster extends SimpleFormController implements InitializingBean {
    private XmlAccesser xmlAccesser;

    public void setXmlAccesser(XmlAccesser xmlAccesser) {
        this.xmlAccesser = xmlAccesser;
    }

    @SuppressWarnings("unused")
    private static class ClusterForm {

        private long clusterId;
        private String clusterName;
        private String deployName;
        private String maintName;
        private String deployDesc;
        private String onlineTime;
        private int sortId;

        public long getClusterId() {
            return clusterId;
        }

        public void setClusterId(long clusterId) {
            this.clusterId = clusterId;
        }

        public String getClusterName() {
            return clusterName;
        }

        public void setClusterName(String clusterName) {
            this.clusterName = clusterName;
        }

        public String getDeployName() {
            return deployName;
        }

        public void setDeployName(String deployName) {
            this.deployName = deployName;
        }

        public String getMaintName() {
            return maintName;
        }

        public void setMaintName(String maintName) {
            this.maintName = maintName;
        }

        public String getDeployDesc() {
            return deployDesc;
        }

        public void setDeployDesc(String deployDesc) {
            this.deployDesc = deployDesc;
        }

        public String getOnlineTime() {
            return onlineTime;
        }

        public void setOnlineTime(String onlineTime) {
            this.onlineTime = onlineTime;
        }

        public int getSortId() {
            return sortId;
        }

        public void setSortId(int sortId) {
            this.sortId = sortId;
        }

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        setCommandClass(ClusterForm.class);
        if (xmlAccesser == null) {
            throw new IllegalArgumentException("property 'xmlAccesser' is null!");
        }
    }

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request,
                                    HttpServletResponse response,
                                    Object command,
                                    BindException errors) throws Exception {
        ClusterForm form = (ClusterForm) command;
        ClusterDO cluster = new ClusterDO();
        cluster.setId(form.getClusterId());
        cluster.setDeployContact(form.getDeployName().trim());
        cluster.setDeployDesc(form.getDeployDesc().trim());
        cluster.setMaintContact(form.getMaintName().trim());
        cluster.setName(form.getClusterName().trim());
        cluster.setOnlineTime(form.getOnlineTime().trim());
        cluster.setSortId(form.getSortId());

        boolean flag = this.xmlAccesser.getClusterDAO().modifyCluster(cluster);

        if (flag) {
            return new ModelAndView("m_success", "info", "modify success");
        } else {
            String reason = form.getClusterName() + " exist";
            return new ModelAndView("failure", "reason", reason);
        }
    }
}
