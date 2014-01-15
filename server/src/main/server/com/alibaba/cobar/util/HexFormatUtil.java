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
package com.alibaba.cobar.util;

/**
 * @author xianmao.hexm 2010-9-6
 */
public final class HexFormatUtil {

    public static byte[] fromHex(String src) {
        String[] hex = src.split(" ");
        byte[] b = new byte[hex.length];
        for (int i = 0; i < hex.length; i++) {
            b[i] = (byte) (Integer.parseInt(hex[i], 16) & 0xff);
        }
        return b;
    }

    public static String fromHex(String hexString, String charset) {
        try {
            byte[] b = fromHex(hexString);
            if (charset == null) {
                return new String(b);
            }
            return new String(b, charset);
        } catch (Exception e) {
            return null;
        }
    }

    public static int fromHex2B(String src) {
        byte[] b = fromHex(src);
        int position = 0;
        int i = (b[position++] & 0xff);
        i |= (b[position++] & 0xff) << 8;
        return i;
    }

    public static int fromHex4B(String src) {
        byte[] b = fromHex(src);
        int position = 0;
        int i = (b[position++] & 0xff);
        i |= (b[position++] & 0xff) << 8;
        i |= (b[position++] & 0xff) << 16;
        i |= (b[position++] & 0xff) << 24;
        return i;
    }

    public static long fromHex8B(String src) {
        byte[] b = fromHex(src);
        int position = 0;
        long l = (b[position++] & 0xff);
        l |= (long) (b[position++] & 0xff) << 8;
        l |= (long) (b[position++] & 0xff) << 16;
        l |= (long) (b[position++] & 0xff) << 24;
        l |= (long) (b[position++] & 0xff) << 32;
        l |= (long) (b[position++] & 0xff) << 40;
        l |= (long) (b[position++] & 0xff) << 48;
        l |= (long) (b[position++] & 0xff) << 56;
        return l;
    }

}
