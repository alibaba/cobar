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

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.cobar.util.ExecutorUtil;

/**
 * @author xianmao.hexm
 */
public class ExecutorTestMain {

    public static void main(String[] args) {
        final AtomicLong count = new AtomicLong(0L);
        final ThreadPoolExecutor executor = ExecutorUtil.create("TestExecutor", 5);

        new Thread() {
            @Override
            public void run() {
                for (;;) {
                    long c = count.get();
                    try {
                        Thread.sleep(5000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("count:" + (count.get() - c) / 5);
                    System.out.println("active:" + executor.getActiveCount());
                    System.out.println("queue:" + executor.getQueue().size());
                    System.out.println("============================");
                }
            }
        }.start();

        new Thread() {
            @Override
            public void run() {
                for (;;) {
                    executor.execute(new Runnable() {

                        @Override
                        public void run() {
                            count.incrementAndGet();
                        }
                    });
                }
            }
        }.start();

        new Thread() {
            @Override
            public void run() {
                for (;;) {
                    executor.execute(new Runnable() {

                        @Override
                        public void run() {
                            count.incrementAndGet();
                        }
                    });
                }
            }
        }.start();
    }

}
