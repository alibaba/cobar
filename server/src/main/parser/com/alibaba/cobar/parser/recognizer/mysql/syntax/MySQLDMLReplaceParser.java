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

import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_INTO;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_REPLACE;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.OP_ASSIGN;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.OP_EQUALS;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.PUNC_COMMA;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.PUNC_RIGHT_PAREN;

import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.misc.QueryExpression;
import com.alibaba.cobar.parser.ast.expression.primary.Identifier;
import com.alibaba.cobar.parser.ast.expression.primary.RowExpression;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLReplaceStatement;
import com.alibaba.cobar.parser.recognizer.mysql.lexer.MySQLLexer;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class MySQLDMLReplaceParser extends MySQLDMLInsertReplaceParser {
    public MySQLDMLReplaceParser(MySQLLexer lexer, MySQLExprParser exprParser) {
        super(lexer, exprParser);
    }

    /**
     * nothing has been pre-consumed <code><pre>
     * 'REPLACE' ('LOW_PRIORITY' | 'DELAYED')? ('INTO')? tableName
     *  (  'SET' colName ('='|':=') (expr|'DEFAULT') (',' colName ('='|':=') (expr|'DEFAULT'))*
     *   | '(' (  colName (','colName)* ')' (  '(' 'SELECT' ... ')'
     *                                       | 'SELECT' ...
     *                                       |('VALUES'|'VALUE') value ( ',' value )*
     *                                      )
     *          | 'SELECT' ... ')'
     *         )
     *   | 'SELECT' ...
     *   |('VALUES'|'VALUE') value ( ',' value )*
     *  )
     * value := '(' (expr|'DEFAULT') ( ',' (expr|'DEFAULT'))* ')'
     * </pre></code>
     */
    public DMLReplaceStatement replace() throws SQLSyntaxErrorException {
        match(KW_REPLACE);
        DMLReplaceStatement.ReplaceMode mode = DMLReplaceStatement.ReplaceMode.UNDEF;
        switch (lexer.token()) {
        case KW_LOW_PRIORITY:
            lexer.nextToken();
            mode = DMLReplaceStatement.ReplaceMode.LOW;
            break;
        case KW_DELAYED:
            lexer.nextToken();
            mode = DMLReplaceStatement.ReplaceMode.DELAY;
            break;
        }
        if (lexer.token() == KW_INTO) {
            lexer.nextToken();
        }
        Identifier table = identifier();
        List<Identifier> columnNameList;
        List<RowExpression> rowList;
        QueryExpression select;

        List<Expression> tempRowValue;
        switch (lexer.token()) {
        case KW_SET:
            lexer.nextToken();
            columnNameList = new LinkedList<Identifier>();
            tempRowValue = new LinkedList<Expression>();
            for (;; lexer.nextToken()) {
                Identifier id = identifier();
                match(OP_EQUALS, OP_ASSIGN);
                Expression expr = exprParser.expression();
                columnNameList.add(id);
                tempRowValue.add(expr);
                if (lexer.token() != PUNC_COMMA) {
                    break;
                }
            }
            rowList = new ArrayList<RowExpression>(1);
            rowList.add(new RowExpression(tempRowValue));
            return new DMLReplaceStatement(mode, table, columnNameList, rowList);
        case IDENTIFIER:
            if (!"VALUE".equals(lexer.stringValueUppercase())) {
                break;
            }
        case KW_VALUES:
            lexer.nextToken();
            columnNameList = null;
            rowList = rowList();
            return new DMLReplaceStatement(mode, table, columnNameList, rowList);
        case KW_SELECT:
            columnNameList = null;
            select = select();
            return new DMLReplaceStatement(mode, table, columnNameList, select);
        case PUNC_LEFT_PAREN:
            switch (lexer.nextToken()) {
            case PUNC_LEFT_PAREN:
            case KW_SELECT:
                columnNameList = null;
                select = selectPrimary();
                match(PUNC_RIGHT_PAREN);
                return new DMLReplaceStatement(mode, table, columnNameList, select);
            }
            columnNameList = idList();
            match(PUNC_RIGHT_PAREN);
            switch (lexer.token()) {
            case PUNC_LEFT_PAREN:
            case KW_SELECT:
                select = selectPrimary();
                return new DMLReplaceStatement(mode, table, columnNameList, select);
            case KW_VALUES:
                lexer.nextToken();
                break;
            default:
                matchIdentifier("VALUE");
            }
            rowList = rowList();
            return new DMLReplaceStatement(mode, table, columnNameList, rowList);
        }
        throw err("unexpected token for replace: " + lexer.token());
    }

}
