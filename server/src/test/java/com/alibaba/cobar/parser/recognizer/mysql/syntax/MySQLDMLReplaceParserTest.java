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

import java.sql.SQLSyntaxErrorException;

import org.junit.Assert;

import com.alibaba.cobar.parser.ast.stmt.dml.DMLReplaceStatement;
import com.alibaba.cobar.parser.recognizer.mysql.MySQLToken;
import com.alibaba.cobar.parser.recognizer.mysql.lexer.MySQLLexer;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class MySQLDMLReplaceParserTest extends AbstractSyntaxTest {
    public void testReplace() throws SQLSyntaxErrorException {
        String sql = "ReplaCe LOW_PRIORITY intO test.t1 seT t1.id1:=?, id2='123'";
        MySQLLexer lexer = new MySQLLexer(sql);
        MySQLDMLReplaceParser parser = new MySQLDMLReplaceParser(lexer, new MySQLExprParser(lexer));
        DMLReplaceStatement replace = parser.replace();
        parser.match(MySQLToken.EOF);
        Assert.assertNotNull(replace);
        String output = output2MySQL(replace, sql);
        Assert.assertEquals("REPLACE LOW_PRIORITY INTO test.t1 (t1.id1, id2) VALUES (?, '123')", output);

        sql = "ReplaCe   test.t1 seT t1.id1:=? ";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDMLReplaceParser(lexer, new MySQLExprParser(lexer));
        replace = parser.replace();
        parser.match(MySQLToken.EOF);
        output = output2MySQL(replace, sql);
        Assert.assertEquals("REPLACE INTO test.t1 (t1.id1) VALUES (?)", output);

        sql = "ReplaCe t1 value (123,?) ";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDMLReplaceParser(lexer, new MySQLExprParser(lexer));
        replace = parser.replace();
        parser.match(MySQLToken.EOF);
        output = output2MySQL(replace, sql);
        Assert.assertEquals("REPLACE INTO t1 VALUES (123, ?)", output);

        sql = "ReplaCe LOW_PRIORITY t1 valueS (12e-2), (?)";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDMLReplaceParser(lexer, new MySQLExprParser(lexer));
        replace = parser.replace();
        parser.match(MySQLToken.EOF);
        output = output2MySQL(replace, sql);
        Assert.assertEquals("REPLACE LOW_PRIORITY INTO t1 VALUES (0.12), (?)", output);

        sql = "ReplaCe LOW_PRIORITY t1 select id from t1";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDMLReplaceParser(lexer, new MySQLExprParser(lexer));
        replace = parser.replace();
        parser.match(MySQLToken.EOF);
        output = output2MySQL(replace, sql);
        Assert.assertEquals("REPLACE LOW_PRIORITY INTO t1 SELECT id FROM t1", output);

        sql = "ReplaCe delayed t1 select id from t1";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDMLReplaceParser(lexer, new MySQLExprParser(lexer));
        replace = parser.replace();
        parser.match(MySQLToken.EOF);
        output = output2MySQL(replace, sql);
        Assert.assertEquals("REPLACE DELAYED INTO t1 SELECT id FROM t1", output);

        sql = "ReplaCe LOW_PRIORITY t1 (select id from t1) ";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDMLReplaceParser(lexer, new MySQLExprParser(lexer));
        replace = parser.replace();
        parser.match(MySQLToken.EOF);
        output = output2MySQL(replace, sql);
        Assert.assertEquals("REPLACE LOW_PRIORITY INTO t1 SELECT id FROM t1", output);

        sql = "ReplaCe LOW_PRIORITY t1 (t1.col1) valueS (123),('12''34')";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDMLReplaceParser(lexer, new MySQLExprParser(lexer));
        replace = parser.replace();
        parser.match(MySQLToken.EOF);
        output = output2MySQL(replace, sql);
        Assert.assertEquals("REPLACE LOW_PRIORITY INTO t1 (t1.col1) VALUES (123), ('12\\'34')", output);

        sql = "ReplaCe LOW_PRIORITY t1 (col1, t1.col2) VALUE (123,'123\\'4') ";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDMLReplaceParser(lexer, new MySQLExprParser(lexer));
        replace = parser.replace();
        parser.match(MySQLToken.EOF);
        output = output2MySQL(replace, sql);
        Assert.assertEquals("REPLACE LOW_PRIORITY INTO t1 (col1, t1.col2) VALUES (123, '123\\'4')", output);

        sql = "REPLACE LOW_PRIORITY t1 (col1, t1.col2) select id from t3 ";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDMLReplaceParser(lexer, new MySQLExprParser(lexer));
        replace = parser.replace();
        parser.match(MySQLToken.EOF);
        output = output2MySQL(replace, sql);
        Assert.assertEquals("REPLACE LOW_PRIORITY INTO t1 (col1, t1.col2) SELECT id FROM t3", output);

        sql = "replace LOW_PRIORITY  intO t1 (col1) ( select id from t3) ";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDMLReplaceParser(lexer, new MySQLExprParser(lexer));
        replace = parser.replace();
        parser.match(MySQLToken.EOF);
        output = output2MySQL(replace, sql);
        Assert.assertEquals("REPLACE LOW_PRIORITY INTO t1 (col1) SELECT id FROM t3", output);

    }
}
