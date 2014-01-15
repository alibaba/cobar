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
package com.alibaba.cobar.parser.ast.expression.primary;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.util.Pair;
import com.alibaba.cobar.parser.visitor.SQLASTVisitor;

/**
 * <code>'CASE' value? ('WHEN' condition 'THEN' result)+ ('ELSE' result)? 'END' </code>
 * 
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class CaseWhenOperatorExpression extends PrimaryExpression {
    private final Expression comparee;
    private final List<Pair<Expression, Expression>> whenList;
    private final Expression elseResult;

    /**
     * @param whenList never null or empry; no pair contains null key or value
     * @param comparee null for format of <code>CASE WHEN ...</code>, otherwise,
     *            <code>CASE comparee WHEN ...</code>
     */
    public CaseWhenOperatorExpression(Expression comparee, List<Pair<Expression, Expression>> whenList,
                                      Expression elseResult) {
        super();
        this.comparee = comparee;
        if (whenList instanceof ArrayList) {
            this.whenList = whenList;
        } else {
            this.whenList = new ArrayList<Pair<Expression, Expression>>(whenList);
        }
        this.elseResult = elseResult;
    }

    public Expression getComparee() {
        return comparee;
    }

    /**
     * @return never null or empty; no pair contains null key or value
     */
    public List<Pair<Expression, Expression>> getWhenList() {
        return whenList;
    }

    public Expression getElseResult() {
        return elseResult;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
