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
 * (created at 2011-8-3)
 */
package com.alibaba.cobar.route.hint;

import java.sql.SQLSyntaxErrorException;

/**
 * Stateless
 * 
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public abstract class HintParser {
    protected static boolean isDigit(char c) {
        switch (c) {
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
            return true;
        default:
            return false;
        }
    }

    /**
     * hint's {@link CobarHint#getCurrentIndex()} will be changed to index of
     * next char after process
     */
    public abstract void process(CobarHint hint, String hintName, String sql) throws SQLSyntaxErrorException;

    private void skipSpace(CobarHint hint, String sql) {
        int ci = hint.getCurrentIndex();
        skip: for (;;) {
            switch (sql.charAt(ci)) {
            case ' ':
            case '\t':
            case '\n':
            case '\r':
                hint.increaseCurrentIndex();
                ++ci;
                break;
            default:
                break skip;
            }
        }
    }

    protected char currentChar(CobarHint hint, String sql) {
        skipSpace(hint, sql);
        return sql.charAt(hint.getCurrentIndex());
    }

    /**
     * current char is not separator
     */
    protected char nextChar(CobarHint hint, String sql) {
        skipSpace(hint, sql);
        skipSpace(hint.increaseCurrentIndex(), sql);
        return sql.charAt(hint.getCurrentIndex());
    }

    protected Object parsePrimary(CobarHint hint, String sql) throws SQLSyntaxErrorException {
        char c = currentChar(hint, sql);
        int ci = hint.getCurrentIndex();
        switch (c) {
        case '\'':
            StringBuilder sb = new StringBuilder();
            for (++ci;; ++ci) {
                c = sql.charAt(ci);
                switch (c) {
                case '\'':
                    hint.setCurrentIndex(ci + 1);
                    return sb.toString();
                case '\\':
                    c = sql.charAt(++ci);
                default:
                    sb.append(c);
                    break;
                }
            }
        case 'n':
        case 'N':
            hint.setCurrentIndex(ci + "null".length());
            return null;
        default:
            if (isDigit(c)) {
                int start = ci++;
                for (; isDigit(sql.charAt(ci)); ++ci) {
                }
                hint.setCurrentIndex(ci);
                return Long.parseLong(sql.substring(start, ci));
            }
            throw new SQLSyntaxErrorException("unknown primary in hint: " + sql);
        }
    }

}
