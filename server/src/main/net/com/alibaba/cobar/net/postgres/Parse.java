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
 * Parse (F) 
 * Byte1('P') Identifies the message as a Parse command. 
 * Int32 Length of message contents in bytes, including self. 
 * String The name of the destination prepared statement (an empty string 
 *        selects the unnamed prepared statement). 
 * String The query string to be parsed. 
 * Int16 The number of parameter data types specified (can be zero). Note 
 *       that this is not an indication of the number of parameters that 
 *       might appear in the query string, only the number that the frontend 
 *       wants to prespecify types for. Then, for each parameter, there is 
 *       the following: 
 * Int32 Specifies the object ID of the parameter data type. Placing a zero 
 *       here is equivalent to leaving the type unspecified.
 * </pre>
 * 
 * @author xianmao.hexm 2012-6-26
 */
public class Parse extends PostgresPacket {

}
