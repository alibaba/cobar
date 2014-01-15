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

import java.nio.ByteBuffer;

import com.alibaba.cobar.mysql.BufferUtil;
import com.alibaba.cobar.mysql.MySQLMessage;
import com.alibaba.cobar.net.BackendConnection;

/**
 * From client to server when the client do heartbeat between cobar cluster.
 * 
 * <pre>
 * Bytes         Name
 * -----         ----
 * 1             command
 * n             id
 * 
 * @author haiqing.zhuhq 2012-07-06
 */
public class HeartbeatPacket extends MySQLPacket {

    public byte command;
    public long id;

    public void read(byte[] data) {
        MySQLMessage mm = new MySQLMessage(data);
        packetLength = mm.readUB3();
        packetId = mm.read();
        command = mm.read();
        id = mm.readLength();
    }

    @Override
    public void write(BackendConnection c) {
        ByteBuffer buffer = c.allocate();
        BufferUtil.writeUB3(buffer, calcPacketSize());
        buffer.put(packetId);
        buffer.put(command);
        BufferUtil.writeLength(buffer, id);
        c.write(buffer);
    }

    @Override
    public int calcPacketSize() {
        return 1 + BufferUtil.getLength(id);
    }

    @Override
    protected String getPacketInfo() {
        return "Cobar Heartbeat Packet";
    }

}
