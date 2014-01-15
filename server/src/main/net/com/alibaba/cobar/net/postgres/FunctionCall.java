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
 * FunctionCall (F) 
 * Byte1('F') Identifies the message as a function call.
 * Int32 Length of message contents in bytes, including self. 
 * Int32 Specifies the object ID of the function to call. 
 * Int16 The number of argument format codes that follow (denoted C below). 
 *       This can be zero to indicate that there are no arguments or that 
 *       the arguments all use the default format (text); or one, in which 
 *       case the specified format code is applied to all arguments; or it 
 *       can equal the actual number of arguments.
 * Int16[C] The argument format codes. Each must presently be zero (text) or
 *          one (binary). 
 * Int16 Specifies the number of arguments being supplied to the function. 
 *       Next, the following pair of fields appear for each argument: 
 * Int32 The length of the argument value, in bytes (this count does not include 
 *       itself). Can be zero. As a special case, -1 indicates a NULL argument 
 *       value. No value bytes follow in the NULL case. 
 * Byten The value of the argument, in the format indicated by the associated 
 *       format code. n is the above length. After the last argument, the 
 *       following field appears: 
 * Int16 The format code for the function result. Must presently be zero (text) 
 *       or one (binary).
 * </pre>
 * 
 * @author xianmao.hexm 2012-6-26
 */
public class FunctionCall extends PostgresPacket {

}
