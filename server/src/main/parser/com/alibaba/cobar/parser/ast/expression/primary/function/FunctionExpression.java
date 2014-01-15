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
 * (created at 2011-4-12)
 */
package com.alibaba.cobar.parser.ast.expression.primary.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.primary.PrimaryExpression;
import com.alibaba.cobar.parser.visitor.SQLASTVisitor;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public abstract class FunctionExpression extends PrimaryExpression {
    protected static List<Expression> wrapList(Expression expr) {
        List<Expression> list = new ArrayList<Expression>(1);
        list.add(expr);
        return list;
    }

    /**
     * <code>this</code> function object being called is a prototype
     */
    public abstract FunctionExpression constructFunction(List<Expression> arguments);

    protected final String functionName;
    protected final List<Expression> arguments;

    public FunctionExpression(String functionName, List<Expression> arguments) {
        super();
        this.functionName = functionName;
        if (arguments == null || arguments.isEmpty()) {
            this.arguments = Collections.emptyList();
        } else {
            if (arguments instanceof ArrayList) {
                this.arguments = arguments;
            } else {
                this.arguments = new ArrayList<Expression>(arguments);
            }
        }
    }

    public void init() {
    }

    /**
     * @return never null
     */
    public List<Expression> getArguments() {
        return arguments;
    }

    public String getFunctionName() {
        return functionName;
    }

    @Override
    public Expression setCacheEvalRst(boolean cacheEvalRst) {
        return super.setCacheEvalRst(cacheEvalRst);
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
