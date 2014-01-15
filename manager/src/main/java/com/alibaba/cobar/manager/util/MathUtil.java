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

package com.alibaba.cobar.manager.util;

/**
 * (created at 2010-9-28)
 * 
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 * @author wenfeng.cenwf 2011-4-16
 * @author haiqing.zhuhq 2011-9-1
 */
public class MathUtil {

    public static double getDerivate(long newVal, long oldVal, long timestamp, long oldtimestamp, double scale) {
        return timestamp - oldtimestamp == 0 ? 0 : (newVal - oldVal) * scale / (timestamp - oldtimestamp);
    }

}
