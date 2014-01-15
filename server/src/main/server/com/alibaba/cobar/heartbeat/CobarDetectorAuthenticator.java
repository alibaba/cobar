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
package com.alibaba.cobar.heartbeat;

import com.alibaba.cobar.mysql.CharsetUtil;
import com.alibaba.cobar.net.NIOHandler;
import com.alibaba.cobar.net.mysql.ErrorPacket;
import com.alibaba.cobar.net.mysql.HandshakePacket;
import com.alibaba.cobar.net.mysql.OkPacket;

/**
 * @author xianmao.hexm
 */
public class CobarDetectorAuthenticator implements NIOHandler {

    private final CobarDetector source;

    public CobarDetectorAuthenticator(CobarDetector source) {
        this.source = source;
    }

    @Override
    public void handle(byte[] data) {
        CobarDetector source = this.source;
        HandshakePacket hsp = source.getHandshake();
        if (hsp == null) {
            // 设置握手数据包
            hsp = new HandshakePacket();
            hsp.read(data);
            source.setHandshake(hsp);

            // 设置字符集编码
            int charsetIndex = (hsp.serverCharsetIndex & 0xff);
            String charset = CharsetUtil.getCharset(charsetIndex);
            if (charset != null) {
                source.setCharsetIndex(charsetIndex);
            } else {
                throw new RuntimeException("Unknown charsetIndex:" + charsetIndex);
            }

            // 发送认证数据包
            source.authenticate();
        } else { // 处理认证结果
            switch (data[4]) {
            case OkPacket.FIELD_COUNT:
                source.setHandler(new CobarDetectorHandler(source));
                source.setAuthenticated(true);
                source.heartbeat();// 认证成功后，发起心跳。
                break;
            case ErrorPacket.FIELD_COUNT:
                ErrorPacket err = new ErrorPacket();
                err.read(data);
                throw new RuntimeException(new String(err.message));
            default:
                throw new RuntimeException("Unknown packet");
            }
        }
    }

}
