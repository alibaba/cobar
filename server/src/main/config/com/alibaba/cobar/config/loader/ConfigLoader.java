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
/**
 * (created at 2012-6-13)
 */
package com.alibaba.cobar.config.loader;

import java.util.Map;
import java.util.Set;

import com.alibaba.cobar.config.model.ClusterConfig;
import com.alibaba.cobar.config.model.DataNodeConfig;
import com.alibaba.cobar.config.model.DataSourceConfig;
import com.alibaba.cobar.config.model.QuarantineConfig;
import com.alibaba.cobar.config.model.SchemaConfig;
import com.alibaba.cobar.config.model.SystemConfig;
import com.alibaba.cobar.config.model.UserConfig;
import com.alibaba.cobar.config.model.rule.RuleAlgorithm;
import com.alibaba.cobar.config.model.rule.RuleConfig;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public interface ConfigLoader {
    Map<String, RuleAlgorithm> getRuleFunction();

    Set<RuleConfig> listRuleConfig();

    SchemaConfig getSchemaConfig(String schema);

    Map<String, SchemaConfig> getSchemaConfigs();

    Map<String, DataNodeConfig> getDataNodes();

    Map<String, DataSourceConfig> getDataSources();

    SystemConfig getSystemConfig();

    UserConfig getUserConfig(String user);

    Map<String, UserConfig> getUserConfigs();

    QuarantineConfig getQuarantineConfig();

    ClusterConfig getClusterConfig();
}
