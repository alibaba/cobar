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

/**
 * @author xianmao.hexm 2010-9-30 上午10:43:53
 */
public final class ParserCount {

    private long parseCount;
    private long timeCount;
    private long maxParseTime;
    private long maxParseSQL;
    private long cachedCount;
    private int cacheSizeCount;

    public void doParse(long sqlId, long time) {
        parseCount++;
        timeCount += time;
        if (time > maxParseTime) {
            maxParseTime = time;
            maxParseSQL = sqlId;
        }
    }

    public long getParseCount() {
        return parseCount;
    }

    public long getTimeCount() {
        return timeCount;
    }

    public long getMaxParseTime() {
        return maxParseTime;
    }

    public long getMaxParseSQL() {
        return maxParseSQL;
    }

    public void doCached() {
        cachedCount++;
    }

    public long getCachedCount() {
        return cachedCount;
    }

    public void setCacheSizeCount(int cacheSizeCount) {
        this.cacheSizeCount = cacheSizeCount;
    }

    public int getCacheSizeCount() {
        return cacheSizeCount;
    }

}
