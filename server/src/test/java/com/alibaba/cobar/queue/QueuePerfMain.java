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
package com.alibaba.cobar.queue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Queue 性能测试
 * 
 * @author xianmao.hexm 2010-7-21 下午01:33:02
 */
public class QueuePerfMain {

    private static byte[] testData = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 };

    private static BlockingQueue<byte[]> arrayQueue = new ArrayBlockingQueue<byte[]>(5000000);
    private static FixedQueue<byte[]> fixedQueue = new FixedQueue<byte[]>(5000000);
    private static Queue<byte[]> testQueue = new Queue<byte[]>();
    private static BlockingQueue<byte[]> linkedQueue = new LinkedBlockingQueue<byte[]>();

    public static void tArrayQueue() {
        new Thread() {

            @Override
            public void run() {
                while (true) {
                    arrayQueue.offer(testData);
                }
            }
        }.start();

        new Thread() {

            @Override
            public void run() {
                int count = 0;
                long num = 0;
                while (true) {
                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                    }
                    count++;
                    num += arrayQueue.size();
                    arrayQueue.clear();
                    if (count == 50) {
                        System.out.println(num / 50);
                        count = 0;
                        num = 0;
                    }
                }
            }
        }.start();
    }

    public static void tFixedQueue() {
        new Thread() {

            @Override
            public void run() {
                while (true) {
                    fixedQueue.offer(testData);
                }
            }
        }.start();

        new Thread() {

            @Override
            public void run() {
                int count = 0;
                long num = 0;
                while (true) {
                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                    }
                    count++;
                    num += fixedQueue.size();
                    fixedQueue.clear();
                    if (count == 50) {
                        System.out.println(num / 50);
                        count = 0;
                        num = 0;
                    }
                }
            }
        }.start();
    }

    public static void tQueue() {
        new Thread() {

            @Override
            public void run() {
                while (true) {
                    testQueue.append(testData);
                }
            }
        }.start();

        new Thread() {

            @Override
            public void run() {
                int count = 0;
                long num = 0;
                while (true) {
                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                    }
                    count++;
                    num += testQueue.size();
                    testQueue.clear();
                    if (count == 50) {
                        System.out.println(num / 50);
                        count = 0;
                        num = 0;
                    }
                }
            }
        }.start();
    }

    public static void tLinkedQueue() {
        new Thread() {

            @Override
            public void run() {
                while (true) {
                    linkedQueue.offer(testData);
                }
            }
        }.start();

        new Thread() {

            @Override
            public void run() {
                int count = 0;
                long num = 0;
                while (true) {
                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                    }
                    count++;
                    num += linkedQueue.size();
                    linkedQueue.clear();
                    if (count == 50) {
                        System.out.println(num / 50);
                        count = 0;
                        num = 0;
                    }
                }
            }
        }.start();
    }

    public static void main(String[] args) {
        // testArrayQueue();
        // testFixedQueue();
        // testQueue();
        // testLinkedQueue();
        // testTransferQueue();
    }

}
