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
package com.alibaba.cobar;

import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.alibaba.cobar.config.Alarms;
import com.alibaba.cobar.config.model.UserConfig;
import com.alibaba.cobar.net.handler.FrontendPrivileges;

/**
 * @author xianmao.hexm
 */
public class CobarPrivileges implements FrontendPrivileges {
    private static final Logger ALARM = Logger.getLogger("alarm");

    @Override
    public boolean schemaExists(String schema) {
        CobarConfig conf = CobarServer.getInstance().getConfig();
        return conf.getSchemas().containsKey(schema);
    }

    @Override
    public boolean userExists(String user, String host) {
        CobarConfig conf = CobarServer.getInstance().getConfig();
        Map<String, Set<String>> quarantineHosts = conf.getQuarantine().getHosts();
        if (quarantineHosts.containsKey(host)) {
            boolean rs = quarantineHosts.get(host).contains(user);
            if (!rs) {
                ALARM.error(new StringBuilder().append(Alarms.QUARANTINE_ATTACK)
                                               .append("[host=")
                                               .append(host)
                                               .append(",user=")
                                               .append(user)
                                               .append(']')
                                               .toString());
            }
            return rs;
        } else {
            if (user != null && user.equals(conf.getSystem().getClusterHeartbeatUser())) {
                return true;
            } else {
                return conf.getUsers().containsKey(user);
            }
        }
    }

    @Override
    public String getPassword(String user) {
        CobarConfig conf = CobarServer.getInstance().getConfig();
        if (user != null && user.equals(conf.getSystem().getClusterHeartbeatUser())) {
            return conf.getSystem().getClusterHeartbeatPass();
        } else {
            UserConfig uc = conf.getUsers().get(user);
            if (uc != null) {
                return uc.getPassword();
            } else {
                return null;
            }
        }
    }

    @Override
    public Set<String> getUserSchemas(String user) {
        CobarConfig conf = CobarServer.getInstance().getConfig();
        UserConfig uc = conf.getUsers().get(user);
        if (uc != null) {
            return uc.getSchemas();
        } else {
            return null;
        }
    }

}
