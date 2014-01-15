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

/**
 * (created at 2011-8-23)
 */
package com.alibaba.cobar.manager.web.commons;

import org.junit.Assert;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import com.alibaba.cobar.manager.dataobject.xml.UserDO;
import com.alibaba.cobar.manager.util.ConstantDefine;
import com.alibaba.cobar.manager.web.PermissionInterceptor;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 * @author haiqing.zhuhq 2011-9-6
 */
public class PermissionInterceptorTest extends AbstractDependencyInjectionSpringContextTests {
    private PermissionInterceptor permissionInterceptor;

    public void setPermissionInterceptor(PermissionInterceptor permissionInterceptor) {
        this.permissionInterceptor = permissionInterceptor;
    }

    public PermissionInterceptorTest() {
        super();
    }

    @Override
    protected String[] getConfigPaths() {
        return new String[] { "/WEB-INF/cobarManager-servlet.xml" };
    }

    public void testPermissionInter() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/cobarDetail.htm");
        MockHttpSession session = new MockHttpSession();
        UserDO user = new UserDO();
        user.setStatus(ConstantDefine.NORMAL);
        user.setUser_role(ConstantDefine.SYSTEM_ADMIN);
        session.setAttribute("user", user);
        request.setSession(session);

        boolean rst = permissionInterceptor.preHandle(request, new MockHttpServletResponse(), null);
        Assert.assertTrue(rst);
    }

}
