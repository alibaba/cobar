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
 * ParameterDescription (B) 
 * Byte1('t') Identifies the message as a parameter description. 
 * Int32 Length of message contents in bytes, including self.
 * Int16 The number of parameters used by the statement (can be zero). 
 *       Then,for each parameter, there is the following: 
 * Int32 Specifies the object ID of the parameter data type.
 * </pre>
 * 
 * @author xianmao.hexm 2012-6-26
 */
public class ParameterDescription extends PostgresPacket {

}
