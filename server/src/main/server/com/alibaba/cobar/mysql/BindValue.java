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
package com.alibaba.cobar.mysql;

/**
 * @author xianmao.hexm 2012-8-28
 */
public class BindValue {

    public boolean isNull; /* NULL indicator */
    public boolean isLongData; /* long data indicator */
    public boolean isSet; /* has this parameter been set */

    public long length; /* Default length of data */
    public int type; /* data type */
    public byte scale;

    /** 数据值 **/
    public byte byteBinding;
    public short shortBinding;
    public int intBinding;
    public float floatBinding;
    public long longBinding;
    public double doubleBinding;
    public Object value; /* Other value to store */

    public void reset() {
        this.isNull = false;
        this.isLongData = false;
        this.isSet = false;

        this.length = 0;
        this.type = 0;
        this.scale = 0;

        this.byteBinding = 0;
        this.shortBinding = 0;
        this.intBinding = 0;
        this.floatBinding = 0;
        this.longBinding = 0L;
        this.doubleBinding = 0D;
        this.value = null;
    }

}
