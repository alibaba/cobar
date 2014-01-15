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
package com.alibaba.cobar.statistic;

import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

/**
 * SQL统计排序记录器
 * 
 * @author xianmao.hexm 2010-9-30 上午10:48:28
 */
public final class SQLRecorder {

    private int index;
    private long minValue;
    private final int count;
    private final int lastIndex;
    private final SQLRecord[] records;
    private final ReentrantLock lock;

    public SQLRecorder(int count) {
        this.count = count;
        this.lastIndex = count - 1;
        this.records = new SQLRecord[count];
        this.lock = new ReentrantLock();
    }

    public SQLRecord[] getRecords() {
        return records;
    }

    /**
     * 检查当前的值能否进入排名
     */
    public boolean check(long value) {
        return (index < count) || (value > minValue);
    }

    public void add(SQLRecord record) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (index < count) {
                records[index++] = record;
                if (index == count) {
                    Arrays.sort(records);
                    minValue = records[0].executeTime;
                }
            } else {
                swap(record);
            }
        } finally {
            lock.unlock();
        }
    }

    public void clear() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            for (int i = 0; i < count; i++) {
                records[i] = null;
            }
            index = 0;
            minValue = 0;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 交换元素位置并重新定义最小值
     */
    private void swap(SQLRecord record) {
        int x = find(record.executeTime, 0, lastIndex);
        switch (x) {
        case 0:
            break;
        case 1:
            minValue = record.executeTime;
            records[0] = record;
            break;
        default:
            --x;// 向左移动一格
            final SQLRecord[] records = this.records;
            for (int i = 0; i < x; i++) {
                records[i] = records[i + 1];
            }
            records[x] = record;
            minValue = records[0].executeTime;
        }
    }

    /**
     * 定位v在当前范围内的排名
     */
    private int find(long v, int from, int to) {
        int x = from + ((to - from + 1) >> 1);
        if (v <= records[x].executeTime) {
            --x;// 向左移动一格
            if (from >= x) {
                return v <= records[from].executeTime ? from : from + 1;
            } else {
                return find(v, from, x);
            }
        } else {
            ++x;// 向右移动一格
            if (x >= to) {
                return v <= records[to].executeTime ? to : to + 1;
            } else {
                return find(v, x, to);
            }
        }
    }

}
