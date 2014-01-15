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
/**
 * (created at 2011-1-21)
 */
package com.alibaba.cobar.parser.ast.expression.primary.literal;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import com.alibaba.cobar.parser.util.ParseString;
import com.alibaba.cobar.parser.visitor.SQLASTVisitor;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class LiteralHexadecimal extends Literal {
    private byte[] bytes;
    private final String introducer;
    private final String charset;
    private final char[] string;
    private final int offset;
    private final int size;

    /**
     * @param introducer e.g. "_latin1"
     * @param string e.g. "select x'89df'"
     * @param offset e.g. 9
     * @param size e.g. 4
     */
    public LiteralHexadecimal(String introducer, char[] string, int offset, int size, String charset) {
        super();
        if (string == null || offset + size > string.length)
            throw new IllegalArgumentException("hex text is invalid");
        if (charset == null)
            throw new IllegalArgumentException("charset is null");
        this.introducer = introducer;
        this.charset = charset;
        this.string = string;
        this.offset = offset;
        this.size = size;
    }

    public String getText() {
        return new String(string, offset, size);
    }

    public String getIntroducer() {
        return introducer;
    }

    public void appendTo(StringBuilder sb) {
        sb.append(string, offset, size);
    }

    @Override
    public Object evaluationInternal(Map<? extends Object, ? extends Object> parameters) {
        try {
            this.bytes = ParseString.hexString2Bytes(string, offset, size);
            return new String(bytes, introducer == null ? charset : introducer.substring(1));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("", e);
        }
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

}
