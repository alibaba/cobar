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
 * @author xianmao.hexm 2010-1-21 上午10:39:54
 */
public class RandomUtil {
    private static final byte[] bytes = { '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', 'q', 'w', 'e', 'r', 't',
            'y', 'u', 'i', 'o', 'p', 'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'z', 'x', 'c', 'v', 'b', 'n', 'm',
            'Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P', 'A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L', 'Z', 'X',
            'C', 'V', 'B', 'N', 'M' };
    private static final long multiplier = 0x5DEECE66DL;
    private static final long addend = 0xBL;
    private static final long mask = (1L << 48) - 1;
    private static final long integerMask = (1L << 33) - 1;
    private static final long seedUniquifier = 8682522807148012L;

    private static long seed;
    static {
        long s = seedUniquifier + System.nanoTime();
        s = (s ^ multiplier) & mask;
        seed = s;
    }

    public static final byte[] randomBytes(int size) {
        byte[] bb = bytes;
        byte[] ab = new byte[size];
        for (int i = 0; i < size; i++) {
            ab[i] = randomByte(bb);
        }
        return ab;
    }

    private static byte randomByte(byte[] b) {
        int ran = (int) ((next() & integerMask) >>> 16);
        return b[ran % b.length];
    }

    private static long next() {
        long oldSeed = seed;
        long nextSeed = 0L;
        do {
            nextSeed = (oldSeed * multiplier + addend) & mask;
        } while (oldSeed == nextSeed);
        seed = nextSeed;
        return nextSeed;
    }

}
