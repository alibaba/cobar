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
 * StartupMessage (F) 
 * Int32 Length of message contents in bytes, including self. 
 * Int32(196608) The protocol version number. The most significant 16 bits are 
 *               the major version number (3 for the protocol described here).
 *               The least significant 16 bits are the minor version number (0 
 *               for the protocol described here). The protocol version number 
 *               is followed by one or more pairs of parameter name and value 
 *               strings. A zero byte is required as a terminator after the 
 *               last name/value pair. Parameters can appear in any order. user 
 *               is required, others are optional. Each parameter is specified as: 
 * String The parameter name. Currently recognized names are: 
 *        user The database user name to connect as. Required; there is no default. 
 *        database The database to connect to. Defaults to the user name. 
 *        options Command-line arguments for the backend. (This is deprecated in 
 *                favor of setting individual run-time parameters.) In addition to
 *                the above, any run-time parameter that can be set at backend start 
 *                time might be listed. Such settings will be applied during backend 
 *                start (after parsing the command-line options if any). The values 
 *                will act as session defaults. 
 * String The parameter value.
 * </pre>
 * 
 * @author xianmao.hexm 2012-6-26
 */
public class StartupMessage extends PostgresPacket {

}
