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
 * (created at 2011-1-19)
 */
package com.alibaba.cobar.parser.ast.expression.comparison;

import java.util.Map;

import com.alibaba.cobar.parser.ast.expression.AbstractExpression;
import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.ReplacableExpression;
import com.alibaba.cobar.parser.visitor.SQLASTVisitor;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class ComparisionIsExpression extends AbstractExpression implements ReplacableExpression {
    public static final int IS_NULL = 1;
    public static final int IS_TRUE = 2;
    public static final int IS_FALSE = 3;
    public static final int IS_UNKNOWN = 4;
    public static final int IS_NOT_NULL = 5;
    public static final int IS_NOT_TRUE = 6;
    public static final int IS_NOT_FALSE = 7;
    public static final int IS_NOT_UNKNOWN = 8;

    private final int mode;
    private final Expression operand;

    /**
     * @param mode {@link #IS_NULL} or {@link #IS_TRUE} or {@link #IS_FALSE} or
     *            {@link #IS_UNKNOWN} or {@link #IS_NOT_NULL} or
     *            {@link #IS_NOT_TRUE} or {@link #IS_NOT_FALSE} or
     *            {@link #IS_NOT_UNKNOWN}
     */
    public ComparisionIsExpression(Expression operand, int mode) {
        this.operand = operand;
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }

    public Expression getOperand() {
        return operand;
    }

    @Override
    public int getPrecedence() {
        return PRECEDENCE_COMPARISION;
    }

    @Override
    public Object evaluationInternal(Map<? extends Object, ? extends Object> parameters) {
        return UNEVALUATABLE;
    }

    private Expression replaceExpr;

    @Override
    public void setReplaceExpr(Expression replaceExpr) {
        this.replaceExpr = replaceExpr;
    }

    @Override
    public void clearReplaceExpr() {
        this.replaceExpr = null;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        if (replaceExpr == null)
            visitor.visit(this);
        else
            replaceExpr.accept(visitor);
    }
}
