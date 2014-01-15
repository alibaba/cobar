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
 * (created at 2012-6-12)
 */
package com.alibaba.cobar.config.model.rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class RuleConfig {
    private final List<String> columns;
    private final String algorithm;
    private RuleAlgorithm ruleAlgorithm;

    public RuleConfig(String[] columns, String algorithm) {
        if (algorithm == null) {
            throw new IllegalArgumentException("algorithm is null");
        }
        this.algorithm = algorithm;
        if (columns == null || columns.length <= 0) {
            throw new IllegalArgumentException("no rule column is found");
        }
        List<String> list = new ArrayList<String>(columns.length);
        for (String column : columns) {
            if (column == null) {
                throw new IllegalArgumentException("column value is null: " + columns);
            }
            list.add(column.toUpperCase());
        }
        this.columns = Collections.unmodifiableList(list);
    }

    public RuleAlgorithm getRuleAlgorithm() {
        return ruleAlgorithm;
    }

    public void setRuleAlgorithm(RuleAlgorithm ruleAlgorithm) {
        this.ruleAlgorithm = ruleAlgorithm;
    }

    /**
     * @return unmodifiable, upper-case
     */
    public List<String> getColumns() {
        return columns;
    }

    /**
     * @return never null
     */
    public String getAlgorithm() {
        return algorithm;
    }

}
