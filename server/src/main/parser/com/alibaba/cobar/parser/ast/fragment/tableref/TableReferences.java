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
 * (created at 2011-1-25)
 */
package com.alibaba.cobar.parser.ast.fragment.tableref;

import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.cobar.parser.visitor.SQLASTVisitor;

/**
 * used in <code>FROM</code> fragment
 * 
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class TableReferences implements TableReference {
    protected static List<TableReference> ensureListType(List<TableReference> list) {
        if (list instanceof ArrayList)
            return list;
        return new ArrayList<TableReference>(list);
    }

    private final List<TableReference> list;

    /**
     * @return never null
     */
    public List<TableReference> getTableReferenceList() {
        return list;
    }

    public TableReferences(List<TableReference> list) throws SQLSyntaxErrorException {
        if (list == null || list.isEmpty()) {
            throw new SQLSyntaxErrorException("at least one table reference");
        }
        this.list = ensureListType(list);
    }

    @Override
    public Object removeLastConditionElement() {
        if (list != null && !list.isEmpty()) {
            return list.get(list.size() - 1).removeLastConditionElement();
        }
        return null;
    }

    @Override
    public boolean isSingleTable() {
        if (list == null) {
            return false;
        }
        int count = 0;
        TableReference first = null;
        for (TableReference ref : list) {
            if (ref != null && 1 == ++count) {
                first = ref;
            }
        }
        return count == 1 && first.isSingleTable();
    }

    @Override
    public int getPrecedence() {
        return TableReference.PRECEDENCE_REFS;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

}
