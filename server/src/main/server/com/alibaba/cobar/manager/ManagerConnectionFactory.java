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
package com.alibaba.cobar.manager;

import java.nio.channels.SocketChannel;

import com.alibaba.cobar.CobarPrivileges;
import com.alibaba.cobar.net.FrontendConnection;
import com.alibaba.cobar.net.factory.FrontendConnectionFactory;

/**
 * @author xianmao.hexm
 */
public class ManagerConnectionFactory extends FrontendConnectionFactory {

    @Override
    protected FrontendConnection getConnection(SocketChannel channel) {
        ManagerConnection c = new ManagerConnection(channel);
        c.setPrivileges(new CobarPrivileges());
        c.setQueryHandler(new ManagerQueryHandler(c));
        return c;
    }

}
