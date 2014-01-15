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

package com.alibaba.cobar.manager.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import com.alibaba.cobar.manager.dataobject.xml.UserDO;
import com.alibaba.cobar.manager.service.XmlAccesser;
import com.alibaba.cobar.manager.web.URLBroker;

/**
 * (created at 2010-7-20)
 * 
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 * @author wenfeng.cenwf 2011-4-7
 */
public class LoginAction extends SimpleFormController implements InitializingBean {

    private XmlAccesser xmlAccesser;

    public void setXmlAccesser(XmlAccesser xmlAccesser) {
        this.xmlAccesser = xmlAccesser;
    }

    @SuppressWarnings("unused")
    private static class Account {

        private String userName;
        private String password;

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        setCommandClass(Account.class);
        if (xmlAccesser == null) {
            throw new IllegalArgumentException("property 'xmlAccesser' is null!");
        }
    }

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request,
                                    HttpServletResponse response,
                                    Object command,
                                    BindException errors) throws Exception {

        Account form = (Account) command;
        UserDO user = xmlAccesser.getUserDAO().validateUser(form.getUserName().trim(), form.getPassword().trim());

        if (user != null) {
            if (logger.isInfoEnabled()) {
                StringBuilder sb = new StringBuilder("User '");
                sb.append(user.getUsername());
                sb.append("' login!");
                logger.info(sb.toString());
            }
            request.getSession().setAttribute("user", user);
            //request.getSession().setMaxInactiveInterval(30);
            String to = (String) request.getSession().getAttribute("lastRequest");
            if (null != to) {
                request.getSession().removeAttribute("click");
                request.getSession().removeAttribute("lastRequest");
                response.sendRedirect(to);
            } else {
                response.sendRedirect(URLBroker.redirectClusterListScreen());
            }
        } else {
            // ??????
            boolean flag = (Boolean) request.getSession().getAttribute("click");
            if (flag) {
                response.sendRedirect(URLBroker.redirectIndexPage("false"));
            } else {
                response.sendRedirect(URLBroker.redirectLogInPage("false"));
            }
        }

        return null;
    }

}
