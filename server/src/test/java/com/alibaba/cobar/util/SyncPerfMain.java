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

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author xianmao.hexm
 */
public class SyncPerfMain {

    long i = 0L;

    private final Object lockA = new Object();
    private final ReentrantLock lockB = new ReentrantLock();

    final void tLockA() {
        final Object lock = this.lockA;
        synchronized (lock) {
            i++;
        }
    }

    final void tLockB() {
        final ReentrantLock lock = this.lockB;
        lock.lock();
        try {
            i++;
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) {
        int count = 10000000;
        SyncPerfMain test = new SyncPerfMain();

        System.currentTimeMillis();
        long t1 = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            test.tLockA();
            // test.testLockB();
        }
        long t2 = System.currentTimeMillis();
        System.out.println("take:" + (t2 - t1) + " ms.");
    }

}
