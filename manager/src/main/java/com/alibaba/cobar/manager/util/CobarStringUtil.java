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

package com.alibaba.cobar.manager.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.util.HtmlUtils;

/**
 * (created at 2010-7-26)
 * 
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 * @author haiqing.zhuhq 2011-9-15
 */
public class CobarStringUtil {
    private static final int DEFAULT_INDEX = -1;

    public static String toSqlEscapedString(Object obj) {
        if (obj == null) return "NULL";
        String raw = obj.toString();
        StringBuilder sb = new StringBuilder("\'");
        for (int i = 0; i < raw.length(); ++i) {
            char c = raw.charAt(i);
            if ('\\' == c) {
                sb.append("\\\\");
            } else if ('\'' == c) {
                sb.append("\\\'");
            } else {
                sb.append(c);
            }
        }
        sb.append('\'');
        return sb.toString();
    }

    public static String toSqlEscapedObject(Object obj) {
        if (obj == null) return "NULL";
        if (obj instanceof String) {
            return toSqlEscapedString(obj);
        } else {
            return String.valueOf(obj);
        }
    }

    /**
     * @return negative number if there is no common start from left side, or
     *         any of the string is empty
     */
    private static int indexOfLastEqualCharacter(String str1, String str2) {
        if (StringUtils.isEmpty(str1) || StringUtils.isEmpty(str2)) return -1;
        String shortOne = (str1.length() < str2.length()) ? str1 : str2;
        String longOne = (str2 == shortOne) ? str1 : str2;
        final int shortSize = shortOne.length();
        for (int i = 0; i < shortSize; ++i) {
            char cs = shortOne.charAt(i);
            char cl = longOne.charAt(i);
            if (cs != cl) return i - 1;
        }
        return shortOne.length() - 1;
    }

    public static String mergeListedStringWithJoin(String[] input, String sep) {
        List<String> rst = mergeListedString(input);
        return StringUtils.join(rst, ",");
    }

    /**
     * e.g. {"mysql_1","mysql_2","mysql_3","mysql_5"} will return
     * {"mysql_$1-3","mysql_5"}<br/>
     * only merge last number
     */
    public static List<String> mergeListedString(String[] input) {
        if (input == null || input.length < 1) return Collections.emptyList();
        if (input.length == 1) {
            List<String> rst = new ArrayList<String>(1);
            rst.add(input[0]);
            return rst;
        }
        List<String> list = new ArrayList<String>(input.length);
        for (String str : input)
            list.add(str);
        Collections.sort(list, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                if (StringUtils.equals(o1, o2)) return 0;
                if (o1.length() == o2.length()) return o1.compareTo(o2);
                return o1.length() < o2.length() ? -1 : 1;
            }
        });

        List<String> rst = new ArrayList<String>();

        String prefix = null;
        Integer from = null;
        Integer to = null;
        String last = list.get(0);
        for (int i = 1; i < list.size(); ++i) {
            String cur = list.get(i);
            if (StringUtils.equals(last, cur)) continue;
            int commonInd = indexOfLastEqualCharacter(last, cur);

            boolean isCon = false;

            if (commonInd >= 0) {
                String suffixLast = last.substring(1 + commonInd);
                String suffixCur = cur.substring(1 + commonInd);
                try {
                    int il = Integer.parseInt(suffixLast);
                    int ic = Integer.parseInt(suffixCur);
                    if (ic - il == 1) isCon = true;
                } catch (Exception e) {
                }
            }

            if (isCon) {
                if (prefix == null) prefix = last.substring(0, commonInd + 1);
                if (from == null) from = Integer.parseInt(last.substring(commonInd + 1));
                to = Integer.parseInt(cur.substring(commonInd + 1));
            } else if (prefix != null) {
                rst.add(new StringBuilder(prefix).append('$').append(from).append('-').append(to).toString());
                prefix = null;
                from = to = null;
            } else {
                rst.add(last);
            }
            last = cur;
        }

        if (prefix != null) {
            rst.add(new StringBuilder(prefix).append('$').append(from).append('-').append(to).toString());
            prefix = null;
            from = to = null;
        } else {
            rst.add(last);
        }

        return rst;
    }

    public static String htmlEscapedString(String str) {
        if (null != str) {
            return HtmlUtils.htmlEscape(str);
        }
        return null;
    }

    /**
     * <pre>
     * ???????????????§Ù?? ??src = "offer_group[4]", l='[', r=']'???
     * ?????Piar<String,Integer>("offer", 4);
     * ??src = "offer_group", l='[', r=']'??? 
     * ????Pair<String, Integer>("offer",-1);
     * </pre>
     */
    public static final Pair<String, Integer> splitIndex(String src, char l, char r) {
        if (src == null) return null;
        int length = src.length();
        if (length == 0) return new Pair<String, Integer>("", DEFAULT_INDEX);
        if (src.charAt(length - 1) != r) return new Pair<String, Integer>(src, DEFAULT_INDEX);
        int offset = src.lastIndexOf(l);
        if (offset == -1) return new Pair<String, Integer>(src, DEFAULT_INDEX);
        int index = DEFAULT_INDEX;
        try {
            index = Integer.parseInt(src.substring(offset + 1, length - 1));
        } catch (NumberFormatException e) {
            return new Pair<String, Integer>(src, DEFAULT_INDEX);
        }
        return new Pair<String, Integer>(src.substring(0, offset), index);
    }

}
