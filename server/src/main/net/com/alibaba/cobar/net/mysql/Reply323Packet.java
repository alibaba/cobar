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
package com.alibaba.cobar.net.mysql;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.alibaba.cobar.mysql.BufferUtil;
import com.alibaba.cobar.mysql.StreamUtil;
import com.alibaba.cobar.net.BackendConnection;

/**
 * @author xianmao.hexm
 */
public class Reply323Packet extends MySQLPacket {

    public byte[] seed;

    public void write(OutputStream out) throws IOException {
        StreamUtil.writeUB3(out, calcPacketSize());
        StreamUtil.write(out, packetId);
        if (seed == null) {
            StreamUtil.write(out, (byte) 0);
        } else {
            StreamUtil.writeWithNull(out, seed);
        }
    }

    @Override
    public void write(BackendConnection c) {
        ByteBuffer buffer = c.allocate();
        BufferUtil.writeUB3(buffer, calcPacketSize());
        buffer.put(packetId);
        if (seed == null) {
            buffer.put((byte) 0);
        } else {
            BufferUtil.writeWithNull(buffer, seed);
        }
        c.write(buffer);
    }

    @Override
    public int calcPacketSize() {
        return seed == null ? 1 : seed.length + 1;
    }

    @Override
    protected String getPacketInfo() {
        return "MySQL Auth323 Packet";
    }

}
