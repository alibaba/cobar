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

import java.util.List;

import com.alibaba.cobar.parser.ast.expression.misc.QueryExpression;
import com.alibaba.cobar.parser.ast.expression.primary.Identifier;
import com.alibaba.cobar.parser.ast.expression.primary.RowExpression;
import com.alibaba.cobar.parser.visitor.SQLASTVisitor;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class DMLReplaceStatement extends DMLInsertReplaceStatement {
    public static enum ReplaceMode {
        /** default */
        UNDEF,
        LOW,
        DELAY
    }

    private final ReplaceMode mode;

    public DMLReplaceStatement(ReplaceMode mode, Identifier table, List<Identifier> columnNameList,
                               List<RowExpression> rowList) {
        super(table, columnNameList, rowList);
        this.mode = mode;
    }

    public DMLReplaceStatement(ReplaceMode mode, Identifier table, List<Identifier> columnNameList,
                               QueryExpression select) {
        super(table, columnNameList, select);
        this.mode = mode;
    }

    public ReplaceMode getMode() {
        return mode;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
