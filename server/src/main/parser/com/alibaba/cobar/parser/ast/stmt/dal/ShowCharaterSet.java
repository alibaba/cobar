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
import com.alibaba.cobar.parser.visitor.SQLASTVisitor;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class ShowCharaterSet extends DALShowStatement {
    private final String pattern;
    private final Expression where;

    public ShowCharaterSet(String pattern) {
        this.pattern = pattern;
        this.where = null;
    }

    public String getPattern() {
        return pattern;
    }

    public Expression getWhere() {
        return where;
    }

    public ShowCharaterSet(Expression where) {
        this.pattern = null;
        this.where = where;
    }

    public ShowCharaterSet() {
        this.pattern = null;
        this.where = null;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
