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
 * (created at 2011-4-12)
 */
package com.alibaba.cobar.parser.recognizer.mysql.syntax;

import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.EOF;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.IDENTIFIER;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_AND;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_AS;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_ASC;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_BY;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_COLLATE;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_DESC;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_DISTINCT;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_FROM;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_IN;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_INTEGER;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_LIKE;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_NOT;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_SELECT;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_SEPARATOR;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_THEN;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_USING;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_WHEN;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_WITH;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.LITERAL_CHARS;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.OP_ASSIGN;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.PUNC_COMMA;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.PUNC_DOT;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.PUNC_LEFT_PAREN;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.PUNC_RIGHT_PAREN;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.USR_VAR;

import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.arithmeic.ArithmeticAddExpression;
import com.alibaba.cobar.parser.ast.expression.arithmeic.ArithmeticDivideExpression;
import com.alibaba.cobar.parser.ast.expression.arithmeic.ArithmeticIntegerDivideExpression;
import com.alibaba.cobar.parser.ast.expression.arithmeic.ArithmeticModExpression;
import com.alibaba.cobar.parser.ast.expression.arithmeic.ArithmeticMultiplyExpression;
import com.alibaba.cobar.parser.ast.expression.arithmeic.ArithmeticSubtractExpression;
import com.alibaba.cobar.parser.ast.expression.arithmeic.MinusExpression;
import com.alibaba.cobar.parser.ast.expression.bit.BitAndExpression;
import com.alibaba.cobar.parser.ast.expression.bit.BitInvertExpression;
import com.alibaba.cobar.parser.ast.expression.bit.BitOrExpression;
import com.alibaba.cobar.parser.ast.expression.bit.BitShiftExpression;
import com.alibaba.cobar.parser.ast.expression.bit.BitXORExpression;
import com.alibaba.cobar.parser.ast.expression.comparison.BetweenAndExpression;
import com.alibaba.cobar.parser.ast.expression.comparison.ComparisionEqualsExpression;
import com.alibaba.cobar.parser.ast.expression.comparison.ComparisionGreaterThanExpression;
import com.alibaba.cobar.parser.ast.expression.comparison.ComparisionGreaterThanOrEqualsExpression;
import com.alibaba.cobar.parser.ast.expression.comparison.ComparisionIsExpression;
import com.alibaba.cobar.parser.ast.expression.comparison.ComparisionLessOrGreaterThanExpression;
import com.alibaba.cobar.parser.ast.expression.comparison.ComparisionLessThanExpression;
import com.alibaba.cobar.parser.ast.expression.comparison.ComparisionLessThanOrEqualsExpression;
import com.alibaba.cobar.parser.ast.expression.comparison.ComparisionNotEqualsExpression;
import com.alibaba.cobar.parser.ast.expression.comparison.ComparisionNullSafeEqualsExpression;
import com.alibaba.cobar.parser.ast.expression.comparison.InExpression;
import com.alibaba.cobar.parser.ast.expression.logical.LogicalAndExpression;
import com.alibaba.cobar.parser.ast.expression.logical.LogicalNotExpression;
import com.alibaba.cobar.parser.ast.expression.logical.LogicalOrExpression;
import com.alibaba.cobar.parser.ast.expression.logical.LogicalXORExpression;
import com.alibaba.cobar.parser.ast.expression.logical.NegativeValueExpression;
import com.alibaba.cobar.parser.ast.expression.misc.AssignmentExpression;
import com.alibaba.cobar.parser.ast.expression.misc.InExpressionList;
import com.alibaba.cobar.parser.ast.expression.misc.QueryExpression;
import com.alibaba.cobar.parser.ast.expression.misc.SubqueryAllExpression;
import com.alibaba.cobar.parser.ast.expression.misc.SubqueryAnyExpression;
import com.alibaba.cobar.parser.ast.expression.misc.UserExpression;
import com.alibaba.cobar.parser.ast.expression.primary.CaseWhenOperatorExpression;
import com.alibaba.cobar.parser.ast.expression.primary.DefaultValue;
import com.alibaba.cobar.parser.ast.expression.primary.ExistsPrimary;
import com.alibaba.cobar.parser.ast.expression.primary.Identifier;
import com.alibaba.cobar.parser.ast.expression.primary.MatchExpression;
import com.alibaba.cobar.parser.ast.expression.primary.MatchExpression.Modifier;
import com.alibaba.cobar.parser.ast.expression.primary.RowExpression;
import com.alibaba.cobar.parser.ast.expression.primary.UsrDefVarPrimary;
import com.alibaba.cobar.parser.ast.expression.primary.Wildcard;
import com.alibaba.cobar.parser.ast.expression.primary.function.FunctionExpression;
import com.alibaba.cobar.parser.ast.expression.primary.function.cast.Cast;
import com.alibaba.cobar.parser.ast.expression.primary.function.cast.Convert;
import com.alibaba.cobar.parser.ast.expression.primary.function.comparison.Interval;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.Curdate;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.Curtime;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.Extract;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.GetFormat;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.Now;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.Timestampadd;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.Timestampdiff;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.UtcDate;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.UtcTime;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.UtcTimestamp;
import com.alibaba.cobar.parser.ast.expression.primary.function.groupby.Avg;
import com.alibaba.cobar.parser.ast.expression.primary.function.groupby.Count;
import com.alibaba.cobar.parser.ast.expression.primary.function.groupby.GroupConcat;
import com.alibaba.cobar.parser.ast.expression.primary.function.groupby.Max;
import com.alibaba.cobar.parser.ast.expression.primary.function.groupby.Min;
import com.alibaba.cobar.parser.ast.expression.primary.function.groupby.Sum;
import com.alibaba.cobar.parser.ast.expression.primary.function.info.CurrentUser;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.Char;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.Locate;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.Substring;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.Trim;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.Trim.Direction;
import com.alibaba.cobar.parser.ast.expression.primary.literal.IntervalPrimary;
import com.alibaba.cobar.parser.ast.expression.primary.literal.LiteralBitField;
import com.alibaba.cobar.parser.ast.expression.primary.literal.LiteralBoolean;
import com.alibaba.cobar.parser.ast.expression.primary.literal.LiteralHexadecimal;
import com.alibaba.cobar.parser.ast.expression.primary.literal.LiteralNull;
import com.alibaba.cobar.parser.ast.expression.primary.literal.LiteralNumber;
import com.alibaba.cobar.parser.ast.expression.primary.literal.LiteralString;
import com.alibaba.cobar.parser.ast.expression.string.LikeExpression;
import com.alibaba.cobar.parser.ast.expression.string.RegexpExpression;
import com.alibaba.cobar.parser.ast.expression.string.SoundsLikeExpression;
import com.alibaba.cobar.parser.ast.expression.type.CastBinaryExpression;
import com.alibaba.cobar.parser.ast.expression.type.CollateExpression;
import com.alibaba.cobar.parser.recognizer.mysql.MySQLFunctionManager;
import com.alibaba.cobar.parser.recognizer.mysql.MySQLToken;
import com.alibaba.cobar.parser.recognizer.mysql.lexer.MySQLLexer;
import com.alibaba.cobar.parser.util.Pair;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class MySQLExprParser extends MySQLParser {
    public MySQLExprParser(MySQLLexer lexer) {
        this(lexer, MySQLFunctionManager.INSTANCE_MYSQL_DEFAULT, true, DEFAULT_CHARSET);
    }

    public MySQLExprParser(MySQLLexer lexer, String charset) {
        this(lexer, MySQLFunctionManager.INSTANCE_MYSQL_DEFAULT, true, charset);
    }

    public MySQLExprParser(MySQLLexer lexer, MySQLFunctionManager functionManager, boolean cacheEvalRst, String charset) {
        super(lexer, cacheEvalRst);
        this.functionManager = functionManager;
        this.charset = charset == null ? DEFAULT_CHARSET : charset;
    }

    private String charset;
    private final MySQLFunctionManager functionManager;
    private MySQLDMLSelectParser selectParser;

    public void setSelectParser(MySQLDMLSelectParser selectParser) {
        this.selectParser = selectParser;
    }

    /**
     * first token of this expression has been scanned, not yet consumed
     */
    public Expression expression() throws SQLSyntaxErrorException {
        MySQLToken token = lexer.token();
        if (token == null) {
            token = lexer.nextToken();
        }
        if (token == EOF) {
            err("unexpected EOF");
        }
        Expression left = logicalOrExpression();
        if (lexer.token() == OP_ASSIGN) {
            lexer.nextToken();
            Expression right = expression();
            return new AssignmentExpression(left, right).setCacheEvalRst(cacheEvalRst);
        }
        return left;
    }

    /**
     * <code>higherPRJExpr ( ( '||' | 'OR' ) higherPRJExpr )*</code>
     * 
     * @throws SQLSyntaxErrorException
     */
    private Expression logicalOrExpression() throws SQLSyntaxErrorException {
        LogicalOrExpression or = null;
        for (Expression expr = logicalXORExpression();;) {
            switch (lexer.token()) {
            case OP_LOGICAL_OR:
            case KW_OR:
                lexer.nextToken();
                if (or == null) {
                    or = new LogicalOrExpression();
                    or.setCacheEvalRst(cacheEvalRst);
                    or.appendOperand(expr);
                    expr = or;
                }
                Expression newExpr = logicalXORExpression();
                or.appendOperand(newExpr);
                break;
            default:
                return expr;
            }
        }
    }

    /**
     * <code>higherPRJExpr ( 'XOR' higherPRJExpr )*</code>
     * 
     * @throws SQLSyntaxErrorException
     */
    private Expression logicalXORExpression() throws SQLSyntaxErrorException {
        for (Expression expr = logicalAndExpression();;) {
            switch (lexer.token()) {
            case KW_XOR:
                lexer.nextToken();
                Expression newExpr = logicalAndExpression();
                expr = new LogicalXORExpression(expr, newExpr).setCacheEvalRst(cacheEvalRst);
                break;
            default:
                return expr;
            }
        }
    }

    /**
     * <code>higherPRJExpr ( ('AND'|'&&') higherPRJExpr )*</code>
     * 
     * @throws SQLSyntaxErrorException
     */
    private Expression logicalAndExpression() throws SQLSyntaxErrorException {
        LogicalAndExpression and = null;
        for (Expression expr = logicalNotExpression();;) {
            switch (lexer.token()) {
            case OP_LOGICAL_AND:
            case KW_AND:
                lexer.nextToken();
                if (and == null) {
                    and = new LogicalAndExpression();
                    and.setCacheEvalRst(cacheEvalRst);
                    and.appendOperand(expr);
                    expr = and;
                }
                Expression newExpr = logicalNotExpression();
                and.appendOperand(newExpr);
                break;
            default:
                return expr;
            }
        }
    }

    /**
     * <code>'NOT'* higherPRJExpr</code>
     * 
     * @throws SQLSyntaxErrorException
     */
    private Expression logicalNotExpression() throws SQLSyntaxErrorException {
        int not = 0;
        for (; lexer.token() == KW_NOT; ++not) {
            lexer.nextToken();
        }
        Expression expr = comparisionExpression();
        for (; not > 0; --not) {
            expr = new LogicalNotExpression(expr).setCacheEvalRst(cacheEvalRst);
        }
        return expr;
    }

    /**
     * <code>BETWEEN ... AND</code> has lower precedence than other comparison
     * operator
     */
    private Expression comparisionExpression() throws SQLSyntaxErrorException {
        Expression temp;
        for (Expression fst = bitOrExpression(null, null);;) {
            switch (lexer.token()) {
            case KW_NOT:
                lexer.nextToken();
                switch (lexer.token()) {
                case KW_BETWEEN:
                    lexer.nextToken();
                    Expression snd = comparisionExpression();
                    match(KW_AND);
                    Expression trd = comparisionExpression();
                    return new BetweenAndExpression(true, fst, snd, trd).setCacheEvalRst(cacheEvalRst);
                case KW_RLIKE:
                case KW_REGEXP:
                    lexer.nextToken();
                    temp = bitOrExpression(null, null);
                    fst = new RegexpExpression(true, fst, temp).setCacheEvalRst(cacheEvalRst);
                    continue;
                case KW_LIKE:
                    lexer.nextToken();
                    temp = bitOrExpression(null, null);
                    Expression escape = null;
                    if (equalsIdentifier("ESCAPE") >= 0) {
                        lexer.nextToken();
                        escape = bitOrExpression(null, null);
                    }
                    fst = new LikeExpression(true, fst, temp, escape).setCacheEvalRst(cacheEvalRst);
                    continue;
                case KW_IN:
                    if (lexer.nextToken() != PUNC_LEFT_PAREN) {
                        lexer.addCacheToke(KW_IN);
                        return fst;
                    }
                    Expression in = rightOprandOfIn();
                    fst = new InExpression(true, fst, in).setCacheEvalRst(cacheEvalRst);
                    continue;
                default:
                    throw err("unexpect token after NOT: " + lexer.token());
                }
            case KW_BETWEEN:
                lexer.nextToken();
                Expression snd = comparisionExpression();
                match(KW_AND);
                Expression trd = comparisionExpression();
                return new BetweenAndExpression(false, fst, snd, trd).setCacheEvalRst(cacheEvalRst);
            case KW_RLIKE:
            case KW_REGEXP:
                lexer.nextToken();
                temp = bitOrExpression(null, null);
                fst = new RegexpExpression(false, fst, temp).setCacheEvalRst(cacheEvalRst);
                continue;
            case KW_LIKE:
                lexer.nextToken();
                temp = bitOrExpression(null, null);
                Expression escape = null;
                if (equalsIdentifier("ESCAPE") >= 0) {
                    lexer.nextToken();
                    escape = bitOrExpression(null, null);
                }
                fst = new LikeExpression(false, fst, temp, escape).setCacheEvalRst(cacheEvalRst);
                continue;
            case KW_IN:
                if (lexer.nextToken() != PUNC_LEFT_PAREN) {
                    lexer.addCacheToke(KW_IN);
                    return fst;
                }
                temp = rightOprandOfIn();
                fst = new InExpression(false, fst, temp).setCacheEvalRst(cacheEvalRst);
                continue;
            case KW_IS:
                switch (lexer.nextToken()) {
                case KW_NOT:
                    switch (lexer.nextToken()) {
                    case LITERAL_NULL:
                        lexer.nextToken();
                        fst = new ComparisionIsExpression(fst, ComparisionIsExpression.IS_NOT_NULL).setCacheEvalRst(cacheEvalRst);
                        continue;
                    case LITERAL_BOOL_FALSE:
                        lexer.nextToken();
                        fst = new ComparisionIsExpression(fst, ComparisionIsExpression.IS_NOT_FALSE).setCacheEvalRst(cacheEvalRst);
                        continue;
                    case LITERAL_BOOL_TRUE:
                        lexer.nextToken();
                        fst = new ComparisionIsExpression(fst, ComparisionIsExpression.IS_NOT_TRUE).setCacheEvalRst(cacheEvalRst);
                        continue;
                    default:
                        matchIdentifier("UNKNOWN");
                        fst = new ComparisionIsExpression(fst, ComparisionIsExpression.IS_NOT_UNKNOWN).setCacheEvalRst(cacheEvalRst);
                        continue;
                    }
                case LITERAL_NULL:
                    lexer.nextToken();
                    fst = new ComparisionIsExpression(fst, ComparisionIsExpression.IS_NULL).setCacheEvalRst(cacheEvalRst);
                    continue;
                case LITERAL_BOOL_FALSE:
                    lexer.nextToken();
                    fst = new ComparisionIsExpression(fst, ComparisionIsExpression.IS_FALSE).setCacheEvalRst(cacheEvalRst);
                    continue;
                case LITERAL_BOOL_TRUE:
                    lexer.nextToken();
                    fst = new ComparisionIsExpression(fst, ComparisionIsExpression.IS_TRUE).setCacheEvalRst(cacheEvalRst);
                    continue;
                default:
                    matchIdentifier("UNKNOWN");
                    fst = new ComparisionIsExpression(fst, ComparisionIsExpression.IS_UNKNOWN).setCacheEvalRst(cacheEvalRst);
                    continue;
                }
            case OP_EQUALS:
                lexer.nextToken();
                temp = anyAllExpression();
                fst = new ComparisionEqualsExpression(fst, temp).setCacheEvalRst(cacheEvalRst);
                continue;
            case OP_NULL_SAFE_EQUALS:
                lexer.nextToken();
                temp = bitOrExpression(null, null);
                fst = new ComparisionNullSafeEqualsExpression(fst, temp).setCacheEvalRst(cacheEvalRst);
                continue;
            case OP_GREATER_OR_EQUALS:
                lexer.nextToken();
                temp = anyAllExpression();
                fst = new ComparisionGreaterThanOrEqualsExpression(fst, temp).setCacheEvalRst(cacheEvalRst);
                continue;
            case OP_GREATER_THAN:
                lexer.nextToken();
                temp = anyAllExpression();
                fst = new ComparisionGreaterThanExpression(fst, temp).setCacheEvalRst(cacheEvalRst);
                continue;
            case OP_LESS_OR_EQUALS:
                lexer.nextToken();
                temp = anyAllExpression();
                fst = new ComparisionLessThanOrEqualsExpression(fst, temp).setCacheEvalRst(cacheEvalRst);
                continue;
            case OP_LESS_THAN:
                lexer.nextToken();
                temp = anyAllExpression();
                fst = new ComparisionLessThanExpression(fst, temp).setCacheEvalRst(cacheEvalRst);
                continue;
            case OP_LESS_OR_GREATER:
                lexer.nextToken();
                temp = anyAllExpression();
                fst = new ComparisionLessOrGreaterThanExpression(fst, temp).setCacheEvalRst(cacheEvalRst);
                continue;
            case OP_NOT_EQUALS:
                lexer.nextToken();
                temp = anyAllExpression();
                fst = new ComparisionNotEqualsExpression(fst, temp).setCacheEvalRst(cacheEvalRst);
                continue;
            default:
                if (equalsIdentifier("SOUNDS") >= 0) {
                    lexer.nextToken();
                    match(KW_LIKE);
                    temp = bitOrExpression(null, null);
                    fst = new SoundsLikeExpression(fst, temp).setCacheEvalRst(cacheEvalRst);
                    continue;
                }
                return fst;
            }
        }
    }

    /**
     * @return {@link QueryExpression} or {@link InExpressionList}
     */
    private Expression rightOprandOfIn() throws SQLSyntaxErrorException {
        match(PUNC_LEFT_PAREN);
        if (KW_SELECT == lexer.token()) {
            QueryExpression subq = subQuery();
            match(PUNC_RIGHT_PAREN);
            return subq;
        }
        return new InExpressionList(expressionList(new LinkedList<Expression>())).setCacheEvalRst(cacheEvalRst);
    }

    private Expression anyAllExpression() throws SQLSyntaxErrorException {
        QueryExpression subquery = null;
        switch (lexer.token()) {
        case KW_ALL:
            lexer.nextToken();
            match(PUNC_LEFT_PAREN);
            subquery = subQuery();
            match(PUNC_RIGHT_PAREN);
            return new SubqueryAllExpression(subquery).setCacheEvalRst(cacheEvalRst);
        default:
            int matchIndex = equalsIdentifier("SOME", "ANY");
            if (matchIndex < 0) {
                return bitOrExpression(null, null);
            }
            String consumed = lexer.stringValue();
            String consumedUp = lexer.stringValueUppercase();
            if (lexer.nextToken() == PUNC_LEFT_PAREN) {
                lexer.nextToken();
                subquery = subQuery();
                match(PUNC_RIGHT_PAREN);
                return new SubqueryAnyExpression(subquery).setCacheEvalRst(cacheEvalRst);
            }
            return bitOrExpression(consumed, consumedUp);
        }
    }

    /**
     * @param consumed not null means that a token that has been pre-consumed
     *            stands for next token
     */
    private Expression bitOrExpression(String consumed, String consumedUp) throws SQLSyntaxErrorException {
        for (Expression expr = bitAndExpression(consumed, consumedUp);;) {
            switch (lexer.token()) {
            case OP_VERTICAL_BAR:
                lexer.nextToken();
                Expression newExpr = bitAndExpression(null, null);
                expr = new BitOrExpression(expr, newExpr).setCacheEvalRst(cacheEvalRst);
                break;
            default:
                return expr;
            }
        }
    }

    private Expression bitAndExpression(String consumed, String consumedUp) throws SQLSyntaxErrorException {
        for (Expression expr = bitShiftExpression(consumed, consumedUp);;) {
            switch (lexer.token()) {
            case OP_AMPERSAND:
                lexer.nextToken();
                Expression newExpr = bitShiftExpression(null, null);
                expr = new BitAndExpression(expr, newExpr).setCacheEvalRst(cacheEvalRst);
                break;
            default:
                return expr;
            }
        }
    }

    /**
     * <code>higherExpr ( ('&lt;&lt;'|'&gt;&gt;') higherExpr)+</code>
     */
    private Expression bitShiftExpression(String consumed, String consumedUp) throws SQLSyntaxErrorException {
        Expression temp;
        for (Expression expr = arithmeticTermOperatorExpression(consumed, consumedUp);;) {
            switch (lexer.token()) {
            case OP_LEFT_SHIFT:
                lexer.nextToken();
                temp = arithmeticTermOperatorExpression(null, null);
                expr = new BitShiftExpression(false, expr, temp).setCacheEvalRst(cacheEvalRst);
                break;
            case OP_RIGHT_SHIFT:
                lexer.nextToken();
                temp = arithmeticTermOperatorExpression(null, null);
                expr = new BitShiftExpression(true, expr, temp).setCacheEvalRst(cacheEvalRst);
                break;
            default:
                return expr;
            }
        }
    }

    /**
     * <code>higherExpr ( ('+'|'-') higherExpr)+</code>
     */
    private Expression arithmeticTermOperatorExpression(String consumed, String consumedUp)
            throws SQLSyntaxErrorException {
        Expression temp;
        for (Expression expr = arithmeticFactorOperatorExpression(consumed, consumedUp);;) {
            switch (lexer.token()) {
            case OP_PLUS:
                lexer.nextToken();
                temp = arithmeticFactorOperatorExpression(null, null);
                expr = new ArithmeticAddExpression(expr, temp).setCacheEvalRst(cacheEvalRst);
                break;
            case OP_MINUS:
                lexer.nextToken();
                temp = arithmeticFactorOperatorExpression(null, null);
                expr = new ArithmeticSubtractExpression(expr, temp).setCacheEvalRst(cacheEvalRst);
                break;
            default:
                return expr;
            }
        }
    }

    /**
     * <code>higherExpr ( ('*'|'/'|'%'|'DIV'|'MOD') higherExpr)+</code>
     */
    private Expression arithmeticFactorOperatorExpression(String consumed, String consumedUp)
            throws SQLSyntaxErrorException {
        Expression temp;
        for (Expression expr = bitXORExpression(consumed, consumedUp);;) {
            switch (lexer.token()) {
            case OP_ASTERISK:
                lexer.nextToken();
                temp = bitXORExpression(null, null);
                expr = new ArithmeticMultiplyExpression(expr, temp).setCacheEvalRst(cacheEvalRst);
                break;
            case OP_SLASH:
                lexer.nextToken();
                temp = bitXORExpression(null, null);
                expr = new ArithmeticDivideExpression(expr, temp).setCacheEvalRst(cacheEvalRst);
                break;
            case KW_DIV:
                lexer.nextToken();
                temp = bitXORExpression(null, null);
                expr = new ArithmeticIntegerDivideExpression(expr, temp).setCacheEvalRst(cacheEvalRst);
                break;
            case OP_PERCENT:
            case KW_MOD:
                lexer.nextToken();
                temp = bitXORExpression(null, null);
                expr = new ArithmeticModExpression(expr, temp).setCacheEvalRst(cacheEvalRst);
                break;
            default:
                return expr;
            }
        }
    }

    /**
     * <code>higherExpr ('^' higherExpr)+</code>
     */
    private Expression bitXORExpression(String consumed, String consumedUp) throws SQLSyntaxErrorException {
        Expression temp;
        for (Expression expr = unaryOpExpression(consumed, consumedUp);;) {
            switch (lexer.token()) {
            case OP_CARET:
                lexer.nextToken();
                temp = unaryOpExpression(null, null);
                expr = new BitXORExpression(expr, temp).setCacheEvalRst(cacheEvalRst);
                break;
            default:
                return expr;
            }
        }
    }

    /**
     * <code>('+'|'-'|'~'|'!'|'BINARY')* higherExpr</code><br/>
     * '!' has higher precedence
     */
    private Expression unaryOpExpression(String consumed, String consumedUp) throws SQLSyntaxErrorException {
        if (consumed == null) {
            Expression expr;
            switch (lexer.token()) {
            case OP_EXCLAMATION:
                lexer.nextToken();
                expr = unaryOpExpression(null, null);
                return new NegativeValueExpression(expr).setCacheEvalRst(cacheEvalRst);
            case OP_PLUS:
                lexer.nextToken();
                return unaryOpExpression(null, null);
            case OP_MINUS:
                lexer.nextToken();
                expr = unaryOpExpression(null, null);
                return new MinusExpression(expr).setCacheEvalRst(cacheEvalRst);
            case OP_TILDE:
                lexer.nextToken();
                expr = unaryOpExpression(null, null);
                return new BitInvertExpression(expr).setCacheEvalRst(cacheEvalRst);
            case KW_BINARY:
                lexer.nextToken();
                expr = unaryOpExpression(null, null);
                return new CastBinaryExpression(expr).setCacheEvalRst(cacheEvalRst);
            }
        }
        return collateExpression(consumed, consumedUp);
    }

    private Expression collateExpression(String consumed, String consumedUp) throws SQLSyntaxErrorException {
        for (Expression expr = userExpression(consumed, consumedUp);;) {
            if (lexer.token() == KW_COLLATE) {
                lexer.nextToken();
                String collateName = lexer.stringValue();
                match(IDENTIFIER);
                expr = new CollateExpression(expr, collateName).setCacheEvalRst(cacheEvalRst);
                continue;
            }
            return expr;
        }
    }

    private Expression userExpression(String consumed, String consumedUp) throws SQLSyntaxErrorException {
        Expression first = primaryExpression(consumed, consumedUp);
        if (lexer.token() == USR_VAR) {
            if (first instanceof LiteralString) {
                StringBuilder str = new StringBuilder().append('\'')
                                                       .append(((LiteralString) first).getString())
                                                       .append('\'')
                                                       .append(lexer.stringValue());
                lexer.nextToken();
                return new UserExpression(str.toString()).setCacheEvalRst(cacheEvalRst);
            } else if (first instanceof Identifier) {
                StringBuilder str = new StringBuilder().append(((Identifier) first).getIdText()).append(
                        lexer.stringValue());
                lexer.nextToken();
                return new UserExpression(str.toString()).setCacheEvalRst(cacheEvalRst);
            }
        }
        return first;
    }

    private Expression primaryExpression(final String consumed, String consumedUp) throws SQLSyntaxErrorException {
        if (consumed != null) {
            return startedFromIdentifier(consumed, consumedUp);
        }
        String tempStr;
        String tempStrUp;
        StringBuilder tempSb;
        Number tempNum;
        Expression tempExpr;
        Expression tempExpr2;
        List<Expression> tempExprList;
        switch (lexer.token()) {
        case PLACE_HOLDER:
            tempStr = lexer.stringValue();
            tempStrUp = lexer.stringValueUppercase();
            lexer.nextToken();
            return createPlaceHolder(tempStr, tempStrUp);
        case LITERAL_BIT:
            tempStr = lexer.stringValue();
            lexer.nextToken();
            return new LiteralBitField(null, tempStr).setCacheEvalRst(cacheEvalRst);
        case LITERAL_HEX:
            LiteralHexadecimal hex = new LiteralHexadecimal(
                    null,
                    lexer.getSQL(),
                    lexer.getOffsetCache(),
                    lexer.getSizeCache(),
                    charset);
            lexer.nextToken();
            return hex.setCacheEvalRst(cacheEvalRst);
        case LITERAL_BOOL_FALSE:
            lexer.nextToken();
            return new LiteralBoolean(false).setCacheEvalRst(cacheEvalRst);
        case LITERAL_BOOL_TRUE:
            lexer.nextToken();
            return new LiteralBoolean(true).setCacheEvalRst(cacheEvalRst);
        case LITERAL_NULL:
            lexer.nextToken();
            return new LiteralNull().setCacheEvalRst(cacheEvalRst);
        case LITERAL_NCHARS:
            tempSb = new StringBuilder();
            do {
                lexer.appendStringContent(tempSb);
            } while (lexer.nextToken() == LITERAL_CHARS);
            return new LiteralString(null, tempSb.toString(), true).setCacheEvalRst(cacheEvalRst);
        case LITERAL_CHARS:
            tempSb = new StringBuilder();
            do {
                lexer.appendStringContent(tempSb);
            } while (lexer.nextToken() == LITERAL_CHARS);
            return new LiteralString(null, tempSb.toString(), false).setCacheEvalRst(cacheEvalRst);
        case LITERAL_NUM_PURE_DIGIT:
            tempNum = lexer.integerValue();
            lexer.nextToken();
            return new LiteralNumber(tempNum).setCacheEvalRst(cacheEvalRst);
        case LITERAL_NUM_MIX_DIGIT:
            tempNum = lexer.decimalValue();
            lexer.nextToken();
            return new LiteralNumber(tempNum).setCacheEvalRst(cacheEvalRst);
        case QUESTION_MARK:
            int index = lexer.paramIndex();
            lexer.nextToken();
            return createParam(index);
        case KW_CASE:
            lexer.nextToken();
            return caseWhenExpression();
        case KW_INTERVAL:
            lexer.nextToken();
            return intervalExpression();
        case KW_EXISTS:
            lexer.nextToken();
            match(PUNC_LEFT_PAREN);
            tempExpr = subQuery();
            match(PUNC_RIGHT_PAREN);
            return new ExistsPrimary((QueryExpression) tempExpr).setCacheEvalRst(cacheEvalRst);
        case USR_VAR:
            tempStr = lexer.stringValue();
            tempExpr = new UsrDefVarPrimary(tempStr).setCacheEvalRst(cacheEvalRst);
            if (lexer.nextToken() == OP_ASSIGN) {
                lexer.nextToken();
                tempExpr2 = expression();
                return new AssignmentExpression(tempExpr, tempExpr2);
            }
            return tempExpr;
        case SYS_VAR:
            return systemVariale();
        case KW_MATCH:
            lexer.nextToken();
            return matchExpression();
        case PUNC_LEFT_PAREN:
            lexer.nextToken();
            if (lexer.token() == KW_SELECT) {
                tempExpr = subQuery();
                match(PUNC_RIGHT_PAREN);
                return tempExpr;
            }
            tempExpr = expression();
            switch (lexer.token()) {
            case PUNC_RIGHT_PAREN:
                lexer.nextToken();
                return tempExpr;
            case PUNC_COMMA:
                lexer.nextToken();
                tempExprList = new LinkedList<Expression>();
                tempExprList.add(tempExpr);
                tempExprList = expressionList(tempExprList);
                return new RowExpression(tempExprList).setCacheEvalRst(cacheEvalRst);
            default:
                throw err("unexpected token: " + lexer.token());
            }
        case KW_UTC_DATE:
            lexer.nextToken();
            if (lexer.token() == PUNC_LEFT_PAREN) {
                lexer.nextToken();
                match(PUNC_RIGHT_PAREN);
            }
            return new UtcDate(null).setCacheEvalRst(cacheEvalRst);
        case KW_UTC_TIME:
            lexer.nextToken();
            if (lexer.token() == PUNC_LEFT_PAREN) {
                lexer.nextToken();
                match(PUNC_RIGHT_PAREN);
            }
            return new UtcTime(null).setCacheEvalRst(cacheEvalRst);
        case KW_UTC_TIMESTAMP:
            lexer.nextToken();
            if (lexer.token() == PUNC_LEFT_PAREN) {
                lexer.nextToken();
                match(PUNC_RIGHT_PAREN);
            }
            return new UtcTimestamp(null).setCacheEvalRst(cacheEvalRst);
        case KW_CURRENT_DATE:
            lexer.nextToken();
            if (lexer.token() == PUNC_LEFT_PAREN) {
                lexer.nextToken();
                match(PUNC_RIGHT_PAREN);
            }
            return new Curdate().setCacheEvalRst(cacheEvalRst);
        case KW_CURRENT_TIME:
            lexer.nextToken();
            if (lexer.token() == PUNC_LEFT_PAREN) {
                lexer.nextToken();
                match(PUNC_RIGHT_PAREN);
            }
            return new Curtime().setCacheEvalRst(cacheEvalRst);
        case KW_CURRENT_TIMESTAMP:
        case KW_LOCALTIME:
        case KW_LOCALTIMESTAMP:
            lexer.nextToken();
            if (lexer.token() == PUNC_LEFT_PAREN) {
                lexer.nextToken();
                match(PUNC_RIGHT_PAREN);
            }
            return new Now().setCacheEvalRst(cacheEvalRst);
        case KW_CURRENT_USER:
            lexer.nextToken();
            if (lexer.token() == PUNC_LEFT_PAREN) {
                lexer.nextToken();
                match(PUNC_RIGHT_PAREN);
            }
            return new CurrentUser().setCacheEvalRst(cacheEvalRst);
        case KW_DEFAULT:
            if (lexer.nextToken() == PUNC_LEFT_PAREN) {
                return ordinaryFunction(lexer.stringValue(), lexer.stringValueUppercase());
            }
            return new DefaultValue().setCacheEvalRst(cacheEvalRst);
        case KW_DATABASE:
        case KW_IF:
        case KW_INSERT:
        case KW_LEFT:
        case KW_REPEAT:
        case KW_REPLACE:
        case KW_RIGHT:
        case KW_SCHEMA:
        case KW_VALUES:
            tempStr = lexer.stringValue();
            tempStrUp = lexer.stringValueUppercase();
            String tempStrUp2 = MySQLToken.keyWordToString(lexer.token());
            if (!tempStrUp2.equals(tempStrUp)) {
                tempStrUp = tempStr = tempStrUp2;
            }
            if (lexer.nextToken() == PUNC_LEFT_PAREN) {
                return ordinaryFunction(tempStr, tempStrUp);
            }
            throw err("keyword not followed by '(' is not expression: " + tempStr);
        case KW_MOD:
            lexer.nextToken();
            match(PUNC_LEFT_PAREN);
            tempExpr = expression();
            match(PUNC_COMMA);
            tempExpr2 = expression();
            match(PUNC_RIGHT_PAREN);
            return new ArithmeticModExpression(tempExpr, tempExpr2).setCacheEvalRst(cacheEvalRst);
        case KW_CHAR:
            lexer.nextToken();
            match(PUNC_LEFT_PAREN);
            return functionChar();
        case KW_CONVERT:
            lexer.nextToken();
            match(PUNC_LEFT_PAREN);
            return functionConvert();
        case IDENTIFIER:
            tempStr = lexer.stringValue();
            tempStrUp = lexer.stringValueUppercase();
            lexer.nextToken();
            return startedFromIdentifier(tempStr, tempStrUp);
        case OP_ASTERISK:
            lexer.nextToken();
            return new Wildcard(null).setCacheEvalRst(cacheEvalRst);
        default:
            throw err("unrecognized token as first token of primary: " + lexer.token());
        }
    }

    /**
     * first '(' has been consumed
     */
    private Timestampdiff timestampdiff() throws SQLSyntaxErrorException {
        IntervalPrimary.Unit unit = intervalPrimaryUnit();
        match(PUNC_COMMA);
        Expression interval = expression();
        match(PUNC_COMMA);
        Expression expr = expression();
        match(PUNC_RIGHT_PAREN);
        List<Expression> argument = new ArrayList<Expression>(2);
        argument.add(interval);
        argument.add(expr);
        Timestampdiff func = new Timestampdiff(unit, argument);
        func.setCacheEvalRst(cacheEvalRst);
        return func;
    }

    /**
     * first '(' has been consumed
     */
    private Timestampadd timestampadd() throws SQLSyntaxErrorException {
        IntervalPrimary.Unit unit = intervalPrimaryUnit();
        match(PUNC_COMMA);
        Expression interval = expression();
        match(PUNC_COMMA);
        Expression expr = expression();
        match(PUNC_RIGHT_PAREN);
        List<Expression> argument = new ArrayList<Expression>(2);
        argument.add(interval);
        argument.add(expr);
        Timestampadd func = new Timestampadd(unit, argument);
        func.setCacheEvalRst(cacheEvalRst);
        return func;
    }

    /**
     * first '(' has been consumed
     */
    private Extract extract() throws SQLSyntaxErrorException {
        IntervalPrimary.Unit unit = intervalPrimaryUnit();
        match(KW_FROM);
        Expression date = expression();
        match(PUNC_RIGHT_PAREN);
        Extract extract = new Extract(unit, date);
        extract.setCacheEvalRst(cacheEvalRst);
        return extract;
    }

    /**
     * first '(' has been consumed
     */
    private Convert functionConvert() throws SQLSyntaxErrorException {
        Expression expr = expression();
        match(KW_USING);
        String tempStr = lexer.stringValue();
        match(IDENTIFIER);
        match(PUNC_RIGHT_PAREN);
        Convert cvt = new Convert(expr, tempStr);
        cvt.setCacheEvalRst(cacheEvalRst);
        return cvt;
    }

    /**
     * first '(' has been consumed
     */
    private Char functionChar() throws SQLSyntaxErrorException {
        Char chr;
        for (List<Expression> tempExprList = new LinkedList<Expression>();;) {
            Expression tempExpr = expression();
            tempExprList.add(tempExpr);
            switch (lexer.token()) {
            case PUNC_COMMA:
                lexer.nextToken();
                continue;
            case PUNC_RIGHT_PAREN:
                lexer.nextToken();
                chr = new Char(tempExprList, null);
                chr.setCacheEvalRst(cacheEvalRst);
                return chr;
            case KW_USING:
                lexer.nextToken();
                String tempStr = lexer.stringValue();
                match(IDENTIFIER);
                match(PUNC_RIGHT_PAREN);
                chr = new Char(tempExprList, tempStr);
                chr.setCacheEvalRst(cacheEvalRst);
                return chr;
            default:
                throw err("expect ',' or 'USING' or ')' but is " + lexer.token());
            }
        }
    }

    /**
     * last token consumed is {@link MySQLToken#IDENTIFIER}, MUST NOT be
     * <code>null</code>
     */
    private Expression startedFromIdentifier(final String consumed, String consumedUp) throws SQLSyntaxErrorException {
        Expression tempExpr;
        Expression tempExpr2;
        List<Expression> tempExprList;
        String tempStr;
        StringBuilder tempSb;
        boolean tempGroupDistinct;
        switch (lexer.token()) {
        case PUNC_DOT:
            for (tempExpr = new Identifier(null, consumed, consumedUp).setCacheEvalRst(cacheEvalRst); lexer.token() == PUNC_DOT;) {
                switch (lexer.nextToken()) {
                case IDENTIFIER:
                    tempExpr = new Identifier((Identifier) tempExpr, lexer.stringValue(), lexer.stringValueUppercase()).setCacheEvalRst(cacheEvalRst);
                    lexer.nextToken();
                    break;
                case OP_ASTERISK:
                    lexer.nextToken();
                    return new Wildcard((Identifier) tempExpr).setCacheEvalRst(cacheEvalRst);
                default:
                    throw err("expect IDENTIFIER or '*' after '.', but is " + lexer.token());
                }
            }
            return tempExpr;
        case LITERAL_BIT:
            if (consumed.charAt(0) != '_') {
                return new Identifier(null, consumed, consumedUp).setCacheEvalRst(cacheEvalRst);
            }
            tempStr = lexer.stringValue();
            lexer.nextToken();
            return new LiteralBitField(consumed, tempStr).setCacheEvalRst(cacheEvalRst);
        case LITERAL_HEX:
            if (consumed.charAt(0) != '_') {
                return new Identifier(null, consumed, consumedUp).setCacheEvalRst(cacheEvalRst);
            }
            LiteralHexadecimal hex = new LiteralHexadecimal(
                    consumed,
                    lexer.getSQL(),
                    lexer.getOffsetCache(),
                    lexer.getSizeCache(),
                    charset);
            lexer.nextToken();
            return hex.setCacheEvalRst(cacheEvalRst);
        case LITERAL_CHARS:
            if (consumed.charAt(0) != '_') {
                return new Identifier(null, consumed, consumedUp).setCacheEvalRst(cacheEvalRst);
            }
            tempSb = new StringBuilder();
            do {
                lexer.appendStringContent(tempSb);
            } while (lexer.nextToken() == LITERAL_CHARS);
            return new LiteralString(consumed, tempSb.toString(), false).setCacheEvalRst(cacheEvalRst);
        case PUNC_LEFT_PAREN:
            consumedUp = Identifier.unescapeName(consumedUp);
            switch (functionManager.getParsingStrategy(consumedUp)) {
            case GET_FORMAT:
                // GET_FORMAT({DATE|TIME|DATETIME},
                // {'EUR'|'USA'|'JIS'|'ISO'|'INTERNAL'})
                lexer.nextToken();
                int gfi = matchIdentifier("DATE", "TIME", "DATETIME", "TIMESTAMP");
                match(PUNC_COMMA);
                Expression getFormatArg = expression();
                match(PUNC_RIGHT_PAREN);
                switch (gfi) {
                case 0:
                    return new GetFormat(GetFormat.FormatType.DATE, getFormatArg);
                case 1:
                    return new GetFormat(GetFormat.FormatType.TIME, getFormatArg);
                case 2:
                case 3:
                    return new GetFormat(GetFormat.FormatType.DATETIME, getFormatArg);
                }
                throw err("unexpected format type for GET_FORMAT()");
            case CAST:
                lexer.nextToken();
                tempExpr = expression();
                match(KW_AS);
                Pair<String, Pair<Expression, Expression>> type = type4specialFunc();
                match(PUNC_RIGHT_PAREN);
                Pair<Expression, Expression> info = type.getValue();
                if (info != null) {
                    return new Cast(tempExpr, type.getKey(), info.getKey(), info.getValue()).setCacheEvalRst(cacheEvalRst);
                } else {
                    return new Cast(tempExpr, type.getKey(), null, null).setCacheEvalRst(cacheEvalRst);
                }
            case POSITION:
                lexer.nextToken();
                tempExprList = new ArrayList<Expression>(2);
                tempExprList.add(expression());
                match(KW_IN);
                tempExprList.add(expression());
                match(PUNC_RIGHT_PAREN);
                return new Locate(tempExprList).setCacheEvalRst(cacheEvalRst);
            case SUBSTRING:
                lexer.nextToken();
                tempExprList = new ArrayList<Expression>(3);
                tempExprList.add(expression());
                match(PUNC_COMMA, KW_FROM);
                tempExprList.add(expression());
                switch (lexer.token()) {
                case PUNC_COMMA:
                case KW_FOR:
                    lexer.nextToken();
                    tempExprList.add(expression());
                default:
                    match(PUNC_RIGHT_PAREN);
                }
                return new Substring(tempExprList).setCacheEvalRst(cacheEvalRst);
            case ROW:
                lexer.nextToken();
                tempExprList = expressionList(new LinkedList<Expression>());
                return new RowExpression(tempExprList).setCacheEvalRst(cacheEvalRst);
            case TRIM:
                Direction direction;
                switch (lexer.nextToken()) {
                case KW_BOTH:
                    lexer.nextToken();
                    direction = Direction.BOTH;
                    break;
                case KW_LEADING:
                    lexer.nextToken();
                    direction = Direction.LEADING;
                    break;
                case KW_TRAILING:
                    lexer.nextToken();
                    direction = Direction.TRAILING;
                    break;
                default:
                    direction = Direction.DEFAULT;
                }
                if (direction == Direction.DEFAULT) {
                    tempExpr = expression();
                    if (lexer.token() == KW_FROM) {
                        lexer.nextToken();
                        tempExpr2 = expression();
                        match(PUNC_RIGHT_PAREN);
                        return new Trim(direction, tempExpr, tempExpr2).setCacheEvalRst(cacheEvalRst);
                    }
                    match(PUNC_RIGHT_PAREN);
                    return new Trim(direction, null, tempExpr).setCacheEvalRst(cacheEvalRst);
                }
                if (lexer.token() == KW_FROM) {
                    lexer.nextToken();
                    tempExpr = expression();
                    match(PUNC_RIGHT_PAREN);
                    return new Trim(direction, null, tempExpr).setCacheEvalRst(cacheEvalRst);
                }
                tempExpr = expression();
                match(KW_FROM);
                tempExpr2 = expression();
                match(PUNC_RIGHT_PAREN);
                return new Trim(direction, tempExpr, tempExpr2).setCacheEvalRst(cacheEvalRst);
            case AVG:
                if (lexer.nextToken() == KW_DISTINCT) {
                    tempGroupDistinct = true;
                    lexer.nextToken();
                } else {
                    tempGroupDistinct = false;
                }
                tempExpr = expression();
                match(PUNC_RIGHT_PAREN);
                return new Avg(tempExpr, tempGroupDistinct).setCacheEvalRst(cacheEvalRst);
            case MAX:
                if (lexer.nextToken() == KW_DISTINCT) {
                    tempGroupDistinct = true;
                    lexer.nextToken();
                } else {
                    tempGroupDistinct = false;
                }
                tempExpr = expression();
                match(PUNC_RIGHT_PAREN);
                return new Max(tempExpr, tempGroupDistinct).setCacheEvalRst(cacheEvalRst);
            case MIN:
                if (lexer.nextToken() == KW_DISTINCT) {
                    tempGroupDistinct = true;
                    lexer.nextToken();
                } else {
                    tempGroupDistinct = false;
                }
                tempExpr = expression();
                match(PUNC_RIGHT_PAREN);
                return new Min(tempExpr, tempGroupDistinct).setCacheEvalRst(cacheEvalRst);
            case SUM:
                if (lexer.nextToken() == KW_DISTINCT) {
                    tempGroupDistinct = true;
                    lexer.nextToken();
                } else {
                    tempGroupDistinct = false;
                }
                tempExpr = expression();
                match(PUNC_RIGHT_PAREN);
                return new Sum(tempExpr, tempGroupDistinct).setCacheEvalRst(cacheEvalRst);
            case COUNT:
                if (lexer.nextToken() == KW_DISTINCT) {
                    lexer.nextToken();
                    tempExprList = expressionList(new LinkedList<Expression>());
                    return new Count(tempExprList).setCacheEvalRst(cacheEvalRst);
                }
                tempExpr = expression();
                match(PUNC_RIGHT_PAREN);
                return new Count(tempExpr).setCacheEvalRst(cacheEvalRst);
            case GROUP_CONCAT:
                if (lexer.nextToken() == KW_DISTINCT) {
                    lexer.nextToken();
                    tempGroupDistinct = true;
                } else {
                    tempGroupDistinct = false;
                }
                for (tempExprList = new LinkedList<Expression>();;) {
                    tempExpr = expression();
                    tempExprList.add(tempExpr);
                    if (lexer.token() == PUNC_COMMA) {
                        lexer.nextToken();
                    } else {
                        break;
                    }
                }
                boolean isDesc = false;
                List<Expression> appendedColumnNames = null;
                tempExpr = null; // order by
                tempStr = null; // literalChars
                switch (lexer.token()) {
                case KW_ORDER:
                    lexer.nextToken();
                    match(KW_BY);
                    tempExpr = expression();
                    if (lexer.token() == KW_ASC) {
                        lexer.nextToken();
                    } else if (lexer.token() == KW_DESC) {
                        isDesc = true;
                        lexer.nextToken();
                    }
                    for (appendedColumnNames = new LinkedList<Expression>(); lexer.token() == PUNC_COMMA;) {
                        lexer.nextToken();
                        appendedColumnNames.add(expression());
                    }
                    if (lexer.token() != KW_SEPARATOR) {
                        break;
                    }
                case KW_SEPARATOR:
                    lexer.nextToken();
                    tempSb = new StringBuilder();
                    lexer.appendStringContent(tempSb);
                    tempStr = LiteralString.getUnescapedString(tempSb.toString());
                    match(LITERAL_CHARS);
                    break;
                }
                match(PUNC_RIGHT_PAREN);
                return new GroupConcat(tempGroupDistinct, tempExprList, tempExpr, isDesc, appendedColumnNames, tempStr).setCacheEvalRst(cacheEvalRst);
            case CHAR:
                lexer.nextToken();
                return functionChar();
            case CONVERT:
                lexer.nextToken();
                return functionConvert();
            case EXTRACT:
                lexer.nextToken();
                return extract();
            case TIMESTAMPDIFF:
                lexer.nextToken();
                return timestampdiff();
            case TIMESTAMPADD:
                lexer.nextToken();
                return timestampadd();
            case _ORDINARY:
                return ordinaryFunction(consumed, consumedUp);
            case _DEFAULT:
                return new Identifier(null, consumed, consumedUp).setCacheEvalRst(cacheEvalRst);
            default:
                throw err("unexpected function parsing strategy for id of " + consumed);
            }
        default:
            return new Identifier(null, consumed, consumedUp).setCacheEvalRst(cacheEvalRst);
        }
    }

    /**
     * @return never null
     */
    private Pair<String, Pair<Expression, Expression>> type4specialFunc() throws SQLSyntaxErrorException {
        Expression exp1 = null;
        Expression exp2 = null;
        // DATE
        // DATETIME
        // SIGNED [INTEGER]
        // TIME
        String typeName;
        switch (lexer.token()) {
        case KW_BINARY:
        case KW_CHAR:
            typeName = MySQLToken.keyWordToString(lexer.token());
            if (lexer.nextToken() == PUNC_LEFT_PAREN) {
                lexer.nextToken();
                exp1 = expression();
                match(PUNC_RIGHT_PAREN);
            }
            return constructTypePair(typeName, exp1, exp2);
        case KW_DECIMAL:
            typeName = MySQLToken.keyWordToString(lexer.token());
            if (lexer.nextToken() == PUNC_LEFT_PAREN) {
                lexer.nextToken();
                exp1 = expression();
                if (lexer.token() == PUNC_COMMA) {
                    lexer.nextToken();
                    exp2 = expression();
                }
                match(PUNC_RIGHT_PAREN);
            }
            return constructTypePair(typeName, exp1, exp2);
        case KW_UNSIGNED:
            typeName = MySQLToken.keyWordToString(lexer.token());
            if (lexer.nextToken() == KW_INTEGER) {
                lexer.nextToken();
            }
            return constructTypePair(typeName, null, null);
        case IDENTIFIER:
            typeName = lexer.stringValueUppercase();
            lexer.nextToken();
            if ("SIGNED".equals(typeName)) {
                if (lexer.token() == KW_INTEGER) {
                    lexer.nextToken();
                }
            } else if (!"DATE".equals(typeName) && !"DATETIME".equals(typeName) && !"TIME".equals(typeName)) {
                throw err("invalide type name: " + typeName);
            }
            return constructTypePair(typeName, null, null);
        default:
            throw err("invalide type name: " + lexer.stringValueUppercase());
        }
    }

    private static Pair<String, Pair<Expression, Expression>> constructTypePair(String typeName, Expression exp1,
                                                                                Expression exp2) {
        return new Pair<String, Pair<Expression, Expression>>(typeName, new Pair<Expression, Expression>(exp1, exp2));
    }

    /**
     * id has been consumed. id must be a function name. current token must be
     * {@link MySQLToken#PUNC_LEFT_PAREN}
     * 
     * @param idUpper must be name of a function
     * @return never null
     */
    private FunctionExpression ordinaryFunction(String id, String idUpper) throws SQLSyntaxErrorException {
        idUpper = Identifier.unescapeName(idUpper);
        match(PUNC_LEFT_PAREN);
        FunctionExpression funcExpr;
        if (lexer.token() == PUNC_RIGHT_PAREN) {
            lexer.nextToken();
            funcExpr = functionManager.createFunctionExpression(idUpper, null);
        } else {
            List<Expression> args = expressionList(new LinkedList<Expression>());
            funcExpr = functionManager.createFunctionExpression(idUpper, args);
        }
        if (funcExpr == null) {
            throw new SQLSyntaxErrorException(id + "() is not a function");
        }
        funcExpr.setCacheEvalRst(cacheEvalRst);
        return funcExpr;
    }

    /**
     * first <code>MATCH</code> has been consumed
     */
    private Expression matchExpression() throws SQLSyntaxErrorException {
        match(PUNC_LEFT_PAREN);
        List<Expression> colList = expressionList(new LinkedList<Expression>());
        matchIdentifier("AGAINST");
        match(PUNC_LEFT_PAREN);
        Expression pattern = expression();
        Modifier modifier = Modifier._DEFAULT;
        switch (lexer.token()) {
        case KW_WITH:
            lexer.nextToken();
            match(IDENTIFIER);
            match(IDENTIFIER);
            modifier = Modifier.WITH_QUERY_EXPANSION;
            break;
        case KW_IN:
            switch (lexer.nextToken()) {
            case KW_NATURAL:
                lexer.nextToken();
                matchIdentifier("LANGUAGE");
                matchIdentifier("MODE");
                if (lexer.token() == KW_WITH) {
                    lexer.nextToken();
                    lexer.nextToken();
                    lexer.nextToken();
                    modifier = Modifier.IN_NATURAL_LANGUAGE_MODE_WITH_QUERY_EXPANSION;
                } else {
                    modifier = Modifier.IN_NATURAL_LANGUAGE_MODE;
                }
                break;
            default:
                matchIdentifier("BOOLEAN");
                matchIdentifier("MODE");
                modifier = Modifier.IN_BOOLEAN_MODE;
                break;
            }
        }
        match(PUNC_RIGHT_PAREN);
        return new MatchExpression(colList, pattern, modifier).setCacheEvalRst(cacheEvalRst);
    }

    /**
     * first <code>INTERVAL</code> has been consumed
     */
    private Expression intervalExpression() throws SQLSyntaxErrorException {
        Expression fstExpr;
        List<Expression> argList = null;
        if (lexer.token() == PUNC_LEFT_PAREN) {
            if (lexer.nextToken() == KW_SELECT) {
                fstExpr = subQuery();
                match(PUNC_RIGHT_PAREN);
            } else {
                fstExpr = expression();
                if (lexer.token() == PUNC_COMMA) {
                    lexer.nextToken();
                    argList = new LinkedList<Expression>();
                    argList.add(fstExpr);
                    argList = expressionList(argList);
                } else {
                    match(PUNC_RIGHT_PAREN);
                }
            }
        } else {
            fstExpr = expression();
        }
        if (argList != null) {
            return new Interval(argList).setCacheEvalRst(cacheEvalRst);
        }

        return new IntervalPrimary(fstExpr, intervalPrimaryUnit()).setCacheEvalRst(cacheEvalRst);
    }

    private IntervalPrimary.Unit intervalPrimaryUnit() throws SQLSyntaxErrorException {
        switch (lexer.token()) {
        case KW_SECOND_MICROSECOND:
            lexer.nextToken();
            return IntervalPrimary.Unit.SECOND_MICROSECOND;
        case KW_MINUTE_MICROSECOND:
            lexer.nextToken();
            return IntervalPrimary.Unit.MINUTE_MICROSECOND;
        case KW_MINUTE_SECOND:
            lexer.nextToken();
            return IntervalPrimary.Unit.MINUTE_SECOND;
        case KW_HOUR_MICROSECOND:
            lexer.nextToken();
            return IntervalPrimary.Unit.HOUR_MICROSECOND;
        case KW_HOUR_SECOND:
            lexer.nextToken();
            return IntervalPrimary.Unit.HOUR_SECOND;
        case KW_HOUR_MINUTE:
            lexer.nextToken();
            return IntervalPrimary.Unit.HOUR_MINUTE;
        case KW_DAY_MICROSECOND:
            lexer.nextToken();
            return IntervalPrimary.Unit.DAY_MICROSECOND;
        case KW_DAY_SECOND:
            lexer.nextToken();
            return IntervalPrimary.Unit.DAY_SECOND;
        case KW_DAY_MINUTE:
            lexer.nextToken();
            return IntervalPrimary.Unit.DAY_MINUTE;
        case KW_DAY_HOUR:
            lexer.nextToken();
            return IntervalPrimary.Unit.DAY_HOUR;
        case KW_YEAR_MONTH:
            lexer.nextToken();
            return IntervalPrimary.Unit.YEAR_MONTH;
        case IDENTIFIER:
            String unitText = lexer.stringValueUppercase();
            IntervalPrimary.Unit unit = IntervalPrimary.getIntervalUnit(unitText);
            if (unit != null) {
                lexer.nextToken();
                return unit;
            }
        default:
            throw err("literal INTERVAL should end with an UNIT");
        }
    }

    /**
     * first <code>CASE</code> has been consumed
     */
    private Expression caseWhenExpression() throws SQLSyntaxErrorException {
        Expression comparee = null;
        if (lexer.token() != KW_WHEN) {
            comparee = expression();
        }
        List<Pair<Expression, Expression>> list = new LinkedList<Pair<Expression, Expression>>();
        for (; lexer.token() == KW_WHEN;) {
            lexer.nextToken();
            Expression when = expression();
            match(KW_THEN);
            Expression then = expression();
            if (when == null || then == null)
                throw err("when or then is null in CASE WHEN expression");
            list.add(new Pair<Expression, Expression>(when, then));
        }
        if (list.isEmpty()) {
            throw err("at least one WHEN ... THEN branch for CASE ... WHEN syntax");
        }
        Expression elseValue = null;
        switch (lexer.token()) {
        case KW_ELSE:
            lexer.nextToken();
            elseValue = expression();
        default:
            matchIdentifier("END");
        }
        return new CaseWhenOperatorExpression(comparee, list, elseValue).setCacheEvalRst(cacheEvalRst);
    }

    /**
     * first <code>'('</code> has been consumed. At least one element. Consume
     * last ')' after invocation <br/>
     * <code>'(' expr (',' expr)* ')'</code>
     */
    private List<Expression> expressionList(List<Expression> exprList) throws SQLSyntaxErrorException {
        for (;;) {
            Expression expr = expression();
            exprList.add(expr);
            switch (lexer.token()) {
            case PUNC_COMMA:
                lexer.nextToken();
                break;
            case PUNC_RIGHT_PAREN:
                lexer.nextToken();
                return exprList;
            default:
                throw err("unexpected token: " + lexer.token());
            }
        }
    }

    /**
     * first token is {@link MySQLToken#KW_SELECT}
     */
    private QueryExpression subQuery() throws SQLSyntaxErrorException {
        if (selectParser == null) {
            selectParser = new MySQLDMLSelectParser(lexer, this);
        }
        return selectParser.select();
    }

}
