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
 * (created at 2011-7-5)
 */
package com.alibaba.cobar.parser.ast.stmt.ddl;

import java.util.Collections;
import java.util.List;

import com.alibaba.cobar.parser.ast.expression.primary.Identifier;
import com.alibaba.cobar.parser.visitor.SQLASTVisitor;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class DDLDropTableStatement implements DDLStatement {
    public static enum Mode {
        UNDEF,
        RESTRICT,
        CASCADE
    }

    private final List<Identifier> tableNames;
    private final boolean temp;
    private final boolean ifExists;
    private final Mode mode;

    public DDLDropTableStatement(List<Identifier> tableNames, boolean temp, boolean ifExists) {
        this(tableNames, temp, ifExists, Mode.UNDEF);
    }

    public DDLDropTableStatement(List<Identifier> tableNames, boolean temp, boolean ifExists, Mode mode) {
        if (tableNames == null || tableNames.isEmpty()) {
            this.tableNames = Collections.emptyList();
        } else {
            this.tableNames = tableNames;
        }
        this.temp = temp;
        this.ifExists = ifExists;
        this.mode = mode;
    }

    public List<Identifier> getTableNames() {
        return tableNames;
    }

    public boolean isTemp() {
        return temp;
    }

    public boolean isIfExists() {
        return ifExists;
    }

    public Mode getMode() {
        return mode;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

}
