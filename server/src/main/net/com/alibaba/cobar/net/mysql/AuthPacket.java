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

import com.alibaba.cobar.config.Capabilities;
import com.alibaba.cobar.mysql.BufferUtil;
import com.alibaba.cobar.mysql.MySQLMessage;
import com.alibaba.cobar.mysql.StreamUtil;
import com.alibaba.cobar.net.BackendConnection;

/**
 * From client to server during initial handshake.
 * 
 * <pre>
 * Bytes                        Name
 * -----                        ----
 * 4                            client_flags
 * 4                            max_packet_size
 * 1                            charset_number
 * 23                           (filler) always 0x00...
 * n (Null-Terminated String)   user
 * n (Length Coded Binary)      scramble_buff (1 + x bytes)
 * n (Null-Terminated String)   databasename (optional)
 * 
 * @see http://forge.mysql.com/wiki/MySQL_Internals_ClientServer_Protocol#Client_Authentication_Packet
 * </pre>
 * 
 * @author xianmao.hexm 2010-7-15 下午04:35:34
 */
public class AuthPacket extends MySQLPacket {
    private static final byte[] FILLER = new byte[23];

    public long clientFlags;
    public long maxPacketSize;
    public int charsetIndex;
    public byte[] extra;// from FILLER(23)
    public String user;
    public byte[] password;
    public String database;

    public void read(byte[] data) {
        MySQLMessage mm = new MySQLMessage(data);
        packetLength = mm.readUB3();
        packetId = mm.read();
        clientFlags = mm.readUB4();
        maxPacketSize = mm.readUB4();
        charsetIndex = (mm.read() & 0xff);
        // read extra
        int current = mm.position();
        int len = (int) mm.readLength();
        if (len > 0 && len < FILLER.length) {
            byte[] ab = new byte[len];
            System.arraycopy(mm.bytes(), mm.position(), ab, 0, len);
            this.extra = ab;
        }
        mm.position(current + FILLER.length);
        user = mm.readStringWithNull();
        password = mm.readBytesWithLength();
        if (((clientFlags & Capabilities.CLIENT_CONNECT_WITH_DB) != 0) && mm.hasRemaining()) {
            database = mm.readStringWithNull();
        }
    }

    public void write(OutputStream out) throws IOException {
        StreamUtil.writeUB3(out, calcPacketSize());
        StreamUtil.write(out, packetId);
        StreamUtil.writeUB4(out, clientFlags);
        StreamUtil.writeUB4(out, maxPacketSize);
        StreamUtil.write(out, (byte) charsetIndex);
        out.write(FILLER);
        if (user == null) {
            StreamUtil.write(out, (byte) 0);
        } else {
            StreamUtil.writeWithNull(out, user.getBytes());
        }
        if (password == null) {
            StreamUtil.write(out, (byte) 0);
        } else {
            StreamUtil.writeWithLength(out, password);
        }
        if (database == null) {
            StreamUtil.write(out, (byte) 0);
        } else {
            StreamUtil.writeWithNull(out, database.getBytes());
        }
    }

    @Override
    public void write(BackendConnection c) {
        ByteBuffer buffer = c.allocate();
        BufferUtil.writeUB3(buffer, calcPacketSize());
        buffer.put(packetId);
        BufferUtil.writeUB4(buffer, clientFlags);
        BufferUtil.writeUB4(buffer, maxPacketSize);
        buffer.put((byte) charsetIndex);
        buffer = c.writeToBuffer(FILLER, buffer);
        if (user == null) {
            buffer = c.checkWriteBuffer(buffer, 1);
            buffer.put((byte) 0);
        } else {
            byte[] userData = user.getBytes();
            buffer = c.checkWriteBuffer(buffer, userData.length + 1);
            BufferUtil.writeWithNull(buffer, userData);
        }
        if (password == null) {
            buffer = c.checkWriteBuffer(buffer, 1);
            buffer.put((byte) 0);
        } else {
            buffer = c.checkWriteBuffer(buffer, BufferUtil.getLength(password));
            BufferUtil.writeWithLength(buffer, password);
        }
        if (database == null) {
            buffer = c.checkWriteBuffer(buffer, 1);
            buffer.put((byte) 0);
        } else {
            byte[] databaseData = database.getBytes();
            buffer = c.checkWriteBuffer(buffer, databaseData.length + 1);
            BufferUtil.writeWithNull(buffer, databaseData);
        }
        c.write(buffer);
    }

    @Override
    public int calcPacketSize() {
        int size = 32;// 4+4+1+23;
        size += (user == null) ? 1 : user.length() + 1;
        size += (password == null) ? 1 : BufferUtil.getLength(password);
        size += (database == null) ? 1 : database.length() + 1;
        return size;
    }

    @Override
    protected String getPacketInfo() {
        return "MySQL Authentication Packet";
    }

}
