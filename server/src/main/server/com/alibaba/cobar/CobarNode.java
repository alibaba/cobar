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

import org.apache.log4j.Logger;

import com.alibaba.cobar.config.model.CobarNodeConfig;
import com.alibaba.cobar.heartbeat.CobarHeartbeat;

/**
 * @author xianmao.hexm
 */
public class CobarNode {
    private static final Logger LOGGER = Logger.getLogger(CobarNode.class);

    private final String name;
    private final CobarNodeConfig config;
    private final CobarHeartbeat heartbeat;

    public CobarNode(CobarNodeConfig config) {
        this.name = config.getName();
        this.config = config;
        this.heartbeat = new CobarHeartbeat(this);
    }

    public String getName() {
        return name;
    }

    public CobarNodeConfig getConfig() {
        return config;
    }

    public CobarHeartbeat getHeartbeat() {
        return heartbeat;
    }

    public void stopHeartbeat() {
        heartbeat.stop();
    }

    public void startHeartbeat() {
        heartbeat.start();
    }

    public void doHeartbeat() {
        if (!heartbeat.isStop()) {
            try {
                heartbeat.heartbeat();
            } catch (Throwable e) {
                LOGGER.error(name + " heartbeat error.", e);
            }
        }
    }

    public boolean isOnline() {
        return (heartbeat.getStatus() == CobarHeartbeat.OK_STATUS);
    }

}
