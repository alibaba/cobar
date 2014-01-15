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
 * @author xianmao.hexm
 */
public class SQLStatisticsMain {

    public void performanc() {
        SQLRecorder sqlStatistics = new SQLRecorder(10);
        for (int i = 0; i < 1000000; i++) {
            if (sqlStatistics.check(i)) {
                SQLRecord recorder = new SQLRecord();
                recorder.executeTime = i;
                sqlStatistics.add(recorder);
            }
        }
    }

    public static void main(String[] args) {
        SQLStatisticsMain test = new SQLStatisticsMain();
        test.performanc();
    }

}
