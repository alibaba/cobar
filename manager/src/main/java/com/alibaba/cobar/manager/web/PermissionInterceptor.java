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
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.alibaba.cobar.manager.dataobject.xml.UserDO;
import com.alibaba.cobar.manager.util.ConstantDefine;

/**
 * @author wenfeng.cenwf 2011-4-2
 */
public class PermissionInterceptor extends HandlerInterceptorAdapter implements InitializingBean {
    private Map<String, Set<String>> nonMatchURIMap;

    public void setNonMatchURIMap(Map<String, Set<String>> nonMatchURIMap) {
        this.nonMatchURIMap = nonMatchURIMap;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (MapUtils.isEmpty(nonMatchURIMap))
            throw new IllegalArgumentException("property 'nonMatchURIMap' is empty!");

    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!permissioned(request)) {
            // TODO redirect
            response.sendRedirect(URLBroker.redirectForbiddenScreen());
            return false;
        }

        return super.preHandle(request, response, handler);
    }

    private boolean permissioned(HttpServletRequest request) {
        UserDO user = (UserDO) request.getSession().getAttribute("user");
        if (user == null) return true;

        String userType = null;
        if (user.getUser_role().equals(ConstantDefine.SYSTEM_ADMIN)) {
            userType = ConstantDefine.SYSTEM_ADMIN;
        } else {
            userType = ConstantDefine.CLUSTER_ADMIN;
        }

        HashSet<String> set = (HashSet<String>) nonMatchURIMap.get(userType);
        String url = request.getServletPath().trim();

        return set.contains(url);
    }

}
