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
 * (created at 2011-5-21)
 */
package com.alibaba.cobar.parser.ast.stmt.dal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.fragment.Limit;
import com.alibaba.cobar.parser.visitor.SQLASTVisitor;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class ShowProfile extends DALShowStatement {
    /** enum name must equals to real sql while ' ' is replaced with '_' */
    public static enum Type {
        ALL,
        BLOCK_IO,
        CONTEXT_SWITCHES,
        CPU,
        IPC,
        MEMORY,
        PAGE_FAULTS,
        SOURCE,
        SWAPS
    }

    private final List<Type> types;
    private final Expression forQuery;
    private final Limit limit;

    public ShowProfile(List<Type> types, Expression forQuery, Limit limit) {
        if (types == null || types.isEmpty()) {
            this.types = Collections.emptyList();
        } else if (types instanceof ArrayList) {
            this.types = types;
        } else {
            this.types = new ArrayList<ShowProfile.Type>(types);
        }
        this.forQuery = forQuery;
        this.limit = limit;
    }

    /**
     * @return never null
     */
    public List<Type> getTypes() {
        return types;
    }

    public Expression getForQuery() {
        return forQuery;
    }

    public Limit getLimit() {
        return limit;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
