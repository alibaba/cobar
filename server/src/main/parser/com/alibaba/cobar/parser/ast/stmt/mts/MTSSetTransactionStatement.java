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
 * (created at 2011-6-8)
 */
package com.alibaba.cobar.parser.ast.stmt.mts;

import com.alibaba.cobar.parser.ast.fragment.VariableScope;
import com.alibaba.cobar.parser.ast.stmt.SQLStatement;
import com.alibaba.cobar.parser.visitor.SQLASTVisitor;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class MTSSetTransactionStatement implements SQLStatement {
    public static enum IsolationLevel {
        READ_UNCOMMITTED,
        READ_COMMITTED,
        REPEATABLE_READ,
        SERIALIZABLE
    }

    private final VariableScope scope;
    private final IsolationLevel level;

    public MTSSetTransactionStatement(VariableScope scope, IsolationLevel level) {
        super();
        if (level == null)
            throw new IllegalArgumentException("isolation level is null");
        this.level = level;
        this.scope = scope;
    }

    /**
     * @retern null means scope undefined
     */
    public VariableScope getScope() {
        return scope;
    }

    public IsolationLevel getLevel() {
        return level;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
