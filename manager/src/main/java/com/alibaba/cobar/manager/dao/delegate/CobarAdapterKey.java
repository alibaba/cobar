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

package com.alibaba.cobar.manager.dao.delegate;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * (created at 2010-7-27)
 * 
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class CobarAdapterKey {
    final private String ip;
    final private int port;
    final private String user;
    final private String password;

    public CobarAdapterKey(String ip, int port, String user, String password) {
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.password = password;
    }

    @Override
    public int hashCode() {
        String join = StringUtils.join(new Object[] { ip, port, user, password }, "\r\n");
        return join.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof CobarAdapterKey)) return false;
        CobarAdapterKey that = (CobarAdapterKey) obj;
        return StringUtils.equals(this.ip, that.ip)
               && this.port == that.port
               && StringUtils.equals(this.user, that.user)
               && StringUtils.equals(this.password, that.password);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("ip", ip)
                                        .append("port", port)
                                        .append("user", user)
                                        .append("password", password)
                                        .toString();
    }
}
