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
 * @author xianmao.hexm 2011-5-9 下午04:16:19
 */
public final class ManagerParseSelect {

    public static final int OTHER = -1;
    public static final int VERSION_COMMENT = 1;
    public static final int SESSION_AUTO_INCREMENT = 2;

    private static final char[] _VERSION_COMMENT = "VERSION_COMMENT".toCharArray();
    private static final char[] _SESSION_AUTO_INCREMENT = "SESSION.AUTO_INCREMENT_INCREMENT".toCharArray();

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
                return select2Check(stmt, i);
            default:
                return OTHER;
            }
        }
        return OTHER;
    }

    static int select2Check(String stmt, int offset) {
        if (stmt.length() > ++offset && stmt.charAt(offset) == '@') {
            if (stmt.length() > ++offset) {
                switch (stmt.charAt(offset)) {
                case 'S':
                case 's':
                    return select2SCheck(stmt, offset);
                case 'V':
                case 'v':
                    return select2VCheck(stmt, offset);
                default:
                    return OTHER;
                }
            }
        }
        return OTHER;
    }

    // VERSION_COMMENT
    static int select2VCheck(String stmt, int offset) {
        int length = offset + _VERSION_COMMENT.length;
        if (stmt.length() >= length) {
            if (ParseUtil.compare(stmt, offset, _VERSION_COMMENT)) {
                if (stmt.length() > length && stmt.charAt(length) != ' ') {
                    return OTHER;
                }
                return VERSION_COMMENT;
            }
        }
        return OTHER;
    }

    // SESSION.AUTO_INCREMENT_INCREMENT
    static int select2SCheck(String stmt, int offset) {
        int length = offset + _SESSION_AUTO_INCREMENT.length;
        if (stmt.length() >= length) {
            if (ParseUtil.compare(stmt, offset, _SESSION_AUTO_INCREMENT)) {
                if (stmt.length() > length && stmt.charAt(length) != ' ') {
                    return OTHER;
                }
                return SESSION_AUTO_INCREMENT;
            }
        }
        return OTHER;
    }

}
