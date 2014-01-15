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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import com.alibaba.cobar.manager.util.FluenceHashMap;

/**
 * @author haiqing.zhuhq 2011-8-17
 */
public class ChooseIndexPage extends AbstractController implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        int maxIndex = Integer.parseInt(request.getParameter("maxIndex"));

        List<Integer> indexList = new ArrayList<Integer>(maxIndex);

        for (int i = 0; i < maxIndex; i++) {
            indexList.add(i);
        }

        return new ModelAndView("c_datanodeIndex", new FluenceHashMap<String, Object>().putKeyValue("indexList", indexList));
    }

}
