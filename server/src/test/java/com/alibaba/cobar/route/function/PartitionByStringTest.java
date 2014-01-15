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
package com.alibaba.cobar.route.function;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import junit.framework.TestCase;

import org.junit.Assert;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.primary.PlaceHolder;
import com.alibaba.cobar.parser.util.ListUtil;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class PartitionByStringTest extends TestCase {

    @SuppressWarnings("unchecked")
    public void testPartition() {
        PartitionByString sut = new PartitionByString(
                "test   ",
                (List<Expression>) ListUtil.createList(new PlaceHolder("member_id", "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("-2:");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals((int) execute(sut, "12"), (int) execute(sut, "012"));
        Assert.assertEquals((int) execute(sut, "112"), (int) execute(sut, "012"));
        Assert.assertEquals((int) execute(sut, "2"), (int) execute(sut, "2"));

        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("-2:-1");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(49, (int) execute(sut, "012"));
        Assert.assertEquals(49, (int) execute(sut, "12"));
        Assert.assertEquals(49, (int) execute(sut, "15"));
        Assert.assertEquals(0, (int) execute(sut, "2"));
        Assert.assertEquals(56, (int) execute(sut, "888888"));
        Assert.assertEquals(56, (int) execute(sut, "89"));
        Assert.assertEquals(56, (int) execute(sut, "780"));

        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("1:-1");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(49, (int) execute(sut, "012"));
        Assert.assertEquals(49, (int) execute(sut, "219"));
        Assert.assertEquals(0, (int) execute(sut, "2"));
        Assert.assertEquals(512, (int) execute(sut, "888888"));

    }

    /**
     * start == end , except 0:0,
     */
    @SuppressWarnings("unchecked")
    public void testPartitionStartEqEnd() {

        // 同号，不越界
        PartitionByString sut = new PartitionByString(
                "test   ",
                (List<Expression>) ListUtil.createList(new PlaceHolder("member_id", "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("1:1");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));

        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("-5:-5");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));

        // 异号，不越界
        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("3:-7");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));

        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("5:-5");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));

        // 同号，边界值
        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("0:0");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(641, (int) execute(sut, "skkdifisd-"));
        Assert.assertEquals(74, (int) execute(sut, "sdsdfsafaw"));

        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("10:10");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));

        // 异号，边界值
        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("0:-10");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));

        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("-1:9");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));

        // 同号，越界
        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("-15:-15");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));

        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("15:15");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));

        // 异号，越界，不存在

    }

    /**
     * if end==0, then end = key.length
     */
    @SuppressWarnings("unchecked")
    public void testPartitionStartLtEnd() {
        // 同号，不越界
        PartitionByString sut = new PartitionByString(
                "test   ",
                (List<Expression>) ListUtil.createList(new PlaceHolder("member_id", "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("6:1");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));

        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("-5:-8");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));

        // 异号，不越界
        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("9:-9");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));

        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("-1:2");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));

        // 同号，边界值， 双边界
        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("9:0");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(119, (int) execute(sut, "qiycgsrmkw"));
        Assert.assertEquals(104, (int) execute(sut, "tbctwicjyh"));

        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("-1:-10");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));

        // 同号，边界值， 单边界
        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("5:0");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(176, (int) execute(sut, "kducgalemc"));
        Assert.assertEquals(182, (int) execute(sut, "1icuwixjsn"));

        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("9:5");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));

        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("-7:-10");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));

        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("-1:-4");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));

        // 异号，边界值，双边界
        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("9:-10");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));

        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("-1:0");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(108, (int) execute(sut, "tcjsyckxhl"));
        Assert.assertEquals(106, (int) execute(sut, "1uxhklsycj"));

        // 异号，边界值，单边界
        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("4:-10");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));

        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("-6:0");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(631, (int) execute(sut, "1kckdlxhxw"));
        Assert.assertEquals(864, (int) execute(sut, "nhyjklouqj"));

        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("9:-5");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));

        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("-1:5");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));

        // 同号，双越界
        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("15:11");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));

        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("-15:-20");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));

        // 同号，单越界
        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("-8:-15");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));

        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("15:6");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));

        // 异号，双越界
        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("19:-20");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));

        // 异号，单越界

        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("15:-8");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));

        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("6:-15");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));

    }

    @SuppressWarnings("unchecked")
    public void testPartitionStartgtEnd() {
        String testKey = "abcdefghij";
        // 同号，不越界
        PartitionByString sut = new PartitionByString(
                "test   ",
                (List<Expression>) ListUtil.createList(new PlaceHolder("member_id", "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("1:6");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(36, (int) execute(sut, testKey));
        Assert.assertEquals(36, (int) execute(sut, "a" + testKey.substring(1, 6) + "abcd"));
        Assert.assertEquals(36, (int) execute(sut, "b" + testKey.substring(1, 6) + "sila"));
        Assert.assertTrue((36 != (int) execute(sut, "c" + testKey.substring(1, 5) + "sil2")));

        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("-8:-5");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(36, (int) execute(sut, testKey));
        Assert.assertEquals(36, (int) execute(sut, "12" + testKey.substring(2, 5) + "12345"));
        Assert.assertEquals(36, (int) execute(sut, "45" + testKey.substring(2, 5) + "78923"));

        // 异号，不越界
        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("-9:9");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(260, (int) execute(sut, "a" + testKey.substring(1, 9) + "8"));
        Assert.assertEquals(260, (int) execute(sut, "f" + testKey.substring(1, 9) + "*"));

        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("2:-1");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(934, (int) execute(sut, "ab" + testKey.substring(2, 9) + "8"));
        Assert.assertEquals(934, (int) execute(sut, "fj" + testKey.substring(2, 9) + "*"));

        // 同号，边界值， 双边界
        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("0:9");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(101, (int) execute(sut, testKey.substring(0, 9) + "#"));
        Assert.assertEquals(101, (int) execute(sut, testKey.substring(0, 9) + "*"));

        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("-10:-1");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(101, (int) execute(sut, testKey.substring(0, 9) + "#"));
        Assert.assertEquals(101, (int) execute(sut, testKey.substring(0, 9) + "*"));

        // 同号，边界值， 单边界
        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("0:5");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(99, (int) execute(sut, testKey.substring(0, 5) + "#uiyt"));
        Assert.assertEquals(99, (int) execute(sut, testKey.substring(0, 5) + "*rfsj"));

        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("5:9");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(386, (int) execute(sut, "#uiyt" + testKey.substring(5, 9) + "a"));
        Assert.assertEquals(386, (int) execute(sut, "*rfsj" + testKey.substring(5, 9) + "%"));

        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("-10:-7");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(36, (int) execute(sut, testKey.substring(0, 5) + "#uiyt45"));
        Assert.assertEquals(36, (int) execute(sut, testKey.substring(0, 5) + "*rfsjkm"));

        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("-4:-1");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(936, (int) execute(sut, "#uiyt4" + testKey.substring(5, 9) + "a"));
        Assert.assertEquals(936, (int) execute(sut, "*rfsj$" + testKey.substring(5, 9) + "%"));

        // 异号，边界值，双边界
        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("-10:9");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(101, (int) execute(sut, testKey.substring(0, 9) + "a"));
        Assert.assertEquals(101, (int) execute(sut, testKey.substring(0, 9) + "%"));

        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("0:-1");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(101, (int) execute(sut, testKey.substring(0, 9) + "a"));
        Assert.assertEquals(101, (int) execute(sut, testKey.substring(0, 9) + "%"));

        // 异号，边界值，单边界
        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("-10:4");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(66, (int) execute(sut, testKey.substring(0, 4) + "asdebh"));
        Assert.assertEquals(66, (int) execute(sut, testKey.substring(0, 4) + "%^&*()"));

        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("0:-6");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(66, (int) execute(sut, testKey.substring(0, 4) + "asdebh"));
        Assert.assertEquals(66, (int) execute(sut, testKey.substring(0, 4) + "%^&*()"));

        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("-5:9");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(386, (int) execute(sut, "#uiyt" + testKey.substring(5, 9) + "a"));
        Assert.assertEquals(386, (int) execute(sut, "*rfsj" + testKey.substring(5, 9) + "%"));

        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("5:-1");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(386, (int) execute(sut, "#uiyt" + testKey.substring(5, 9) + "a"));
        Assert.assertEquals(386, (int) execute(sut, "*rfsj" + testKey.substring(5, 9) + "%"));

        // 同号，双越界
        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("11:15");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));

        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("-20:-15");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));

        // 同号，单越界
        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("-15:-8");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(33, (int) execute(sut, testKey.substring(0, 2) + "dskfdijc"));
        Assert.assertEquals(33, (int) execute(sut, testKey.substring(0, 2) + "cuiejdjj"));
        Assert.assertEquals(129, (int) execute(sut, "$%cuiejdjj"));

        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("6:15");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(450, (int) execute(sut, "#uiyt#" + testKey.substring(6, 10)));
        Assert.assertEquals(450, (int) execute(sut, "*rfsj*" + testKey.substring(6, 10)));
        Assert.assertEquals(345, (int) execute(sut, "#uiyt#" + "dkug"));

        // 异号，双越界
        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("-20:19");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(165, (int) execute(sut, testKey));
        Assert.assertEquals(725, (int) execute(sut, "1" + testKey.substring(1, 10)));

        // 异号，单越界

        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("-8:15");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(388, (int) execute(sut, "1q" + testKey.substring(2, 10)));
        Assert.assertEquals(388, (int) execute(sut, "sd" + testKey.substring(2, 10)));

        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("-15:6");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(99, (int) execute(sut, testKey.substring(0, 6) + "abcd"));
        Assert.assertEquals(99, (int) execute(sut, testKey.substring(0, 6) + "efgh"));

    }

    @SuppressWarnings("unchecked")
    public void testPartitionNoStartOrNoEnd() {
        String testKey = "abcdefghij";
        // 无start， 不越界
        PartitionByString sut = new PartitionByString(
                "test   ",
                (List<Expression>) ListUtil.createList(new PlaceHolder("member_id", "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice(":6");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(99, (int) execute(sut, testKey));
        Assert.assertEquals(99, (int) execute(sut, testKey.substring(0, 6) + "abcd"));
        Assert.assertEquals(99, (int) execute(sut, testKey.substring(0, 6) + "sila"));
        Assert.assertTrue((99 != (int) execute(sut, "c" + testKey.substring(1, 5) + "sil2")));

        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice(":-4");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(99, (int) execute(sut, testKey));
        Assert.assertEquals(99, (int) execute(sut, testKey.substring(0, 6) + "abcd"));
        Assert.assertEquals(99, (int) execute(sut, testKey.substring(0, 6) + "sila"));
        Assert.assertTrue((99 != (int) execute(sut, "c" + testKey.substring(1, 5) + "sil2")));

        // 无start， 越界
        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice(":15");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(165, (int) execute(sut, testKey));
        Assert.assertEquals(647, (int) execute(sut, "b" + testKey));

        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice(":-15");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));

        // 无end， 不越界
        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("2:");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(388, (int) execute(sut, testKey));
        Assert.assertEquals(388, (int) execute(sut, "ab" + testKey.substring(2, 10)));
        Assert.assertEquals(388, (int) execute(sut, "e&" + testKey.substring(2, 10)));
        Assert.assertTrue((388 != (int) execute(sut, "c" + testKey.substring(1, 5) + "sil2")));

        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("-5:");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(808, (int) execute(sut, testKey));
        Assert.assertEquals(808, (int) execute(sut, "abT*1" + testKey.substring(5, 10)));
        Assert.assertEquals(808, (int) execute(sut, "ab^^!" + testKey.substring(5, 10)));

        // 无end， 越界
        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("-15:");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(165, (int) execute(sut, testKey));
        Assert.assertEquals(647, (int) execute(sut, "b" + testKey));

        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice("15:");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));
        Assert.assertEquals(0, (int) execute(sut, UUID.randomUUID().toString().substring(0, 10)));

        // 无start 无end
        sut = new PartitionByString("test   ", (List<Expression>) ListUtil.createList(new PlaceHolder(
                "member_id",
                "MEMBER_ID").setCacheEvalRst(false)));
        sut.setCacheEvalRst(false);
        sut.setHashSlice(":");
        sut.setPartitionCount("1024");
        sut.setPartitionLength("1");
        sut.init();
        Assert.assertEquals(165, (int) execute(sut, testKey));
        Assert.assertEquals(452, (int) execute(sut, "b" + testKey.substring(1)));

    }

    private static Integer execute(PartitionByString sut, String key) {
        Map<String, Object> map = new HashMap<String, Object>(1, 1);
        map.put("MEMBER_ID", key);
        Integer v = (Integer) sut.evaluation(map);
        return v;
    }

}
