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

import com.alibaba.cobar.parser.ast.expression.primary.Identifier;
import com.alibaba.cobar.parser.visitor.SQLASTVisitor;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class ShowIndex extends DALShowStatement {
    public static enum Type {
        INDEX,
        INDEXES,
        KEYS
    }

    private final Type type;
    private final Identifier table;

    public ShowIndex(Type type, Identifier table, Identifier database) {
        this.table = table;
        this.table.setParent(database);
        this.type = type;
    }

    public ShowIndex(Type type, Identifier table) {
        this.table = table;
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public Identifier getTable() {
        return table;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
