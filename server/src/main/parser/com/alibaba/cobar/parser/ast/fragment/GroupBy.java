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
 * (created at 2011-5-10)
 */
package com.alibaba.cobar.parser.ast.fragment;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.alibaba.cobar.parser.ast.ASTNode;
import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.util.Pair;
import com.alibaba.cobar.parser.visitor.SQLASTVisitor;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class GroupBy implements ASTNode {
    /** might be {@link LinkedList} */
    private final List<Pair<Expression, SortOrder>> orderByList;
    private boolean withRollup = false;

    public boolean isWithRollup() {
        return withRollup;
    }

    /**
     * @return never null
     */
    public List<Pair<Expression, SortOrder>> getOrderByList() {
        return orderByList;
    }

    /**
     * performance tip: expect to have only 1 order item
     */
    public GroupBy(Expression expr, SortOrder order, boolean withRollup) {
        this.orderByList = new ArrayList<Pair<Expression, SortOrder>>(1);
        this.orderByList.add(new Pair<Expression, SortOrder>(expr, order));
        this.withRollup = withRollup;
    }

    /**
     * performance tip: linked list is used
     */
    public GroupBy() {
        this.orderByList = new LinkedList<Pair<Expression, SortOrder>>();
    }

    public GroupBy setWithRollup() {
        withRollup = true;
        return this;
    }

    public GroupBy addOrderByItem(Expression expr, SortOrder order) {
        orderByList.add(new Pair<Expression, SortOrder>(expr, order));
        return this;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
