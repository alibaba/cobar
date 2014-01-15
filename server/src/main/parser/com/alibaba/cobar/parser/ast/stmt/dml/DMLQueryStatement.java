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
 * (created at 2011-6-17)
 */
package com.alibaba.cobar.parser.ast.stmt.dml;

import java.util.Map;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.misc.QueryExpression;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public abstract class DMLQueryStatement extends DMLStatement implements QueryExpression {
    @Override
    public int getPrecedence() {
        return PRECEDENCE_QUERY;
    }

    @Override
    public Expression setCacheEvalRst(boolean cacheEvalRst) {
        return this;
    }

    @Override
    public Object evaluation(Map<? extends Object, ? extends Object> parameters) {
        return UNEVALUATABLE;
    }
}
