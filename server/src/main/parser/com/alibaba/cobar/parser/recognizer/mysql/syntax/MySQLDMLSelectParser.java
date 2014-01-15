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
 * (created at 2011-5-13)
 */
package com.alibaba.cobar.parser.recognizer.mysql.syntax;

import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_DUAL;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_FROM;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_HAVING;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_IN;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_SELECT;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_UPDATE;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_WHERE;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.PUNC_COMMA;

import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.fragment.GroupBy;
import com.alibaba.cobar.parser.ast.fragment.Limit;
import com.alibaba.cobar.parser.ast.fragment.OrderBy;
import com.alibaba.cobar.parser.ast.fragment.tableref.Dual;
import com.alibaba.cobar.parser.ast.fragment.tableref.TableReference;
import com.alibaba.cobar.parser.ast.fragment.tableref.TableReferences;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLQueryStatement;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLSelectStatement;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLSelectUnionStatement;
import com.alibaba.cobar.parser.recognizer.mysql.MySQLToken;
import com.alibaba.cobar.parser.recognizer.mysql.lexer.MySQLLexer;
import com.alibaba.cobar.parser.util.Pair;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class MySQLDMLSelectParser extends MySQLDMLParser {
    public MySQLDMLSelectParser(MySQLLexer lexer, MySQLExprParser exprParser) {
        super(lexer, exprParser);
        this.exprParser.setSelectParser(this);
    }

    private static enum SpecialIdentifier {
        SQL_BUFFER_RESULT,
        SQL_CACHE,
        SQL_NO_CACHE
    }

    private static final Map<String, SpecialIdentifier> specialIdentifiers = new HashMap<String, SpecialIdentifier>();
    static {
        specialIdentifiers.put("SQL_BUFFER_RESULT", SpecialIdentifier.SQL_BUFFER_RESULT);
        specialIdentifiers.put("SQL_CACHE", SpecialIdentifier.SQL_CACHE);
        specialIdentifiers.put("SQL_NO_CACHE", SpecialIdentifier.SQL_NO_CACHE);
    }

    private DMLSelectStatement.SelectOption selectOption() throws SQLSyntaxErrorException {
        for (DMLSelectStatement.SelectOption option = new DMLSelectStatement.SelectOption();; lexer.nextToken()) {
            outer: switch (lexer.token()) {
            case KW_ALL:
                option.resultDup = DMLSelectStatement.SelectDuplicationStrategy.ALL;
                break outer;
            case KW_DISTINCT:
                option.resultDup = DMLSelectStatement.SelectDuplicationStrategy.DISTINCT;
                break outer;
            case KW_DISTINCTROW:
                option.resultDup = DMLSelectStatement.SelectDuplicationStrategy.DISTINCTROW;
                break outer;
            case KW_HIGH_PRIORITY:
                option.highPriority = true;
                break outer;
            case KW_STRAIGHT_JOIN:
                option.straightJoin = true;
                break outer;
            case KW_SQL_SMALL_RESULT:
                option.resultSize = DMLSelectStatement.SmallOrBigResult.SQL_SMALL_RESULT;
                break outer;
            case KW_SQL_BIG_RESULT:
                option.resultSize = DMLSelectStatement.SmallOrBigResult.SQL_BIG_RESULT;
                break outer;
            case KW_SQL_CALC_FOUND_ROWS:
                option.sqlCalcFoundRows = true;
                break outer;
            case IDENTIFIER:
                String optionStringUp = lexer.stringValueUppercase();
                SpecialIdentifier specialId = specialIdentifiers.get(optionStringUp);
                if (specialId != null) {
                    switch (specialId) {
                    case SQL_BUFFER_RESULT:
                        if (option.sqlBufferResult)
                            return option;
                        option.sqlBufferResult = true;
                        break outer;
                    case SQL_CACHE:
                        if (option.queryCache != DMLSelectStatement.QueryCacheStrategy.UNDEF)
                            return option;
                        option.queryCache = DMLSelectStatement.QueryCacheStrategy.SQL_CACHE;
                        break outer;
                    case SQL_NO_CACHE:
                        if (option.queryCache != DMLSelectStatement.QueryCacheStrategy.UNDEF)
                            return option;
                        option.queryCache = DMLSelectStatement.QueryCacheStrategy.SQL_NO_CACHE;
                        break outer;
                    }
                }
            default:
                return option;
            }
        }
    }

    private List<Pair<Expression, String>> selectExprList() throws SQLSyntaxErrorException {
        Expression expr = exprParser.expression();
        String alias = as();
        List<Pair<Expression, String>> list;
        if (lexer.token() == PUNC_COMMA) {
            list = new LinkedList<Pair<Expression, String>>();
            list.add(new Pair<Expression, String>(expr, alias));
        } else {
            list = new ArrayList<Pair<Expression, String>>(1);
            list.add(new Pair<Expression, String>(expr, alias));
            return list;
        }
        for (; lexer.token() == PUNC_COMMA; list.add(new Pair<Expression, String>(expr, alias))) {
            lexer.nextToken();
            expr = exprParser.expression();
            alias = as();
        }
        return list;
    }

    @Override
    public DMLSelectStatement select() throws SQLSyntaxErrorException {
        match(KW_SELECT);
        DMLSelectStatement.SelectOption option = selectOption();
        List<Pair<Expression, String>> exprList = selectExprList();
        TableReferences tables = null;
        Expression where = null;
        GroupBy group = null;
        Expression having = null;
        OrderBy order = null;
        Limit limit = null;

        boolean dual = false;
        if (lexer.token() == KW_FROM) {
            if (lexer.nextToken() == KW_DUAL) {
                lexer.nextToken();
                dual = true;
                List<TableReference> trs = new ArrayList<TableReference>(1);
                trs.add(new Dual());
                tables = new TableReferences(trs);
            } else {
                tables = tableRefs();
            }
        }
        if (lexer.token() == KW_WHERE) {
            lexer.nextToken();
            where = exprParser.expression();
        }
        if (!dual) {
            group = groupBy();
            if (lexer.token() == KW_HAVING) {
                lexer.nextToken();
                having = exprParser.expression();
            }
            order = orderBy();
        }
        limit = limit();
        if (!dual) {
            switch (lexer.token()) {
            case KW_FOR:
                lexer.nextToken();
                match(KW_UPDATE);
                option.lockMode = DMLSelectStatement.LockMode.FOR_UPDATE;
                break;
            case KW_LOCK:
                lexer.nextToken();
                match(KW_IN);
                matchIdentifier("SHARE");
                matchIdentifier("MODE");
                option.lockMode = DMLSelectStatement.LockMode.LOCK_IN_SHARE_MODE;
                break;
            }
        }
        return new DMLSelectStatement(option, exprList, tables, where, group, having, order, limit);
    }

    /**
     * first token is either {@link MySQLToken#KW_SELECT} or
     * {@link MySQLToken#PUNC_LEFT_PAREN} which has been scanned but not yet
     * consumed
     * 
     * @return {@link DMLSelectStatement} or {@link DMLSelectUnionStatement}
     */
    public DMLQueryStatement selectUnion() throws SQLSyntaxErrorException {
        DMLSelectStatement select = selectPrimary();
        DMLQueryStatement query = buildUnionSelect(select);
        return query;
    }

}
