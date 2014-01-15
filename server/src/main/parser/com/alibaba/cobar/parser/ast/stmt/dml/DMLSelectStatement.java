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
 * (created at 2011-1-28)
 */
package com.alibaba.cobar.parser.ast.stmt.dml;

import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.fragment.GroupBy;
import com.alibaba.cobar.parser.ast.fragment.Limit;
import com.alibaba.cobar.parser.ast.fragment.OrderBy;
import com.alibaba.cobar.parser.ast.fragment.tableref.TableReferences;
import com.alibaba.cobar.parser.util.Pair;
import com.alibaba.cobar.parser.visitor.SQLASTVisitor;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class DMLSelectStatement extends DMLQueryStatement {
    public static enum SelectDuplicationStrategy {
        /** default */
        ALL,
        DISTINCT,
        DISTINCTROW
    }

    public static enum QueryCacheStrategy {
        UNDEF,
        SQL_CACHE,
        SQL_NO_CACHE
    }

    public static enum SmallOrBigResult {
        UNDEF,
        SQL_SMALL_RESULT,
        SQL_BIG_RESULT
    }

    public static enum LockMode {
        UNDEF,
        FOR_UPDATE,
        LOCK_IN_SHARE_MODE
    }

    public static final class SelectOption {
        public SelectDuplicationStrategy resultDup = SelectDuplicationStrategy.ALL;
        public boolean highPriority = false;
        public boolean straightJoin = false;
        public SmallOrBigResult resultSize = SmallOrBigResult.UNDEF;
        public boolean sqlBufferResult = false;
        public QueryCacheStrategy queryCache = QueryCacheStrategy.UNDEF;
        public boolean sqlCalcFoundRows = false;
        public LockMode lockMode = LockMode.UNDEF;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(getClass().getSimpleName()).append('{');
            sb.append("resultDup").append('=').append(resultDup.name());
            sb.append(", ").append("highPriority").append('=').append(highPriority);
            sb.append(", ").append("straightJoin").append('=').append(straightJoin);
            sb.append(", ").append("resultSize").append('=').append(resultSize.name());
            sb.append(", ").append("sqlBufferResult").append('=').append(sqlBufferResult);
            sb.append(", ").append("queryCache").append('=').append(queryCache.name());
            sb.append(", ").append("sqlCalcFoundRows").append('=').append(sqlCalcFoundRows);
            sb.append(", ").append("lockMode").append('=').append(lockMode.name());
            sb.append('}');
            return sb.toString();
        }
    }

    private final SelectOption option;
    /** string: id | `id` | 'id' */
    private final List<Pair<Expression, String>> selectExprList;
    private final TableReferences tables;
    private final Expression where;
    private final GroupBy group;
    private final Expression having;
    private final OrderBy order;
    private final Limit limit;

    /**
     * @throws SQLSyntaxErrorException
     */
    @SuppressWarnings("unchecked")
    public DMLSelectStatement(SelectOption option, List<Pair<Expression, String>> selectExprList,
                              TableReferences tables, Expression where, GroupBy group, Expression having,
                              OrderBy order, Limit limit) {
        if (option == null)
            throw new IllegalArgumentException("argument 'option' is null");
        this.option = option;
        if (selectExprList == null || selectExprList.isEmpty()) {
            this.selectExprList = Collections.emptyList();
        } else {
            this.selectExprList = ensureListType(selectExprList);
        }
        this.tables = tables;
        this.where = where;
        this.group = group;
        this.having = having;
        this.order = order;
        this.limit = limit;
    }

    public SelectOption getOption() {
        return option;
    }

    /**
     * @return never null
     */
    public List<Pair<Expression, String>> getSelectExprList() {
        return selectExprList;
    }

    /** @performance slow */
    public List<Expression> getSelectExprListWithoutAlias() {
        if (selectExprList == null || selectExprList.isEmpty())
            return Collections.emptyList();
        List<Expression> list = new ArrayList<Expression>(selectExprList.size());
        for (Pair<Expression, String> p : selectExprList) {
            if (p != null && p.getKey() != null) {
                list.add(p.getKey());
            }
        }
        return list;
    }

    public TableReferences getTables() {
        return tables;
    }

    public Expression getWhere() {
        return where;
    }

    public GroupBy getGroup() {
        return group;
    }

    public Expression getHaving() {
        return having;
    }

    public OrderBy getOrder() {
        return order;
    }

    public Limit getLimit() {
        return limit;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
