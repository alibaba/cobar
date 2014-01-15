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

import com.alibaba.cobar.jdbc.util.MySQLMessage;

/**
 * From server to client after command, if no error and result set -- that is,
 * if the command was a query which returned a result set. The Result Set Header
 * Packet is the first of several, possibly many, packets that the server sends
 * for result sets. The order of packets for a result set is:
 * 
 * <pre>
 * (Result Set Header Packet)   the number of columns
 * (Field Packets)              column descriptors
 * (EOF Packet)                 marker: end of Field Packets
 * (Row Data Packets)           row contents
 * (EOF Packet)                 marker: end of Data Packets
 * 
 * Bytes                        Name
 * -----                        ----
 * 1-9   (Length-Coded-Binary)  field_count
 * 1-9   (Length-Coded-Binary)  extra
 * 
 * @see http://forge.mysql.com/wiki/MySQL_Internals_ClientServer_Protocol#Result_Set_Header_Packet
 * </pre>
 * 
 * @author xianmao.hexm 2010-7-22 下午05:59:55
 */
public class ResultSetHeaderPacket extends MySQLPacket {

    public int fieldCount;
    public long extra;

    public void read(BinaryPacket data) {
        this.packetLength = data.packetLength;
        this.packetId = data.packetId;
        MySQLMessage mm = new MySQLMessage(data.value);
        this.fieldCount = (int) mm.readLength();
        if (mm.hasRemaining()) {
            this.extra = mm.readLength();
        }
    }

    @Override
    protected String packetInfo() {
        return "MySQL ResultSetHeader Packet";
    }

}
