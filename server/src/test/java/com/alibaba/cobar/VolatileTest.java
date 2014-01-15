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
package com.alibaba.cobar;

import org.junit.Test;

/**
 * @author xianmao.hexm 2012-5-9
 */
public class VolatileTest {
    @Test
    public void testNoop() {
    }

    static class VolatileObject {
        volatile Object object = new Object();
    }

    public static void main(String[] args) {
        final VolatileObject vo = new VolatileObject();

        // set
        new Thread() {
            @Override
            public void run() {
                System.out.print("set...");
                while (true) {
                    vo.object = new Object();
                }
            }
        }.start();

        // get
        new Thread() {
            @Override
            public void run() {
                System.out.print("get...");
                while (true) {
                    Object oo = vo.object;
                    oo.toString();
                }
            }
        }.start();
    }

}
