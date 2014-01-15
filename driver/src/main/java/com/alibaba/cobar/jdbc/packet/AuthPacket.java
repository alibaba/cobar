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
package com.alibaba.cobar.jdbc.packet;

import java.io.IOException;
import java.io.OutputStream;

import com.alibaba.cobar.jdbc.Driver;
import com.alibaba.cobar.jdbc.util.ByteUtil;
import com.alibaba.cobar.jdbc.util.StreamUtil;

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
    static {
        byte[] version = Driver.VERSION.getBytes();
        byte[] header = ByteUtil.getBytesWithLength(version.length);
        if ((header.length + version.length) <= FILLER.length) {
            int index = 0;
            for (int i = 0; i < header.length; i++) {
                FILLER[index++] = header[i];
            }
            for (int i = 0; i < version.length; i++) {
                FILLER[index++] = version[i];
            }
        }
    }

    public long clientFlags;
    public long maxPacketSize;
    public int charsetIndex;
    public String user;
    public byte[] password;
    public String database;

    public void write(OutputStream out) throws IOException {
        StreamUtil.writeUB3(out, getPacketLength());
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

    protected int getPacketLength() {
        int size = 32;// 4+4+1+23;
        size += (user == null) ? 1 : user.length() + 1;
        size += (password == null) ? 1 : ByteUtil.getLengthWithBytes(password);
        size += (database == null) ? 1 : database.length() + 1;
        return size;
    }

    @Override
    protected String packetInfo() {
        return "MySQL Authentication Packet";
    }

}
