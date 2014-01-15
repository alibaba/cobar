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
 * (created at 2011-1-27)
 */
package com.alibaba.cobar.parser.ast.stmt.dml;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.stmt.SQLStatement;
import com.alibaba.cobar.parser.visitor.MySQLOutputASTVisitor;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public abstract class DMLStatement implements SQLStatement {
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected static List ensureListType(List list) {
        if (list == null || list.size() <= 0)
            return null;
        if (list instanceof ArrayList)
            return list;
        return new ArrayList(list);
    }

    @SuppressWarnings("unchecked")
    protected static List<List<Expression>> checkAndConvertValuesList(List<List<Expression>> valuesList) {
        if (valuesList == null || valuesList.isEmpty()) {
            throw new IllegalArgumentException("argument 'valuesList' is empty");
        }
        List<List<Expression>> rst = (valuesList instanceof ArrayList) ? valuesList : new ArrayList<List<Expression>>(
                valuesList.size());
        boolean copy = rst != valuesList;
        int size = -1;
        if (copy) {
            for (List<Expression> values : valuesList) {
                if (values == null || values.size() <= 0) {
                    throw new IllegalArgumentException("argument 'valuesList' contains empty element");
                }
                if (size < 0) {
                    size = values.size();
                } else if (size != values.size()) {
                    throw new IllegalArgumentException(
                            "argument 'valuesList' contains empty elements with different size: " + size + " != "
                                    + values.size());
                }
                rst.add(ensureListType(values));
            }
            return rst;
        }
        for (int i = 0; i < valuesList.size(); ++i) {
            List<Expression> values = valuesList.get(i);
            if (values == null || values.size() <= 0) {
                throw new IllegalArgumentException("argument 'valuesList' contains empty element");
            }
            if (size < 0) {
                size = values.size();
            } else if (size != values.size()) {
                throw new IllegalArgumentException(
                        "argument 'valuesList' contains empty elements with different size: " + size + " != "
                                + values.size());
            }
            if (!(values instanceof ArrayList)) {
                valuesList.set(i, new ArrayList<Expression>(values));
            }
        }
        return rst;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        accept(new MySQLOutputASTVisitor(sb));
        return sb.toString();
    }
}
