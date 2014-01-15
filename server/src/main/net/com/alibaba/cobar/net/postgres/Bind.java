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
 * Bind (F) 
 * Byte1('B') Identifies the message as a Bind command. 
 * Int32 Length of message contents in bytes, including self. 
 * String The name of the destination portal (an empty string selects the unnamed portal).
 * String The name of the source prepared statement (an empty string selects the unnamed 
 *        prepared statement). 
 * Int16 The number of parameter format codes that follow (denoted C below). 
 *       This can be zero to indicate that there are no parameters or that the parameters 
 *       all use the default format(text); or one, in which case the specified format code 
 *       is applied to all parameters; or it can equal the actual number of parameters. 
 * Int16[C] The parameter format codes. Each must presently be zero (text) or one(binary). 
 * Int16 The number of parameter values that follow (possibly zero). This must match the 
 *       number of parameters needed by the query. Next, the following pair of fields appear 
 *       for each parameter: 
 * Int32 The length of the parameter value, in bytes (this count does not include
 *       itself). Can be zero. As a special case, -1 indicates a NULL parameter
 *       value. No value bytes follow in the NULL case. 
 * Byten The value of the parameter, in the format indicated by the associated format code. 
 *       n is the above length. After the last parameter, the following fields appear:
 * Int16 The number of result-column format codes that follow (denoted R
 *       below). This can be zero to indicate that there are no result columns or
 *       that the result columns should all use the default format (text); or one,
 *       in which case the specified format code is applied to all result columns
 *       (if any); or it can equal the actual number of result columns of the query. 
 * Int16[R] The result-column format codes. Each must presently be zero (text) or one (binary).
 * </pre>
 * 
 * @author xianmao.hexm 2012-6-26
 */
public class Bind extends PostgresPacket {

}
