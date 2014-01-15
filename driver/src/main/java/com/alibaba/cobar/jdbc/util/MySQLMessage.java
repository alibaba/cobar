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

import java.util.Arrays;

/**
 * @author xianmao.hexm
 */
public class MySQLMessage {

    private static final long NULL_LENGTH = -1;
    private static final byte[] EMPTY_BYTES = new byte[0];

    private final byte[] data;
    private final int length;
    private int position;

    public MySQLMessage(byte[] data) {
        this.data = data;
        this.length = data.length;
        this.position = 0;
    }

    public int position() {
        return position;
    }

    public void move(int i) {
        position += i;
    }

    public boolean hasRemaining() {
        return length > position;
    }

    public byte read(int i) {
        return data[i];
    }

    public byte read() {
        return data[position++];
    }

    public int readUB2() {
        final byte[] b = this.data;
        int i = b[position++] & 0xff;
        i |= (b[position++] & 0xff) << 8;
        return i;
    }

    public int readUB3() {
        final byte[] b = this.data;
        int i = b[position++] & 0xff;
        i |= (b[position++] & 0xff) << 8;
        i |= (b[position++] & 0xff) << 16;
        return i;
    }

    public long readUB4() {
        final byte[] b = this.data;
        long l = (long) (b[position++] & 0xff);
        l |= (long) (b[position++] & 0xff) << 8;
        l |= (long) (b[position++] & 0xff) << 16;
        l |= (long) (b[position++] & 0xff) << 24;
        return l;
    }

    public long readLong() {
        final byte[] b = this.data;
        long l = (long) (b[position++] & 0xff);
        l |= (long) (b[position++] & 0xff) << 8;
        l |= (long) (b[position++] & 0xff) << 16;
        l |= (long) (b[position++] & 0xff) << 24;
        l |= (long) (b[position++] & 0xff) << 32;
        l |= (long) (b[position++] & 0xff) << 40;
        l |= (long) (b[position++] & 0xff) << 48;
        l |= (long) (b[position++] & 0xff) << 56;
        return l;
    }

    public long readLength() {
        int length = data[position++] & 0xff;
        switch (length) {
        case 251:
            return NULL_LENGTH;
        case 252:
            return readUB2();
        case 253:
            return readUB3();
        case 254:
            return readLong();
        default:
            return length;
        }
    }

    public byte[] readBytes(int length) {
        byte[] ab = new byte[length];
        System.arraycopy(data, position, ab, 0, length);
        position += length;
        return ab;
    }

    public byte[] readBytes() {
        if (position >= length) {
            return EMPTY_BYTES;
        }
        byte[] ab = new byte[length - position];
        System.arraycopy(data, position, ab, 0, ab.length);
        position = length;
        return ab;
    }

    public byte[] readBytesWithNull() {
        final byte[] b = this.data;
        if (position >= length) {
            return EMPTY_BYTES;
        }
        int offset = -1;
        for (int i = position; i < length; i++) {
            if (b[i] == 0) {
                offset = i;
                break;
            }
        }
        switch (offset) {
        case -1:
            byte[] ab1 = new byte[length - position];
            System.arraycopy(b, position, ab1, 0, ab1.length);
            position = length;
            return ab1;
        case 0:
            position++;
            return EMPTY_BYTES;
        default:
            byte[] ab2 = new byte[offset - position];
            System.arraycopy(b, position, ab2, 0, ab2.length);
            position = offset + 1;
            return ab2;
        }
    }

    public byte[] readBytesWithLength() {
        int length = (int) readLength();
        if (length <= 0) {
            return EMPTY_BYTES;
        }
        byte[] ab = new byte[length];
        System.arraycopy(data, position, ab, 0, ab.length);
        position += length;
        return ab;
    }

    public String toString() {
        return new StringBuilder().append(Arrays.toString(data)).toString();
    }

}
