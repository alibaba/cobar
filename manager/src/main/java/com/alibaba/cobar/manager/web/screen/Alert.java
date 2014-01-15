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

package com.alibaba.cobar.manager.web.screen;

import static com.alibaba.cobar.manager.util.ConstantDefine.CHOOSE_COBAR;
import static com.alibaba.cobar.manager.util.ConstantDefine.CHOOSE_DATANODE;
import static com.alibaba.cobar.manager.util.ConstantDefine.CONNECTION_FAIL;
import static com.alibaba.cobar.manager.util.ConstantDefine.DATANODE_DIFF;
import static com.alibaba.cobar.manager.util.ConstantDefine.LOGIN;
import static com.alibaba.cobar.manager.util.ConstantDefine.PASSWORD_NULL;
import static com.alibaba.cobar.manager.util.ConstantDefine.UNKNOW;
import static com.alibaba.cobar.manager.util.ConstantDefine.USER_NULL;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class Alert extends AbstractController implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        int reason = 0;
        String result = null;
        try {
            reason = Integer.parseInt(request.getParameter("reason").trim());
        } catch (NullPointerException e) {
            reason = 0;
        }
        result = typeMap.get(reason);
        if (null == result) {
            result = typeMap.get(UNKNOW);
        }
        return new ModelAndView("failure", "reason", result);
    }

    private static final Map<Integer, String> typeMap = new HashMap<Integer, String>();
    static {
        typeMap.put(UNKNOW, "unknown");
        typeMap.put(CHOOSE_COBAR, "choose cobar!");
        typeMap.put(CONNECTION_FAIL, "cann't connect to cobar in cluster");
        typeMap.put(DATANODE_DIFF, "datanodes index is not consistent");
        typeMap.put(LOGIN, "LOGIN");
        typeMap.put(USER_NULL, "username is null");
        typeMap.put(PASSWORD_NULL, "password is null");
        typeMap.put(CHOOSE_DATANODE, "choose datanode");
    }

}
