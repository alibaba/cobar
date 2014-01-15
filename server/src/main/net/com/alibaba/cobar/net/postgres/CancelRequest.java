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
 * CancelRequest (F) 
 * Int32(16) Length of message contents in bytes,including self. 
 * Int32(80877102) The cancel request code. The value is chosen to 
 *                 contain 1234 in the most significant 16 bits, and 
 *                 5678 in the least 16 significant bits. (To avoid 
 *                 confusion, this code must not be the same as any 
 *                 protocol version number.) 
 * Int32 The process ID of the target backend. 
 * Int32 The secret key for the target backend.
 * </pre>
 * 
 * @author xianmao.hexm 2012-6-26
 */
public class CancelRequest extends PostgresPacket {

}
