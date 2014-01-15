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
package com.alibaba.cobar.manager.parser;

import com.alibaba.cobar.parser.util.ParseUtil;

/**
 * @author xianmao.hexm 2011-5-31 下午12:45:12
 */
public final class ManagerParseKill {

    public static final int OTHER = -1;
    public static final int CONNECTION = 1;

    public static int parse(String stmt, int offset) {
        int i = offset;
        for (; i < stmt.length(); i++) {
            switch (stmt.charAt(i)) {
            case ' ':
                continue;
            case '/':
            case '#':
                i = ParseUtil.comment(stmt, i);
                continue;
            case '@':
                return kill2Check(stmt, i);
            default:
                return OTHER;
            }
        }
        return OTHER;
    }

    // KILL @@CONNECTION
    static int kill2Check(String stmt, int offset) {
        if (stmt.length() > ++offset && stmt.charAt(offset) == '@') {
            if (stmt.length() > offset + 10) {
                char c1 = stmt.charAt(++offset);
                char c2 = stmt.charAt(++offset);
                char c3 = stmt.charAt(++offset);
                char c4 = stmt.charAt(++offset);
                char c5 = stmt.charAt(++offset);
                char c6 = stmt.charAt(++offset);
                char c7 = stmt.charAt(++offset);
                char c8 = stmt.charAt(++offset);
                char c9 = stmt.charAt(++offset);
                char c10 = stmt.charAt(++offset);
                if ((c1 == 'C' || c1 == 'c') && (c2 == 'O' || c2 == 'o') && (c3 == 'N' || c3 == 'n')
                        && (c4 == 'N' || c4 == 'n') && (c5 == 'E' || c5 == 'e') && (c6 == 'C' || c6 == 'c')
                        && (c7 == 'T' || c7 == 't') && (c8 == 'I' || c8 == 'i') && (c9 == 'O' || c9 == 'o')
                        && (c10 == 'N' || c10 == 'n')) {
                    if (stmt.length() > ++offset && stmt.charAt(offset) != ' ') {
                        return OTHER;
                    }
                    return CONNECTION;
                }
            }
        }
        return OTHER;
    }

}
