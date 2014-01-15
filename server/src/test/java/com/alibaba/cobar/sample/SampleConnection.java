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
package com.alibaba.cobar.sample;

import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import com.alibaba.cobar.config.ErrorCode;
import com.alibaba.cobar.net.FrontendConnection;

/**
 * @author xianmao.hexm 2011-4-21 上午11:22:57
 */
public class SampleConnection extends FrontendConnection {
    private static final Logger LOGGER = Logger.getLogger(SampleConnection.class);

    public SampleConnection(SocketChannel channel) {
        super(channel);
    }

    @Override
    public void error(int errCode, Throwable t) {
        LOGGER.warn(toString(), t);
        switch (errCode) {
        case ErrorCode.ERR_HANDLE_DATA:
            writeErrMessage(ErrorCode.ER_YES, t.getMessage());
            break;
        default:
            close();
        }
    }

}
