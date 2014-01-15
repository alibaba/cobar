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
package com.alibaba.cobar.server.session;

import com.alibaba.cobar.net.FrontendConnection;
import com.alibaba.cobar.route.RouteResultset;

/**
 * @author xianmao.hexm 2012-7-11
 */
public interface Session {

    /**
     * 取得源端连接
     */
    FrontendConnection getSource();

    /**
     * 取得当前目标端数量
     */
    int getTargetCount();

    /**
     * 开启一个会话执行
     */
    void execute(RouteResultset rrs, int type);

    /**
     * 提交一个会话执行
     */
    void commit();

    /**
     * 回滚一个会话执行
     */
    void rollback();

    /**
     * 取消一个正在执行中的会话
     * 
     * @param sponsor 如果发起者为null，则表示由自己发起。
     */
    void cancel(FrontendConnection sponsor);

    /**
     * 终止会话，必须在关闭源端连接后执行该方法。
     */
    void terminate();

}
