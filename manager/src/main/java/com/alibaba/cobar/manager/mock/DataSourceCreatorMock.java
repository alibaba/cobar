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

package com.alibaba.cobar.manager.mock;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;

import com.alibaba.cobar.manager.dao.delegate.DataSourceFactory;

public class DataSourceCreatorMock implements DataSourceFactory, InitializingBean {
    @Override
    public DataSource createDataSource(String ip, int port, String user, String password) {
        return new SimpleDateSource();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // TODO Auto-generated method stub

    }
}
