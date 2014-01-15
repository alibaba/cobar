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

import java.io.UnsupportedEncodingException;

import com.alibaba.cobar.mysql.BindValue;
import com.alibaba.cobar.mysql.BindValueUtil;
import com.alibaba.cobar.mysql.MySQLMessage;
import com.alibaba.cobar.mysql.PreparedStatement;

/**
 * <pre>
 *  Bytes                      Name
 *  -----                      ----
 *  1                          code
 *  4                          statement_id
 *  1                          flags
 *  4                          iteration_count 
 *  (param_count+7)/8          null_bit_map
 *  1                          new_parameter_bound_flag (if new_params_bound == 1:)
 *  n*2                        type of parameters
 *  n                          values for the parameters   
 *  --------------------------------------------------------------------------------
 *  code:                      always COM_EXECUTE
 *  
 *  statement_id:              statement identifier
 *  
 *  flags:                     reserved for future use. In MySQL 4.0, always 0.
 *                             In MySQL 5.0: 
 *                               0: CURSOR_TYPE_NO_CURSOR
 *                               1: CURSOR_TYPE_READ_ONLY
 *                               2: CURSOR_TYPE_FOR_UPDATE
 *                               4: CURSOR_TYPE_SCROLLABLE
 *  
 *  iteration_count:           reserved for future use. Currently always 1.
 *  
 *  null_bit_map:              A bitmap indicating parameters that are NULL.
 *                             Bits are counted from LSB, using as many bytes
 *                             as necessary ((param_count+7)/8)
 *                             i.e. if the first parameter (parameter 0) is NULL, then
 *                             the least significant bit in the first byte will be 1.
 *  
 *  new_parameter_bound_flag:  Contains 1 if this is the first time
 *                             that "execute" has been called, or if
 *                             the parameters have been rebound.
 *  
 *  type:                      Occurs once for each parameter; 
 *                             The highest significant bit of this 16-bit value
 *                             encodes the unsigned property. The other 15 bits
 *                             are reserved for the type (only 8 currently used).
 *                             This block is sent when parameters have been rebound
 *                             or when a prepared statement is executed for the 
 *                             first time.
 * 
 *  values:                    for all non-NULL values, each parameters appends its value
 *                             as described in Row Data Packet: Binary (column values)
 * @see http://dev.mysql.com/doc/internals/en/execute-packet.html
 * </pre>
 * 
 * @author xianmao.hexm 2012-8-28
 */
public class ExecutePacket extends MySQLPacket {

    public byte code;
    public long statementId;
    public byte flags;
    public long iterationCount;
    public byte[] nullBitMap;
    public byte newParameterBoundFlag;
    public BindValue[] values;
    protected PreparedStatement pstmt;

    public ExecutePacket(PreparedStatement pstmt) {
        this.pstmt = pstmt;
        this.values = new BindValue[pstmt.getParametersNumber()];
    }

    public void read(byte[] data, String charset) throws UnsupportedEncodingException {
        MySQLMessage mm = new MySQLMessage(data);
        packetLength = mm.readUB3();
        packetId = mm.read();
        code = mm.read();
        statementId = mm.readUB4();
        flags = mm.read();
        iterationCount = mm.readUB4();

        // 读取NULL指示器数据
        int parameterCount = values.length;
        nullBitMap = new byte[(parameterCount + 7) / 8];
        for (int i = 0; i < nullBitMap.length; i++) {
            nullBitMap[i] = mm.read();
        }

        // 当newParameterBoundFlag==1时，更新参数类型。
        newParameterBoundFlag = mm.read();
        if (newParameterBoundFlag == (byte) 1) {
            for (int i = 0; i < parameterCount; i++) {
                pstmt.getParametersType()[i] = mm.readUB2();
            }
        }

        // 设置参数类型和读取参数值
        byte[] nullBitMap = this.nullBitMap;
        for (int i = 0; i < parameterCount; i++) {
            BindValue bv = new BindValue();
            bv.type = pstmt.getParametersType()[i];
            if ((nullBitMap[i / 8] & (1 << (i & 7))) != 0) {
                bv.isNull = true;
            } else {
                BindValueUtil.read(mm, bv, charset);
            }
            values[i] = bv;
        }
    }

    @Override
    public int calcPacketSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    protected String getPacketInfo() {
        return "MySQL Execute Packet";
    }

}
