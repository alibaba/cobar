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
package com.alibaba.cobar.parser.ast.expression.primary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.visitor.SQLASTVisitor;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class RowExpression extends PrimaryExpression {
    private final List<Expression> rowExprList;

    public RowExpression(List<Expression> rowExprList) {
        if (rowExprList == null || rowExprList.isEmpty()) {
            this.rowExprList = Collections.emptyList();
        } else if (rowExprList instanceof ArrayList) {
            this.rowExprList = rowExprList;
        } else {
            this.rowExprList = new ArrayList<Expression>(rowExprList);
        }
    }

    /**
     * @return never null
     */
    public List<Expression> getRowExprList() {
        return rowExprList;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
