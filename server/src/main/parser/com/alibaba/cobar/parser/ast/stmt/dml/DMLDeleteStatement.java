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
 * (created at 2011-5-17)
 */
package com.alibaba.cobar.parser.ast.stmt.dml;

import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.primary.Identifier;
import com.alibaba.cobar.parser.ast.fragment.Limit;
import com.alibaba.cobar.parser.ast.fragment.OrderBy;
import com.alibaba.cobar.parser.ast.fragment.tableref.TableReferences;
import com.alibaba.cobar.parser.visitor.SQLASTVisitor;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class DMLDeleteStatement extends DMLStatement {
    private final boolean lowPriority;
    private final boolean quick;
    private final boolean ignore;
    /** tableName[.*] */
    private final List<Identifier> tableNames;
    private final TableReferences tableRefs;
    private final Expression whereCondition;
    private final OrderBy orderBy;
    private final Limit limit;

    // ------- single-row delete------------
    public DMLDeleteStatement(boolean lowPriority, boolean quick, boolean ignore, Identifier tableName)
            throws SQLSyntaxErrorException {
        this(lowPriority, quick, ignore, tableName, null, null, null);
    }

    public DMLDeleteStatement(boolean lowPriority, boolean quick, boolean ignore, Identifier tableName, Expression where)
            throws SQLSyntaxErrorException {
        this(lowPriority, quick, ignore, tableName, where, null, null);
    }

    public DMLDeleteStatement(boolean lowPriority, boolean quick, boolean ignore, Identifier tableName,
                              Expression where, OrderBy orderBy, Limit limit) throws SQLSyntaxErrorException {
        this.lowPriority = lowPriority;
        this.quick = quick;
        this.ignore = ignore;
        this.tableNames = new ArrayList<Identifier>(1);
        this.tableNames.add(tableName);
        this.tableRefs = null;
        this.whereCondition = where;
        this.orderBy = orderBy;
        this.limit = limit;
    }

    // ------- multi-row delete------------
    public DMLDeleteStatement(boolean lowPriority, boolean quick, boolean ignore, List<Identifier> tableNameList,
                              TableReferences tableRefs) throws SQLSyntaxErrorException {
        this(lowPriority, quick, ignore, tableNameList, tableRefs, null);
    }

    public DMLDeleteStatement(boolean lowPriority, boolean quick, boolean ignore, List<Identifier> tableNameList,
                              TableReferences tableRefs, Expression whereCondition) throws SQLSyntaxErrorException {
        this.lowPriority = lowPriority;
        this.quick = quick;
        this.ignore = ignore;
        if (tableNameList == null || tableNameList.isEmpty()) {
            throw new IllegalArgumentException("argument 'tableNameList' is empty");
        } else if (tableNameList instanceof ArrayList) {
            this.tableNames = tableNameList;
        } else {
            this.tableNames = new ArrayList<Identifier>(tableNameList);
        }
        if (tableRefs == null) {
            throw new IllegalArgumentException("argument 'tableRefs' is null");
        }
        this.tableRefs = tableRefs;
        this.whereCondition = whereCondition;
        this.orderBy = null;
        this.limit = null;
    }

    public List<Identifier> getTableNames() {
        return tableNames;
    }

    public TableReferences getTableRefs() {
        return tableRefs;
    }

    public Expression getWhereCondition() {
        return whereCondition;
    }

    public OrderBy getOrderBy() {
        return orderBy;
    }

    public Limit getLimit() {
        return limit;
    }

    public boolean isLowPriority() {
        return lowPriority;
    }

    public boolean isQuick() {
        return quick;
    }

    public boolean isIgnore() {
        return ignore;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
