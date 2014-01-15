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
package com.alibaba.cobar.config.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.alibaba.cobar.config.model.rule.RuleConfig;
import com.alibaba.cobar.config.model.rule.TableRuleConfig;
import com.alibaba.cobar.util.SplitUtil;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class TableConfig {
    private final String name;
    private final String[] dataNodes;
    private final TableRuleConfig rule;
    private final Set<String> columnIndex;
    private final boolean ruleRequired;

    public TableConfig(String name, String dataNode, TableRuleConfig rule, boolean ruleRequired) {
        if (name == null) {
            throw new IllegalArgumentException("table name is null");
        }
        this.name = name.toUpperCase();
        this.dataNodes = SplitUtil.split(dataNode, ',', '$', '-', '[', ']');
        if (this.dataNodes == null || this.dataNodes.length <= 0) {
            throw new IllegalArgumentException("invalid table dataNodes: " + dataNode);
        }
        this.rule = rule;
        this.columnIndex = buildColumnIndex(rule);
        this.ruleRequired = ruleRequired;
    }

    public boolean existsColumn(String columnNameUp) {
        return columnIndex.contains(columnNameUp);
    }

    /**
     * @return upper-case
     */
    public String getName() {
        return name;
    }

    public String[] getDataNodes() {
        return dataNodes;
    }

    public boolean isRuleRequired() {
        return ruleRequired;
    }

    public TableRuleConfig getRule() {
        return rule;
    }

    private static Set<String> buildColumnIndex(TableRuleConfig rule) {
        if (rule == null) {
            return Collections.emptySet();
        }
        List<RuleConfig> rs = rule.getRules();
        if (rs == null || rs.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> columnIndex = new HashSet<String>();
        for (RuleConfig r : rs) {
            List<String> columns = r.getColumns();
            if (columns != null) {
                for (String col : columns) {
                    if (col != null) {
                        columnIndex.add(col.toUpperCase());
                    }
                }
            }
        }
        return columnIndex;
    }

}
