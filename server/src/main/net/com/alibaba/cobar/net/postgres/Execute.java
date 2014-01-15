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
 * Execute (F) 
 * Byte1('E') Identifies the message as an Execute command.
 * Int32 Length of message contents in bytes, including self. 
 * String The name of the portal to execute (an empty string 
 *        selects the unnamed portal). 
 * Int32 Maximum number of rows to return, if portal contains a
 *       query that returns rows (ignored otherwise). 
 *       Zero denotes "no limit".
 * </pre>
 * 
 * @author xianmao.hexm 2012-6-26
 */
public class Execute extends PostgresPacket {

}
