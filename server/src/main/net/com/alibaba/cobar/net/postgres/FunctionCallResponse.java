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
 * FunctionCallResponse (B) 
 * Byte1('V') Identifies the message as a function call result. 
 * Int32 Length of message contents in bytes, including self.
 * Int32 The length of the function result value, in bytes (this count does
 *       not include itself). Can be zero. As a special case, -1 indicates a 
 *       NULL function result. No value bytes follow in the NULL case. 
 * Byten The value of the function result, in the format indicated by the 
 *       associated format code. n is the above length.
 * </pre>
 * 
 * @author xianmao.hexm 2012-6-26
 */
public class FunctionCallResponse extends PostgresPacket {

}
