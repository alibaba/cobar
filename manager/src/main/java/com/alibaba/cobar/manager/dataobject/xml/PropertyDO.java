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

package com.alibaba.cobar.manager.dataobject.xml;

import java.util.ArrayList;
import java.util.List;

/**
 * @author haiqing.zhuhq 2011-6-17
 */
public class PropertyDO {
    private List<Integer> stopTimes;

    public PropertyDO() {
        this.stopTimes = new ArrayList<Integer>();
    }

    public List<Integer> getStopTimes() {
        return stopTimes;
    }

    public void setStopTimes(List<Integer> stopTimes) {
        this.stopTimes = stopTimes;
    }

    @Override
    public String toString() {
        if (stopTimes.size() < 1) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int i;
        for (i = 0; i < stopTimes.size() - 1; i++) {
            sb.append(stopTimes.get(i)).append(",");
        }
        sb.append(stopTimes.get(i));
        return sb.toString();
    }

}
