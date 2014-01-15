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
package com.alibaba.cobar.route.util;

/**
 * 数据分区工具单独版本，请使用singleton的模式调用。
 * 
 * @author xianmao.hexm 2009-3-16 上午11:56:45
 */
public final class PartitionForSingle {

    // 分区长度:数据段分布定义，其中取模的数一定要是2^n， 因为这里使用x % 2^n == x & (2^n - 1)等式，来优化性能。
    private static final int PARTITION_LENGTH = 1024;

    private static final int DEFAULT_HASH_LENGTH = 8;

    // %转换为&操作的换算数值
    private static final long AND_VALUE = PARTITION_LENGTH - 1;

    // 分区线段
    private final int[] segment = new int[PARTITION_LENGTH];

    /**
     * @param count 表示定义的分区数
     * @param length 表示对应每个分区的取值长度
     *            <p>
     *            注意：其中count,length两个数组的长度必须是一致的。
     *            </p>
     */
    public PartitionForSingle(int[] count, int[] length) {
        if (count == null || length == null || (count.length != length.length)) {
            throw new RuntimeException("error,check your scope & scopeLength definition.");
        }
        int segmentLength = 0;
        for (int i = 0; i < count.length; i++) {
            segmentLength += count[i];
        }
        int[] scopeSegment = new int[segmentLength + 1];

        int index = 0;
        for (int i = 0; i < count.length; i++) {
            for (int j = 0; j < count[i]; j++) {
                scopeSegment[++index] = scopeSegment[index - 1] + length[i];
            }
        }
        if (scopeSegment[scopeSegment.length - 1] != PARTITION_LENGTH) {
            throw new RuntimeException("error,check your partitionScope definition.");
        }

        // 数据映射操作
        for (int i = 1; i < scopeSegment.length; i++) {
            for (int j = scopeSegment[i - 1]; j < scopeSegment[i]; j++) {
                segment[j] = (i - 1);
            }
        }
    }

    public int partition(long h) {
        return segment[(int) (h & AND_VALUE)];
    }

    public int partition(String key) {
        return segment[(int) (hash(key) & AND_VALUE)];
    }

    private static long hash(String s) {
        long h = 0;
        int len = s.length();
        for (int i = 0; (i < DEFAULT_HASH_LENGTH && i < len); i++) {
            h = (h << 5) - h + s.charAt(i);
        }
        return h;
    }

    // for test
    public static void main(String[] args) {
        // 拆分为16份，每份长度均为：64。
        // Scope scope = new Scope(new int[] { 16 }, new int[] { 64 });

        // // 拆分为23份，前8份长度为：8，后15份长度为：64。
        // Scope scope = new Scope(new int[] { 8, 15 }, new int[] { 8, 64 });

        // // 拆分为128份，每份长度均为：8。
        // Scope scope = new Scope(new int[] { 128 }, new int[] { 8 });

        PartitionForSingle p = new PartitionForSingle(new int[] { 8, 15 }, new int[] { 8, 64 });

        String memberId = "xianmao.hexm";

        int value = 0;
        long st = System.currentTimeMillis();
        for (int i = 0; i < 10000000; i++) {
            value = p.partition(memberId);
        }
        long et = System.currentTimeMillis();

        System.out.println("value:" + value + ",take time:" + (et - st) + " ms.");
    }

}
