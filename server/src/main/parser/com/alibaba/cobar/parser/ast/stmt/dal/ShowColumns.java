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
 * (created at 2011-5-20)
 */
package com.alibaba.cobar.parser.ast.stmt.dal;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.primary.Identifier;
import com.alibaba.cobar.parser.visitor.SQLASTVisitor;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class ShowColumns extends DALShowStatement {
    private final boolean full;
    private final Identifier table;
    private final String pattern;
    private final Expression where;

    public ShowColumns(boolean full, Identifier table, Identifier database, Expression where) {
        this.full = full;
        this.table = table;
        if (database != null) {
            this.table.setParent(database);
        }
        this.pattern = null;
        this.where = where;
    }

    public ShowColumns(boolean full, Identifier table, Identifier database, String pattern) {
        this.full = full;
        this.table = table;
        if (database != null) {
            this.table.setParent(database);
        }
        this.pattern = pattern;
        this.where = null;
    }

    public ShowColumns(boolean full, Identifier table, Identifier database) {
        this.full = full;
        this.table = table;
        if (database != null) {
            this.table.setParent(database);
        }
        this.pattern = null;
        this.where = null;
    }

    public boolean isFull() {
        return full;
    }

    public Identifier getTable() {
        return table;
    }

    public String getPattern() {
        return pattern;
    }

    public Expression getWhere() {
        return where;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
