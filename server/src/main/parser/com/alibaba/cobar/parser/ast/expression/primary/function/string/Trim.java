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
 * (created at 2011-1-23)
 */
package com.alibaba.cobar.parser.ast.expression.primary.function.string;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.primary.function.FunctionExpression;
import com.alibaba.cobar.parser.visitor.SQLASTVisitor;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class Trim extends FunctionExpression {
    public static enum Direction {
        /** no tag for direction */
        DEFAULT,
        BOTH,
        LEADING,
        TRAILING
    }

    private final Direction direction;

    private static List<Expression> wrapList(Expression str, Expression remstr) {
        if (str == null)
            throw new IllegalArgumentException("str is null");
        List<Expression> list = remstr != null ? new ArrayList<Expression>(2) : new ArrayList<Expression>(1);
        list.add(str);
        if (remstr != null)
            list.add(remstr);
        return list;
    }

    public Trim(Direction direction, Expression remstr, Expression str) {
        super("TRIM", wrapList(str, remstr));
        this.direction = direction;
    }

    /**
     * @return never null
     */
    public Expression getString() {
        return getArguments().get(0);
    }

    public Expression getRemainString() {
        List<Expression> args = getArguments();
        if (args.size() < 2)
            return null;
        return getArguments().get(1);
    }

    public Direction getDirection() {
        return direction;
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        throw new UnsupportedOperationException("function of trim has special arguments");
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
