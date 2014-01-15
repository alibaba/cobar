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
 * (created at 2011-5-19)
 */
package com.alibaba.cobar.parser.recognizer.mysql.syntax;

import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_CALL;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.PUNC_COMMA;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.PUNC_LEFT_PAREN;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.PUNC_RIGHT_PAREN;

import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.primary.Identifier;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLCallStatement;
import com.alibaba.cobar.parser.recognizer.mysql.lexer.MySQLLexer;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class MySQLDMLCallParser extends MySQLDMLParser {
    public MySQLDMLCallParser(MySQLLexer lexer, MySQLExprParser exprParser) {
        super(lexer, exprParser);
    }

    public DMLCallStatement call() throws SQLSyntaxErrorException {
        match(KW_CALL);
        Identifier procedure = identifier();
        match(PUNC_LEFT_PAREN);
        if (lexer.token() == PUNC_RIGHT_PAREN) {
            lexer.nextToken();
            return new DMLCallStatement(procedure);
        }
        List<Expression> arguments;
        Expression expr = exprParser.expression();
        switch (lexer.token()) {
        case PUNC_COMMA:
            arguments = new LinkedList<Expression>();
            arguments.add(expr);
            for (; lexer.token() == PUNC_COMMA;) {
                lexer.nextToken();
                expr = exprParser.expression();
                arguments.add(expr);
            }
            match(PUNC_RIGHT_PAREN);
            return new DMLCallStatement(procedure, arguments);
        case PUNC_RIGHT_PAREN:
            lexer.nextToken();
            arguments = new ArrayList<Expression>(1);
            arguments.add(expr);
            return new DMLCallStatement(procedure, arguments);
        default:
            throw err("expect ',' or ')' after first argument of procedure");
        }
    }

}
