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

 * (created at 2011-1-20)
 */
package com.alibaba.cobar.parser.ast.expression.type;

import java.util.Map;

import com.alibaba.cobar.parser.ast.expression.AbstractExpression;
import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.visitor.SQLASTVisitor;

/**
 * <code>higherExpr 'COLLATE' collateName</code>
 * 
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class CollateExpression extends AbstractExpression {
    private final String collateName;
    private final Expression string;

    public CollateExpression(Expression string, String collateName) {
        if (collateName == null)
            throw new IllegalArgumentException("collateName is null");
        this.string = string;
        this.collateName = collateName;
    }

    public String getCollateName() {
        return collateName;
    }

    public Expression getString() {
        return string;
    }

    @Override
    public int getPrecedence() {
        return PRECEDENCE_COLLATE;
    }

    @Override
    public Object evaluationInternal(Map<? extends Object, ? extends Object> parameters) {
        return string.evaluation(parameters);
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
