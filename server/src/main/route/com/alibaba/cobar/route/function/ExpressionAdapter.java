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
 * (created at 2012-6-19)
 */
package com.alibaba.cobar.route.function;

import java.util.Map;

import com.alibaba.cobar.config.model.rule.RuleAlgorithm;
import com.alibaba.cobar.parser.ast.expression.Expression;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class ExpressionAdapter implements RuleAlgorithm {
    private final Expression expr;

    public ExpressionAdapter(Expression expr) {
        this.expr = expr;
    }

    @Override
    public RuleAlgorithm constructMe(Object... objects) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void initialize() {
    }

    @Override
    public Integer[] calculate(Map<? extends Object, ? extends Object> parameters) {
        Integer[] rst;
        Object eval = expr.evaluation(parameters);
        if (eval instanceof Integer) {
            rst = new Integer[1];
            rst[0] = (Integer) eval;
        } else if (eval instanceof Integer[]) {
            rst = (Integer[]) eval;
        } else if (eval instanceof Number) {
            rst = new Integer[1];
            rst[0] = ((Number) eval).intValue();
        } else if (eval instanceof String) {
            rst = new Integer[1];
            rst[0] = new Integer(((String) eval));
        } else if (eval instanceof int[]) {
            int[] ints = (int[]) eval;
            rst = new Integer[ints.length];
            for (int i = 0, len = ints.length; i < len; ++i) {
                rst[0] = ints[i];
            }
        } else if (eval instanceof Number[]) {
            Number[] longs = (Number[]) eval;
            rst = new Integer[longs.length];
            for (int i = 0, len = longs.length; i < len; ++i) {
                rst[0] = new Integer(longs[i].intValue());
            }
        } else if (eval instanceof long[]) {
            long[] longs = (long[]) eval;
            rst = new Integer[longs.length];
            for (int i = 0, len = longs.length; i < len; ++i) {
                rst[0] = new Integer((int) longs[i]);
            }
        } else {
            throw new IllegalArgumentException("rule calculate err: result of route function is wrong type or null: "
                    + eval);
        }
        return rst;
    }

}
