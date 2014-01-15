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
package com.alibaba.cobar.jdbc.util;

/**
 * @author xianmao.hexm 2012-4-28
 */
public class ByteUtil {

    public static final int getLengthWithBytes(byte[] src) {
        int length = src.length;
        if (length < 251) {
            return 1 + length;
        } else if (length < 0x10000L) {
            return 3 + length;
        } else if (length < 0x1000000L) {
            return 4 + length;
        } else {
            return 9 + length;
        }
    }

    public static final byte[] getBytesWithLength(int length) {
        byte[] bb = null;
        if (length < 251) {
            bb = new byte[1];
            bb[0] = (byte) length;
        } else if (length < 0x10000L) {
            bb = new byte[3];
            bb[0] = (byte) 252;
            bb[1] = (byte) (length & 0xff);
            bb[2] = (byte) (length >>> 8);
        } else if (length < 0x1000000L) {
            bb = new byte[4];
            bb[0] = (byte) 253;
            bb[1] = (byte) (length & 0xff);
            bb[2] = (byte) (length >>> 8);
            bb[3] = (byte) (length >>> 16);
        } else {
            bb = new byte[9];
            bb[0] = (byte) 254;
            bb[1] = (byte) (length & 0xff);
            bb[2] = (byte) (length >>> 8);
            bb[3] = (byte) (length >>> 16);
            bb[4] = (byte) (length >>> 24);
            bb[5] = (byte) (length >>> 32);
            bb[6] = (byte) (length >>> 40);
            bb[7] = (byte) (length >>> 48);
            bb[8] = (byte) (length >>> 56);
        }
        return bb;
    }

}
