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
package com.alibaba.cobar.jdbc;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

/**
 * @author xianmao.hexm 2012-4-27
 */
public class UrlProvider {
    private static final String URL_COBAR_PREFIX = "jdbc:cobar://";
    private static final String URL_MYSQL_PREFIX = "jdbc:mysql://";
    private static final int PREFIX_LENGTH = URL_COBAR_PREFIX.length();
    private static final int SOCKET_CONNECT_TIMEOUT = 10 * 1000;
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final Random RANDOM = new Random();

    /**
     * 只处理非空并且以jdbc:cobar://开头的url
     */
    public static final String getUrl(String url, Properties info) throws SQLException {
        if (url != null && url.regionMatches(true, 0, URL_COBAR_PREFIX, 0, PREFIX_LENGTH)) {
            UrlConnection c = null;
            try {
                ConnectInfo ci = parseUrl(url, info);
                c = new UrlConnection(ci.host, ci.port, ci.user, ci.password, ci.database);
                c.connect(SOCKET_CONNECT_TIMEOUT);
                return selectUrl(c.getServerList(), url, ci);
            } catch (Throwable e) {
                throw new SQLException(e);
            } finally {
                if (c != null) {
                    c.close();
                }
            }
        } else {
            return url;
        }
    }

    /**
     * 取得MySQL格式的URL
     */
    public static final String getMySQLUrl(String url) {
        if (url != null && url.regionMatches(true, 0, URL_COBAR_PREFIX, 0, PREFIX_LENGTH)) {
            StringBuilder sb = new StringBuilder();
            sb.append(URL_MYSQL_PREFIX).append(url.substring(PREFIX_LENGTH));
            url = sb.toString();
        }
        return url;
    }

    private static ConnectInfo parseUrl(String url, Properties info) {
        ConnectInfo ci = new ConnectInfo();
        ci.user = info.getProperty("user");
        ci.password = info.getProperty("password");
        ci.database = info.getProperty("DBNAME");

        // 解析参数
        int index = url.indexOf('?');
        if (index != -1) {
            String paramString = url.substring(index + 1);
            url = url.substring(0, index);
            ci.paramString = paramString;

            String[] params = split(paramString, '&');
            for (String param : params) {
                int indexOfEquals = 0;
                if (param != null && (indexOfEquals = param.indexOf('=')) != -1) {
                    String key = param.substring(0, indexOfEquals);
                    if ("user".equals(key)) {
                        if (indexOfEquals + 1 < param.length()) {
                            ci.user = param.substring(indexOfEquals + 1);
                        }
                    } else if ("password".equals(key)) {
                        if (indexOfEquals + 1 < param.length()) {
                            ci.password = param.substring(indexOfEquals + 1);
                        }
                    } else if ("DBNAME".equals(key)) {
                        if (indexOfEquals + 1 < param.length()) {
                            ci.database = param.substring(indexOfEquals + 1);
                        }
                    }
                }
            }
        }

        // 解析host/port/database
        url = url.substring(PREFIX_LENGTH);
        String hostStuff = null;
        int index2 = url.indexOf('/');
        if (index2 != -1) {
            hostStuff = url.substring(0, index2);
            if (index2 + 1 < url.length()) {
                ci.database = url.substring(index2 + 1);
            }
        } else {
            hostStuff = url;
        }
        // 如果有多个主机只取第一个
        int index3 = hostStuff.indexOf(',');
        if (index3 != -1) {
            hostStuff = hostStuff.substring(0, index3);
        }
        int index4 = hostStuff.indexOf(':');
        if (index4 != -1) {
            ci.host = hostStuff.substring(0, index4).trim();
            if (index4 + 1 < hostStuff.length()) {
                ci.port = Integer.parseInt(hostStuff.substring(index4 + 1).trim());
            }
        } else {
            ci.host = hostStuff.trim();
            ci.port = 8066;
        }

        return ci;
    }

    /**
     * 结合权重和随机策略
     */
    private static String selectUrl(List<CobarNode> list, String originUrl, ConnectInfo info) {
        int total = 0;
        for (CobarNode node : list) {
            total += node.getWeight();
        }
        // 如果总权重小于等于零则使用原来的url
        if (total <= 0) {
            return originUrl;
        }
        int rnd = 1 + RANDOM.nextInt(total);
        String host = null;
        for (CobarNode node : list) {
            if ((rnd -= node.getWeight()) <= 0) {
                host = node.getHost();
                break;
            }
        }
        if (host == null) {
            int i = RANDOM.nextInt(list.size());
            host = list.get(i).getHost();
        }

        // 无法取得新的host，则返回原来的url。
        if (host == null) {
            return originUrl;
        }

        // 生成新的URL
        StringBuilder url = new StringBuilder();
        url.append(URL_MYSQL_PREFIX).append(host).append(':').append(info.port);
        if (info.database != null) {
            url.append('/').append(info.database);
        }
        if (info.paramString != null) {
            url.append('?').append(info.paramString);
        }
        return url.toString();
    }

    private static String[] split(String src, char separatorChar) {
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

    private static class ConnectInfo {
        private String host;
        private int port;
        private String user;
        private String password;
        private String database;
        private String paramString;
    }

}
