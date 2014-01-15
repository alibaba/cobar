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
 * (created at 2011-7-19)
 */
package com.alibaba.cobar.route.function;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.cobar.config.model.rule.RuleAlgorithm;
import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.primary.function.FunctionExpression;
import com.alibaba.cobar.parser.util.Pair;
import com.alibaba.cobar.parser.util.PairUtil;
import com.alibaba.cobar.util.StringUtil;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public final class PartitionByString extends PartitionFunction implements RuleAlgorithm {
    public PartitionByString(String functionName) {
        this(functionName, null);
    }

    public PartitionByString(String functionName, List<Expression> arguments) {
        super(functionName, arguments);
    }

    private int hashSliceStart = 0;
    /** 0 means str.length(), -1 means str.length()-1 */
    private int hashSliceEnd = 8;

    public void setHashLength(int hashLength) {
        setHashSlice(String.valueOf(hashLength));
    }

    public void setHashSlice(String hashSlice) {
        Pair<Integer, Integer> p = PairUtil.sequenceSlicing(hashSlice);
        hashSliceStart = p.getKey();
        hashSliceEnd = p.getValue();
    }

    @Override
    public Object evaluationInternal(Map<? extends Object, ? extends Object> parameters) {
        return calculate(parameters)[0];
    }

    @Override
    public Integer[] calculate(Map<? extends Object, ? extends Object> parameters) {
        Integer[] rst = new Integer[1];
        Object arg = arguments.get(0).evaluation(parameters);
        if (arg == UNEVALUATABLE) {
            throw new IllegalArgumentException("argument is UNEVALUATABLE");
        }
        String key = String.valueOf(arg);
        int start = hashSliceStart >= 0 ? hashSliceStart : key.length() + hashSliceStart;
        int end = hashSliceEnd > 0 ? hashSliceEnd : key.length() + hashSliceEnd;
        long hash = StringUtil.hash(key, start, end);
        rst[0] = partitionIndex(hash);
        return rst;
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        if (arguments == null || arguments.size() != 1) {
            throw new IllegalArgumentException("function " + getFunctionName() + " must have 1 argument but is "
                    + arguments);
        }
        Object[] args = new Object[arguments.size()];
        int i = -1;
        for (Expression arg : arguments) {
            args[++i] = arg;
        }
        return (FunctionExpression) constructMe(args);
    }

    @Override
    public RuleAlgorithm constructMe(Object... objects) {
        List<Expression> args = new ArrayList<Expression>(objects.length);
        for (Object obj : objects) {
            args.add((Expression) obj);
        }
        PartitionByString partitionFunc = new PartitionByString(functionName, args);
        partitionFunc.hashSliceStart = hashSliceStart;
        partitionFunc.hashSliceEnd = hashSliceEnd;
        partitionFunc.count = count;
        partitionFunc.length = length;
        return partitionFunc;
    }

    @Override
    public void initialize() {
        init();
    }

}
