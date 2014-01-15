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
package com.alibaba.cobar.exception;

/**
 * 未知字符集异常
 * 
 * @author xianmao.hexm
 */
public class UnknownCharsetException extends RuntimeException {
    private static final long serialVersionUID = 552833416065882969L;

    public UnknownCharsetException() {
        super();
    }

    public UnknownCharsetException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownCharsetException(String message) {
        super(message);
    }

    public UnknownCharsetException(Throwable cause) {
        super(cause);
    }

}
