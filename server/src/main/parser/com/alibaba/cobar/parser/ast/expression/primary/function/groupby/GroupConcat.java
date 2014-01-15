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
 * (created at 2011-1-23)
 */
package com.alibaba.cobar.parser.ast.expression.primary.function.groupby;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.primary.function.FunctionExpression;
import com.alibaba.cobar.parser.visitor.SQLASTVisitor;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class GroupConcat extends FunctionExpression {
    private final boolean distinct;
    private final Expression orderBy;
    private final boolean isDesc;
    private final List<Expression> appendedColumnNames;
    private final String separator;

    public GroupConcat(boolean distinct, List<Expression> exprList, Expression orderBy, boolean isDesc,
                       List<Expression> appendedColumnNames, String separator) {
        super("GROUP_CONCAT", exprList);
        this.distinct = distinct;
        this.orderBy = orderBy;
        this.isDesc = isDesc;
        if (appendedColumnNames == null || appendedColumnNames.isEmpty()) {
            this.appendedColumnNames = Collections.emptyList();
        } else if (appendedColumnNames instanceof ArrayList) {
            this.appendedColumnNames = appendedColumnNames;
        } else {
            this.appendedColumnNames = new ArrayList<Expression>(appendedColumnNames);
        }
        this.separator = separator == null ? "," : separator;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public Expression getOrderBy() {
        return orderBy;
    }

    public boolean isDesc() {
        return isDesc;
    }

    public List<Expression> getAppendedColumnNames() {
        return appendedColumnNames;
    }

    public String getSeparator() {
        return separator;
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        throw new UnsupportedOperationException("function of char has special arguments");
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
