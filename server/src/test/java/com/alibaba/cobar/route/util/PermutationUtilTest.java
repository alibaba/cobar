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
 * (created at 2011-10-19)
 */
package com.alibaba.cobar.route.util;

import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class PermutationUtilTest {

    @Test
    public void testPermutate() {
        Set<String> set = PermutationUtil.permutateSQL("-", "1");
        Assert.assertEquals(1, set.size());
        Assert.assertTrue(set.contains("1"));

        set = PermutationUtil.permutateSQL("-", "1", "1");
        Assert.assertEquals(1, set.size());
        Assert.assertTrue(set.contains("1-1"));

        set = PermutationUtil.permutateSQL("-", "1", "2");
        Assert.assertEquals(2, set.size());
        Assert.assertTrue(set.contains("1-2"));
        Assert.assertTrue(set.contains("2-1"));

        set = PermutationUtil.permutateSQL("-", "1", "2", "2");
        Assert.assertEquals(3, set.size());
        Assert.assertTrue(set.contains("1-2-2"));
        Assert.assertTrue(set.contains("2-1-2"));
        Assert.assertTrue(set.contains("2-2-1"));

        set = PermutationUtil.permutateSQL("-", "1", "2", "3");
        Assert.assertEquals(6, set.size());
        Assert.assertTrue(set.contains("1-2-3"));
        Assert.assertTrue(set.contains("1-3-2"));
        Assert.assertTrue(set.contains("2-1-3"));
        Assert.assertTrue(set.contains("2-3-1"));
        Assert.assertTrue(set.contains("3-2-1"));
        Assert.assertTrue(set.contains("3-1-2"));
    }

    @Test
    public void testPermutateNull() {
        try {
            PermutationUtil.permutateSQL("-");
            Assert.assertFalse(true);
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        } catch (Throwable t) {
            Assert.assertFalse(true);
        }
    }

}
