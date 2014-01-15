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

import java.io.UnsupportedEncodingException;

import com.alibaba.cobar.config.Fields;

/**
 * @author xianmao.hexm 2012-8-28
 */
public class BindValueUtil {

    public static final void read(MySQLMessage mm, BindValue bv, String charset) throws UnsupportedEncodingException {
        switch (bv.type & 0xff) {
        case Fields.FIELD_TYPE_BIT:
            bv.value = mm.readBytesWithLength();
            break;
        case Fields.FIELD_TYPE_TINY:
            bv.byteBinding = mm.read();
            break;
        case Fields.FIELD_TYPE_SHORT:
            bv.shortBinding = (short) mm.readUB2();
            break;
        case Fields.FIELD_TYPE_LONG:
            bv.intBinding = mm.readInt();
            break;
        case Fields.FIELD_TYPE_LONGLONG:
            bv.longBinding = mm.readLong();
            break;
        case Fields.FIELD_TYPE_FLOAT:
            bv.floatBinding = mm.readFloat();
            break;
        case Fields.FIELD_TYPE_DOUBLE:
            bv.doubleBinding = mm.readDouble();
            break;
        case Fields.FIELD_TYPE_TIME:
            bv.value = mm.readTime();
            break;
        case Fields.FIELD_TYPE_DATE:
        case Fields.FIELD_TYPE_DATETIME:
        case Fields.FIELD_TYPE_TIMESTAMP:
            bv.value = mm.readDate();
            break;
        case Fields.FIELD_TYPE_VAR_STRING:
        case Fields.FIELD_TYPE_STRING:
        case Fields.FIELD_TYPE_VARCHAR:
            bv.value = mm.readStringWithLength(charset);
            if (bv.value == null) {
                bv.isNull = true;
            }
            break;
        case Fields.FIELD_TYPE_DECIMAL:
        case Fields.FIELD_TYPE_NEW_DECIMAL:
            bv.value = mm.readBigDecimal();
            if (bv.value == null) {
                bv.isNull = true;
            }
            break;
        default:
            throw new IllegalArgumentException("bindValue error,unsupported type:" + bv.type);
        }
        bv.isSet = true;
    }

}
