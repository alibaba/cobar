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
 * (created at 2011-7-20)
 */
package com.alibaba.cobar.parser.ast.expression.arithmeic;

import java.util.Map;

import com.alibaba.cobar.parser.ast.expression.BinaryOperatorExpression;
import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.util.BinaryOperandCalculator;
import com.alibaba.cobar.parser.util.ExprEvalUtils;
import com.alibaba.cobar.parser.util.Pair;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public abstract class ArithmeticBinaryOperatorExpression extends BinaryOperatorExpression
        implements BinaryOperandCalculator {
    protected ArithmeticBinaryOperatorExpression(Expression leftOprand, Expression rightOprand, int precedence) {
        super(leftOprand, rightOprand, precedence, true);
    }

    @Override
    public Object evaluationInternal(Map<? extends Object, ? extends Object> parameters) {
        Object left = leftOprand.evaluation(parameters);
        Object right = rightOprand.evaluation(parameters);
        if (left == null || right == null)
            return null;
        if (left == UNEVALUATABLE || right == UNEVALUATABLE)
            return UNEVALUATABLE;
        Pair<Number, Number> pair = ExprEvalUtils.convertNum2SameLevel(left, right);
        return ExprEvalUtils.calculate(this, pair.getKey(), pair.getValue());
    }

}
