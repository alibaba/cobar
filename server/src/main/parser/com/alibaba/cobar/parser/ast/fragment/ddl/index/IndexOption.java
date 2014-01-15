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
 * (created at 2012-8-13)
 */
package com.alibaba.cobar.parser.ast.fragment.ddl.index;

import com.alibaba.cobar.parser.ast.ASTNode;
import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.primary.Identifier;
import com.alibaba.cobar.parser.ast.expression.primary.literal.LiteralString;
import com.alibaba.cobar.parser.visitor.SQLASTVisitor;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class IndexOption implements ASTNode {
    public static enum IndexType {
        BTREE,
        HASH
    }

    private final Expression keyBlockSize;
    private final IndexType indexType;
    private final Identifier parserName;
    private final LiteralString comment;

    public IndexOption(Expression keyBlockSize) {
        this.keyBlockSize = keyBlockSize;
        this.indexType = null;
        this.parserName = null;
        this.comment = null;
    }

    public IndexOption(IndexType indexType) {
        this.keyBlockSize = null;
        this.indexType = indexType;
        this.parserName = null;
        this.comment = null;
    }

    public IndexOption(Identifier parserName) {
        this.keyBlockSize = null;
        this.indexType = null;
        this.parserName = parserName;
        this.comment = null;
    }

    public IndexOption(LiteralString comment) {
        this.keyBlockSize = null;
        this.indexType = null;
        this.parserName = null;
        this.comment = comment;
    }

    public Expression getKeyBlockSize() {
        return keyBlockSize;
    }

    public IndexType getIndexType() {
        return indexType;
    }

    public Identifier getParserName() {
        return parserName;
    }

    public LiteralString getComment() {
        return comment;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

}
