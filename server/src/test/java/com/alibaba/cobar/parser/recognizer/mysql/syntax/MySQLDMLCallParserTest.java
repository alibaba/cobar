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
 * (created at 2011-7-18)
 */
package com.alibaba.cobar.parser.recognizer.mysql.syntax;

import java.sql.SQLSyntaxErrorException;

import org.junit.Assert;

import com.alibaba.cobar.parser.ast.stmt.dml.DMLCallStatement;
import com.alibaba.cobar.parser.recognizer.mysql.MySQLToken;
import com.alibaba.cobar.parser.recognizer.mysql.lexer.MySQLLexer;

/**
 * @author <a href="mailto:danping.yudp@alibaba-inc.com">YU Danping</a>
 */

public class MySQLDMLCallParserTest extends AbstractSyntaxTest {
    public void testCall() throws SQLSyntaxErrorException {
        String sql = "call p(?,?) ";
        MySQLLexer lexer = new MySQLLexer(sql);
        MySQLDMLCallParser parser = new MySQLDMLCallParser(lexer, new MySQLExprParser(lexer));
        DMLCallStatement calls = parser.call();
        parser.match(MySQLToken.EOF);
        String output = output2MySQL(calls, sql);
        Assert.assertEquals("CALL p(?, ?)", output);

        sql = "call p(@var1,'@var2',var3)";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDMLCallParser(lexer, new MySQLExprParser(lexer));
        calls = parser.call();
        parser.match(MySQLToken.EOF);
        output = output2MySQL(calls, sql);
        Assert.assertEquals("CALL p(@var1, '@var2', var3)", output);

        sql = "call p()";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDMLCallParser(lexer, new MySQLExprParser(lexer));
        calls = parser.call();
        parser.match(MySQLToken.EOF);
        output = output2MySQL(calls, sql);
        Assert.assertEquals("CALL p()", output);

        sql = "call p(?)";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDMLCallParser(lexer, new MySQLExprParser(lexer));
        calls = parser.call();
        parser.match(MySQLToken.EOF);
        output = output2MySQL(calls, sql);
        Assert.assertEquals("CALL p(?)", output);
    }
}
