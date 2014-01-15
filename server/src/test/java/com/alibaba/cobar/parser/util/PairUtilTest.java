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
/**
 * (created at 2011-10-31)
 */
package com.alibaba.cobar.parser.util;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class PairUtilTest extends TestCase {

    @Test
    public void testSequenceSlicing() {
        Assert.assertEquals(new Pair<Integer, Integer>(0, 2), PairUtil.sequenceSlicing("2"));
        Assert.assertEquals(new Pair<Integer, Integer>(1, 2), PairUtil.sequenceSlicing("1: 2"));
        Assert.assertEquals(new Pair<Integer, Integer>(1, 0), PairUtil.sequenceSlicing(" 1 :"));
        Assert.assertEquals(new Pair<Integer, Integer>(-1, 0), PairUtil.sequenceSlicing("-1: "));
        Assert.assertEquals(new Pair<Integer, Integer>(-1, 0), PairUtil.sequenceSlicing(" -1:0"));
        Assert.assertEquals(new Pair<Integer, Integer>(0, 0), PairUtil.sequenceSlicing(" :"));
    }

    @Test
    public void splitIndexTest() {
        String src1 = "offer_group[10]";
        Pair<String, Integer> pair1 = PairUtil.splitIndex(src1, '[', ']');
        Assert.assertEquals("offer_group", pair1.getKey());
        Assert.assertEquals(Integer.valueOf(10), pair1.getValue());

        String src2 = "offer_group";
        Pair<String, Integer> pair2 = PairUtil.splitIndex(src2, '[', ']');
        Assert.assertEquals("offer_group", pair2.getKey());
        Assert.assertEquals(Integer.valueOf(-1), pair2.getValue());
    }

}
