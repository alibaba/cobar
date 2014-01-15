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
 * (created at 2011-8-4)
 */
package com.alibaba.cobar.route.hint;

import java.sql.SQLSyntaxErrorException;
import java.util.LinkedList;
import java.util.List;

import com.alibaba.cobar.parser.util.Pair;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public final class PartitionOperandHintParser extends HintParser {

    private static String[] convert2String(Object[] objs) {
        String[] strings = new String[objs.length];
        for (int i = 0; i < objs.length; ++i) {
            strings[i] = (String) objs[i];
        }
        return strings;
    }

    @Override
    public void process(CobarHint hint, String hintName, String sql) throws SQLSyntaxErrorException {
        String[] columns;
        if (nextChar(hint, sql) == '[') {
            hint.increaseCurrentIndex();
            columns = convert2String(parseArray(hint, sql, -1));
        } else {
            columns = new String[1];
            columns[0] = (String) parsePrimary(hint, sql);
        }
        Object[][] values;
        switch (nextChar(hint, sql)) {
        case '[':
            if (columns.length == 1) {
                hint.increaseCurrentIndex();
                Object[] vs = parseArray(hint, sql, -1);
                values = new Object[vs.length][1];
                for (int i = 0; i < vs.length; ++i) {
                    values[i][0] = vs[i];
                }
            } else {
                values = parseArrayArray(hint, sql, columns.length);
            }
            break;
        default:
            if (columns.length == 1) {
                values = new Object[1][1];
                values[0][0] = parsePrimary(hint, sql);
            } else {
                throw new SQLSyntaxErrorException("err for partitionOperand: " + sql);
            }
        }
        hint.setPartitionOperand(new Pair<String[], Object[][]>(columns, values));
        if (currentChar(hint, sql) == ')')
            hint.increaseCurrentIndex();
    }

    /**
     * current char is char after '[', after call, current char is char after
     * ']'
     * 
     * @param len less than 0 for array length unknown
     */
    private Object[] parseArray(CobarHint hint, String sql, int len) throws SQLSyntaxErrorException {
        Object[] rst = null;
        List<Object> list = null;
        if (len >= 0) {
            rst = new Object[len];
        } else {
            list = new LinkedList<Object>();
        }
        for (int i = 0;; ++i) {
            Object obj = parsePrimary(hint, sql);
            if (len >= 0)
                rst[i] = obj;
            else
                list.add(obj);
            switch (currentChar(hint, sql)) {
            case ']':
                hint.increaseCurrentIndex();
                if (len >= 0)
                    return rst;
                else
                    return list.toArray(new Object[list.size()]);
            case ',':
                hint.increaseCurrentIndex();
                break;
            default:
                throw new SQLSyntaxErrorException("err for partitionOperand array: " + sql);
            }
        }
    }

    /**
     * current char is '[[', after call, current char is char after ']]'
     */
    private Object[][] parseArrayArray(CobarHint hint, String sql, int columnNum) throws SQLSyntaxErrorException {
        if (nextChar(hint, sql) == '[') {
            List<Object[]> list = new LinkedList<Object[]>();
            for (;;) {
                nextChar(hint, sql);
                list.add(parseArray(hint, sql, columnNum));
                char c = currentChar(hint, sql);
                switch (c) {
                case ']':
                    hint.increaseCurrentIndex();
                    return list.toArray(new Object[list.size()][]);
                case ',':
                    nextChar(hint, sql);
                    break;
                default:
                    throw new SQLSyntaxErrorException("err for partitionOperand array[]: " + sql);
                }
            }
        } else {
            Object[][] rst = new Object[1][columnNum];
            rst[0] = parseArray(hint, sql, columnNum);
            return rst;
        }
    }
}
