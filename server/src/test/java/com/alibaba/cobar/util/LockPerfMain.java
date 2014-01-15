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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author xianmao.hexm 2011-4-20 下午05:35:10
 */
public class LockPerfMain {

    public void tReentrantLock() {
        System.currentTimeMillis();
        ReentrantLock lock = new ReentrantLock();

        long t1 = System.currentTimeMillis();
        for (int i = 0; i < 10000000; i++) {
            if (lock.tryLock())
                try {
                    // ...
                } finally {
                    lock.unlock();
                }
        }
        long t2 = System.currentTimeMillis();

        System.out.println("take time:" + (t2 - t1) + " ms.");
    }

    public void tAtomicBoolean() {
        System.currentTimeMillis();
        AtomicBoolean atomic = new AtomicBoolean();

        long t1 = System.currentTimeMillis();
        for (int i = 0; i < 10000000; i++) {
            if (atomic.compareAndSet(false, true)) {
                try {
                    // ...
                } finally {
                    atomic.set(false);
                }
            }
        }
        long t2 = System.currentTimeMillis();

        System.out.println("take time:" + (t2 - t1) + " ms.");
    }

}
