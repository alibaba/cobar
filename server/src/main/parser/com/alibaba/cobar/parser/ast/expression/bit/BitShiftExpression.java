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
package com.alibaba.cobar.parser.ast.expression.bit;

import com.alibaba.cobar.parser.ast.expression.BinaryOperatorExpression;
import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.visitor.SQLASTVisitor;

/**
 * <code>higherExpr ( ('&lt;&lt;'|'&gt;&gt;') higherExpr )+</code>
 * 
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class BitShiftExpression extends BinaryOperatorExpression {
    private final boolean negative;

    /**
     * @param negative true if right shift
     */
    public BitShiftExpression(boolean negative, Expression leftOprand, Expression rightOprand) {
        super(leftOprand, rightOprand, PRECEDENCE_BIT_SHIFT);
        this.negative = negative;
    }

    public boolean isRightShift() {
        return negative;
    }

    @Override
    public String getOperator() {
        return negative ? ">>" : "<<";
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
