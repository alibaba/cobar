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
 * (created at 2011-2-9)
 */
package com.alibaba.cobar.parser.ast.fragment.tableref;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.visitor.SQLASTVisitor;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class InnerJoin implements TableReference {
    private static List<String> ensureListType(List<String> list) {
        if (list == null)
            return null;
        if (list.isEmpty())
            return Collections.emptyList();
        if (list instanceof ArrayList)
            return list;
        return new ArrayList<String>(list);
    }

    private final TableReference leftTableRef;
    private final TableReference rightTableRef;
    private Expression onCond;
    private List<String> using;

    private InnerJoin(TableReference leftTableRef, TableReference rightTableRef, Expression onCond, List<String> using) {
        super();
        this.leftTableRef = leftTableRef;
        this.rightTableRef = rightTableRef;
        this.onCond = onCond;
        this.using = ensureListType(using);
    }

    public InnerJoin(TableReference leftTableRef, TableReference rightTableRef) {
        this(leftTableRef, rightTableRef, null, null);
    }

    public InnerJoin(TableReference leftTableRef, TableReference rightTableRef, Expression onCond) {
        this(leftTableRef, rightTableRef, onCond, null);
    }

    public InnerJoin(TableReference leftTableRef, TableReference rightTableRef, List<String> using) {
        this(leftTableRef, rightTableRef, null, using);
    }

    public TableReference getLeftTableRef() {
        return leftTableRef;
    }

    public TableReference getRightTableRef() {
        return rightTableRef;
    }

    public Expression getOnCond() {
        return onCond;
    }

    public List<String> getUsing() {
        return using;
    }

    @Override
    public Object removeLastConditionElement() {
        Object obj;
        if (onCond != null) {
            obj = onCond;
            onCond = null;
        } else if (using != null) {
            obj = using;
            using = null;
        } else {
            return null;
        }
        return obj;
    }

    @Override
    public boolean isSingleTable() {
        return false;
    }

    @Override
    public int getPrecedence() {
        return TableReference.PRECEDENCE_JOIN;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

}
