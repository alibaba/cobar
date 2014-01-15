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

import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author xianmao.hexm 2011-5-20 下午12:50:39
 */
public class QueueSimpleMain {

    static long putCount = 0;
    static long takeCount = 0;

    public static void main(String[] args) {
        // final SynchronousQueue<String> queue = new
        // SynchronousQueue<String>();
        //         final ArrayBlockingQueue<String> queue = new
        //         ArrayBlockingQueue<String>(10000000);
        final LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<String>();

        new Thread() {
            @Override
            public void run() {
                for (;;) {
                    long put = putCount;
                    long take = takeCount;
                    try {
                        Thread.sleep(5000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("put:" + (putCount - put) / 5 + " take:" + (takeCount - take) / 5);
                }
            }
        }.start();

        new Thread() {
            @Override
            public void run() {
                for (;;) {
                    // try {
                    if (queue.poll() != null)
                        takeCount++;
                    // } catch (InterruptedException e) {
                    // e.printStackTrace();
                    // }
                    // try {
                    // Thread.sleep(10L);
                    // } catch (InterruptedException e) {
                    // // TODO Auto-generated catch block
                    // e.printStackTrace();
                    // }
                }
            }
        }.start();
    }

}
