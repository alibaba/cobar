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
 * CopyData (F & B) 
 * Byte1('d') Identifies the message as COPY data. 
 * Int32 Length of message contents in bytes, including self. 
 * Byten Data that forms part of a COPY data stream. Messages sent from the backend will
 *       always correspond to single data rows, but messages sent by frontends
 *       might divide the data stream arbitrarily.
 * </pre>
 * 
 * @author xianmao.hexm 2012-6-26
 */
public class CopyData extends PostgresPacket {

}
