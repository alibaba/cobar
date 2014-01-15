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
 * (created at 2011-2-10)
 */
package com.alibaba.cobar.parser.ast.fragment.tableref;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.alibaba.cobar.parser.ast.ASTNode;
import com.alibaba.cobar.parser.visitor.SQLASTVisitor;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class IndexHint implements ASTNode {
    public static enum IndexAction {
        USE,
        IGNORE,
        FORCE
    }

    public static enum IndexType {
        INDEX,
        KEY
    }

    public static enum IndexScope {
        /** not specified */
        ALL,
        JOIN,
        GROUP_BY,
        ORDER_BY
    }

    private final IndexAction action;
    private final IndexType type;
    private final IndexScope scope;
    private final List<String> indexList;

    public IndexHint(IndexAction action, IndexType type, IndexScope scope, List<String> indexList) {
        super();
        if (action == null)
            throw new IllegalArgumentException("index hint action is null");
        if (type == null)
            throw new IllegalArgumentException("index hint type is null");
        if (scope == null)
            throw new IllegalArgumentException("index hint scope is null");
        this.action = action;
        this.type = type;
        this.scope = scope;
        if (indexList == null || indexList.isEmpty()) {
            this.indexList = Collections.emptyList();
        } else if (indexList instanceof ArrayList) {
            this.indexList = indexList;
        } else {
            this.indexList = new ArrayList<String>(indexList);
        }
    }

    public IndexAction getAction() {
        return action;
    }

    public IndexType getType() {
        return type;
    }

    public IndexScope getScope() {
        return scope;
    }

    public List<String> getIndexList() {
        return indexList;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
