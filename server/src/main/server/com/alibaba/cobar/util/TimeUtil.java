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

/**
 * 弱精度的计时器，考虑性能不使用同步策略。
 * 
 * @author xianmao.hexm 2011-1-18 下午06:10:55
 */
public class TimeUtil {
    private static long CURRENT_TIME = System.currentTimeMillis();

    public static final long currentTimeMillis() {
        return CURRENT_TIME;
    }

    public static final void update() {
        CURRENT_TIME = System.currentTimeMillis();
    }

}
