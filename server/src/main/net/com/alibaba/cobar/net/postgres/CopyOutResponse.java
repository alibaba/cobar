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
 * CopyOutResponse (B) 
 * Byte1('H') Identifies the message as a Start Copy Out response. 
 *            This message will be followed by copy-out data. Int32 Length of
 *            message contents in bytes, including self. 
 * Int8 0 indicates the overall COPY format is textual (rows separated by 
 *      newlines, columns separated by separator characters, etc). 1 indicates 
 *      the overall copy format is binary(similar to DataRow format). 
 *      See COPY for more information. 
 * Int16 The number of columns in the data to be copied (denoted N below). 
 * Int16[N] The format codes to be used for each column. Each must presently 
 *          be zero(text) or one (binary). All must be zero if the overall 
 *          copy format is textual.
 * </pre>
 * 
 * @author xianmao.hexm 2012-6-26
 */
public class CopyOutResponse extends PostgresPacket {

}
