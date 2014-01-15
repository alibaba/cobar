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
 * (created at 2011-5-10)
 */
package com.alibaba.cobar.parser.recognizer.mysql.syntax;

import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.IDENTIFIER;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_AS;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_BY;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_FOR;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_GROUP;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_JOIN;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_ORDER;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_OUTER;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_UNION;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_WITH;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.LITERAL_CHARS;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.PUNC_COMMA;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.PUNC_LEFT_PAREN;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.PUNC_RIGHT_PAREN;

import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.misc.QueryExpression;
import com.alibaba.cobar.parser.ast.expression.primary.Identifier;
import com.alibaba.cobar.parser.ast.fragment.GroupBy;
import com.alibaba.cobar.parser.ast.fragment.OrderBy;
import com.alibaba.cobar.parser.ast.fragment.SortOrder;
import com.alibaba.cobar.parser.ast.fragment.tableref.IndexHint;
import com.alibaba.cobar.parser.ast.fragment.tableref.InnerJoin;
import com.alibaba.cobar.parser.ast.fragment.tableref.NaturalJoin;
import com.alibaba.cobar.parser.ast.fragment.tableref.OuterJoin;
import com.alibaba.cobar.parser.ast.fragment.tableref.StraightJoin;
import com.alibaba.cobar.parser.ast.fragment.tableref.SubqueryFactor;
import com.alibaba.cobar.parser.ast.fragment.tableref.TableRefFactor;
import com.alibaba.cobar.parser.ast.fragment.tableref.TableReference;
import com.alibaba.cobar.parser.ast.fragment.tableref.TableReferences;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLQueryStatement;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLSelectStatement;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLSelectUnionStatement;
import com.alibaba.cobar.parser.recognizer.mysql.MySQLToken;
import com.alibaba.cobar.parser.recognizer.mysql.lexer.MySQLLexer;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public abstract class MySQLDMLParser extends MySQLParser {
    protected MySQLExprParser exprParser;

    public MySQLDMLParser(MySQLLexer lexer, MySQLExprParser exprParser) {
        super(lexer);
        this.exprParser = exprParser;
    }

    /**
     * nothing has been pre-consumed
     * 
     * @return null if there is no order by
     */
    protected GroupBy groupBy() throws SQLSyntaxErrorException {
        if (lexer.token() != KW_GROUP) {
            return null;
        }
        lexer.nextToken();
        match(KW_BY);
        Expression expr = exprParser.expression();
        SortOrder order = SortOrder.ASC;
        GroupBy groupBy;
        switch (lexer.token()) {
        case KW_DESC:
            order = SortOrder.DESC;
        case KW_ASC:
            lexer.nextToken();
        default:
            break;
        }
        switch (lexer.token()) {
        case KW_WITH:
            lexer.nextToken();
            matchIdentifier("ROLLUP");
            return new GroupBy(expr, order, true);
        case PUNC_COMMA:
            break;
        default:
            return new GroupBy(expr, order, false);
        }
        for (groupBy = new GroupBy().addOrderByItem(expr, order); lexer.token() == PUNC_COMMA;) {
            lexer.nextToken();
            order = SortOrder.ASC;
            expr = exprParser.expression();
            switch (lexer.token()) {
            case KW_DESC:
                order = SortOrder.DESC;
            case KW_ASC:
                lexer.nextToken();
            default:
                break;
            }
            groupBy.addOrderByItem(expr, order);
            if (lexer.token() == KW_WITH) {
                lexer.nextToken();
                matchIdentifier("ROLLUP");
                return groupBy.setWithRollup();
            }
        }
        return groupBy;
    }

    /**
     * nothing has been pre-consumed
     * 
     * @return null if there is no order by
     */
    protected OrderBy orderBy() throws SQLSyntaxErrorException {
        if (lexer.token() != KW_ORDER) {
            return null;
        }
        lexer.nextToken();
        match(KW_BY);
        Expression expr = exprParser.expression();
        SortOrder order = SortOrder.ASC;
        OrderBy orderBy;
        switch (lexer.token()) {
        case KW_DESC:
            order = SortOrder.DESC;
        case KW_ASC:
            if (lexer.nextToken() != PUNC_COMMA) {
                return new OrderBy(expr, order);
            }
        case PUNC_COMMA:
            orderBy = new OrderBy();
            orderBy.addOrderByItem(expr, order);
            break;
        default:
            return new OrderBy(expr, order);
        }
        for (; lexer.token() == PUNC_COMMA;) {
            lexer.nextToken();
            order = SortOrder.ASC;
            expr = exprParser.expression();
            switch (lexer.token()) {
            case KW_DESC:
                order = SortOrder.DESC;
            case KW_ASC:
                lexer.nextToken();
            }
            orderBy.addOrderByItem(expr, order);
        }
        return orderBy;
    }

    /**
     * @param id never null
     */
    protected List<Identifier> buildIdList(Identifier id) throws SQLSyntaxErrorException {
        if (lexer.token() != PUNC_COMMA) {
            List<Identifier> list = new ArrayList<Identifier>(1);
            list.add(id);
            return list;
        }
        List<Identifier> list = new LinkedList<Identifier>();
        list.add(id);
        for (; lexer.token() == PUNC_COMMA;) {
            lexer.nextToken();
            id = identifier();
            list.add(id);
        }
        return list;
    }

    /**
     * <code>(id (',' id)*)?</code>
     * 
     * @return never null or empty. {@link LinkedList} is possible
     */
    protected List<Identifier> idList() throws SQLSyntaxErrorException {
        return buildIdList(identifier());
    }

    /**
     * <code>( idName (',' idName)*)? ')'</code>
     * 
     * @return empty list if emtpy id list
     */
    protected List<String> idNameList() throws SQLSyntaxErrorException {
        if (lexer.token() != IDENTIFIER) {
            match(PUNC_RIGHT_PAREN);
            return Collections.emptyList();
        }
        List<String> list;
        String str = lexer.stringValue();
        if (lexer.nextToken() == PUNC_COMMA) {
            list = new LinkedList<String>();
            list.add(str);
            for (; lexer.token() == PUNC_COMMA;) {
                lexer.nextToken();
                list.add(lexer.stringValue());
                match(IDENTIFIER);
            }
        } else {
            list = new ArrayList<String>(1);
            list.add(str);
        }
        match(PUNC_RIGHT_PAREN);
        return list;
    }

    /**
     * @return never null
     */
    protected TableReferences tableRefs() throws SQLSyntaxErrorException {
        TableReference ref = tableReference();
        return buildTableReferences(ref);
    }

    private TableReferences buildTableReferences(TableReference ref) throws SQLSyntaxErrorException {
        List<TableReference> list;
        if (lexer.token() == PUNC_COMMA) {
            list = new LinkedList<TableReference>();
            list.add(ref);
            for (; lexer.token() == PUNC_COMMA;) {
                lexer.nextToken();
                ref = tableReference();
                list.add(ref);
            }
        } else {
            list = new ArrayList<TableReference>(1);
            list.add(ref);
        }
        return new TableReferences(list);
    }

    private TableReference tableReference() throws SQLSyntaxErrorException {
        TableReference ref = tableFactor();
        return buildTableReference(ref);
    }

    @SuppressWarnings("unchecked")
    private TableReference buildTableReference(TableReference ref) throws SQLSyntaxErrorException {
        for (;;) {
            Expression on;
            List<String> using;
            TableReference temp;
            boolean isOut = false;
            boolean isLeft = true;
            switch (lexer.token()) {
            case KW_INNER:
            case KW_CROSS:
                lexer.nextToken();
            case KW_JOIN:
                lexer.nextToken();
                temp = tableFactor();
                switch (lexer.token()) {
                case KW_ON:
                    lexer.nextToken();
                    on = exprParser.expression();
                    ref = new InnerJoin(ref, temp, on);
                    break;
                case KW_USING:
                    lexer.nextToken();
                    match(PUNC_LEFT_PAREN);
                    using = idNameList();
                    ref = new InnerJoin(ref, temp, using);
                    break;
                default:
                    ref = new InnerJoin(ref, temp);
                    break;
                }
                break;
            case KW_STRAIGHT_JOIN:
                lexer.nextToken();
                temp = tableFactor();
                switch (lexer.token()) {
                case KW_ON:
                    lexer.nextToken();
                    on = exprParser.expression();
                    ref = new StraightJoin(ref, temp, on);
                    break;
                default:
                    ref = new StraightJoin(ref, temp);
                    break;
                }
                break;
            case KW_RIGHT:
                isLeft = false;
            case KW_LEFT:
                lexer.nextToken();
                if (lexer.token() == KW_OUTER) {
                    lexer.nextToken();
                }
                match(KW_JOIN);
                temp = tableReference();
                switch (lexer.token()) {
                case KW_ON:
                    lexer.nextToken();
                    on = exprParser.expression();
                    ref = new OuterJoin(isLeft, ref, temp, on);
                    break;
                case KW_USING:
                    lexer.nextToken();
                    match(PUNC_LEFT_PAREN);
                    using = idNameList();
                    ref = new OuterJoin(isLeft, ref, temp, using);
                    break;
                default:
                    Object condition = temp.removeLastConditionElement();
                    if (condition instanceof Expression) {
                        ref = new OuterJoin(isLeft, ref, temp, (Expression) condition);
                    } else if (condition instanceof List) {
                        ref = new OuterJoin(isLeft, ref, temp, (List<String>) condition);
                    } else {
                        throw err("conditionExpr cannot be null for outer join");
                    }
                    break;
                }
                break;
            case KW_NATURAL:
                lexer.nextToken();
                switch (lexer.token()) {
                case KW_RIGHT:
                    isLeft = false;
                case KW_LEFT:
                    lexer.nextToken();
                    if (lexer.token() == KW_OUTER) {
                        lexer.nextToken();
                    }
                    isOut = true;
                case KW_JOIN:
                    lexer.nextToken();
                    temp = tableFactor();
                    ref = new NaturalJoin(isOut, isLeft, ref, temp);
                    break;
                default:
                    throw err("unexpected token after NATURAL for natural join:" + lexer.token());
                }
                break;
            default:
                return ref;
            }
        }
    }

    private TableReference tableFactor() throws SQLSyntaxErrorException {
        String alias = null;
        switch (lexer.token()) {
        case PUNC_LEFT_PAREN:
            lexer.nextToken();
            Object ref = trsOrQuery();
            match(PUNC_RIGHT_PAREN);
            if (ref instanceof QueryExpression) {
                alias = as();
                return new SubqueryFactor((QueryExpression) ref, alias);
            }
            return (TableReferences) ref;
        case IDENTIFIER:
            Identifier table = identifier();
            alias = as();
            List<IndexHint> hintList = hintList();
            return new TableRefFactor(table, alias, hintList);
        default:
            throw err("unexpected token for tableFactor: " + lexer.token());
        }
    }

    /**
     * @return never empty. upper-case if id format.
     *         <code>"alias1" |"`al`ias1`" | "'alias1'" | "_latin1'alias1'"</code>
     */
    protected String as() throws SQLSyntaxErrorException {
        if (lexer.token() == KW_AS) {
            lexer.nextToken();
        }
        StringBuilder alias = new StringBuilder();
        boolean id = false;
        if (lexer.token() == IDENTIFIER) {
            alias.append(lexer.stringValueUppercase());
            id = true;
            lexer.nextToken();
        }
        if (lexer.token() == LITERAL_CHARS) {
            if (!id || id && alias.charAt(0) == '_') {
                alias.append(lexer.stringValue());
                lexer.nextToken();
            }
        }
        return alias.length() > 0 ? alias.toString() : null;
    }

    /**
     * @return type of {@link QueryExpression} or {@link TableReferences}
     */
    private Object trsOrQuery() throws SQLSyntaxErrorException {
        Object ref;
        switch (lexer.token()) {
        case KW_SELECT:
            DMLSelectStatement select = selectPrimary();
            return buildUnionSelect(select);
        case PUNC_LEFT_PAREN:
            lexer.nextToken();
            ref = trsOrQuery();
            match(PUNC_RIGHT_PAREN);
            if (ref instanceof QueryExpression) {
                if (ref instanceof DMLSelectStatement) {
                    QueryExpression rst = buildUnionSelect((DMLSelectStatement) ref);
                    if (rst != ref) {
                        return rst;
                    }
                }
                String alias = as();
                if (alias != null) {
                    ref = new SubqueryFactor((QueryExpression) ref, alias);
                } else {
                    return ref;
                }
            }
            // ---- build factor complete---------------
            ref = buildTableReference((TableReference) ref);
            // ---- build ref complete---------------
            break;
        default:
            ref = tableReference();
        }

        List<TableReference> list;
        if (lexer.token() == PUNC_COMMA) {
            list = new LinkedList<TableReference>();
            list.add((TableReference) ref);
            for (; lexer.token() == PUNC_COMMA;) {
                lexer.nextToken();
                ref = tableReference();
                list.add((TableReference) ref);
            }
            return new TableReferences(list);
        }
        list = new ArrayList<TableReference>(1);
        list.add((TableReference) ref);
        return new TableReferences(list);
    }

    /**
     * @return null if there is no hint
     */
    private List<IndexHint> hintList() throws SQLSyntaxErrorException {
        IndexHint hint = hint();
        if (hint == null)
            return null;
        List<IndexHint> list;
        IndexHint hint2 = hint();
        if (hint2 == null) {
            list = new ArrayList<IndexHint>(1);
            list.add(hint);
            return list;
        }
        list = new LinkedList<IndexHint>();
        list.add(hint);
        list.add(hint2);
        for (; (hint2 = hint()) != null; list.add(hint2));
        return list;
    }

    /**
     * @return null if there is no hint
     */
    private IndexHint hint() throws SQLSyntaxErrorException {
        IndexHint.IndexAction action;
        switch (lexer.token()) {
        case KW_USE:
            action = IndexHint.IndexAction.USE;
            break;
        case KW_IGNORE:
            action = IndexHint.IndexAction.IGNORE;
            break;
        case KW_FORCE:
            action = IndexHint.IndexAction.FORCE;
            break;
        default:
            return null;
        }
        IndexHint.IndexType type;
        switch (lexer.nextToken()) {
        case KW_INDEX:
            type = IndexHint.IndexType.INDEX;
            break;
        case KW_KEY:
            type = IndexHint.IndexType.KEY;
            break;
        default:
            throw err("must be INDEX or KEY for hint type, not " + lexer.token());
        }
        IndexHint.IndexScope scope = IndexHint.IndexScope.ALL;
        if (lexer.nextToken() == KW_FOR) {
            switch (lexer.nextToken()) {
            case KW_JOIN:
                lexer.nextToken();
                scope = IndexHint.IndexScope.JOIN;
                break;
            case KW_ORDER:
                lexer.nextToken();
                match(KW_BY);
                scope = IndexHint.IndexScope.ORDER_BY;
                break;
            case KW_GROUP:
                lexer.nextToken();
                match(KW_BY);
                scope = IndexHint.IndexScope.GROUP_BY;
                break;
            default:
                throw err("must be JOIN or ORDER or GROUP for hint scope, not " + lexer.token());
            }
        }

        match(PUNC_LEFT_PAREN);
        List<String> indexList = idNameList();
        return new IndexHint(action, type, scope, indexList);
    }

    /**
     * @return argument itself if there is no union
     */
    protected DMLQueryStatement buildUnionSelect(DMLSelectStatement select) throws SQLSyntaxErrorException {
        if (lexer.token() != KW_UNION) {
            return select;
        }
        DMLSelectUnionStatement union = new DMLSelectUnionStatement(select);
        for (; lexer.token() == KW_UNION;) {
            lexer.nextToken();
            boolean isAll = false;
            switch (lexer.token()) {
            case KW_ALL:
                isAll = true;
            case KW_DISTINCT:
                lexer.nextToken();
                break;
            }
            select = selectPrimary();
            union.addSelect(select, isAll);
        }
        union.setOrderBy(orderBy()).setLimit(limit());
        return union;
    }

    protected DMLSelectStatement selectPrimary() throws SQLSyntaxErrorException {
        switch (lexer.token()) {
        case KW_SELECT:
            return select();
        case PUNC_LEFT_PAREN:
            lexer.nextToken();
            DMLSelectStatement select = selectPrimary();
            match(PUNC_RIGHT_PAREN);
            return select;
        default:
            throw err("unexpected token: " + lexer.token());
        }
    }

    /**
     * first token is {@link MySQLToken#KW_SELECT SELECT} which has been scanned
     * but not yet consumed
     */
    public DMLSelectStatement select() throws SQLSyntaxErrorException {
        return new MySQLDMLSelectParser(lexer, exprParser).select();
    }

}
