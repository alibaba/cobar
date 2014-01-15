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

package com.alibaba.cobar.manager.mock;

import org.easymock.IArgumentMatcher;
import static org.easymock.EasyMock.reportMatcher;

public class SQLEquals implements IArgumentMatcher {

    private String expectedSQL = null;
    private int length;

    public SQLEquals(String expectedSQL) {
        this.expectedSQL = expectedSQL;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    @Override
    public void appendTo(StringBuffer buffer) {
        buffer.append("SQLEquals(\"" + expectedSQL + "\")");
    }

    @Override
    public boolean matches(Object actualSQL) {
        if (actualSQL == null && expectedSQL == null) return true;
        else if (actualSQL instanceof String) if ((((String) actualSQL).toLowerCase()).startsWith("switch @@datasource")) return true;
        else if ((((String) actualSQL).toLowerCase()).startsWith("kill @@connection")) return true;
        else if ((((String) actualSQL).toLowerCase()).startsWith("stop @@heartbeat")) return true;
        else return expectedSQL.equalsIgnoreCase((String) actualSQL);
        else return false;
    }

    public static String sqlEquals(String in) {
        reportMatcher(new SQLEquals(in));
        return in;
    }

}
