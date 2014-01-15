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

package com.alibaba.cobar.manager.web;

/**
 * (created at 2010-7-20)
 * 
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 * @author wenfeng.cenwf 2011-3-27
 * @author haiqing.zhuhq 2011-12-14
 */
public class URLBroker {

    public static String redirectClusterListScreen() {
        return "clusterList.htm";
    }

    public static String redirectLogInPage(String result) {
        if (result.equalsIgnoreCase("null")) {
            return "index.htm";
        } else if ("login".equals(result)) {
            return "login.htm";
        }
        return "login.htm?result=" + result;
    }

    public static String redirectIndexPage(String result) {
        if (result.equalsIgnoreCase("null")) {
            return "index.htm";
        }
        return "index.htm?result=" + result;
    }

    public static String redirectForbiddenScreen() {
        return "forbidden.htm";
    }

}
