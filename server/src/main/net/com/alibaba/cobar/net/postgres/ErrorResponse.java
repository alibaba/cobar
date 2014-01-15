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
package com.alibaba.cobar.net.postgres;

/**
 * <pre>
 * ErrorResponse (B) 
 * Byte1('E') Identifies the message as an error. 
 * Int32 Length of message contents in bytes, including self. 
 *       The message body consists of one or more identified fields, 
 *       followed by a zero byte as a terminator. Fields can appear 
 *       in any order. For each field there is the following: 
 * Byte1 A code identifying the field type; if zero, this is the
 *       message terminator and no string follows. The presently defined 
 *       field types are listed in Section 46.6. Since more field types 
 *       might be added in future, frontends should silently ignore 
 *       fields of unrecognized type.
 * String The field value.
 * </pre>
 * 
 * @author xianmao.hexm 2012-6-26
 */
public class ErrorResponse extends PostgresPacket {

}
