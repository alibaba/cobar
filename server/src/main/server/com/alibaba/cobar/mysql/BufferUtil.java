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
package com.alibaba.cobar.mysql;

import java.nio.ByteBuffer;

/**
 * @author xianmao.hexm 2010-9-3 下午02:29:44
 */
public class BufferUtil {

    public static final void writeUB2(ByteBuffer buffer, int i) {
        buffer.put((byte) (i & 0xff));
        buffer.put((byte) (i >>> 8));
    }

    public static final void writeUB3(ByteBuffer buffer, int i) {
        buffer.put((byte) (i & 0xff));
        buffer.put((byte) (i >>> 8));
        buffer.put((byte) (i >>> 16));
    }

    public static final void writeInt(ByteBuffer buffer, int i) {
        buffer.put((byte) (i & 0xff));
        buffer.put((byte) (i >>> 8));
        buffer.put((byte) (i >>> 16));
        buffer.put((byte) (i >>> 24));
    }

    public static final void writeFloat(ByteBuffer buffer, float f) {
        writeInt(buffer, Float.floatToIntBits(f));
    }

    public static final void writeUB4(ByteBuffer buffer, long l) {
        buffer.put((byte) (l & 0xff));
        buffer.put((byte) (l >>> 8));
        buffer.put((byte) (l >>> 16));
        buffer.put((byte) (l >>> 24));
    }

    public static final void writeLong(ByteBuffer buffer, long l) {
        buffer.put((byte) (l & 0xff));
        buffer.put((byte) (l >>> 8));
        buffer.put((byte) (l >>> 16));
        buffer.put((byte) (l >>> 24));
        buffer.put((byte) (l >>> 32));
        buffer.put((byte) (l >>> 40));
        buffer.put((byte) (l >>> 48));
        buffer.put((byte) (l >>> 56));
    }

    public static final void writeDouble(ByteBuffer buffer, double d) {
        writeLong(buffer, Double.doubleToLongBits(d));
    }

    public static final void writeLength(ByteBuffer buffer, long l) {
        if (l < 251) {
            buffer.put((byte) l);
        } else if (l < 0x10000L) {
            buffer.put((byte) 252);
            writeUB2(buffer, (int) l);
        } else if (l < 0x1000000L) {
            buffer.put((byte) 253);
            writeUB3(buffer, (int) l);
        } else {
            buffer.put((byte) 254);
            writeLong(buffer, l);
        }
    }

    public static final void writeWithNull(ByteBuffer buffer, byte[] src) {
        buffer.put(src);
        buffer.put((byte) 0);
    }

    public static final void writeWithLength(ByteBuffer buffer, byte[] src) {
        int length = src.length;
        if (length < 251) {
            buffer.put((byte) length);
        } else if (length < 0x10000L) {
            buffer.put((byte) 252);
            writeUB2(buffer, length);
        } else if (length < 0x1000000L) {
            buffer.put((byte) 253);
            writeUB3(buffer, length);
        } else {
            buffer.put((byte) 254);
            writeLong(buffer, length);
        }
        buffer.put(src);
    }

    public static final void writeWithLength(ByteBuffer buffer, byte[] src, byte nullValue) {
        if (src == null) {
            buffer.put(nullValue);
        } else {
            writeWithLength(buffer, src);
        }
    }

    public static final int getLength(long length) {
        if (length < 251) {
            return 1;
        } else if (length < 0x10000L) {
            return 3;
        } else if (length < 0x1000000L) {
            return 4;
        } else {
            return 9;
        }
    }

    public static final int getLength(byte[] src) {
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

}
