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

import org.junit.Assert;
import org.junit.Test;

/**
 * @author xianmao.hexm from PartitionUtil.main()
 */
public class PartitionUtilTest {

    @Test
    public void testPartition() {
        // 本例的分区策略：希望将数据水平分成3份，前两份各占25%，第三份占50%。（故本例非均匀分区）
        // |<---------------------1024------------------------>|
        // |<----256--->|<----256--->|<----------512---------->|
        // | partition0 | partition1 | partition2 |
        // | 共2份,故count[0]=2 | 共1份，故count[1]=1 |
        int[] count = new int[] { 2, 1 };
        int[] length = new int[] { 256, 512 };
        PartitionUtil pu = new PartitionUtil(count, length);

        // 下面代码演示分别以offerId字段或memberId字段根据上述分区策略拆分的分配结果
        int DEFAULT_STR_HEAD_LEN = 8; // cobar默认会配置为此值
        long offerId = 12345;
        String memberId = "qiushuo";

        // 若根据offerId分配，partNo1将等于0，即按照上述分区策略，offerId为12345时将会被分配到partition0中
        int partNo1 = pu.partition(offerId);

        // 若根据memberId分配，partNo2将等于2，即按照上述分区策略，memberId为qiushuo时将会被分到partition2中
        int partNo2 = pu.partition(memberId, 0, DEFAULT_STR_HEAD_LEN);

        Assert.assertEquals(0, partNo1);
        Assert.assertEquals(2, partNo2);
    }

}
