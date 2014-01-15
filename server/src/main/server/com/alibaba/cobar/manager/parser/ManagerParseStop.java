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

import com.alibaba.cobar.parser.util.Pair;
import com.alibaba.cobar.parser.util.ParseUtil;
import com.alibaba.cobar.util.SplitUtil;

/**
 * @author xianmao.hexm
 */
public final class ManagerParseStop {

    public static final int OTHER = -1;
    public static final int HEARTBEAT = 1;

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
                return stop2Check(stmt, i);
            default:
                return OTHER;
            }
        }
        return OTHER;
    }

    public static Pair<String[], Integer> getPair(String stmt) {
        int offset = stmt.indexOf("@@");
        String s = stmt.substring(offset + 11).trim();
        int p1 = s.lastIndexOf(':');
        if (p1 == -1) {
            String[] src = SplitUtil.split(s, ',', '$', '-', '[', ']');
            return new Pair<String[], Integer>(src, null);
        } else {
            String[] src = SplitUtil.split(s, ':', true);
            String[] src1 = SplitUtil.split(src[0], ',', '$', '-', '[', ']');
            return new Pair<String[], Integer>(src1, Integer.valueOf(src[1]));
        }
    }

    // HEARTBEAT
    static int stop2Check(String stmt, int offset) {
        if (stmt.length() > ++offset && stmt.charAt(offset) == '@') {
            if (stmt.length() > offset + 9) {
                char c1 = stmt.charAt(++offset);
                char c2 = stmt.charAt(++offset);
                char c3 = stmt.charAt(++offset);
                char c4 = stmt.charAt(++offset);
                char c5 = stmt.charAt(++offset);
                char c6 = stmt.charAt(++offset);
                char c7 = stmt.charAt(++offset);
                char c8 = stmt.charAt(++offset);
                char c9 = stmt.charAt(++offset);
                if ((c1 == 'H' || c1 == 'h') && (c2 == 'E' || c2 == 'e') && (c3 == 'A' || c3 == 'a')
                        && (c4 == 'R' || c4 == 'r') && (c5 == 'T' || c5 == 't') && (c6 == 'B' || c6 == 'b')
                        && (c7 == 'E' || c7 == 'e') && (c8 == 'A' || c8 == 'a') && (c9 == 'T' || c9 == 't')) {
                    if (stmt.length() > ++offset && stmt.charAt(offset) != ' ') {
                        return OTHER;
                    }
                    return HEARTBEAT;
                }
            }
        }
        return OTHER;
    }

}
