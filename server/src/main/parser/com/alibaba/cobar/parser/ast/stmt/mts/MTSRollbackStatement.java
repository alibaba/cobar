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
 * (created at 2011-9-12)
 */
package com.alibaba.cobar.parser.ast.stmt.mts;

import com.alibaba.cobar.parser.ast.expression.primary.Identifier;
import com.alibaba.cobar.parser.ast.stmt.SQLStatement;
import com.alibaba.cobar.parser.visitor.SQLASTVisitor;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class MTSRollbackStatement implements SQLStatement {
    public static enum CompleteType {
        /** not specified, then use default */
        UN_DEF,
        CHAIN,
        /** MySQL's default */
        NO_CHAIN,
        RELEASE,
        NO_RELEASE
    }

    private final CompleteType completeType;
    private final Identifier savepoint;

    public MTSRollbackStatement(CompleteType completeType) {
        if (completeType == null)
            throw new IllegalArgumentException("complete type is null!");
        this.completeType = completeType;
        this.savepoint = null;
    }

    public MTSRollbackStatement(Identifier savepoint) {
        this.completeType = null;
        if (savepoint == null)
            throw new IllegalArgumentException("savepoint is null!");
        this.savepoint = savepoint;
    }

    /**
     * @return null if roll back to SAVEPOINT
     */
    public CompleteType getCompleteType() {
        return completeType;
    }

    /**
     * @return null for roll back the whole transaction
     */
    public Identifier getSavepoint() {
        return savepoint;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

}
