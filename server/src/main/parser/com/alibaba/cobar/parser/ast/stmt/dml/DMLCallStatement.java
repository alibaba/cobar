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
 * (created at 2011-5-19)
 */
package com.alibaba.cobar.parser.ast.stmt.dml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.primary.Identifier;
import com.alibaba.cobar.parser.visitor.SQLASTVisitor;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class DMLCallStatement extends DMLStatement {
    private final Identifier procedure;
    private final List<Expression> arguments;

    public DMLCallStatement(Identifier procedure, List<Expression> arguments) {
        this.procedure = procedure;
        if (arguments == null || arguments.isEmpty()) {
            this.arguments = Collections.emptyList();
        } else if (arguments instanceof ArrayList) {
            this.arguments = arguments;
        } else {
            this.arguments = new ArrayList<Expression>(arguments);
        }
    }

    public DMLCallStatement(Identifier procedure) {
        this.procedure = procedure;
        this.arguments = Collections.emptyList();
    }

    public Identifier getProcedure() {
        return procedure;
    }

    /**
     * @return never null
     */
    public List<Expression> getArguments() {
        return arguments;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
