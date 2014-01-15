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

package com.alibaba.cobar.manager.test;

import java.util.List;

import com.alibaba.cobar.manager.dao.CobarAdapterDAO;
import com.alibaba.cobar.manager.dao.delegate.AdapterDelegate;
import com.alibaba.cobar.manager.dao.xml.ClusterDAOImple;
import com.alibaba.cobar.manager.dao.xml.CobarDAOImple;
import com.alibaba.cobar.manager.dao.xml.XMLFileLoaderPath;
import com.alibaba.cobar.manager.dataobject.cobarnode.ServerStatus;
import com.alibaba.cobar.manager.service.CobarAccesser;
import com.alibaba.cobar.manager.service.XmlAccesser;

/**
 * @author haiqing.zhuhq 2011-6-15
 */
public class CobarAccessTest {

    public static void main(String[] args) throws Exception {
        String xmlpath = "./src/main/resources/";
        CobarAccesser accesser = new CobarAccesser();
        XmlAccesser xmlAccesser = new XmlAccesser();
        XMLFileLoaderPath xmlFileLoader = new XMLFileLoaderPath();
        xmlFileLoader.setXmlPath(xmlpath);

        ClusterDAOImple cluster = new ClusterDAOImple();
        cluster.setXmlFileLoader(xmlFileLoader);
        cluster.afterPropertiesSet();

        CobarDAOImple cobar = new CobarDAOImple();
        cobar.setXmlFileLoader(xmlFileLoader);
        cobar.afterPropertiesSet();

        xmlAccesser.setClusterDAO(cluster);
        xmlAccesser.setCobarDAO(cobar);

        accesser.setXmlAccesser(xmlAccesser);

        AdapterDelegate res = new AdapterDelegate();
        accesser.setCobarAdapterDelegate(res);

        CobarAdapterDAO perf = accesser.getAccesser(1L);
        String version = perf.getVersion();
        System.out.println(version);

        ServerStatus ss = perf.getServerStatus();
        System.out.println(ss.getMaxMemory());
        System.out.println(ss.getStatus());
        System.out.println(ss.getTotalMemory());
        System.out.println(ss.getUptime());
        System.out.println(ss.getUsedMemory());
        System.out.println(ss.getReloadTime());
        System.out.println(ss.getRollbackTime());

        List<String> databases = perf.listDataBases();
        for (String s : databases) {
            System.out.println(s);
        }

        //        perf.reloadConfig();
        //        perf.rollbackConfig();

        //        int num = perf.switchDataNode("circe,cndb,dubbo,napoli", 0);
        //        System.out.println("num: " + num);

        //        int num = perf.stopHeartbeat("circe,cndb,dubbo,napoli", -10);
        //        System.out.println("num: " + num);

    }
}
