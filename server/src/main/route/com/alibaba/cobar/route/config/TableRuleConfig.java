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
 * (created at 2011-7-13)
 */
package com.alibaba.cobar.route.config;

import com.alibaba.cobar.parser.ast.expression.Expression;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public final class TableRuleConfig {

    private final String name;
    private final RuleConfig[] rules;

    public TableRuleConfig(String name, RuleConfig[] rules) {
        this.name = name;
        this.rules = rules;
        if (rules != null) {
            for (RuleConfig r : rules) {
                r.tableRuleName = name;
            }
        }
    }

    public String getName() {
        return name;
    }

    public RuleConfig[] getRules() {
        return rules;
    }

    public static final class RuleConfig {
        private String tableRuleName;
        /** upper-case */
        private final String[] columns;
        private final Expression algorithm;

        public RuleConfig(String[] columns, Expression algorithm) {
            this.columns = columns == null ? new String[0] : columns;
            this.algorithm = algorithm;
        }

        public String[] getColumns() {
            return columns;
        }

        public Expression getAlgorithm() {
            return algorithm;
        }

        @Override
        public String toString() {
            StringBuilder s = new StringBuilder();
            s.append("{tableRule:").append(tableRuleName).append(", columns:[");
            for (int i = 0; i < columns.length; ++i) {
                if (i > 0)
                    s.append(", ");
                s.append(columns[i]);
            }
            s.append("]}");
            return s.toString();
        }
    }

}
