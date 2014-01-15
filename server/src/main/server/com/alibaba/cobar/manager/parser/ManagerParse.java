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
 * @author xianmao.hexm
 * @author wenfeng.cenwf 2011-4-19
 */
public final class ManagerParse {

    public static final int OTHER = -1;
    public static final int SELECT = 1;
    public static final int SET = 2;
    public static final int SHOW = 3;
    public static final int SWITCH = 4;
    public static final int KILL_CONN = 5;
    public static final int STOP = 6;
    public static final int RELOAD = 7;
    public static final int ROLLBACK = 8;
    public static final int OFFLINE = 9;
    public static final int ONLINE = 10;
    public static final int CLEAR = 11;

    public static int parse(String stmt) {
        for (int i = 0; i < stmt.length(); i++) {
            switch (stmt.charAt(i)) {
            case ' ':
                continue;
            case '/':
            case '#':
                i = ParseUtil.comment(stmt, i);
                continue;
            case 'C':
            case 'c':
                return cCheck(stmt, i);
            case 'S':
            case 's':
                return sCheck(stmt, i);
            case 'K':
            case 'k':
                return kill(stmt, i);
            case 'O':
            case 'o':
                return oCheck(stmt, i);
            case 'R':
            case 'r':
                return rCheck(stmt, i);
            default:
                return OTHER;
            }
        }
        return OTHER;
    }

    // CLEAR
    private static int cCheck(String stmt, int offset) {
        if (stmt.length() > offset + "LEAR ".length()) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            char c5 = stmt.charAt(++offset);
            if ((c1 == 'L' || c1 == 'l') && (c2 == 'E' || c2 == 'e') && (c3 == 'A' || c3 == 'a')
                    && (c4 == 'R' || c4 == 'r') && (c5 == ' ' || c5 == '\t' || c5 == '\r' || c5 == '\n')) {
                return (offset << 8) | CLEAR;
            }
        }
        return OTHER;
    }

    private static int oCheck(String stmt, int offset) {
        if (stmt.length() > ++offset) {
            switch (stmt.charAt(offset)) {
            case 'F':
            case 'f':
                return ofCheck(stmt, offset);
            case 'N':
            case 'n':
                return onCheck(stmt, offset);
            default:
                return OTHER;
            }
        }
        return OTHER;
    }

    private static int onCheck(String stmt, int offset) {
        if (stmt.length() > offset + "line".length()) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            if ((c1 == 'l' || c1 == 'L') && (c2 == 'i' || c2 == 'I') && (c3 == 'n' || c3 == 'N')
                    && (c4 == 'e' || c4 == 'E') && (stmt.length() == ++offset || ParseUtil.isEOF(stmt.charAt(offset)))) {
                return ONLINE;
            }
        }
        return OTHER;
    }

    private static int ofCheck(String stmt, int offset) {
        if (stmt.length() > offset + "fline".length()) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            char c5 = stmt.charAt(++offset);
            if ((c1 == 'f' || c1 == 'F') && (c2 == 'l' || c2 == 'L') && (c3 == 'i' || c3 == 'I')
                    && (c4 == 'n' || c4 == 'N') && (c5 == 'e' || c5 == 'E')
                    && (stmt.length() == ++offset || ParseUtil.isEOF(stmt.charAt(offset)))) {
                return OFFLINE;
            }
        }
        return OTHER;
    }

    private static int sCheck(String stmt, int offset) {
        if (stmt.length() > ++offset) {
            switch (stmt.charAt(offset)) {
            case 'E':
            case 'e':
                return seCheck(stmt, offset);
            case 'H':
            case 'h':
                return show(stmt, offset);
            case 'W':
            case 'w':
                return swh(stmt, offset);
            case 'T':
            case 't':
                return stop(stmt, offset);
            default:
                return OTHER;
            }
        }
        return OTHER;
    }

    private static int seCheck(String stmt, int offset) {
        if (stmt.length() > ++offset) {
            switch (stmt.charAt(offset)) {
            case 'L':
            case 'l':
                return select(stmt, offset);
            case 'T':
            case 't':
                if (stmt.length() > ++offset) {
                    char c = stmt.charAt(offset);
                    if (c == ' ' || c == '\r' || c == '\n' || c == '\t' || c == '/' || c == '#') {
                        return SET;
                    }
                }
                return OTHER;
            default:
                return OTHER;
            }
        }
        return OTHER;
    }

    private static int rCheck(String stmt, int offset) {
        if (stmt.length() > ++offset) {
            switch (stmt.charAt(offset)) {
            case 'E':
            case 'e':
                return reload(stmt, offset);
            case 'O':
            case 'o':
                return rollback(stmt, offset);
            default:
                return OTHER;
            }
        }
        return OTHER;
    }

    // RELOAD' '
    private static int reload(String stmt, int offset) {
        if (stmt.length() > offset + 5) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            char c5 = stmt.charAt(++offset);
            if ((c1 == 'L' || c1 == 'l') && (c2 == 'O' || c2 == 'o') && (c3 == 'A' || c3 == 'a')
                    && (c4 == 'D' || c4 == 'd') && (c5 == ' ' || c5 == '\t' || c5 == '\r' || c5 == '\n')) {
                return (offset << 8) | RELOAD;
            }
        }
        return OTHER;
    }

    // ROLLBACK' '
    private static int rollback(String stmt, int offset) {
        if (stmt.length() > offset + 7) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            char c5 = stmt.charAt(++offset);
            char c6 = stmt.charAt(++offset);
            char c7 = stmt.charAt(++offset);
            if ((c1 == 'L' || c1 == 'l') && (c2 == 'L' || c2 == 'l') && (c3 == 'B' || c3 == 'b')
                    && (c4 == 'A' || c4 == 'a') && (c5 == 'C' || c5 == 'c') && (c6 == 'K' || c6 == 'k')
                    && (c7 == ' ' || c7 == '\t' || c7 == '\r' || c7 == '\n')) {
                return (offset << 8) | ROLLBACK;
            }
        }
        return OTHER;
    }

    // SELECT' '
    private static int select(String stmt, int offset) {
        if (stmt.length() > offset + 4) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            if ((c1 == 'E' || c1 == 'e') && (c2 == 'C' || c2 == 'c') && (c3 == 'T' || c3 == 't')
                    && (c4 == ' ' || c4 == '\t' || c4 == '\r' || c4 == '\n')) {
                return (offset << 8) | SELECT;
            }
        }
        return OTHER;
    }

    // SHOW' '
    private static int show(String stmt, int offset) {
        if (stmt.length() > offset + 3) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            if ((c1 == 'O' || c1 == 'o') && (c2 == 'W' || c2 == 'w')
                    && (c3 == ' ' || c3 == '\t' || c3 == '\r' || c3 == '\n')) {
                return (offset << 8) | SHOW;
            }
        }
        return OTHER;
    }

    // SWITCH' '
    private static int swh(String stmt, int offset) {
        if (stmt.length() > offset + 5) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            char c5 = stmt.charAt(++offset);
            if ((c1 == 'I' || c1 == 'i') && (c2 == 'T' || c2 == 't') && (c3 == 'C' || c3 == 'c')
                    && (c4 == 'H' || c4 == 'h') && (c5 == ' ' || c5 == '\t' || c5 == '\r' || c5 == '\n')) {
                return (offset << 8) | SWITCH;
            }
        }
        return OTHER;
    }

    // STOP' '
    private static int stop(String stmt, int offset) {
        if (stmt.length() > offset + 3) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            if ((c1 == 'O' || c1 == 'o') && (c2 == 'P' || c2 == 'p')
                    && (c3 == ' ' || c3 == '\t' || c3 == '\r' || c3 == '\n')) {
                return (offset << 8) | STOP;
            }
        }
        return OTHER;
    }

    // KILL @
    private static int kill(String stmt, int offset) {
        if (stmt.length() > offset + 3) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            if ((c1 == 'I' || c1 == 'i') && (c2 == 'L' || c2 == 'l') && (c3 == 'L' || c3 == 'l')
                    && (c4 == ' ' || c4 == '\t' || c4 == '\r' || c4 == '\n')) {
                while (stmt.length() > ++offset) {
                    switch (stmt.charAt(offset)) {
                    case ' ':
                    case '\t':
                    case '\r':
                    case '\n':
                        continue;
                    case '@':
                        return killConnection(stmt, offset);
                    default:
                        return OTHER;
                    }
                }
                return OTHER;
            }
        }
        return OTHER;
    }

    // KILL @@CONNECTION' ' XXXXXX
    private static int killConnection(String stmt, int offset) {
        if (stmt.length() > offset + "@CONNECTION ".length()) {
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
            char c11 = stmt.charAt(++offset);
            char c12 = stmt.charAt(++offset);
            if ((c1 == '@') && (c2 == 'C' || c2 == 'c') && (c3 == 'O' || c3 == 'o') && (c4 == 'N' || c4 == 'n')
                    && (c5 == 'N' || c5 == 'n') && (c6 == 'E' || c6 == 'e') && (c7 == 'C' || c7 == 'c')
                    && (c8 == 'T' || c8 == 't') && (c9 == 'I' || c9 == 'i') && (c10 == 'O' || c10 == 'o')
                    && (c11 == 'N' || c11 == 'n') && (c12 == ' ' || c12 == '\t' || c12 == '\r' || c12 == '\n')) {
                while (stmt.length() > ++offset) {
                    switch (stmt.charAt(offset)) {
                    case ' ':
                    case '\t':
                    case '\r':
                    case '\n':
                        continue;
                    default:
                        return (offset << 8) | KILL_CONN;
                    }
                }
            }
        }
        return OTHER;
    }

}
