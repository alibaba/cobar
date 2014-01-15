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
 * (created at 2012-8-13)
 */
package com.alibaba.cobar.parser.ast.fragment.ddl.index;

import com.alibaba.cobar.parser.ast.ASTNode;
import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.primary.Identifier;
import com.alibaba.cobar.parser.visitor.SQLASTVisitor;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class IndexColumnName implements ASTNode {
    private final Identifier columnName;
    /** null is possible */
    private final Expression length;
    private final boolean asc;

    public IndexColumnName(Identifier columnName, Expression length, boolean asc) {
        this.columnName = columnName;
        this.length = length;
        this.asc = asc;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

    public Identifier getColumnName() {
        return columnName;
    }

    public Expression getLength() {
        return length;
    }

    public boolean isAsc() {
        return asc;
    }

}
