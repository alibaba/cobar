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

import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.alibaba.cobar.manager.dataobject.xml.UserDO;

/**
 * (created at 2010-7-20)
 * 
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 * @author wenfeng.cenwf 2011-4-2
 * @author haiqing.zhuhq 2011-6-20
 */
public class AuthenticationInterceptor extends HandlerInterceptorAdapter implements InitializingBean {

    private Set<String> nonMatchURISet = new HashSet<String>();
    private Set<String> switchURISet = new HashSet<String>();

    public void setNonMatchURISet(Set<String> nonMatchURISet) {
        this.nonMatchURISet = nonMatchURISet;
    }

    public void setSwitchURISet(Set<String> switchURISet) {
        this.switchURISet = switchURISet;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (nonMatchURISet == null) throw new IllegalArgumentException("property 'nonMatchURISet' is null!");
        if (switchURISet == null) throw new IllegalArgumentException("property 'switchURISet' is null!");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        boolean click = false;
        try {
            click = Boolean.parseBoolean(request.getParameter("click").trim());
        } catch (NullPointerException e) {
            click = false;
        }
        request.getSession().setAttribute("click", click);

        if (!authenticated(request)) {
            // TODO redirect
            if ((Boolean) request.getSession().getAttribute("click")) {
                response.sendRedirect(URLBroker.redirectIndexPage("login"));
                return false;
            }
            response.sendRedirect(URLBroker.redirectLogInPage("login"));
            return false;
        }

        return super.preHandle(request, response, handler);
    }

    private boolean authenticated(HttpServletRequest request) {

        UserDO o = (UserDO) request.getSession().getAttribute("user");
        if (o != null) {
            return true;
        }
        String url = request.getServletPath().trim();

        if (nonMatchURISet.contains(url)) {
            return true;
        } else if (switchURISet.contains(url)) {
            String qString = request.getQueryString();

            StringBuilder sb = new StringBuilder(url.substring(1));
            if (null != qString) {
                sb.append("?").append(qString);
            }
            request.getSession().setAttribute("lastRequest", sb.toString());
        }
        return false;

    }
}
