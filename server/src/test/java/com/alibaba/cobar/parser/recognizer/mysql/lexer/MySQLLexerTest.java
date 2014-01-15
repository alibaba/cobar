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
 * (created at 2011-3-14)
 */
package com.alibaba.cobar.parser.recognizer.mysql.lexer;

import java.sql.SQLSyntaxErrorException;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.alibaba.cobar.parser.Performance;
import com.alibaba.cobar.parser.recognizer.mysql.MySQLToken;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class MySQLLexerTest extends TestCase {

    public static void main(String[] args) throws SQLSyntaxErrorException {
        String sql = Performance.SQL_BENCHMARK_SELECT;
        char[] chars = sql.toCharArray();
        MySQLLexer sut = new MySQLLexer(sql);
        long start = System.currentTimeMillis();
        long end = System.currentTimeMillis();
        for (int i = 0; i < 1; ++i) {
            for (; !sut.eof();) {
                sut.nextToken();
                switch (sut.token()) {
                case LITERAL_NUM_MIX_DIGIT:
                    sut.decimalValue();
                    break;
                case LITERAL_NUM_PURE_DIGIT:
                    sut.integerValue();
                    break;
                default:
                    sut.stringValue();
                    break;
                }
            }
        }

        int loop = 5000000;
        sut = new MySQLLexer(sql);
        start = System.currentTimeMillis();
        for (int i = 0; i < loop; ++i) {
            sut = new MySQLLexer(chars);
            for (; !sut.eof();) {
                sut.nextToken();
                switch (sut.token()) {
                case LITERAL_NUM_MIX_DIGIT:
                    sut.decimalValue();
                    break;
                case LITERAL_NUM_PURE_DIGIT:
                    sut.integerValue();
                    break;
                default:
                    sut.stringValue();
                    break;
                }
            }
        }
        end = System.currentTimeMillis();
        System.out.println((end - start) * 1.0d / (loop / 1000) + " us.");
    }

    public void testParameter() throws SQLSyntaxErrorException {
        MySQLLexer sut = new MySQLLexer("?,?,?");
        Assert.assertEquals(MySQLToken.QUESTION_MARK, sut.token());
        Assert.assertEquals(1, sut.paramIndex());
        sut.nextToken();
        sut.nextToken();
        Assert.assertEquals(MySQLToken.QUESTION_MARK, sut.token());
        Assert.assertEquals(2, sut.paramIndex());
        sut.nextToken();
        sut.nextToken();
        Assert.assertEquals(MySQLToken.QUESTION_MARK, sut.token());
        Assert.assertEquals(3, sut.paramIndex());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());
    }

    public void testUserDefVar() throws SQLSyntaxErrorException {
        MySQLLexer sut = new MySQLLexer("@abc  ");
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@abc", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("@abc.d  ");
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@abc.d", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("@abc_$.d");
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@abc_$.d", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("@abc_$_.");
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@abc_$_.", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("@''");
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@''", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("@'\\''");
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@'\\''", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("@''''");
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@''''", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("@'\"'");
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@'\"'", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("@'\"\"'");
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@'\"\"'", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("@'\\\"'");
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@'\\\"'", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("@'ac\\''  ");
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@'ac\\''", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("@'''ac\\''  ");
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@'''ac\\''", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("@'abc'''ac\\''  ");
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@'abc'''", sut.stringValue());

        sut = new MySQLLexer("@''abc''");
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@''", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("abc", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.LITERAL_CHARS, sut.token());
        Assert.assertEquals("''", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("@\"\"  ");
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@\"\"", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("@\"\"\"abc\"  ");
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@\"\"\"abc\"", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("@\"\\\"\"\"abc\"  ");
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@\"\\\"\"\"abc\"", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("@\"\\\"\"  ");
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@\"\\\"\"", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("@\"\"\"\\\"d\"  ");
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@\"\"\"\\\"d\"", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("@\"'\"  ");
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@\"'\"", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("@`` ");
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@``", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("@````");
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@````", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("@` `");
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@` `", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("@`abv```");
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@`abv```", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("@`````abc`");
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@`````abc`", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("@`````abc```");
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@`````abc```", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("@``abc");
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@``", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("abc", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("@`abc`````abc```");
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@`abc`````", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("abc", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("```", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer(" -- \n  @  #abc\n\r\t\"\"@\"abc\\\\''-- abc\n'''\\\"\"\"\"");
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.LITERAL_CHARS, sut.token());
        Assert.assertEquals("''", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@\"abc\\\\''-- abc\n'''\\\"\"\"\"", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("/**/@a #@abc\n@.\r\t");
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@a", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@.", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("  #@abc\n@\"1a_-@#!''`=\\a\"-- @\r\n@'-_1a/**/\\\"\\''/*@abc*/@`_1@\\''\"`");
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@\"1a_-@#!''`=\\a\"", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@'-_1a/**/\\\"\\''", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@`_1@\\''\"`", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("  /*! */@._a$ @_a.b$c.\r@1_a.$#\n@A.a_/@-- \n@_--@.[]'\"@#abc'@a,@;@~#@abc");
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@._a$", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@_a.b$c.", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@1_a.$", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@A.a_", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.OP_SLASH, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@_", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.OP_MINUS, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.OP_MINUS, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@.", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.PUNC_LEFT_BRACKET, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.PUNC_RIGHT_BRACKET, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.LITERAL_CHARS, sut.token());
        Assert.assertEquals("'\"@#abc'", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@a", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.PUNC_COMMA, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.PUNC_SEMICOLON, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.OP_TILDE, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());
    }

    public void testSystemVar() throws SQLSyntaxErrorException {
        MySQLLexer sut = new MySQLLexer("@@abc  ");
        Assert.assertEquals(MySQLToken.SYS_VAR, sut.token());
        Assert.assertEquals("abc", sut.stringValue());
        Assert.assertEquals("ABC", sut.stringValueUppercase());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("@@`abc`  ");
        Assert.assertEquals(MySQLToken.SYS_VAR, sut.token());
        Assert.assertEquals("`abc`", sut.stringValue());
        Assert.assertEquals("`ABC`", sut.stringValueUppercase());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("@@```abc`  ");
        Assert.assertEquals(MySQLToken.SYS_VAR, sut.token());
        Assert.assertEquals("```abc`", sut.stringValue());
        Assert.assertEquals("```ABC`", sut.stringValueUppercase());

        sut = new MySQLLexer("@@``  ");
        Assert.assertEquals(MySQLToken.SYS_VAR, sut.token());
        Assert.assertEquals("``", sut.stringValue());
        Assert.assertEquals("``", sut.stringValueUppercase());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("@@`a```  ");
        Assert.assertEquals(MySQLToken.SYS_VAR, sut.token());
        Assert.assertEquals("`a```", sut.stringValue());
        Assert.assertEquals("`A```", sut.stringValueUppercase());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("@@````  ");
        Assert.assertEquals(MySQLToken.SYS_VAR, sut.token());
        Assert.assertEquals("````", sut.stringValue());
        Assert.assertEquals("````", sut.stringValueUppercase());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("@@`~!````@#$%^&*()``_+=-1{}[]\";:'<>,./?|\\`  ");
        Assert.assertEquals(MySQLToken.SYS_VAR, sut.token());
        Assert.assertEquals("`~!````@#$%^&*()``_+=-1{}[]\";:'<>,./?|\\`", sut.stringValue());
        Assert.assertEquals("`~!````@#$%^&*()``_+=-1{}[]\";:'<>,./?|\\`", sut.stringValueUppercase());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("@@global.var1  ");
        Assert.assertEquals(MySQLToken.SYS_VAR, sut.token());
        Assert.assertEquals("global", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.PUNC_DOT, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("var1", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("@@'abc'  ");
        Assert.assertEquals(MySQLToken.SYS_VAR, sut.token());
        Assert.assertEquals("", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.LITERAL_CHARS, sut.token());
        Assert.assertEquals("'abc'", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("@@\"abc\"  ");
        Assert.assertEquals(MySQLToken.SYS_VAR, sut.token());
        Assert.assertEquals("", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.LITERAL_CHARS, sut.token());
        Assert.assertEquals("'abc'", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer(
                "@@.  /*@@abc*/@@`abc''\"\\@@!%*&+_abcQ`//@@_1.  @@$#\n@@$var.-- @@a\t\n@@system_var:@@a`b`?");
        Assert.assertEquals(MySQLToken.SYS_VAR, sut.token());
        Assert.assertEquals("", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.PUNC_DOT, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.SYS_VAR, sut.token());
        Assert.assertEquals("`abc''\"\\@@!%*&+_abcQ`", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.OP_SLASH, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.OP_SLASH, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.SYS_VAR, sut.token());
        Assert.assertEquals("_1", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.PUNC_DOT, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.SYS_VAR, sut.token());
        Assert.assertEquals("$", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.SYS_VAR, sut.token());
        Assert.assertEquals("$var", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.PUNC_DOT, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.SYS_VAR, sut.token());
        Assert.assertEquals("system_var", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.PUNC_COLON, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.SYS_VAR, sut.token());
        Assert.assertEquals("a", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("`b`", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.QUESTION_MARK, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());
    }

    public void testPlaceHolder() throws SQLSyntaxErrorException {
        MySQLLexer sut = new MySQLLexer(" ${abc}. ");
        Assert.assertEquals(MySQLToken.PLACE_HOLDER, sut.token());
        Assert.assertEquals("abc", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.PUNC_DOT, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer(" ${abc");
        Assert.assertEquals(MySQLToken.PLACE_HOLDER, sut.token());
        Assert.assertEquals("abc", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer(" ${abc}");
        Assert.assertEquals(MySQLToken.PLACE_HOLDER, sut.token());
        Assert.assertEquals("abc", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer(" ${abc}abn");
        Assert.assertEquals(MySQLToken.PLACE_HOLDER, sut.token());
        Assert.assertEquals("abc", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("abn", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer(" ${abc12@,,.~`*-_$}}}}");
        Assert.assertEquals(MySQLToken.PLACE_HOLDER, sut.token());
        Assert.assertEquals("abc12@,,.~`*-_$", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.PUNC_RIGHT_BRACE, sut.token());
        sut.nextToken();
        sut.nextToken();
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer(" #${abc\n,${abc12@,,.~`*-_$}");
        Assert.assertEquals(MySQLToken.PUNC_COMMA, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.PLACE_HOLDER, sut.token());
        Assert.assertEquals("abc12@,,.~`*-_$", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("${abc(123,345)} ,");
        Assert.assertEquals(MySQLToken.PLACE_HOLDER, sut.token());
        Assert.assertEquals("abc(123,345)", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.PUNC_COMMA, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());
    }

    public void testId1() throws SQLSyntaxErrorException {
        MySQLLexer sut = new MySQLLexer("id . 12e3f /***/`12\\3```-- d\n \r#\r  ##\n\t123d");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.PUNC_DOT, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("12e3f", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("`12\\3```", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("123d", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("`ab``c`");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("`ab``c`", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("`,\"'\\//*$#\nab``c  -`");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("`,\"'\\//*$#\nab``c  -`", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("`ab````c```");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("`ab````c```", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("`ab`````c``````");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("`ab`````", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("c", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("``````", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("n123 \t b123 x123");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("n123", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("b123", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("x123", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("n邱 硕");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("n邱", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("硕", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("n邱硕");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("n邱硕", sut.stringValue());
        sut.nextToken();

        sut = new MySQLLexer(" $abc");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("$abc", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("$abc  ");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("$abc", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer(" $abc  ");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("$abc", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer(" 123d +=_&*_1a^abc-- $123");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("123d", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.OP_PLUS, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.OP_EQUALS, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("_", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.OP_AMPERSAND, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.OP_ASTERISK, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("_1a", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.OP_CARET, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("abc", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer(" $abc  ,#$abc\n{`_``12`(123a)_abcnd; //x123");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("$abc", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.PUNC_COMMA, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.PUNC_LEFT_BRACE, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("`_``12`", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.PUNC_LEFT_PAREN, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("123a", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.PUNC_RIGHT_PAREN, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("_abcnd", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.PUNC_SEMICOLON, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.OP_SLASH, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.OP_SLASH, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("x123", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

    }

    public void testString() throws SQLSyntaxErrorException {
        MySQLLexer sut = new MySQLLexer("''");
        Assert.assertEquals(MySQLToken.LITERAL_CHARS, sut.token());
        Assert.assertEquals("''", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("'''\\''");
        Assert.assertEquals(MySQLToken.LITERAL_CHARS, sut.token());
        Assert.assertEquals("'\\'\\''", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("\'\'\'\'\'\\''");
        Assert.assertEquals(MySQLToken.LITERAL_CHARS, sut.token());
        Assert.assertEquals("'\\'\\'\\''", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("''''''/'abc\\''");
        Assert.assertEquals(MySQLToken.LITERAL_CHARS, sut.token());
        Assert.assertEquals("'\\'\\''", sut.stringValue());
        sut.nextToken();
        sut.nextToken();
        Assert.assertEquals(MySQLToken.LITERAL_CHARS, sut.token());
        Assert.assertEquals("'abc\\''", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("\'abc\\\'\'");
        Assert.assertEquals(MySQLToken.LITERAL_CHARS, sut.token());
        Assert.assertEquals("'abc\\''", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("'\\\\\\\"\"\"'");
        Assert.assertEquals(MySQLToken.LITERAL_CHARS, sut.token());
        Assert.assertEquals("'\\\\\\\"\"\"'", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("'\'\''");
        Assert.assertEquals(MySQLToken.LITERAL_CHARS, sut.token());
        Assert.assertEquals("'\\''", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("''''");
        Assert.assertEquals(MySQLToken.LITERAL_CHARS, sut.token());
        Assert.assertEquals("'\\''", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("'he\"\"\"llo'");
        Assert.assertEquals(MySQLToken.LITERAL_CHARS, sut.token());
        Assert.assertEquals("'he\"\"\"llo'", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("'he'\''\'llo'");
        Assert.assertEquals(MySQLToken.LITERAL_CHARS, sut.token());
        Assert.assertEquals("'he\\'\\'llo'", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("'\''hello'");
        Assert.assertEquals(MySQLToken.LITERAL_CHARS, sut.token());
        Assert.assertEquals("'\\'hello'", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("\"abc'\\d\"\"ef\"");
        Assert.assertEquals(MySQLToken.LITERAL_CHARS, sut.token());
        Assert.assertEquals("'abc\\'\\d\"ef'", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("\"abc'\\\\\"\"\"");
        Assert.assertEquals(MySQLToken.LITERAL_CHARS, sut.token());
        Assert.assertEquals("'abc\\'\\\\\"'", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("\"\"");
        Assert.assertEquals(MySQLToken.LITERAL_CHARS, sut.token());
        Assert.assertEquals("''", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("\"\\'\"");
        Assert.assertEquals(MySQLToken.LITERAL_CHARS, sut.token());
        Assert.assertEquals("'\\''", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("\"\"\"\"");
        Assert.assertEquals(MySQLToken.LITERAL_CHARS, sut.token());
        Assert.assertEquals("'\"'", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("\"abc\" '\\'s'");
        Assert.assertEquals(MySQLToken.LITERAL_CHARS, sut.token());
        Assert.assertEquals("'abc'", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.LITERAL_CHARS, sut.token());
        Assert.assertEquals("'\\'s'", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("\"\"\"'\"");
        Assert.assertEquals(MySQLToken.LITERAL_CHARS, sut.token());
        Assert.assertEquals("'\"\\''", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("\"\\\"\"");
        Assert.assertEquals(MySQLToken.LITERAL_CHARS, sut.token());
        Assert.assertEquals("'\\\"'", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("\"\\\\\"");
        Assert.assertEquals(MySQLToken.LITERAL_CHARS, sut.token());
        Assert.assertEquals("'\\\\'", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("\"   hello '''/**/#\n-- \n~=+\"\"\"\"\"");
        Assert.assertEquals(MySQLToken.LITERAL_CHARS, sut.token());
        Assert.assertEquals("'   hello \\'\\'\\'/**/#\n-- \n~=+\"\"'", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("\r--\t\n\"abc\"");
        Assert.assertEquals(MySQLToken.LITERAL_CHARS, sut.token());
        Assert.assertEquals("'abc'", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("N'ab\\'c'");
        Assert.assertEquals(MySQLToken.LITERAL_NCHARS, sut.token());
        Assert.assertEquals("'ab\\'c'", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("  \'abc\\\\\\'\' 'abc\\a\\'\''\"\"'/\"abc\\\"\".\"\"\"abc\"\"\"\"'\''\"n'ab\\'c'");
        Assert.assertEquals(MySQLToken.LITERAL_CHARS, sut.token());
        Assert.assertEquals("'abc\\\\\\''", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.LITERAL_CHARS, sut.token());
        Assert.assertEquals("'abc\\a\\'\\'\"\"'", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.OP_SLASH, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.LITERAL_CHARS, sut.token());
        Assert.assertEquals("'abc\\\"'", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.PUNC_DOT, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.LITERAL_CHARS, sut.token());
        Assert.assertEquals("'\"abc\"\"\\'\\'\\''", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.LITERAL_NCHARS, sut.token());
        Assert.assertEquals("'ab\\'c'", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

    }

    public void testHexBit() throws SQLSyntaxErrorException {
        MySQLLexer sut = new MySQLLexer("0x123  ");
        Assert.assertEquals(MySQLToken.LITERAL_HEX, sut.token());
        Assert.assertEquals("123", new String(sut.getSQL(), sut.getOffsetCache(), sut.getSizeCache()));
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("0x123");
        Assert.assertEquals(MySQLToken.LITERAL_HEX, sut.token());
        Assert.assertEquals("123", new String(sut.getSQL(), sut.getOffsetCache(), sut.getSizeCache()));
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("0x123aDef");
        Assert.assertEquals(MySQLToken.LITERAL_HEX, sut.token());
        Assert.assertEquals("123aDef", new String(sut.getSQL(), sut.getOffsetCache(), sut.getSizeCache()));
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("0x0");
        Assert.assertEquals(MySQLToken.LITERAL_HEX, sut.token());
        Assert.assertEquals("0", new String(sut.getSQL(), sut.getOffsetCache(), sut.getSizeCache()));
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("0xABC");
        Assert.assertEquals(MySQLToken.LITERAL_HEX, sut.token());
        Assert.assertEquals("ABC", new String(sut.getSQL(), sut.getOffsetCache(), sut.getSizeCache()));
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("0xA01aBC");
        Assert.assertEquals(MySQLToken.LITERAL_HEX, sut.token());
        Assert.assertEquals("A01aBC", new String(sut.getSQL(), sut.getOffsetCache(), sut.getSizeCache()));
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("0x123re2  ");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("0x123re2", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("x'123'e  ");
        Assert.assertEquals(MySQLToken.LITERAL_HEX, sut.token());
        Assert.assertEquals("123", new String(sut.getSQL(), sut.getOffsetCache(), sut.getSizeCache()));
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("e", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("x'123'");
        Assert.assertEquals(MySQLToken.LITERAL_HEX, sut.token());
        Assert.assertEquals("123", new String(sut.getSQL(), sut.getOffsetCache(), sut.getSizeCache()));
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("x'102AaeF3'");
        Assert.assertEquals(MySQLToken.LITERAL_HEX, sut.token());
        Assert.assertEquals("102AaeF3", new String(sut.getSQL(), sut.getOffsetCache(), sut.getSizeCache()));
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("0b10");
        Assert.assertEquals(MySQLToken.LITERAL_BIT, sut.token());
        Assert.assertEquals("10", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("0b101101");
        Assert.assertEquals(MySQLToken.LITERAL_BIT, sut.token());
        Assert.assertEquals("101101", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("0b103  ");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("0b103", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("b'10'b  ");
        Assert.assertEquals(MySQLToken.LITERAL_BIT, sut.token());
        Assert.assertEquals("10", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("b", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("\r 0xabc.123;x'e'a0x1.3x'a2w'--\t0b11\n0b12*b '123' b'101'");
        Assert.assertEquals(MySQLToken.LITERAL_HEX, sut.token());
        Assert.assertEquals("abc", new String(sut.getSQL(), sut.getOffsetCache(), sut.getSizeCache()));
        sut.nextToken();
        Assert.assertEquals(MySQLToken.LITERAL_NUM_MIX_DIGIT, sut.token());
        Assert.assertEquals("0.123", sut.decimalValue().toPlainString());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.PUNC_SEMICOLON, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.LITERAL_HEX, sut.token());
        Assert.assertEquals("e", new String(sut.getSQL(), sut.getOffsetCache(), sut.getSizeCache()));
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("a0x1", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.PUNC_DOT, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("3x", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.LITERAL_CHARS, sut.token());
        Assert.assertEquals("'a2w'", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("0b12", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.OP_ASTERISK, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("b", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.LITERAL_CHARS, sut.token());
        Assert.assertEquals("'123'", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.LITERAL_BIT, sut.token());
        Assert.assertEquals("101", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());
    }

    public void testNumber() throws SQLSyntaxErrorException {
        MySQLLexer sut = new MySQLLexer(
                " . 12e3/***/.12e3#/**\n.123ee123.1--  \r\t\n.12e/*a*//* !*/.12e_a/12e-- \r\t.12e-1");
        Assert.assertEquals(MySQLToken.PUNC_DOT, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.LITERAL_NUM_MIX_DIGIT, sut.token());
        Assert.assertEquals("12000", sut.decimalValue().toPlainString());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.LITERAL_NUM_MIX_DIGIT, sut.token());
        Assert.assertEquals("120", sut.decimalValue().toPlainString());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.PUNC_DOT, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("123ee123", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.LITERAL_NUM_MIX_DIGIT, sut.token());
        Assert.assertEquals("0.1", sut.decimalValue().toPlainString());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.PUNC_DOT, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("12e", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.PUNC_DOT, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("12e_a", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.OP_SLASH, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("12e", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer(".12e-1  ");
        Assert.assertEquals("0.012", sut.decimalValue().toPlainString());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("12e000000000000000  ");
        Assert.assertEquals("12", sut.decimalValue().toPlainString());

        sut = new MySQLLexer(".12e-  ");
        Assert.assertEquals(MySQLToken.PUNC_DOT, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("12e", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.OP_MINUS, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer(".12e-1d  ");
        Assert.assertEquals(MySQLToken.PUNC_DOT, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("12e", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.OP_MINUS, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("1d", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("12.e+1d  ");
        Assert.assertEquals(MySQLToken.LITERAL_NUM_MIX_DIGIT, sut.token());
        Assert.assertEquals("120", sut.decimalValue().toPlainString());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("d", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("12.f ");
        Assert.assertEquals(MySQLToken.LITERAL_NUM_MIX_DIGIT, sut.token());
        Assert.assertEquals("12", sut.decimalValue().toPlainString());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("f", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer(".12f ");
        Assert.assertEquals(MySQLToken.PUNC_DOT, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("12f", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("1.2f ");
        Assert.assertEquals(MySQLToken.LITERAL_NUM_MIX_DIGIT, sut.token());
        Assert.assertEquals("1.2", sut.decimalValue().toPlainString());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("f", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        try {
            sut = new MySQLLexer("12.e ");
            Assert.assertFalse("should not reach here", true);
        } catch (SQLSyntaxErrorException e) {
        }

        sut = new MySQLLexer("0e  ");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("0e", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("12. e  ");
        Assert.assertEquals(MySQLToken.LITERAL_NUM_MIX_DIGIT, sut.token());
        Assert.assertEquals("12", sut.decimalValue().toPlainString());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("e", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("12. e+1  ");
        Assert.assertEquals(MySQLToken.LITERAL_NUM_MIX_DIGIT, sut.token());
        Assert.assertEquals("12", sut.decimalValue().toPlainString());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("e", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.OP_PLUS, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.LITERAL_NUM_PURE_DIGIT, sut.token());
        Assert.assertEquals("1", sut.integerValue().toString());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("12.e+1  ");
        Assert.assertEquals(MySQLToken.LITERAL_NUM_MIX_DIGIT, sut.token());
        Assert.assertEquals("120", sut.decimalValue().toPlainString());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("12.");
        Assert.assertEquals(MySQLToken.LITERAL_NUM_MIX_DIGIT, sut.token());
        Assert.assertEquals("12", sut.decimalValue().toPlainString());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer(".12");
        Assert.assertEquals(MySQLToken.LITERAL_NUM_MIX_DIGIT, sut.token());
        Assert.assertEquals("0.12", sut.decimalValue().toPlainString());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("12e");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("12e", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("12ef");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("12ef", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer(".12e");
        Assert.assertEquals(MySQLToken.PUNC_DOT, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("12e", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("1.0e0");
        Assert.assertEquals(MySQLToken.LITERAL_NUM_MIX_DIGIT, sut.token());
        Assert.assertEquals("1.0", sut.decimalValue().toPlainString());

        sut = new MySQLLexer("1.01e0,");
        Assert.assertEquals(MySQLToken.LITERAL_NUM_MIX_DIGIT, sut.token());
        Assert.assertEquals("1.01", sut.decimalValue().toPlainString());

        sut = new MySQLLexer(".12e-");
        Assert.assertEquals(MySQLToken.PUNC_DOT, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("12e", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.OP_MINUS, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer(".12e-d");
        Assert.assertEquals(MySQLToken.PUNC_DOT, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("12e", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.OP_MINUS, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("d", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("123E2.*");
        Assert.assertEquals(MySQLToken.LITERAL_NUM_MIX_DIGIT, sut.token());
        Assert.assertEquals("12300", sut.decimalValue().toPlainString());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.PUNC_DOT, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.OP_ASTERISK, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("1e-1  ");
        Assert.assertEquals("0.1", sut.decimalValue().toPlainString());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer(".E5");
        Assert.assertEquals(MySQLToken.PUNC_DOT, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("E5", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("0E5d");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("0E5d", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("0E10");
        Assert.assertEquals(MySQLToken.LITERAL_NUM_MIX_DIGIT, sut.token());
        Assert.assertEquals("0", sut.decimalValue().toPlainString());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer(".   ");
        Assert.assertEquals(MySQLToken.PUNC_DOT, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("12345678901234567890123 1234567890 1234567890123456789");
        Assert.assertEquals(MySQLToken.LITERAL_NUM_PURE_DIGIT, sut.token());
        Assert.assertEquals("12345678901234567890123", String.valueOf(sut.integerValue()));
        sut.nextToken();
        Assert.assertEquals(MySQLToken.LITERAL_NUM_PURE_DIGIT, sut.token());
        Assert.assertEquals("1234567890", String.valueOf(sut.integerValue()));
        sut.nextToken();
        Assert.assertEquals(MySQLToken.LITERAL_NUM_PURE_DIGIT, sut.token());
        Assert.assertEquals("1234567890123456789", String.valueOf(sut.integerValue()));
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer(".");
        Assert.assertEquals(MySQLToken.PUNC_DOT, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());
    }

    public void testSkipSeparator() throws SQLSyntaxErrorException {
        MySQLLexer sut = new MySQLLexer("  /**//***/ \t\n\r\n -- \n#\n/*/*-- \n*/");
        Assert.assertEquals(sut.eofIndex, sut.curIndex);
        Assert.assertEquals(sut.sql[sut.eofIndex], sut.ch);

    }

    public void testCStyleComment() throws SQLSyntaxErrorException {
        MySQLLexer sut = new MySQLLexer("id1 /*!id2 */ id3");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id1", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id2", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id3", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("id1 /*! id2 */ id3");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id1", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id2", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id3", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        sut = new MySQLLexer("id1 /*!*/ id3");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id1", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id3", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

        int version = MySQLLexer.setCStyleCommentVersion(40000);
        sut = new MySQLLexer("id1 /*!40001id2*/ id3");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id1", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id3", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());
        MySQLLexer.setCStyleCommentVersion(version);

        version = MySQLLexer.setCStyleCommentVersion(40000);
        sut = new MySQLLexer("id1 /*!40000id2*/ id3");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id1", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id2", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id3", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());
        MySQLLexer.setCStyleCommentVersion(version);

        version = MySQLLexer.setCStyleCommentVersion(40000);
        sut = new MySQLLexer("id1 /*!4000id2*/ id3");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id1", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("4000id2", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id3", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());
        MySQLLexer.setCStyleCommentVersion(version);

        version = MySQLLexer.setCStyleCommentVersion(40000);
        sut = new MySQLLexer("id1 /*!400001id2*/ id3");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id1", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("1id2", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id3", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());
        MySQLLexer.setCStyleCommentVersion(version);

        version = MySQLLexer.setCStyleCommentVersion(40000);
        sut = new MySQLLexer("id1 /*!400011id2*/ id3");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id1", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id3", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());
        MySQLLexer.setCStyleCommentVersion(version);

        version = MySQLLexer.setCStyleCommentVersion(40000);
        sut = new MySQLLexer("id1 /*!4000*/ id3");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id1", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.LITERAL_NUM_PURE_DIGIT, sut.token());
        Assert.assertEquals("4000", String.valueOf(sut.integerValue()));
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id3", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());
        MySQLLexer.setCStyleCommentVersion(version);

        version = MySQLLexer.setCStyleCommentVersion(40000);
        sut = new MySQLLexer("id1 /*!40000*/ id3");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id1", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id3", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());
        MySQLLexer.setCStyleCommentVersion(version);

        version = MySQLLexer.setCStyleCommentVersion(40000);
        sut = new MySQLLexer("id1 /*!40001*/ id3");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id1", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id3", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());
        MySQLLexer.setCStyleCommentVersion(version);

        version = MySQLLexer.setCStyleCommentVersion(40000);
        sut = new MySQLLexer("id1 /*!400001*/ id3");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id1", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.LITERAL_NUM_PURE_DIGIT, sut.token());
        Assert.assertEquals("1", String.valueOf(sut.integerValue()));
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id3", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());
        MySQLLexer.setCStyleCommentVersion(version);

        version = MySQLLexer.setCStyleCommentVersion(40000);
        sut = new MySQLLexer("id1 /*!40000 -- id2\n*/ id3");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id1", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id3", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());
        MySQLLexer.setCStyleCommentVersion(version);

        version = MySQLLexer.setCStyleCommentVersion(40000);
        sut = new MySQLLexer("id1 /*!40000 /* id2*/*/ id3");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id1", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id3", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());
        MySQLLexer.setCStyleCommentVersion(version);

        version = MySQLLexer.setCStyleCommentVersion(40000);
        sut = new MySQLLexer("id1 /*!40000/* id2*/*/ id3");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id1", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id3", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());
        MySQLLexer.setCStyleCommentVersion(version);

        version = MySQLLexer.setCStyleCommentVersion(40000);
        sut = new MySQLLexer("id1 /*!40000id2*/* id3");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id1", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id2", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.OP_ASTERISK, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id3", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());
        MySQLLexer.setCStyleCommentVersion(version);

        version = MySQLLexer.setCStyleCommentVersion(40000);
        sut = new MySQLLexer("id1 /*!40000/*/*/id2*/ id3");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id1", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id2", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id3", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());
        MySQLLexer.setCStyleCommentVersion(version);

        version = MySQLLexer.setCStyleCommentVersion(40000);
        sut = new MySQLLexer("id1 /*!40001/*/*/id2*/ id3");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id1", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id3", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());
        MySQLLexer.setCStyleCommentVersion(version);

        version = MySQLLexer.setCStyleCommentVersion(40000);
        sut = new MySQLLexer("id1 /*!40000/*/*/id2*/ id3");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id1", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id2", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id3", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());
        MySQLLexer.setCStyleCommentVersion(version);

        version = MySQLLexer.setCStyleCommentVersion(40000);
        sut = new MySQLLexer("id1 /*!40000/*/*/id2*/ /*!40000 id4*/ id3");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id1", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id2", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id4", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id3", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());
        MySQLLexer.setCStyleCommentVersion(version);

        version = MySQLLexer.setCStyleCommentVersion(40000);
        sut = new MySQLLexer("id1 /*!40001/*/*/id2*/ /*!40000 id4*/ id3");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id1", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id4", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id3", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());
        MySQLLexer.setCStyleCommentVersion(version);

        version = MySQLLexer.setCStyleCommentVersion(40000);
        sut = new MySQLLexer("id1 /*!40000/*/*/id2*/ /*!40001 id4*/ id3");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id1", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id2", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id3", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());
        MySQLLexer.setCStyleCommentVersion(version);

        version = MySQLLexer.setCStyleCommentVersion(40000);
        sut = new MySQLLexer("id1 /*!40000/*/*/id2*//*!40001 id4*//*!40001 id5*/ id3");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id1", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id2", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id3", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());
        MySQLLexer.setCStyleCommentVersion(version);

        version = MySQLLexer.setCStyleCommentVersion(40000);
        sut = new MySQLLexer("id1 /*!40000/*/*/id2*//*!40001 id4*//*!40000id5*/ id3");
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id1", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id2", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id5", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("id3", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());
        MySQLLexer.setCStyleCommentVersion(version);

    }

    public void testLexer() throws SQLSyntaxErrorException {
        MySQLLexer sut = new MySQLLexer(" @a.1_$ .1e+1a%x'a1e'*0b11a \r#\"\"\n@@`123`@@'abc'1.e-1d`/`1.1e1.1e1");
        Assert.assertEquals(MySQLToken.USR_VAR, sut.token());
        Assert.assertEquals("@a.1_$", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.PUNC_DOT, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("1e", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.OP_PLUS, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("1a", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.OP_PERCENT, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.LITERAL_HEX, sut.token());
        Assert.assertEquals("a1e", new String(sut.getSQL(), sut.getOffsetCache(), sut.getSizeCache()));
        sut.nextToken();
        Assert.assertEquals(MySQLToken.OP_ASTERISK, sut.token());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("0b11a", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.SYS_VAR, sut.token());
        Assert.assertEquals("`123`", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.SYS_VAR, sut.token());
        Assert.assertEquals("", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.LITERAL_CHARS, sut.token());
        Assert.assertEquals("'abc'", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.LITERAL_NUM_MIX_DIGIT, sut.token());
        Assert.assertEquals("0.1", sut.decimalValue().toPlainString());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("d", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.IDENTIFIER, sut.token());
        Assert.assertEquals("`/`", sut.stringValue());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.LITERAL_NUM_MIX_DIGIT, sut.token());
        Assert.assertEquals("11", sut.decimalValue().toPlainString());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.LITERAL_NUM_MIX_DIGIT, sut.token());
        Assert.assertEquals("1", sut.decimalValue().toPlainString());
        sut.nextToken();
        Assert.assertEquals(MySQLToken.EOF, sut.token());

    }
}
