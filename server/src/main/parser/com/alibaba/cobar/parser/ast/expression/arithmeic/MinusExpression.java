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
package com.alibaba.cobar.parser.ast.expression.arithmeic;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.UnaryOperatorExpression;
import com.alibaba.cobar.parser.util.ExprEvalUtils;
import com.alibaba.cobar.parser.util.UnaryOperandCalculator;

/**
 * <code>'-' higherExpr</code>
 * 
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class MinusExpression extends UnaryOperatorExpression implements UnaryOperandCalculator {
    public MinusExpression(Expression operand) {
        super(operand, PRECEDENCE_UNARY_OP);
    }

    @Override
    public String getOperator() {
        return "-";
    }

    @Override
    public Object evaluationInternal(Map<? extends Object, ? extends Object> parameters) {
        Object operand = getOperand().evaluation(parameters);
        if (operand == null)
            return null;
        if (operand == UNEVALUATABLE)
            return UNEVALUATABLE;
        Number num = null;
        if (operand instanceof String) {
            num = ExprEvalUtils.string2Number((String) operand);
        } else {
            num = (Number) operand;
        }
        return ExprEvalUtils.calculate(this, num);
    }

    @Override
    public Number calculate(Integer num) {
        if (num == null)
            return null;
        int n = num.intValue();
        if (n == Integer.MIN_VALUE) {
            return new Long(-(long) n);
        }
        return new Integer(-n);
    }

    @Override
    public Number calculate(Long num) {
        if (num == null)
            return null;
        long n = num.longValue();
        if (n == Long.MIN_VALUE) {
            return new Long(-(long) n);
        }
        return new Long(-n);
    }

    @Override
    public Number calculate(BigInteger num) {
        if (num == null)
            return null;
        return num.negate();
    }

    @Override
    public Number calculate(BigDecimal num) {
        if (num == null)
            return null;
        return num.negate();
    }
}
