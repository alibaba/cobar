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
import com.alibaba.cobar.mysql.SecurityUtil;
import com.alibaba.cobar.net.NIOHandler;
import com.alibaba.cobar.net.mysql.EOFPacket;
import com.alibaba.cobar.net.mysql.ErrorPacket;
import com.alibaba.cobar.net.mysql.HandshakePacket;
import com.alibaba.cobar.net.mysql.OkPacket;
import com.alibaba.cobar.net.mysql.Reply323Packet;

/**
 * @author xianmao.hexm
 */
public class MySQLDetectorAuthenticator implements NIOHandler {

    private final MySQLDetector source;

    public MySQLDetectorAuthenticator(MySQLDetector source) {
        this.source = source;
    }

    @Override
    public void handle(byte[] data) {
        MySQLDetector source = this.source;
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
        } else {
            switch (data[4]) {
            case OkPacket.FIELD_COUNT:
                source.setHandler(new MySQLDetectorHandler(source));
                source.setAuthenticated(true);
                source.heartbeat();// 成功后发起心跳。
                break;
            case ErrorPacket.FIELD_COUNT:
                ErrorPacket err = new ErrorPacket();
                err.read(data);
                throw new RuntimeException(new String(err.message));
            case EOFPacket.FIELD_COUNT:
                auth323(data[3], hsp.seed);
                break;
            default:
                throw new RuntimeException("Unknown packet");
            }
        }
    }

    /**
     * 发送323响应认证数据包
     */
    private void auth323(byte packetId, byte[] seed) {
        Reply323Packet r323 = new Reply323Packet();
        r323.packetId = ++packetId;
        String pass = source.getPassword();
        if (pass != null && pass.length() > 0) {
            r323.seed = SecurityUtil.scramble323(pass, new String(seed)).getBytes();
        }
        r323.write(source);
    }

}
