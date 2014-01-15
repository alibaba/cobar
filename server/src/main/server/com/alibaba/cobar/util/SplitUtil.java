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
package com.alibaba.cobar.util;

import java.util.LinkedList;
import java.util.List;

/**
 * @author xianmao.hexm 2012-5-31
 */
public class SplitUtil {
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * 解析字符串<br>
     * 比如:c1='$',c2='-' 输入字符串：mysql_db$0-2<br>
     * 输出array:mysql_db[0],mysql_db[1],mysql_db[2]
     */
    public static String[] split2(String src, char c1, char c2) {
        if (src == null) {
            return null;
        }
        int length = src.length();
        if (length == 0) {
            return EMPTY_STRING_ARRAY;
        }
        List<String> list = new LinkedList<String>();
        String[] p = split(src, c1, true);
        if (p.length > 1) {
            String[] scope = split(p[1], c2, true);
            int min = Integer.parseInt(scope[0]);
            int max = Integer.parseInt(scope[scope.length - 1]);
            for (int x = min; x <= max; x++) {
                list.add(new StringBuilder(p[0]).append('[').append(x).append(']').toString());
            }
        } else {
            list.add(p[0]);
        }
        return list.toArray(new String[list.size()]);
    }

    public static String[] split(String src) {
        return split(src, null, -1);
    }

    public static String[] split(String src, char separatorChar) {
        if (src == null) {
            return null;
        }
        int length = src.length();
        if (length == 0) {
            return EMPTY_STRING_ARRAY;
        }
        List<String> list = new LinkedList<String>();
        int i = 0;
        int start = 0;
        boolean match = false;
        while (i < length) {
            if (src.charAt(i) == separatorChar) {
                if (match) {
                    list.add(src.substring(start, i));
                    match = false;
                }
                start = ++i;
                continue;
            }
            match = true;
            i++;
        }
        if (match) {
            list.add(src.substring(start, i));
        }
        return list.toArray(new String[list.size()]);
    }

    public static String[] split(String src, char separatorChar, boolean trim) {
        if (src == null) {
            return null;
        }
        int length = src.length();
        if (length == 0) {
            return EMPTY_STRING_ARRAY;
        }
        List<String> list = new LinkedList<String>();
        int i = 0;
        int start = 0;
        boolean match = false;
        while (i < length) {
            if (src.charAt(i) == separatorChar) {
                if (match) {
                    if (trim) {
                        list.add(src.substring(start, i).trim());
                    } else {
                        list.add(src.substring(start, i));
                    }
                    match = false;
                }
                start = ++i;
                continue;
            }
            match = true;
            i++;
        }
        if (match) {
            if (trim) {
                list.add(src.substring(start, i).trim());
            } else {
                list.add(src.substring(start, i));
            }
        }
        return list.toArray(new String[list.size()]);
    }

    public static String[] split(String str, String separatorChars) {
        return split(str, separatorChars, -1);
    }

    public static String[] split(String src, String separatorChars, int max) {
        if (src == null) {
            return null;
        }
        int length = src.length();
        if (length == 0) {
            return EMPTY_STRING_ARRAY;
        }
        List<String> list = new LinkedList<String>();
        int sizePlus1 = 1;
        int i = 0;
        int start = 0;
        boolean match = false;
        if (separatorChars == null) {// null表示使用空白作为分隔符
            while (i < length) {
                if (Character.isWhitespace(src.charAt(i))) {
                    if (match) {
                        if (sizePlus1++ == max) {
                            i = length;
                        }
                        list.add(src.substring(start, i));
                        match = false;
                    }
                    start = ++i;
                    continue;
                }
                match = true;
                i++;
            }
        } else if (separatorChars.length() == 1) {// 优化分隔符长度为1的情形
            char sep = separatorChars.charAt(0);
            while (i < length) {
                if (src.charAt(i) == sep) {
                    if (match) {
                        if (sizePlus1++ == max) {
                            i = length;
                        }
                        list.add(src.substring(start, i));
                        match = false;
                    }
                    start = ++i;
                    continue;
                }
                match = true;
                i++;
            }
        } else {// 一般情形
            while (i < length) {
                if (separatorChars.indexOf(src.charAt(i)) >= 0) {
                    if (match) {
                        if (sizePlus1++ == max) {
                            i = length;
                        }
                        list.add(src.substring(start, i));
                        match = false;
                    }
                    start = ++i;
                    continue;
                }
                match = true;
                i++;
            }
        }
        if (match) {
            list.add(src.substring(start, i));
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * 解析字符串，比如: <br>
     * 1. c1='$',c2='-',c3='[',c4=']' 输入字符串：mysql_db$0-2<br>
     * 输出mysql_db[0],mysql_db[1],mysql_db[2]<br>
     * 2. c1='$',c2='-',c3='#',c4='0' 输入字符串：mysql_db$0-2<br>
     * 输出mysql_db#0,mysql_db#1,mysql_db#2<br>
     * 3. c1='$',c2='-',c3='0',c4='0' 输入字符串：mysql_db$0-2<br>
     * 输出mysql_db0,mysql_db1,mysql_db2<br>
     */
    public static String[] split(String src, char c1, char c2, char c3, char c4) {
        if (src == null) {
            return null;
        }
        int length = src.length();
        if (length == 0) {
            return EMPTY_STRING_ARRAY;
        }
        List<String> list = new LinkedList<String>();
        if (src.indexOf(c1) == -1) {
            list.add(src.trim());
        } else {
            String[] s = split(src, c1, true);
            String[] scope = split(s[1], c2, true);
            int min = Integer.parseInt(scope[0]);
            int max = Integer.parseInt(scope[scope.length - 1]);
            if (c3 == '0') {
                for (int x = min; x <= max; x++) {
                    list.add(new StringBuilder(s[0]).append(x).toString());
                }
            } else if (c4 == '0') {
                for (int x = min; x <= max; x++) {
                    list.add(new StringBuilder(s[0]).append(c3).append(x).toString());
                }
            } else {
                for (int x = min; x <= max; x++) {
                    list.add(new StringBuilder(s[0]).append(c3).append(x).append(c4).toString());
                }
            }
        }
        return list.toArray(new String[list.size()]);
    }

    public static String[] split(String src, char fi, char se, char th) {
        return split(src, fi, se, th, '0', '0');
    }

    public static String[] split(String src, char fi, char se, char th, char left, char right) {
        List<String> list = new LinkedList<String>();
        String[] pools = split(src, fi, true);
        for (int i = 0; i < pools.length; i++) {
            if (pools[i].indexOf(se) == -1) {
                list.add(pools[i]);
                continue;
            }
            String[] s = split(pools[i], se, th, left, right);
            for (int j = 0; j < s.length; j++) {
                list.add(s[j]);
            }
        }
        return list.toArray(new String[list.size()]);
    }

}
