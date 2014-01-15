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
 * @author xianmao.hexm
 */
public class StringHashPerfMain {

    public static void main(String[] args) {
        String s = "abcdejdsalfp";
        int end = s.length();
        for (int i = 0; i < 10; i++) {
            StringUtil.hash(s, 0, end);
        }
        long loop = 10000 * 10000;
        long t1 = System.currentTimeMillis();
        t1 = System.currentTimeMillis();
        for (long i = 0; i < loop; ++i) {
            StringUtil.hash(s, 0, end);
        }
        long t2 = System.currentTimeMillis();
        System.out.println((((t2 - t1) * 1000 * 1000) / loop) + " ns.");
    }

}
