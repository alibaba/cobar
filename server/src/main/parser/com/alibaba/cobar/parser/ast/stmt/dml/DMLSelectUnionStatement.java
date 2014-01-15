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
 * (created at 2011-1-29)
 */
package com.alibaba.cobar.parser.ast.stmt.dml;

import java.util.LinkedList;
import java.util.List;

import com.alibaba.cobar.parser.ast.fragment.Limit;
import com.alibaba.cobar.parser.ast.fragment.OrderBy;
import com.alibaba.cobar.parser.visitor.SQLASTVisitor;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class DMLSelectUnionStatement extends DMLQueryStatement {
    /** might be {@link LinkedList} */
    private final List<DMLSelectStatement> selectStmtList;
    /**
     * <code>Mixed UNION types are treated such that a DISTINCT union overrides any ALL union to its left</code>
     * <br/>
     * 0 means all relations of selects are union all<br/>
     * last index of {@link #selectStmtList} means all relations of selects are
     * union distinct<br/>
     */
    private int firstDistinctIndex = 0;
    private OrderBy orderBy;
    private Limit limit;

    public DMLSelectUnionStatement(DMLSelectStatement select) {
        super();
        this.selectStmtList = new LinkedList<DMLSelectStatement>();
        this.selectStmtList.add(select);
    }

    public DMLSelectUnionStatement addSelect(DMLSelectStatement select, boolean unionAll) {
        selectStmtList.add(select);
        if (!unionAll) {
            firstDistinctIndex = selectStmtList.size() - 1;
        }
        return this;
    }

    public DMLSelectUnionStatement setOrderBy(OrderBy orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    public DMLSelectUnionStatement setLimit(Limit limit) {
        this.limit = limit;
        return this;
    }

    public List<DMLSelectStatement> getSelectStmtList() {
        return selectStmtList;
    }

    public int getFirstDistinctIndex() {
        return firstDistinctIndex;
    }

    public OrderBy getOrderBy() {
        return orderBy;
    }

    public Limit getLimit() {
        return limit;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
