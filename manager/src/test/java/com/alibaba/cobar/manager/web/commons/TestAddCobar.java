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

package com.alibaba.cobar.manager.web.commons;

import junit.framework.Assert;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.cobar.manager.dataobject.xml.UserDO;
import com.alibaba.cobar.manager.util.ConstantDefine;
import com.alibaba.cobar.manager.web.action.AddCobar;

public class TestAddCobar extends AbstractDependencyInjectionSpringContextTests {
    private AddCobar addcobar;

    public void setAddcobar(AddCobar addcobar) {
        this.addcobar = addcobar;
    }

    public TestAddCobar() {
        super();
    }

    @Override
    protected String[] getConfigPaths() {
        return new String[] { "/WEB-INF/cobarManager-servlet.xml" };
    }

    public void testAddCobar() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        MockHttpSession session = new MockHttpSession();
        UserDO user = new UserDO();
        user.setStatus(ConstantDefine.NORMAL);
        user.setUser_role(ConstantDefine.CLUSTER_ADMIN);
        session.setAttribute("user", user);
        request.setSession(session);
        request.addParameter("clusterId", "1");
        request.addParameter("host", "1.2.4.3");
        request.addParameter("cobarName", "test");
        request.addParameter("port", "8066");
        request.addParameter("userName", "test");
        request.addParameter("password", "TTT");
        request.addParameter("status", "ACTIVE");
        
        
        ModelAndView mav = addcobar.handleRequest(request, new MockHttpServletResponse());
        Assert.assertEquals("add cobar success", String.valueOf(mav.getModel().get("info")));
    }
}
