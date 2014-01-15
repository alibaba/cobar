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
 * (created at 2011-5-3)
 */
package com.alibaba.cobar.parser.recognizer.mysql.syntax;

import junit.framework.Assert;

import com.alibaba.cobar.parser.ast.expression.BinaryOperatorExpression;
import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.PolyadicOperatorExpression;
import com.alibaba.cobar.parser.ast.expression.TernaryOperatorExpression;
import com.alibaba.cobar.parser.ast.expression.UnaryOperatorExpression;
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
import com.alibaba.cobar.parser.ast.expression.misc.UserExpression;
import com.alibaba.cobar.parser.ast.expression.primary.CaseWhenOperatorExpression;
import com.alibaba.cobar.parser.ast.expression.primary.Identifier;
import com.alibaba.cobar.parser.ast.expression.primary.ParamMarker;
import com.alibaba.cobar.parser.ast.expression.primary.RowExpression;
import com.alibaba.cobar.parser.ast.expression.primary.SysVarPrimary;
import com.alibaba.cobar.parser.ast.expression.primary.UsrDefVarPrimary;
import com.alibaba.cobar.parser.ast.expression.primary.Wildcard;
import com.alibaba.cobar.parser.ast.expression.primary.literal.LiteralBitField;
import com.alibaba.cobar.parser.ast.expression.primary.literal.LiteralHexadecimal;
import com.alibaba.cobar.parser.ast.expression.primary.literal.LiteralNull;
import com.alibaba.cobar.parser.ast.expression.primary.literal.LiteralNumber;
import com.alibaba.cobar.parser.ast.expression.primary.literal.LiteralString;
import com.alibaba.cobar.parser.ast.expression.string.LikeExpression;
import com.alibaba.cobar.parser.ast.expression.string.RegexpExpression;
import com.alibaba.cobar.parser.ast.expression.string.SoundsLikeExpression;
import com.alibaba.cobar.parser.ast.expression.type.CastBinaryExpression;
import com.alibaba.cobar.parser.ast.expression.type.CollateExpression;
import com.alibaba.cobar.parser.ast.fragment.VariableScope;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLSelectStatement;
import com.alibaba.cobar.parser.recognizer.mysql.lexer.MySQLLexer;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class MySQLExprParserTest extends AbstractSyntaxTest {

    public void testExpr1() throws Exception {
        String sql = "\"abc\" /* */  '\\'s' + id2/ id3, 123-456*(ii moD d)";
        MySQLLexer lexer = new MySQLLexer(sql);
        MySQLExprParser parser = new MySQLExprParser(lexer);
        Expression expr = parser.expression();
        String output = output2MySQL(expr, sql);
        Assert.assertEquals("'abc\\'s' + id2 / id3", output);
        Assert.assertEquals(ArithmeticAddExpression.class, expr.getClass());
        BinaryOperatorExpression bex = (BinaryOperatorExpression) ((ArithmeticAddExpression) expr).getRightOprand();
        Assert.assertEquals(ArithmeticDivideExpression.class, bex.getClass());
        Assert.assertEquals(Identifier.class, bex.getRightOprand().getClass());
        lexer.nextToken();
        parser = new MySQLExprParser(lexer);
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("123 - 456 * (ii % d)", output);
        Assert.assertEquals(ArithmeticSubtractExpression.class, expr.getClass());

        sql = "(n'\"abc\"' \"abc\" /* */  '\\'s' + 1.123e1/ id3)*(.1e3-a||b)mod x'abc'&&(select 0b1001^b'0000')";
        lexer = new MySQLLexer(sql);
        parser = new MySQLExprParser(lexer);
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals(
                "(N'\"abc\"abc\\'s' + 11.23 / id3) * (1E+2 - a OR b) % x'abc' AND (SELECT b'1001' ^ b'0000')",
                output);
        Assert.assertEquals(LogicalAndExpression.class, expr.getClass());
        bex = (BinaryOperatorExpression) ((LogicalAndExpression) expr).getOperand(0);
        Assert.assertEquals(ArithmeticModExpression.class, bex.getClass());
        bex = (BinaryOperatorExpression) ((ArithmeticModExpression) bex).getLeftOprand();
        Assert.assertEquals(ArithmeticMultiplyExpression.class, bex.getClass());
        bex = (BinaryOperatorExpression) ((ArithmeticMultiplyExpression) bex).getLeftOprand();
        Assert.assertEquals(ArithmeticAddExpression.class, bex.getClass());
        Assert.assertEquals(LiteralString.class, ((ArithmeticAddExpression) bex).getLeftOprand().getClass());
        bex = (BinaryOperatorExpression) ((ArithmeticAddExpression) bex).getRightOprand();
        Assert.assertEquals(ArithmeticDivideExpression.class, bex.getClass());
        Assert.assertEquals(DMLSelectStatement.class, ((LogicalAndExpression) expr).getOperand(1).getClass());

        sql = "not! ~`select` in (1,current_date,`current_date`)like `all` div a between (c&&d) and (d|e)";
        lexer = new MySQLLexer(sql);
        parser = new MySQLExprParser(lexer);
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals(
                "NOT ! ~ `select` IN (1, CURDATE(), `current_date`) LIKE `all` DIV a BETWEEN (c AND d) AND d | e",
                output);
        Assert.assertEquals(LogicalNotExpression.class, expr.getClass());
        TernaryOperatorExpression tex = (TernaryOperatorExpression) ((LogicalNotExpression) expr).getOperand();
        Assert.assertEquals(BetweenAndExpression.class, tex.getClass());
        Assert.assertEquals(LikeExpression.class, tex.getFirst().getClass());
        Assert.assertEquals(LogicalAndExpression.class, tex.getSecond().getClass());
        Assert.assertEquals(BitOrExpression.class, tex.getThird().getClass());
        tex = (TernaryOperatorExpression) ((BetweenAndExpression) tex).getFirst();
        Assert.assertEquals(InExpression.class, tex.getFirst().getClass());
        Assert.assertEquals(ArithmeticIntegerDivideExpression.class, tex.getSecond().getClass());
        bex = (BinaryOperatorExpression) (InExpression) tex.getFirst();
        Assert.assertEquals(NegativeValueExpression.class, bex.getLeftOprand().getClass());
        Assert.assertEquals(InExpressionList.class, bex.getRightOprand().getClass());
        UnaryOperatorExpression uex = (UnaryOperatorExpression) ((NegativeValueExpression) bex.getLeftOprand());
        Assert.assertEquals(BitInvertExpression.class, uex.getOperand().getClass());

        sql = " binary case ~a||b&&c^d xor e when 2>any(select a ) then 3 else 4 end is not null =a";
        lexer = new MySQLLexer(sql);
        parser = new MySQLExprParser(lexer);
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals(
                "BINARY CASE ~ a OR b AND c ^ d XOR e WHEN 2 > ANY (SELECT a) THEN 3 ELSE 4 END IS NOT NULL = a",
                output);
        Assert.assertEquals(ComparisionEqualsExpression.class, expr.getClass());
        bex = (BinaryOperatorExpression) ((ComparisionEqualsExpression) expr);
        Assert.assertEquals(ComparisionIsExpression.class, bex.getLeftOprand().getClass());
        Assert.assertEquals(Identifier.class, bex.getRightOprand().getClass());
        ComparisionIsExpression cex = (ComparisionIsExpression) bex.getLeftOprand();
        Assert.assertEquals(CastBinaryExpression.class, cex.getOperand().getClass());
        uex = (UnaryOperatorExpression) cex.getOperand();
        Assert.assertEquals(CaseWhenOperatorExpression.class, uex.getOperand().getClass());
        CaseWhenOperatorExpression cwex = (CaseWhenOperatorExpression) uex.getOperand();
        Assert.assertEquals(LogicalOrExpression.class, cwex.getComparee().getClass());
        PolyadicOperatorExpression pex = (LogicalOrExpression) cwex.getComparee();
        Assert.assertEquals(BitInvertExpression.class, pex.getOperand(0).getClass());
        Assert.assertEquals(LogicalXORExpression.class, pex.getOperand(1).getClass());
        bex = (LogicalXORExpression) pex.getOperand(1);
        Assert.assertEquals(LogicalAndExpression.class, bex.getLeftOprand().getClass());
        Assert.assertEquals(Identifier.class, bex.getRightOprand().getClass());
        pex = (LogicalAndExpression) bex.getLeftOprand();
        Assert.assertEquals(Identifier.class, pex.getOperand(0).getClass());
        Assert.assertEquals(BitXORExpression.class, pex.getOperand(1).getClass());

        sql = " !interval(a,b)<=>a>>b collate x /?+a!=@@1 or @var sounds like -(a-b) mod -(d or e)";
        lexer = new MySQLLexer(sql);
        parser = new MySQLExprParser(lexer);
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals(
                "! INTERVAL(a, b) <=> a >> b COLLATE x / ? + a != @@1 OR @var SOUNDS LIKE - (a - b) % - (d OR e)",
                output);
        Assert.assertEquals(LogicalOrExpression.class, expr.getClass());
        pex = (LogicalOrExpression) expr;
        Assert.assertEquals(ComparisionNotEqualsExpression.class, pex.getOperand(0).getClass());
        Assert.assertEquals(SoundsLikeExpression.class, pex.getOperand(1).getClass());
        bex = (BinaryOperatorExpression) pex.getOperand(0);
        Assert.assertEquals(ComparisionNullSafeEqualsExpression.class, bex.getLeftOprand().getClass());
        Assert.assertEquals(SysVarPrimary.class, bex.getRightOprand().getClass());
        bex = (BinaryOperatorExpression) bex.getLeftOprand();
        Assert.assertEquals(NegativeValueExpression.class, bex.getLeftOprand().getClass());
        Assert.assertEquals(BitShiftExpression.class, bex.getRightOprand().getClass());
        bex = (BinaryOperatorExpression) bex.getRightOprand();
        Assert.assertEquals(Identifier.class, bex.getLeftOprand().getClass());
        Assert.assertEquals(ArithmeticAddExpression.class, bex.getRightOprand().getClass());
        bex = (BinaryOperatorExpression) bex.getRightOprand();
        Assert.assertEquals(ArithmeticDivideExpression.class, bex.getLeftOprand().getClass());
        Assert.assertEquals(Identifier.class, bex.getRightOprand().getClass());
        bex = (BinaryOperatorExpression) bex.getLeftOprand();
        Assert.assertEquals(CollateExpression.class, bex.getLeftOprand().getClass());
        Assert.assertEquals(ParamMarker.class, bex.getRightOprand().getClass());
        bex = (BinaryOperatorExpression) ((LogicalOrExpression) expr).getOperand(1);
        Assert.assertEquals(UsrDefVarPrimary.class, bex.getLeftOprand().getClass());
        Assert.assertEquals(ArithmeticModExpression.class, bex.getRightOprand().getClass());
        bex = (BinaryOperatorExpression) bex.getRightOprand();
        Assert.assertEquals(MinusExpression.class, bex.getLeftOprand().getClass());
        Assert.assertEquals(MinusExpression.class, bex.getRightOprand().getClass());
        uex = (UnaryOperatorExpression) bex.getLeftOprand();
        Assert.assertEquals(ArithmeticSubtractExpression.class, uex.getOperand().getClass());
        uex = (UnaryOperatorExpression) bex.getRightOprand();
        Assert.assertEquals(LogicalOrExpression.class, uex.getOperand().getClass());
    }

    public void testAssignment() throws Exception {
        String sql = "a /*dd*/:=b:=c";
        MySQLExprParser parser = new MySQLExprParser(new MySQLLexer(sql));
        Expression expr = parser.expression();
        String output = output2MySQL(expr, sql);
        Assert.assertEquals("a := b := c", output);
        Assert.assertEquals(AssignmentExpression.class, expr.getClass());
        AssignmentExpression ass = (AssignmentExpression) expr;
        Assert.assertEquals(AssignmentExpression.class, ass.getRightOprand().getClass());
        ass = (AssignmentExpression) ass.getRightOprand();
        Assert.assertEquals("b", ((Identifier) ass.getLeftOprand()).getIdText());

        sql = "c=@var:=1";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("c = (@var := 1)", output);
        Assert.assertEquals(ComparisionEqualsExpression.class, expr.getClass());
        ass = (AssignmentExpression) ((BinaryOperatorExpression) expr).getRightOprand();
        UsrDefVarPrimary usr = (UsrDefVarPrimary) ass.getLeftOprand();
        Assert.assertEquals("@var", usr.getVarText());

        sql = "a:=b or c &&d :=0b1101 or b'01'&0xabc";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("a := b OR c AND d := b'1101' OR b'01' & x'abc'", output);
        Assert.assertEquals(AssignmentExpression.class, expr.getClass());
        ass = (AssignmentExpression) expr;
        Assert.assertEquals(AssignmentExpression.class, ass.getRightOprand().getClass());
        ass = (AssignmentExpression) ass.getRightOprand();
        Assert.assertEquals(LogicalOrExpression.class, ass.getLeftOprand().getClass());
        Assert.assertEquals(LogicalOrExpression.class, ass.getRightOprand().getClass());
        LogicalOrExpression lor = (LogicalOrExpression) ass.getLeftOprand();
        Assert.assertEquals(LogicalAndExpression.class, lor.getOperand(1).getClass());
        lor = (LogicalOrExpression) ass.getRightOprand();
        Assert.assertEquals(LiteralBitField.class, lor.getOperand(0).getClass());
        Assert.assertEquals(BitAndExpression.class, lor.getOperand(1).getClass());

        sql = "a:=((b or (c &&d)) :=((0b1101 or (b'01'&0xabc))))";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("a := b OR c AND d := b'1101' OR b'01' & x'abc'", output);
        Assert.assertEquals(AssignmentExpression.class, expr.getClass());
        ass = (AssignmentExpression) expr;
        Assert.assertEquals(AssignmentExpression.class, ass.getRightOprand().getClass());
        ass = (AssignmentExpression) ass.getRightOprand();
        Assert.assertEquals(LogicalOrExpression.class, ass.getLeftOprand().getClass());
        Assert.assertEquals(LogicalOrExpression.class, ass.getRightOprand().getClass());
        lor = (LogicalOrExpression) ass.getLeftOprand();
        Assert.assertEquals(LogicalAndExpression.class, lor.getOperand(1).getClass());
        lor = (LogicalOrExpression) ass.getRightOprand();
        Assert.assertEquals(LiteralBitField.class, lor.getOperand(0).getClass());
        Assert.assertEquals(BitAndExpression.class, lor.getOperand(1).getClass());

        sql = "(a:=b) or c &&(d :=0b1101 or b'01')&0xabc ^null";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("(a := b) OR c AND (d := b'1101' OR b'01') & x'abc' ^ NULL", output);
        Assert.assertEquals(LogicalOrExpression.class, expr.getClass());
        lor = (LogicalOrExpression) expr;
        Assert.assertEquals(AssignmentExpression.class, lor.getOperand(0).getClass());
        Assert.assertEquals(LogicalAndExpression.class, lor.getOperand(1).getClass());
        LogicalAndExpression land = (LogicalAndExpression) lor.getOperand(1);
        Assert.assertEquals(Identifier.class, land.getOperand(0).getClass());
        Assert.assertEquals(BitAndExpression.class, land.getOperand(1).getClass());
        BitAndExpression band = (BitAndExpression) land.getOperand(1);
        Assert.assertEquals(AssignmentExpression.class, band.getLeftOprand().getClass());
        Assert.assertEquals(BitXORExpression.class, band.getRightOprand().getClass());
        ass = (AssignmentExpression) band.getLeftOprand();
        Assert.assertEquals(LogicalOrExpression.class, ass.getRightOprand().getClass());
        BitXORExpression bxor = (BitXORExpression) band.getRightOprand();
        Assert.assertEquals(LiteralHexadecimal.class, bxor.getLeftOprand().getClass());
        Assert.assertEquals(LiteralNull.class, bxor.getRightOprand().getClass());

    }

    public void testLogical() throws Exception {
        String sql = "a || b Or c";
        MySQLExprParser parser = new MySQLExprParser(new MySQLLexer(sql));
        Expression expr = parser.expression();
        String output = output2MySQL(expr, sql);
        Assert.assertEquals("a OR b OR c", output);
        Assert.assertEquals(LogicalOrExpression.class, expr.getClass());
        LogicalOrExpression or = (LogicalOrExpression) expr;
        Assert.assertEquals(3, or.getArity());
        Assert.assertEquals("b", ((Identifier) or.getOperand(1)).getIdText());

        sql = "a XOR b xOr c";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("a XOR b XOR c", output);
        Assert.assertEquals(LogicalXORExpression.class, expr.getClass());
        LogicalXORExpression xor = (LogicalXORExpression) expr;
        Assert.assertEquals(LogicalXORExpression.class, xor.getLeftOprand().getClass());
        xor = (LogicalXORExpression) xor.getLeftOprand();
        Assert.assertEquals("b", ((Identifier) xor.getRightOprand()).getIdText());

        sql = "a XOR( b xOr c)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("a XOR (b XOR c)", output);
        xor = (LogicalXORExpression) expr;
        LogicalXORExpression xor2 = (LogicalXORExpression) xor.getRightOprand();
        Assert.assertEquals("a", ((Identifier) xor.getLeftOprand()).getIdText());
        Assert.assertEquals("b", ((Identifier) xor2.getLeftOprand()).getIdText());
        Assert.assertEquals("c", ((Identifier) xor2.getRightOprand()).getIdText());

        sql = "a and     b && c";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("a AND b AND c", output);
        Assert.assertEquals(LogicalAndExpression.class, expr.getClass());
        LogicalAndExpression and = (LogicalAndExpression) expr;
        Assert.assertEquals(3, or.getArity());
        Assert.assertEquals("b", ((Identifier) and.getOperand(1)).getIdText());

        sql = "not NOT Not a";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("NOT NOT NOT a", output);
        Assert.assertEquals(LogicalNotExpression.class, expr.getClass());
        LogicalNotExpression not = (LogicalNotExpression) ((LogicalNotExpression) expr).getOperand();
        Assert.assertEquals(LogicalNotExpression.class, not.getClass());
        not = (LogicalNotExpression) not.getOperand();
        Assert.assertEquals(LogicalNotExpression.class, not.getClass());
        Assert.assertEquals("a", ((Identifier) not.getOperand()).getIdText());
    }

    public void testComparision() throws Exception {
        String sql = "a  betwEen b and c Not between d and e";
        MySQLExprParser parser = new MySQLExprParser(new MySQLLexer(sql));
        Expression expr = parser.expression();
        String output = output2MySQL(expr, sql);
        Assert.assertEquals("a BETWEEN b AND c NOT BETWEEN d AND e", output);
        BetweenAndExpression ba = (BetweenAndExpression) expr;
        Assert.assertEquals("a", ((Identifier) ba.getFirst()).getIdText());
        Assert.assertEquals("b", ((Identifier) ba.getSecond()).getIdText());
        Assert.assertEquals(false, ba.isNot());
        ba = (BetweenAndExpression) ba.getThird();
        Assert.assertEquals("c", ((Identifier) ba.getFirst()).getIdText());
        Assert.assertEquals("d", ((Identifier) ba.getSecond()).getIdText());
        Assert.assertEquals("e", ((Identifier) ba.getThird()).getIdText());
        Assert.assertEquals(true, ba.isNot());

        sql = "a between b between c and d and e between f and g";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("a BETWEEN b BETWEEN c AND d AND e BETWEEN f AND g", output);
        ba = (BetweenAndExpression) expr;
        BetweenAndExpression ba2 = (BetweenAndExpression) ba.getSecond();
        BetweenAndExpression ba3 = (BetweenAndExpression) ba.getThird();
        Assert.assertEquals("a", ((Identifier) ba.getFirst()).getIdText());
        Assert.assertEquals("b", ((Identifier) ba2.getFirst()).getIdText());
        Assert.assertEquals("c", ((Identifier) ba2.getSecond()).getIdText());
        Assert.assertEquals("d", ((Identifier) ba2.getThird()).getIdText());
        Assert.assertEquals("e", ((Identifier) ba3.getFirst()).getIdText());
        Assert.assertEquals("f", ((Identifier) ba3.getSecond()).getIdText());
        Assert.assertEquals("g", ((Identifier) ba3.getThird()).getIdText());

        sql = "((select a)) between (select b)   and (select d) ";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("(SELECT a) BETWEEN (SELECT b) AND (SELECT d)", output);

        sql = "a  rliKe b not REGEXP c";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("a REGEXP b NOT REGEXP c", output);
        RegexpExpression re = (RegexpExpression) expr;
        RegexpExpression re2 = (RegexpExpression) re.getLeftOprand();
        Assert.assertEquals("a", ((Identifier) re2.getLeftOprand()).getIdText());
        Assert.assertEquals("b", ((Identifier) re2.getRightOprand()).getIdText());
        Assert.assertEquals("c", ((Identifier) re.getRightOprand()).getIdText());
        Assert.assertEquals(true, re.isNot());
        Assert.assertEquals(false, re2.isNot());

        sql = "((a)) like (((b)))escape (((d)))";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("a LIKE b ESCAPE d", output);

        sql = "((select a)) like (((select b)))escape (((select d)))";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("(SELECT a) LIKE (SELECT b) ESCAPE (SELECT d)", output);

        sql = "a  like b NOT LIKE c escape d";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("a LIKE b NOT LIKE c ESCAPE d", output);
        LikeExpression le = (LikeExpression) expr;
        LikeExpression le2 = (LikeExpression) le.getFirst();
        Assert.assertEquals("a", ((Identifier) le2.getFirst()).getIdText());
        Assert.assertEquals("b", ((Identifier) le2.getSecond()).getIdText());
        Assert.assertEquals("c", ((Identifier) le.getSecond()).getIdText());
        Assert.assertEquals("d", ((Identifier) le.getThird()).getIdText());
        Assert.assertEquals(true, le.isNot());
        Assert.assertEquals(false, le2.isNot());

        sql = "b NOT LIKE c ";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("b NOT LIKE c", output);

        sql = "a in (b) not in (select id from t1)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("a IN (b) NOT IN (SELECT id FROM t1)", output);
        InExpression in = (InExpression) expr;
        InExpression in2 = (InExpression) in.getLeftOprand();
        Assert.assertEquals("a", ((Identifier) in2.getLeftOprand()).getIdText());
        Assert.assertTrue(QueryExpression.class.isAssignableFrom(in.getRightOprand().getClass()));
        Assert.assertEquals(true, in.isNot());
        Assert.assertEquals(false, in2.isNot());

        sql = "(select a)is not null";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("(SELECT a) IS NOT NULL", output);

        sql = "a is not null is not false is not true is not UNKNOWn is null is false is true is unknown";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals(
                "a IS NOT NULL IS NOT FALSE IS NOT TRUE IS NOT UNKNOWN IS NULL IS FALSE IS TRUE IS UNKNOWN",
                output);
        ComparisionIsExpression is = (ComparisionIsExpression) expr;
        ComparisionIsExpression is2 = (ComparisionIsExpression) is.getOperand();
        ComparisionIsExpression is3 = (ComparisionIsExpression) is2.getOperand();
        ComparisionIsExpression is4 = (ComparisionIsExpression) is3.getOperand();
        ComparisionIsExpression is5 = (ComparisionIsExpression) is4.getOperand();
        ComparisionIsExpression is6 = (ComparisionIsExpression) is5.getOperand();
        ComparisionIsExpression is7 = (ComparisionIsExpression) is6.getOperand();
        ComparisionIsExpression is8 = (ComparisionIsExpression) is7.getOperand();
        Assert.assertEquals(ComparisionIsExpression.IS_UNKNOWN, is.getMode());
        Assert.assertEquals(ComparisionIsExpression.IS_TRUE, is2.getMode());
        Assert.assertEquals(ComparisionIsExpression.IS_FALSE, is3.getMode());
        Assert.assertEquals(ComparisionIsExpression.IS_NULL, is4.getMode());
        Assert.assertEquals(ComparisionIsExpression.IS_NOT_UNKNOWN, is5.getMode());
        Assert.assertEquals(ComparisionIsExpression.IS_NOT_TRUE, is6.getMode());
        Assert.assertEquals(ComparisionIsExpression.IS_NOT_FALSE, is7.getMode());
        Assert.assertEquals(ComparisionIsExpression.IS_NOT_NULL, is8.getMode());
        Assert.assertEquals("a", ((Identifier) is8.getOperand()).getIdText());

        sql = "a = b <=> c >= d > e <= f < g <> h != i";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("a = b <=> c >= d > e <= f < g <> h != i", output);
        ComparisionNotEqualsExpression neq = (ComparisionNotEqualsExpression) expr;
        ComparisionLessOrGreaterThanExpression lg = (ComparisionLessOrGreaterThanExpression) neq.getLeftOprand();
        ComparisionLessThanExpression l = (ComparisionLessThanExpression) lg.getLeftOprand();
        ComparisionLessThanOrEqualsExpression leq = (ComparisionLessThanOrEqualsExpression) l.getLeftOprand();
        ComparisionGreaterThanExpression g = (ComparisionGreaterThanExpression) leq.getLeftOprand();
        ComparisionGreaterThanOrEqualsExpression geq = (ComparisionGreaterThanOrEqualsExpression) g.getLeftOprand();
        ComparisionNullSafeEqualsExpression nseq = (ComparisionNullSafeEqualsExpression) geq.getLeftOprand();
        ComparisionEqualsExpression eq = (ComparisionEqualsExpression) nseq.getLeftOprand();
        Assert.assertEquals("i", ((Identifier) neq.getRightOprand()).getIdText());
        Assert.assertEquals("h", ((Identifier) lg.getRightOprand()).getIdText());
        Assert.assertEquals("g", ((Identifier) l.getRightOprand()).getIdText());
        Assert.assertEquals("f", ((Identifier) leq.getRightOprand()).getIdText());
        Assert.assertEquals("e", ((Identifier) g.getRightOprand()).getIdText());
        Assert.assertEquals("d", ((Identifier) geq.getRightOprand()).getIdText());
        Assert.assertEquals("c", ((Identifier) nseq.getRightOprand()).getIdText());
        Assert.assertEquals("b", ((Identifier) eq.getRightOprand()).getIdText());
        Assert.assertEquals("a", ((Identifier) eq.getLeftOprand()).getIdText());

        sql = "a sounds like b sounds like c";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("a SOUNDS LIKE b SOUNDS LIKE c", output);
        SoundsLikeExpression sl = (SoundsLikeExpression) expr;
        SoundsLikeExpression sl2 = (SoundsLikeExpression) sl.getLeftOprand();
        Assert.assertEquals("a", ((Identifier) sl2.getLeftOprand()).getIdText());
        Assert.assertEquals("b", ((Identifier) sl2.getRightOprand()).getIdText());
        Assert.assertEquals("c", ((Identifier) sl.getRightOprand()).getIdText());

        sql = "a like b escape c";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("a LIKE b ESCAPE c", output);

        sql = "(select a) collate z";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("(SELECT a) COLLATE z", output);

        sql = "val1 IN (1,2,'a')";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("val1 IN (1, 2, 'a')", output);
    }

    public void testBit() throws Exception {
        String sql = "0b01001001 | 3 & 1.2 <<d >> 0x0f";
        MySQLExprParser parser = new MySQLExprParser(new MySQLLexer(sql));
        Expression expr = parser.expression();
        String output = output2MySQL(expr, sql);
        Assert.assertEquals("b'01001001' | 3 & 1.2 << d >> x'0f'", output);
        BitOrExpression or = (BitOrExpression) expr;
        BitAndExpression and = (BitAndExpression) or.getRightOprand();
        BitShiftExpression rs = (BitShiftExpression) and.getRightOprand();
        BitShiftExpression ls = (BitShiftExpression) rs.getLeftOprand();
        Assert.assertEquals("d", ((Identifier) ls.getRightOprand()).getIdText());
        Assert.assertTrue(rs.isRightShift());
        Assert.assertFalse(ls.isRightShift());

        sql = "true + b & false ^ d - null ";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("TRUE + b & FALSE ^ d - NULL", output);
        and = (BitAndExpression) expr;
        ArithmeticAddExpression add = (ArithmeticAddExpression) and.getLeftOprand();
        ArithmeticSubtractExpression sub = (ArithmeticSubtractExpression) and.getRightOprand();
        BitXORExpression xor = (BitXORExpression) sub.getLeftOprand();
        Assert.assertEquals("d", ((Identifier) xor.getRightOprand()).getIdText());
        Assert.assertEquals("b", ((Identifier) add.getRightOprand()).getIdText());
    }

    public void testArithmetic() throws Exception {
        String sql = "? + @usrVar1 * c/@@version- e % -f diV g";
        MySQLExprParser parser = new MySQLExprParser(new MySQLLexer(sql));
        Expression expr = parser.expression();
        String output = output2MySQL(expr, sql);
        Assert.assertEquals("? + @usrVar1 * c / @@version - e % - f DIV g", output);
        ArithmeticSubtractExpression sub = (ArithmeticSubtractExpression) expr;
        ArithmeticAddExpression add = (ArithmeticAddExpression) sub.getLeftOprand();
        ArithmeticIntegerDivideExpression idiv = (ArithmeticIntegerDivideExpression) sub.getRightOprand();
        ArithmeticModExpression mod = (ArithmeticModExpression) idiv.getLeftOprand();
        ArithmeticDivideExpression div = (ArithmeticDivideExpression) add.getRightOprand();
        ArithmeticMultiplyExpression mt = (ArithmeticMultiplyExpression) div.getLeftOprand();
        MinusExpression mi = (MinusExpression) mod.getRightOprand();
        Assert.assertEquals("c", ((Identifier) mt.getRightOprand()).getIdText());
        Assert.assertEquals("f", ((Identifier) mi.getOperand()).getIdText());

        sql = "a+-b";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("a + - b", output);

        sql = "a+--b";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("a + - - b", output);

        sql = "a++b";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("a + b", output);

        sql = "a+++b";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("a + b", output);

        sql = "a++-b";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("a + - b", output);

        sql = "a + b mod (-((select id from t1 limit 1)- e) ) ";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("a + b % - ((SELECT id FROM t1 LIMIT 0, 1) - e)", output);
        add = (ArithmeticAddExpression) expr;
        mod = (ArithmeticModExpression) add.getRightOprand();
        mi = (MinusExpression) mod.getRightOprand();
        sub = (ArithmeticSubtractExpression) mi.getOperand();
        Assert.assertTrue(QueryExpression.class.isAssignableFrom(sub.getLeftOprand().getClass()));
        Assert.assertEquals("e", ((Identifier) sub.getRightOprand()).getIdText());

    }

    public void testBitHex() throws Exception {
        String sql = "x'89af' ";
        MySQLExprParser parser = new MySQLExprParser(new MySQLLexer(sql));
        Expression expr = parser.expression();
        String output = output2MySQL(expr, sql);
        Assert.assertEquals("x'89af'", output);
        Assert.assertEquals("89af", ((LiteralHexadecimal) expr).getText());

        sql = "_latin1 b'1011' ";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("_latin1 b'1011'", output);
        Assert.assertEquals("1011", ((LiteralBitField) expr).getText());
        Assert.assertEquals("_latin1", ((LiteralBitField) expr).getIntroducer());

        sql = "abc 0b1011 ";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("abc", output);

        sql = "_latin1 0xabc ";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("_latin1 x'abc'", output);

        sql = "jkl 0xabc ";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("jkl", output);
    }

    public void testString() throws Exception {
        String sql = "_latin1'abc\\'d' 'ef\"'";
        MySQLLexer lexer = new MySQLLexer(sql);
        MySQLExprParser parser = new MySQLExprParser(lexer);
        Expression expr = parser.expression();
        String output = output2MySQL(expr, sql);
        Assert.assertEquals("_latin1'abc\\'def\"'", output);

        sql = "n'abc\\'d' \"ef'\"\"\"";
        lexer = new MySQLLexer(sql);
        parser = new MySQLExprParser(lexer);
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("N'abc\\'def\\'\"'", output);

        sql = "`char`'an'";
        lexer = new MySQLLexer(sql);
        parser = new MySQLExprParser(lexer);
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("`char`", output);

        sql = "_latin1 n'abc' ";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("_latin1", output);
    }

    public void testAnyAll() throws Exception {
        String sql = "1 >= any (select id from t1 limit 1)";
        MySQLExprParser parser = new MySQLExprParser(new MySQLLexer(sql));
        Expression expr = parser.expression();
        String output = output2MySQL(expr, sql);
        Assert.assertEquals("1 >= ANY (SELECT id FROM t1 LIMIT 0, 1)", output);
        Assert.assertEquals(ComparisionGreaterThanOrEqualsExpression.class, expr.getClass());

        sql = "1 >= any (select id from t1 limit 1) > aLl(select tb1.id from tb1 t1,tb2 as t2 where t1.id=t2.id limit 1)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals(
                "1 >= ANY (SELECT id FROM t1 LIMIT 0, 1) > ALL (SELECT tb1.id FROM tb1 AS T1, tb2 AS T2 WHERE t1.id = t2.id LIMIT 0, 1)",
                output);
        ComparisionGreaterThanExpression gt = (ComparisionGreaterThanExpression) expr;
        ComparisionGreaterThanOrEqualsExpression ge = (ComparisionGreaterThanOrEqualsExpression) gt.getLeftOprand();
        Assert.assertEquals(LiteralNumber.class, ge.getLeftOprand().getClass());

        sql = "1 >= any + any";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("1 >= any + any", output);
    }

    public void testUnary() throws Exception {
        String sql = "!-~ binary a collate latin1_danish_ci";
        MySQLExprParser parser = new MySQLExprParser(new MySQLLexer(sql));
        Expression expr = parser.expression();
        String output = output2MySQL(expr, sql);
        Assert.assertEquals("! - ~ BINARY a COLLATE latin1_danish_ci", output);
        NegativeValueExpression neg = (NegativeValueExpression) expr;
        MinusExpression mi = (MinusExpression) neg.getOperand();
        BitInvertExpression bi = (BitInvertExpression) mi.getOperand();
        CastBinaryExpression bin = (CastBinaryExpression) bi.getOperand();
        CollateExpression col = (CollateExpression) bin.getOperand();
        Assert.assertEquals("a", ((Identifier) col.getString()).getIdText());
    }

    public void testUser() throws Exception {
        String sql = "'root'@'localhost'";
        MySQLExprParser parser = new MySQLExprParser(new MySQLLexer(sql));
        Expression expr = parser.expression();
        String output = output2MySQL(expr, sql);
        Assert.assertEquals("'root'@'localhost'", output);
        UserExpression usr = (UserExpression) expr;
        Assert.assertEquals("'root'@'localhost'", usr.getUserAtHost());

        sql = "root@localhost";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("root@localhost", output);
        usr = (UserExpression) expr;
        Assert.assertEquals("root@localhost", usr.getUserAtHost());

        sql = "var@'localhost'";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("var@'localhost'", output);
        usr = (UserExpression) expr;
        Assert.assertEquals("var@'localhost'", usr.getUserAtHost());
    }

    public void testPrimarySystemVar() throws Exception {
        String sql = "@@gloBal . /*dd*/ `all`";
        MySQLLexer lexer = new MySQLLexer(sql);
        MySQLExprParser parser = new MySQLExprParser(lexer);
        Expression expr = parser.expression();
        String output = output2MySQL(expr, sql);
        Assert.assertEquals("@@global.`all`", output);
        Assert.assertEquals(SysVarPrimary.class, expr.getClass());
        SysVarPrimary sysvar = (SysVarPrimary) expr;
        Assert.assertEquals(VariableScope.GLOBAL, sysvar.getScope());
        Assert.assertEquals("`all`", sysvar.getVarText());

        sql = "@@Session . /*dd*/ any";
        lexer = new MySQLLexer(sql);
        parser = new MySQLExprParser(lexer);
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("@@any", output);
        Assert.assertEquals(SysVarPrimary.class, expr.getClass());
        sysvar = (SysVarPrimary) expr;
        Assert.assertEquals(VariableScope.SESSION, sysvar.getScope());
        Assert.assertEquals("any", sysvar.getVarText());

        sql = "@@LOCAl . /*dd*/ `usage`";
        lexer = new MySQLLexer(sql);
        parser = new MySQLExprParser(lexer);
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("@@`usage`", output);
        Assert.assertEquals(SysVarPrimary.class, expr.getClass());
        sysvar = (SysVarPrimary) expr;
        Assert.assertEquals(VariableScope.SESSION, sysvar.getScope());
        Assert.assertEquals("`usage`", sysvar.getVarText());

        sql = "@@LOCAl . /*dd*/ `var1`";
        lexer = new MySQLLexer(sql);
        parser = new MySQLExprParser(lexer);
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("@@`var1`", output);
        Assert.assertEquals(SysVarPrimary.class, expr.getClass());
        sysvar = (SysVarPrimary) expr;
        Assert.assertEquals(VariableScope.SESSION, sysvar.getScope());
        Assert.assertEquals("`var1`", sysvar.getVarText());

        sql = "@@var1   ,";
        lexer = new MySQLLexer(sql);
        parser = new MySQLExprParser(lexer);
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("@@var1", output);
        Assert.assertEquals(SysVarPrimary.class, expr.getClass());
        sysvar = (SysVarPrimary) expr;
        Assert.assertEquals(VariableScope.SESSION, sysvar.getScope());
        Assert.assertEquals("var1", sysvar.getVarText());

        sql = "@@`case``1`   ,@@_";
        lexer = new MySQLLexer(sql);
        parser = new MySQLExprParser(lexer);
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("@@`case``1`", output);
        Assert.assertEquals(SysVarPrimary.class, expr.getClass());
        sysvar = (SysVarPrimary) expr;
        Assert.assertEquals(VariableScope.SESSION, sysvar.getScope());
        Assert.assertEquals("`case``1`", sysvar.getVarText());
        lexer.nextToken();
        parser = new MySQLExprParser(lexer);
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("@@_", output);
        Assert.assertEquals(SysVarPrimary.class, expr.getClass());
        sysvar = (SysVarPrimary) expr;
        Assert.assertEquals(VariableScope.SESSION, sysvar.getScope());
        Assert.assertEquals("_", sysvar.getVarText());
    }

    public void testPrimary() throws Exception {
        String sql = "(1,2,existS (select id.* from t1))";
        MySQLExprParser parser = new MySQLExprParser(new MySQLLexer(sql));
        Expression expr = parser.expression();
        String output = output2MySQL(expr, sql);
        Assert.assertEquals("ROW(1, 2, EXISTS (SELECT id.* FROM t1))", output);
        RowExpression row = (RowExpression) expr;
        Assert.assertEquals(3, row.getRowExprList().size());

        sql = "*";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("*", output);
        Assert.assertTrue(Wildcard.class.isAssignableFrom(expr.getClass()));

        sql = "case v1 when `index` then a when 2 then b else c end";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("CASE v1 WHEN `index` THEN a WHEN 2 THEN b ELSE c END", output);
        CaseWhenOperatorExpression cw = (CaseWhenOperatorExpression) expr;
        Assert.assertEquals("v1", ((Identifier) cw.getComparee()).getIdText());
        Assert.assertEquals(2, cw.getWhenList().size());
        Assert.assertEquals("c", ((Identifier) cw.getElseResult()).getIdText());

        sql = "case  when 1=value then a  end";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("CASE WHEN 1 = value THEN a END", output);
        cw = (CaseWhenOperatorExpression) expr;
        Assert.assertNull(cw.getComparee());
        Assert.assertEquals(1, cw.getWhenList().size());
        Assert.assertNull(cw.getElseResult());

        sql = "case  when 1=`in` then a  end";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("CASE WHEN 1 = `in` THEN a END", output);

        sql = " ${INSENSITIVE}. ";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("${INSENSITIVE}", output);

        sql = "current_date, ";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("CURDATE()", output);

        sql = "CurRent_Date  (  ) ";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("CURDATE()", output);

        sql = "CurRent_TiMe   ";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("CURTIME()", output);

        sql = "CurRent_TiMe  () ";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("CURTIME()", output);

        sql = "CurRent_TimesTamp ";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("NOW()", output);

        sql = "CurRent_TimesTamp  ()";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("NOW()", output);

        sql = "localTimE";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("NOW()", output);

        sql = "localTimE  () ";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("NOW()", output);

        sql = "localTimesTamP  ";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("NOW()", output);

        sql = "localTimesTamP  () ";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("NOW()", output);

        sql = "CurRent_user ";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("CURRENT_USER()", output);

        sql = "CurRent_user  () ";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("CURRENT_USER()", output);

        sql = "default  () ";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("DEFAULT()", output);

        sql = "vaLueS(1,col1*2)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("VALUES(1, col1 * 2)", output);

        sql = "(1,2,mod(m,n))";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("ROW(1, 2, m % n)", output);

        sql = "chaR (77,121,'77.3')";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("CHAR(77, 121, '77.3')", output);

        sql = "CHARSET(CHAR(0x65 USING utf8))";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("CHARSET(CHAR(x'65' USING utf8))", output);

        sql = "CONVERT(_latin1'Müller' USING utf8)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("CONVERT(_latin1'Müller' USING utf8)", output);

        // QS_TODO

    }

    public void testStartedFromIdentifier() throws Exception {
        // QS_TODO
        String sql = "cast(CAST(1-2 AS UNSIGNED) AS SIGNED)";
        MySQLExprParser parser = new MySQLExprParser(new MySQLLexer(sql));
        Expression expr = parser.expression();
        String output = output2MySQL(expr, sql);
        Assert.assertEquals("CAST(CAST(1 - 2 AS UNSIGNED) AS SIGNED)", output);

        sql = "position('a' in \"abc\")";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("LOCATE('a', 'abc')", output);

        sql = "cast(CAST(1-2 AS UNSIGNED integer) AS SIGNED integer)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("CAST(CAST(1 - 2 AS UNSIGNED) AS SIGNED)", output);

        sql = "CAST(expr as char)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("CAST(expr AS CHAR)", output);

        sql = "CAST(6/4 AS DECIMAL(3,1))";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("CAST(6 / 4 AS DECIMAL(3, 1))", output);

        sql = "CAST(6/4 AS DECIMAL(3))";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("CAST(6 / 4 AS DECIMAL(3))", output);

        sql = "CAST(6/4 AS DECIMAL)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("CAST(6 / 4 AS DECIMAL)", output);

        sql = "CAST(now() as date)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("CAST(NOW() AS DATE)", output);

        sql = "CAST(expr as char(5))";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("CAST(expr AS CHAR(5))", output);

        sql = "SUBSTRING('abc',pos,len)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("SUBSTRING('abc', pos, len)", output);

        sql = "SUBSTRING('abc' FROM pos FOR len)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("SUBSTRING('abc', pos, len)", output);

        sql = "SUBSTRING(str,pos)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("SUBSTRING(str, pos)", output);

        sql = "SUBSTRING('abc',1,2)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("SUBSTRING('abc', 1, 2)", output);

        sql = "row(1,2,str)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("ROW(1, 2, str)", output);

        sql = "position(\"abc\" in '/*abc*/')";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("LOCATE('abc', '/*abc*/')", output);

        sql = "locate(localtime,b)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("LOCATE(NOW(), b)", output);

        sql = "locate(locate(a,b),`match`)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("LOCATE(LOCATE(a, b), `match`)", output);

        sql = "TRIM(LEADING 'x' FROM 'xxxbarxxx')";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("TRIM(LEADING 'x' FROM 'xxxbarxxx')", output);

        sql = "TRIM(BOTH 'x' FROM 'xxxbarxxx')";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("TRIM(BOTH 'x' FROM 'xxxbarxxx')", output);

        sql = "TRIM(TRAILING 'xyz' FROM 'barxxyz')";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("TRIM(TRAILING 'xyz' FROM 'barxxyz')", output);

        sql = "TRIM('  if   ')";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("TRIM('  if   ')", output);

        sql = "TRIM( 'x' FROM 'xxxbarxxx')";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("TRIM('x' FROM 'xxxbarxxx')", output);

        sql = "TRIM(both  FROM 'barxxyz')";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("TRIM(BOTH  FROM 'barxxyz')", output);

        sql = "TRIM(leading  FROM 'barxxyz')";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("TRIM(LEADING  FROM 'barxxyz')", output);

        sql = "TRIM(TRAILING  FROM 'barxxyz')";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("TRIM(TRAILING  FROM 'barxxyz')", output);

        sql = "avg(DISTINCT results)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("AVG(DISTINCT results)", output);

        sql = "avg(results)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("AVG(results)", output);

        sql = "max(DISTINCT results)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("MAX(DISTINCT results)", output);

        sql = "max(results)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("MAX(results)", output);

        sql = "min(DISTINCT results)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("MIN(DISTINCT results)", output);

        sql = "min(results)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("MIN(results)", output);

        sql = "sum(DISTINCT results)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("SUM(DISTINCT results)", output);

        sql = "sum(results)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("SUM(results)", output);

        sql = "count(DISTINCT expr1,expr2,expr3)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("COUNT(DISTINCT expr1, expr2, expr3)", output);

        sql = "count(*)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("COUNT(*)", output);

        sql = "GROUP_CONCAT(DISTINCT expr1,expr2,expr3 ORDER BY col_name1 DESC,col_name2 SEPARATOR ' ')";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals(
                "GROUP_CONCAT(DISTINCT expr1, expr2, expr3 ORDER BY col_name1 DESC, col_name2 SEPARATOR  )",
                output);

        sql = "GROUP_CONCAT(a||b,expr2,expr3 ORDER BY col_name1 asc,col_name2 SEPARATOR '@ ')";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("GROUP_CONCAT(a OR b, expr2, expr3 ORDER BY col_name1 ASC, col_name2 SEPARATOR @ )", output);

        sql = "GROUP_CONCAT(expr1 ORDER BY col_name1 asc,col_name2 SEPARATOR 'str_val ')";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("GROUP_CONCAT(expr1 ORDER BY col_name1 ASC, col_name2 SEPARATOR str_val )", output);

        sql = "GROUP_CONCAT(DISTINCT test_score ORDER BY test_score DESC )";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("GROUP_CONCAT(DISTINCT test_score ORDER BY test_score DESC SEPARATOR ,)", output);

        sql = "GROUP_CONCAT(DISTINCT test_score ORDER BY test_score asc )";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("GROUP_CONCAT(DISTINCT test_score ORDER BY test_score ASC SEPARATOR ,)", output);

        sql = "GROUP_CONCAT(c1)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("GROUP_CONCAT(c1 SEPARATOR ,)", output);

        sql = "GROUP_CONCAT(c1 separator '')";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("GROUP_CONCAT(c1 SEPARATOR )", output);

        sql = "default";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("DEFAULT", output);

        sql = "default(col)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("DEFAULT(col)", output);

        sql = "database()";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("DATABASE()", output);

        sql = "if(1>2,a+b,a:=1)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("IF(1 > 2, a + b, a := 1)", output);

        sql = "insert('abc',1,2,'')";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("INSERT('abc', 1, 2, '')", output);

        sql = "left(\"hjkafag\",4)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("LEFT('hjkafag', 4)", output);

        sql = "repeat('ag',2.1e1)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("REPEAT('ag', 21)", output);

        sql = "replace('anjd',\"df\",'af')";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("REPLACE('anjd', 'df', 'af')", output);

        sql = "right(\"hjkafag\",4)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("RIGHT('hjkafag', 4)", output);

        sql = "schema()";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("DATABASE()", output);

        sql = "utc_date()";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("UTC_DATE()", output);

        sql = "Utc_time()";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("UTC_TIME()", output);

        sql = "Utc_timestamp()";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("UTC_TIMESTAMP()", output);
    }

    public void testInterval() throws Exception {
        // QS_TODO
        String sql = "DATE_ADD('2009-01-01', INTERVAL (6/4) HOUR_MINUTE)";
        MySQLExprParser parser = new MySQLExprParser(new MySQLLexer(sql));
        Expression expr = parser.expression();
        String output = output2MySQL(expr, sql);
        Assert.assertEquals("DATE_ADD('2009-01-01', INTERVAL (6 / 4) HOUR_MINUTE)", output);

        sql = "'2008-12-31 23:59:59' + INTERVAL 1 SECOND";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("'2008-12-31 23:59:59' + INTERVAL 1 SECOND", output);

        sql = " INTERVAL 1 DAY + '2008-12-31'";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("INTERVAL 1 DAY + '2008-12-31'", output);

        sql = "DATE_ADD('2100-12-31 23:59:59',INTERVAL '1:1' MINUTE_SECOND)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("DATE_ADD('2100-12-31 23:59:59', INTERVAL '1:1' MINUTE_SECOND)", output);

        sql = "DATE_SUB('2005-01-01 00:00:00',INTERVAL '1 1:1:1' DAY_SECOND)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("DATE_SUB('2005-01-01 00:00:00', INTERVAL '1 1:1:1' DAY_SECOND)", output);

        sql = "DATE_ADD('1900-01-01 00:00:00',INTERVAL '-1 10' DAY_HOUR)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("DATE_ADD('1900-01-01 00:00:00', INTERVAL '-1 10' DAY_HOUR)", output);

        sql = "DATE_SUB('1998-01-02', INTERVAL 31 DAY)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("DATE_SUB('1998-01-02', INTERVAL 31 DAY)", output);

        sql = "DATE_ADD('1992-12-31 23:59:59.000002',INTERVAL '1.999999' SECOND_MICROSECOND)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("DATE_ADD('1992-12-31 23:59:59.000002', INTERVAL '1.999999' SECOND_MICROSECOND)", output);

        sql = "DATE_ADD('2013-01-01', INTERVAL 1 HOUR)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("DATE_ADD('2013-01-01', INTERVAL 1 HOUR)", output);

        sql = "DATE_ADD('2009-01-30', INTERVAL 1 MONTH)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("DATE_ADD('2009-01-30', INTERVAL 1 MONTH)", output);

        sql = "DATE_ADD('1992-12-31 23:59:59.000002',INTERVAL '1:1.999999' minute_MICROSECOND)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("DATE_ADD('1992-12-31 23:59:59.000002', INTERVAL '1:1.999999' MINUTE_MICROSECOND)", output);

        sql = "DATE_ADD('1992-12-31 23:59:59.000002',INTERVAL '1:1:1.999999' hour_MICROSECOND)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("DATE_ADD('1992-12-31 23:59:59.000002', INTERVAL '1:1:1.999999' HOUR_MICROSECOND)", output);

        sql = "DATE_ADD('2100-12-31 23:59:59',INTERVAL '1:1:1' hour_SECOND)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("DATE_ADD('2100-12-31 23:59:59', INTERVAL '1:1:1' HOUR_SECOND)", output);

        sql = "DATE_ADD('1992-12-31 23:59:59.000002',INTERVAL '1 1:1:1.999999' day_MICROSECOND)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("DATE_ADD('1992-12-31 23:59:59.000002', INTERVAL '1 1:1:1.999999' DAY_MICROSECOND)", output);

        sql = "DATE_ADD('2100-12-31 23:59:59',INTERVAL '1 1:1' day_minute)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("DATE_ADD('2100-12-31 23:59:59', INTERVAL '1 1:1' DAY_MINUTE)", output);

        sql = "DATE_ADD('2100-12-31',INTERVAL '1-1' year_month)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("DATE_ADD('2100-12-31', INTERVAL '1-1' YEAR_MONTH)", output);

        sql = "INTERVAL(n1,n2,n3)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("INTERVAL(n1, n2, n3)", output);

        sql = "INTERVAL a+b day";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("INTERVAL (a + b) DAY", output);

        sql = "INTERVAL(select id from t1) day";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("INTERVAL (SELECT id FROM t1) DAY", output);

        sql = "INTERVAL(('jklj'+a))day";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("INTERVAL ('jklj' + a) DAY", output);
    }

    public void testMatchExpression() throws Exception {
        // QS_TODO
        String sql = "MATCH (title,body) AGAINST ('database' WITH QUERY EXPANSION)";
        MySQLExprParser parser = new MySQLExprParser(new MySQLLexer(sql));
        Expression expr = parser.expression();
        String output = output2MySQL(expr, sql);
        Assert.assertEquals("MATCH (title, body) AGAINST ('database' WITH QUERY EXPANSION)", output);
        Assert.assertEquals("MATCH (title, body) AGAINST ('database' WITH QUERY EXPANSION)", output);

        sql = "MATCH (title,body) AGAINST ( (abc in (d)) IN boolean MODE)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("MATCH (title, body) AGAINST ((abc IN (d)) IN BOOLEAN MODE)", output);

        sql = "MATCH (title,body) AGAINST ('database')";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("MATCH (title, body) AGAINST ('database')", output);

        sql = "MATCH (col1,col2,col3) AGAINST ((a:=b:=c) IN boolean MODE)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("MATCH (col1, col2, col3) AGAINST (a := b := c IN BOOLEAN MODE)", output);

        sql = "MATCH (title,body) AGAINST ((a and (b ||c)) IN boolean MODE)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("MATCH (title, body) AGAINST (a AND (b OR c) IN BOOLEAN MODE)", output);

        sql = "MATCH (title,body) AGAINST ((a between b and c) IN boolean MODE)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("MATCH (title, body) AGAINST (a BETWEEN b AND c IN BOOLEAN MODE)", output);

        sql = "MATCH (title,body) AGAINST ((a between b and (abc in (d))) IN boolean MODE)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("MATCH (title, body) AGAINST ((a BETWEEN b AND abc IN (d)) IN BOOLEAN MODE)", output);

        sql = "MATCH (title,body) AGAINST ((not not a) IN boolean MODE)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("MATCH (title, body) AGAINST (NOT NOT a IN BOOLEAN MODE)", output);

        sql = "MATCH (title,body) AGAINST ((a is true) IN boolean MODE)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("MATCH (title, body) AGAINST (a IS TRUE IN BOOLEAN MODE)", output);

        sql = "MATCH (title,body) AGAINST ((select a) IN boolean MODE)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("MATCH (title, body) AGAINST (SELECT a IN BOOLEAN MODE)", output);

        sql = "MATCH (title,body) AGAINST ('database' IN NATURAL LANGUAGE MODE)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals("MATCH (title, body) AGAINST ('database' IN NATURAL LANGUAGE MODE)", output);

        sql = "MATCH (title,body) AGAINST ('database' IN NATURAL LANGUAGE MODE WITH QUERY EXPANSION)";
        parser = new MySQLExprParser(new MySQLLexer(sql));
        expr = parser.expression();
        output = output2MySQL(expr, sql);
        Assert.assertEquals(
                "MATCH (title, body) AGAINST ('database' IN NATURAL LANGUAGE MODE WITH QUERY EXPANSION)",
                output);
    }
}
