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
 * Describe (F) 
 * Byte1('D') Identifies the message as a Describe command.
 * Int32 Length of message contents in bytes, including self. 
 * Byte1 'S' to describe a prepared statement; or 'P' to describe a portal. 
 * String The name of the prepared statement or portal to describe (an empty 
 *        string selects the unnamed prepared statement or portal).
 * </pre>
 * 
 * @author xianmao.hexm 2012-6-26
 */
public class Describe extends PostgresPacket {

}
