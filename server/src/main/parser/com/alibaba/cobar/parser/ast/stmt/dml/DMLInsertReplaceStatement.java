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
 * (created at 2011-7-31)
 */
package com.alibaba.cobar.parser.ast.stmt.dml;

import java.util.List;

import com.alibaba.cobar.parser.ast.expression.misc.QueryExpression;
import com.alibaba.cobar.parser.ast.expression.primary.Identifier;
import com.alibaba.cobar.parser.ast.expression.primary.RowExpression;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public abstract class DMLInsertReplaceStatement extends DMLStatement {
    protected final Identifier table;
    protected final List<Identifier> columnNameList;
    protected List<RowExpression> rowList;
    protected final QueryExpression select;

    @SuppressWarnings("unchecked")
    public DMLInsertReplaceStatement(Identifier table, List<Identifier> columnNameList, List<RowExpression> rowList) {
        this.table = table;
        this.columnNameList = ensureListType(columnNameList);
        this.rowList = ensureListType(rowList);
        this.select = null;
    }

    @SuppressWarnings("unchecked")
    public DMLInsertReplaceStatement(Identifier table, List<Identifier> columnNameList, QueryExpression select) {
        if (select == null)
            throw new IllegalArgumentException("argument 'select' is empty");
        this.select = select;
        this.table = table;
        this.columnNameList = ensureListType(columnNameList);
        this.rowList = null;
    }

    public Identifier getTable() {
        return table;
    }

    /**
     * @return {@link java.util.ArrayList ArrayList}
     */
    public List<Identifier> getColumnNameList() {
        return columnNameList;
    }

    /**
     * @return {@link java.util.ArrayList ArrayList} or
     *         {@link java.util.Collections#emptyList() EMPTY_LIST}
     */
    public List<RowExpression> getRowList() {
        return rowList;
    }

    public QueryExpression getSelect() {
        return select;
    }

    private List<RowExpression> rowListBak;

    public void setReplaceRowList(List<RowExpression> list) {
        rowListBak = rowList;
        rowList = list;
    }

    public void clearReplaceRowList() {
        if (rowListBak != null) {
            rowList = rowListBak;
            rowListBak = null;
        }
    }

}
