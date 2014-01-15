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
 * (created at 2011-7-27)
 */
package com.alibaba.cobar.parser.ast.fragment.tableref;

import com.alibaba.cobar.parser.ast.expression.primary.Identifier;
import com.alibaba.cobar.parser.ast.expression.primary.literal.LiteralString;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public abstract class AliasableTableReference implements TableReference {
    protected final String alias;
    protected String aliasUpEscape;

    public AliasableTableReference(String alias) {
        this.alias = alias;
    }

    /**
     * @return upper-case, empty is possible
     */
    public String getAliasUnescapeUppercase() {
        if (alias == null || alias.length() <= 0)
            return alias;
        if (aliasUpEscape != null)
            return aliasUpEscape;

        switch (alias.charAt(0)) {
        case '`':
            return aliasUpEscape = Identifier.unescapeName(alias, true);
        case '\'':
            return aliasUpEscape = LiteralString.getUnescapedString(alias.substring(1, alias.length() - 1), true);
        case '_':
            int ind = -1;
            for (int i = 1; i < alias.length(); ++i) {
                if (alias.charAt(i) == '\'') {
                    ind = i;
                    break;
                }
            }
            if (ind >= 0) {
                LiteralString st = new LiteralString(alias.substring(0, ind), alias.substring(
                        ind + 1,
                        alias.length() - 1), false);
                return aliasUpEscape = st.getUnescapedString(true);
            }
        default:
            return aliasUpEscape = alias.toUpperCase();
        }
    }

    public String getAlias() {
        return alias;
    }
}
