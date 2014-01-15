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
 * @author xianmao.hexm 2012-4-16
 */
public class ManagerParseClear {

    public static final int OTHER = -1;
    public static final int SLOW_SCHEMA = 1;
    public static final int SLOW_DATANODE = 2;

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
                return clear2Check(stmt, i);
            default:
                return OTHER;
            }
        }
        return OTHER;
    }

    // CLEAR @@SLOW
    static int clear2Check(String stmt, int offset) {
        if (stmt.length() > ++offset && stmt.charAt(offset) == '@') {
            if (stmt.length() > offset + "SLOW ".length()) {
                char c1 = stmt.charAt(++offset);
                char c2 = stmt.charAt(++offset);
                char c3 = stmt.charAt(++offset);
                char c4 = stmt.charAt(++offset);
                char c5 = stmt.charAt(++offset);
                if ((c1 == 'S' || c1 == 's') && (c2 == 'L' || c2 == 'l') && (c3 == 'O' || c3 == 'o')
                        && (c4 == 'W' || c4 == 'w') && (c5 == ' ')) {
                    while (stmt.length() > ++offset) {
                        switch (stmt.charAt(offset)) {
                        case ' ':
                            continue;
                        case 'W':
                        case 'w':
                            return clear2WhereCheck(stmt, offset);
                        default:
                            return OTHER;
                        }
                    }
                }
            }
        }
        return OTHER;
    }

    // CLEAR @@SLOW WHERE
    static int clear2WhereCheck(String stmt, int offset) {
        if (stmt.length() > offset + "HERE ".length()) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            char c5 = stmt.charAt(++offset);
            if ((c1 == 'H' || c1 == 'h') && (c2 == 'E' || c2 == 'e') && (c3 == 'R' || c3 == 'r')
                    && (c4 == 'E' || c4 == 'e') && (c5 == ' ')) {
                while (stmt.length() > ++offset) {
                    switch (stmt.charAt(offset)) {
                    case ' ':
                        continue;
                    case 'D':
                    case 'd':
                        return clear2DCheck(stmt, offset);
                    case 'S':
                    case 's':
                        return clear2SCheck(stmt, offset);
                    default:
                        return OTHER;
                    }
                }
            }
        }
        return OTHER;
    }

    // CLEAR @@SLOW WHERE DATANODE = XXXXXX
    static int clear2DCheck(String stmt, int offset) {
        if (stmt.length() > offset + "ATANODE".length()) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            char c5 = stmt.charAt(++offset);
            char c6 = stmt.charAt(++offset);
            char c7 = stmt.charAt(++offset);
            if ((c1 == 'A' || c1 == 'a') && (c2 == 'T' || c2 == 't') && (c3 == 'A' || c3 == 'a')
                    && (c4 == 'N' || c4 == 'n') && (c5 == 'O' || c5 == 'o') && (c6 == 'D' || c6 == 'd')
                    && (c7 == 'E' || c7 == 'e')) {
                while (stmt.length() > ++offset) {
                    switch (stmt.charAt(offset)) {
                    case ' ':
                        continue;
                    case '=':
                        while (stmt.length() > ++offset) {
                            switch (stmt.charAt(offset)) {
                            case ' ':
                                continue;
                            default:
                                return (offset << 8) | SLOW_DATANODE;
                            }
                        }
                        return OTHER;
                    default:
                        return OTHER;
                    }
                }
            }
        }
        return OTHER;
    }

    // CLEAR @@SLOW WHERE SCHEMA = XXXXXX
    static int clear2SCheck(String stmt, int offset) {
        if (stmt.length() > offset + "CHEMA".length()) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            char c5 = stmt.charAt(++offset);
            if ((c1 == 'C' || c1 == 'c') && (c2 == 'H' || c2 == 'h') && (c3 == 'E' || c3 == 'e')
                    && (c4 == 'M' || c4 == 'm') && (c5 == 'A' || c5 == 'a')) {
                while (stmt.length() > ++offset) {
                    switch (stmt.charAt(offset)) {
                    case ' ':
                        continue;
                    case '=':
                        while (stmt.length() > ++offset) {
                            switch (stmt.charAt(offset)) {
                            case ' ':
                                continue;
                            default:
                                return (offset << 8) | SLOW_SCHEMA;
                            }
                        }
                        return OTHER;
                    default:
                        return OTHER;
                    }
                }
            }
        }
        return OTHER;
    }

}
