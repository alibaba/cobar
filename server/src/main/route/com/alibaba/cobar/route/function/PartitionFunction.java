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

import java.util.List;
import java.util.Map;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.primary.function.FunctionExpression;
import com.alibaba.cobar.route.util.PartitionUtil;
import com.alibaba.cobar.util.SplitUtil;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public abstract class PartitionFunction extends FunctionExpression {
    public PartitionFunction(String functionName, List<Expression> arguments) {
        super(functionName, arguments);
    }

    protected int[] count;
    protected int[] length;
    protected PartitionUtil partitionUtil;

    public void setPartitionCount(String partitionCount) {
        this.count = toIntArray(partitionCount);
    }

    public void setPartitionLength(String partitionLength) {
        this.length = toIntArray(partitionLength);
    }

    private static int[] toIntArray(String string) {
        String[] strs = SplitUtil.split(string, ',', true);
        int[] ints = new int[strs.length];
        for (int i = 0; i < strs.length; ++i) {
            ints[i] = Integer.parseInt(strs[i]);
        }
        return ints;
    }

    @Override
    public void init() {
        partitionUtil = new PartitionUtil(count, length);
    }

    protected int partitionIndex(long hash) {
        return partitionUtil.partition(hash);
    }

    @Override
    public abstract Object evaluationInternal(Map<? extends Object, ? extends Object> parameters);

}
