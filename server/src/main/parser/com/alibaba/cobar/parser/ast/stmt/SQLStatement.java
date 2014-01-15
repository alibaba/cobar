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
 * (created at 2011-2-18)
 */
package com.alibaba.cobar.parser.ast.stmt;

import com.alibaba.cobar.parser.ast.ASTNode;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public interface SQLStatement extends ASTNode {
    public static enum StmtType {
        DML_SELECT,
        DML_DELETE,
        DML_INSERT,
        DML_REPLACE,
        DML_UPDATE,
        DML_CALL,
        DAL_SET,
        DAL_SHOW,
        MTL_START,
        /** COMMIT or ROLLBACK */
        MTL_TERMINATE,
        MTL_ISOLATION
    }
}
