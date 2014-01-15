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
package com.alibaba.cobar.parser.recognizer.mysql.syntax;

import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.IDENTIFIER;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_LIMIT;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.PUNC_DOT;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.SYS_VAR;

import java.sql.SQLSyntaxErrorException;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.cobar.parser.ast.expression.primary.Identifier;
import com.alibaba.cobar.parser.ast.expression.primary.ParamMarker;
import com.alibaba.cobar.parser.ast.expression.primary.PlaceHolder;
import com.alibaba.cobar.parser.ast.expression.primary.SysVarPrimary;
import com.alibaba.cobar.parser.ast.expression.primary.Wildcard;
import com.alibaba.cobar.parser.ast.fragment.Limit;
import com.alibaba.cobar.parser.ast.fragment.VariableScope;
import com.alibaba.cobar.parser.recognizer.mysql.MySQLToken;
import com.alibaba.cobar.parser.recognizer.mysql.lexer.MySQLLexer;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public abstract class MySQLParser {
    public static final String DEFAULT_CHARSET = "utf-8";
    protected final MySQLLexer lexer;

    public MySQLParser(MySQLLexer lexer) {
        this(lexer, true);
    }

    public MySQLParser(MySQLLexer lexer, boolean cacheEvalRst) {
        this.lexer = lexer;
        this.cacheEvalRst = cacheEvalRst;
    }

    private static enum SpecialIdentifier {
        GLOBAL,
        LOCAL,
        SESSION
    }

    private static final Map<String, SpecialIdentifier> specialIdentifiers = new HashMap<String, SpecialIdentifier>();
    static {
        specialIdentifiers.put("GLOBAL", SpecialIdentifier.GLOBAL);
        specialIdentifiers.put("SESSION", SpecialIdentifier.SESSION);
        specialIdentifiers.put("LOCAL", SpecialIdentifier.LOCAL);
    }

    protected final boolean cacheEvalRst;

    /**
     * @return type of {@link Wildcard} is possible. never null
     * @throws SQLSyntaxErrorException if identifier dose not matched
     */
    public Identifier identifier() throws SQLSyntaxErrorException {
        if (lexer.token() == null) {
            lexer.nextToken();
        }
        Identifier id;
        switch (lexer.token()) {
        case OP_ASTERISK:
            lexer.nextToken();
            Wildcard wc = new Wildcard(null);
            wc.setCacheEvalRst(cacheEvalRst);
            return wc;
        case IDENTIFIER:
            id = new Identifier(null, lexer.stringValue(), lexer.stringValueUppercase());
            id.setCacheEvalRst(cacheEvalRst);
            lexer.nextToken();
            break;
        default:
            throw err("expect id or * after '.'");
        }
        for (; lexer.token() == PUNC_DOT;) {
            switch (lexer.nextToken()) {
            case OP_ASTERISK:
                lexer.nextToken();
                Wildcard wc = new Wildcard(id);
                wc.setCacheEvalRst(cacheEvalRst);
                return wc;
            case IDENTIFIER:
                id = new Identifier(id, lexer.stringValue(), lexer.stringValueUppercase());
                id.setCacheEvalRst(cacheEvalRst);
                lexer.nextToken();
                break;
            default:
                throw err("expect id or * after '.'");
            }
        }
        return id;
    }

    /**
     * first token must be {@link MySQLToken#SYS_VAR}
     */
    public SysVarPrimary systemVariale() throws SQLSyntaxErrorException {
        SysVarPrimary sys;
        VariableScope scope = VariableScope.SESSION;
        String str = lexer.stringValue();
        String strUp = lexer.stringValueUppercase();
        match(SYS_VAR);
        SpecialIdentifier si = specialIdentifiers.get(strUp);
        if (si != null) {
            switch (si) {
            case GLOBAL:
                scope = VariableScope.GLOBAL;
            case SESSION:
            case LOCAL:
                match(PUNC_DOT);
                str = lexer.stringValue();
                strUp = lexer.stringValueUppercase();
                match(IDENTIFIER);
                sys = new SysVarPrimary(scope, str, strUp);
                sys.setCacheEvalRst(cacheEvalRst);
                return sys;
            }
        }
        sys = new SysVarPrimary(scope, str, strUp);
        sys.setCacheEvalRst(cacheEvalRst);
        return sys;
    }

    protected ParamMarker createParam(int index) {
        ParamMarker param = new ParamMarker(index);
        param.setCacheEvalRst(cacheEvalRst);
        return param;
    }

    protected PlaceHolder createPlaceHolder(String str, String strUp) {
        PlaceHolder ph = new PlaceHolder(str, strUp);
        ph.setCacheEvalRst(cacheEvalRst);
        return ph;
    }

    /**
     * nothing has been pre-consumed
     * 
     * @return null if there is no order limit
     */
    protected Limit limit() throws SQLSyntaxErrorException {
        if (lexer.token() != KW_LIMIT) {
            return null;
        }
        int paramIndex1;
        int paramIndex2;
        Number num1;
        switch (lexer.nextToken()) {
        case LITERAL_NUM_PURE_DIGIT:
            num1 = lexer.integerValue();
            switch (lexer.nextToken()) {
            case PUNC_COMMA:
                switch (lexer.nextToken()) {
                case LITERAL_NUM_PURE_DIGIT:
                    Number num2 = lexer.integerValue();
                    lexer.nextToken();
                    return new Limit(num1, num2);
                case QUESTION_MARK:
                    paramIndex1 = lexer.paramIndex();
                    lexer.nextToken();
                    return new Limit(num1, createParam(paramIndex1));
                default:
                    throw err("expect digit or ? after , for limit");
                }
            case IDENTIFIER:
                if ("OFFSET".equals(lexer.stringValueUppercase())) {
                    switch (lexer.nextToken()) {
                    case LITERAL_NUM_PURE_DIGIT:
                        Number num2 = lexer.integerValue();
                        lexer.nextToken();
                        return new Limit(num2, num1);
                    case QUESTION_MARK:
                        paramIndex1 = lexer.paramIndex();
                        lexer.nextToken();
                        return new Limit(createParam(paramIndex1), num1);
                    default:
                        throw err("expect digit or ? after , for limit");
                    }
                }
            }
            return new Limit(new Integer(0), num1);
        case QUESTION_MARK:
            paramIndex1 = lexer.paramIndex();
            switch (lexer.nextToken()) {
            case PUNC_COMMA:
                switch (lexer.nextToken()) {
                case LITERAL_NUM_PURE_DIGIT:
                    num1 = lexer.integerValue();
                    lexer.nextToken();
                    return new Limit(createParam(paramIndex1), num1);
                case QUESTION_MARK:
                    paramIndex2 = lexer.paramIndex();
                    lexer.nextToken();
                    return new Limit(createParam(paramIndex1), createParam(paramIndex2));
                default:
                    throw err("expect digit or ? after , for limit");
                }
            case IDENTIFIER:
                if ("OFFSET".equals(lexer.stringValueUppercase())) {
                    switch (lexer.nextToken()) {
                    case LITERAL_NUM_PURE_DIGIT:
                        num1 = lexer.integerValue();
                        lexer.nextToken();
                        return new Limit(num1, createParam(paramIndex1));
                    case QUESTION_MARK:
                        paramIndex2 = lexer.paramIndex();
                        lexer.nextToken();
                        return new Limit(createParam(paramIndex2), createParam(paramIndex1));
                    default:
                        throw err("expect digit or ? after , for limit");
                    }
                }
            }
            return new Limit(new Integer(0), createParam(paramIndex1));
        default:
            throw err("expect digit or ? after limit");
        }
    }

    /**
     * @param expectTextUppercase must be upper-case
     * @return index (start from 0) of expected text which is first matched. -1
     *         if none is matched.
     */
    protected int equalsIdentifier(String... expectTextUppercases) throws SQLSyntaxErrorException {
        if (lexer.token() == MySQLToken.IDENTIFIER) {
            String id = lexer.stringValueUppercase();
            for (int i = 0; i < expectTextUppercases.length; ++i) {
                if (expectTextUppercases[i].equals(id)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * @return index of expected token, start from 0
     * @throws SQLSyntaxErrorException if no token is matched
     */
    protected int matchIdentifier(String... expectTextUppercase) throws SQLSyntaxErrorException {
        if (expectTextUppercase == null || expectTextUppercase.length <= 0)
            throw new IllegalArgumentException("at least one expect token");
        if (lexer.token() != MySQLToken.IDENTIFIER) {
            throw err("expect an id");
        }
        String id = lexer.stringValueUppercase();
        for (int i = 0; i < expectTextUppercase.length; ++i) {
            if (id == null ? expectTextUppercase[i] == null : id.equals(expectTextUppercase[i])) {
                lexer.nextToken();
                return i;
            }
        }
        throw err("expect " + expectTextUppercase);
    }

    /**
     * @return index of expected token, start from 0
     * @throws SQLSyntaxErrorException if no token is matched
     */
    protected int match(MySQLToken... expectToken) throws SQLSyntaxErrorException {
        if (expectToken == null || expectToken.length <= 0)
            throw new IllegalArgumentException("at least one expect token");
        MySQLToken token = lexer.token();
        for (int i = 0; i < expectToken.length; ++i) {
            if (token == expectToken[i]) {
                if (token != MySQLToken.EOF || i < expectToken.length - 1) {
                    lexer.nextToken();
                }
                return i;
            }
        }
        throw err("expect " + expectToken);
    }

    /**
     * side-effect is forbidden
     */
    protected SQLSyntaxErrorException err(String msg) throws SQLSyntaxErrorException {
        StringBuilder errmsg = new StringBuilder();
        errmsg.append(msg).append(". lexer state: ").append(String.valueOf(lexer));
        throw new SQLSyntaxErrorException(errmsg.toString());
    }
}
