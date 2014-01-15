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
package com.alibaba.cobar.manager.response;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.alibaba.cobar.CobarCluster;
import com.alibaba.cobar.CobarConfig;
import com.alibaba.cobar.CobarServer;
import com.alibaba.cobar.ConfigInitializer;
import com.alibaba.cobar.config.ErrorCode;
import com.alibaba.cobar.config.model.DataSourceConfig;
import com.alibaba.cobar.config.model.QuarantineConfig;
import com.alibaba.cobar.config.model.SchemaConfig;
import com.alibaba.cobar.config.model.UserConfig;
import com.alibaba.cobar.manager.ManagerConnection;
import com.alibaba.cobar.mysql.MySQLDataNode;
import com.alibaba.cobar.mysql.MySQLDataSource;
import com.alibaba.cobar.net.mysql.OkPacket;

/**
 * @author xianmao.hexm
 */
public final class ReloadConfig {
    private static final Logger LOGGER = Logger.getLogger(ReloadConfig.class);

    public static void execute(ManagerConnection c) {
        final ReentrantLock lock = CobarServer.getInstance().getConfig().getLock();
        lock.lock();
        try {
            if (reload()) {
                StringBuilder s = new StringBuilder();
                s.append(c).append("Reload config success by manager");
                LOGGER.warn(s.toString());
                OkPacket ok = new OkPacket();
                ok.packetId = 1;
                ok.affectedRows = 1;
                ok.serverStatus = 2;
                ok.message = "Reload config success".getBytes();
                ok.write(c);
            } else {
                c.writeErrMessage(ErrorCode.ER_YES, "Reload config failure");
            }
        } finally {
            lock.unlock();
        }
    }

    private static boolean reload() {
        // 载入新的配置
        ConfigInitializer loader = new ConfigInitializer();
        Map<String, UserConfig> users = loader.getUsers();
        Map<String, SchemaConfig> schemas = loader.getSchemas();
        Map<String, MySQLDataNode> dataNodes = loader.getDataNodes();
        Map<String, DataSourceConfig> dataSources = loader.getDataSources();
        CobarCluster cluster = loader.getCluster();
        QuarantineConfig quarantine = loader.getQuarantine();

        // 应用新配置
        CobarConfig conf = CobarServer.getInstance().getConfig();

        // 如果重载已经存在的数据节点，初始化连接数参考空闲连接数，否则为1。
        boolean reloadStatus = true;
        Map<String, MySQLDataNode> cNodes = conf.getDataNodes();
        for (MySQLDataNode dn : dataNodes.values()) {
            MySQLDataNode cdn = cNodes.get(dn.getName());
            if (cdn != null && cdn.getSource() != null) {
                int size = Math.min(cdn.getSource().getIdleCount(), dn.getConfig().getPoolSize());
                dn.init(size > 0 ? size : 1, 0);
            } else {
                dn.init(1, 0);
            }
            if (!dn.isInitSuccess()) {
                reloadStatus = false;
                break;
            }
        }
        // 如果重载不成功，则清理已初始化的资源。
        if (!reloadStatus) {
            for (MySQLDataNode dn : dataNodes.values()) {
                MySQLDataSource ds = dn.getSource();
                if (ds != null) {
                    ds.clear();
                }
            }
            return false;
        }

        // 应用重载
        conf.reload(users, schemas, dataNodes, dataSources, cluster, quarantine);

        // 处理旧的资源
        for (MySQLDataNode dn : cNodes.values()) {
            MySQLDataSource ds = dn.getSource();
            if (ds != null) {
                ds.clear();
            }
        }

        return true;
    }

}
