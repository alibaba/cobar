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
package com.alibaba.cobar.mysql.bio;

/**
 * @author xianmao.hexm 2011-5-5 上午11:44:31
 */
public interface Channel {

    /**
     * 取得最近活跃时间
     */
    long getLastAcitveTime();

    /**
     * 更新最近活跃时间
     */
    void setLastActiveTime(long time);

    /**
     * 连接通道
     */
    void connect(long timeout) throws Exception;

    /**
     * 是否事务自动递交模式
     */
    boolean isAutocommit();

    /**
     * 通道是否正在执行中
     */
    boolean isRunning();

    /**
     * 设置通道是否正在执行
     */
    void setRunning(boolean running);

    /**
     * 将通道释放到数据源池里
     */
    void release();

    /**
     * 检查通道是否已关闭
     */
    boolean isClosed();

    /**
     * {@link #close()} and ensure that the remote side has closed this channel
     * too
     */
    void kill();

    /**
     * 关闭数据通道 (thread-safe)
     */
    void close();

    /**
     * 关闭未激活的数据通道
     */
    void closeNoActive();

}
