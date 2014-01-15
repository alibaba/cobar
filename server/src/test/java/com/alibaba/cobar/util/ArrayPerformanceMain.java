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

import java.util.ArrayList;
import java.util.List;

/**
 * @author xianmao.hexm
 */
public class ArrayPerformanceMain {

    public void tArray() {
        byte[] a = new byte[] { 1, 2, 3, 4, 5, 6, 7 };
        System.currentTimeMillis();
        long t1 = System.currentTimeMillis();
        for (int x = 0; x < 1000000; x++) {
            byte[][] ab = new byte[10][];
            for (int i = 0; i < ab.length; i++) {
                ab[i] = a;
            }
        }
        long t2 = System.currentTimeMillis();
        System.out.println("array take time:" + (t2 - t1) + " ms.");
    }

    public void tList() {
        byte[] a = new byte[] { 1, 2, 3, 4, 5, 6, 7 };
        System.currentTimeMillis();
        long t1 = System.currentTimeMillis();
        for (int x = 0; x < 1000000; x++) {
            List<byte[]> ab = new ArrayList<byte[]>(10);
            for (int i = 0; i < ab.size(); i++) {
                ab.add(a);
            }
        }
        long t2 = System.currentTimeMillis();
        System.out.println("list take time:" + (t2 - t1) + " ms.");
    }

}
