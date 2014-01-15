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

import junit.framework.Assert;

import com.alibaba.cobar.parser.ast.stmt.dml.DMLUpdateStatement;
import com.alibaba.cobar.parser.recognizer.mysql.lexer.MySQLLexer;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class MySQLDMLUpdateParserTest extends AbstractSyntaxTest {
    /**
     * nothing has been pre-consumed <code><pre>
     * 'UPDATE' 'LOW_PRIORITY'? 'IGNORE'? table_reference
     *   'SET' colName ('='|'=') (expr|'DEFAULT') (',' colName ('='|'=') (expr|'DEFAULT'))*
     *     ('WHERE' cond)?
     *     {singleTable}? => ('ORDER' 'BY' orderBy)?  ('LIMIT' count)?
     * </pre></code>
     */

    public void testUpdate() throws SQLSyntaxErrorException {
        String sql = "upDate LOw_PRIORITY IGNORE test.t1 sEt t1.col1=?, col2=DefaulT";
        MySQLLexer lexer = new MySQLLexer(sql);
        MySQLDMLUpdateParser parser = new MySQLDMLUpdateParser(lexer, new MySQLExprParser(lexer));
        DMLUpdateStatement update = parser.update();
        String output = output2MySQL(update, sql);
        Assert.assertNotNull(update);
        Assert.assertEquals("UPDATE LOW_PRIORITY IGNORE test.t1 SET t1.col1 = ?, col2 = DEFAULT", output);

        sql = "upDate  IGNORE (t1) set col2=DefaulT order bY t1.col2 ";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDMLUpdateParser(lexer, new MySQLExprParser(lexer));
        update = parser.update();
        output = output2MySQL(update, sql);
        Assert.assertEquals("UPDATE IGNORE t1 SET col2 = DEFAULT ORDER BY t1.col2", output);

        sql = "upDate   (test.t1) SET col2=DefaulT order bY t1.col2 limit ? offset 1";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDMLUpdateParser(lexer, new MySQLExprParser(lexer));
        update = parser.update();
        output = output2MySQL(update, sql);
        Assert.assertEquals("UPDATE test.t1 SET col2 = DEFAULT ORDER BY t1.col2 LIMIT 1, ?", output);

        sql = "upDate LOW_PRIORITY  t1, test.t2 SET col2=DefaulT , col2='123''4'";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDMLUpdateParser(lexer, new MySQLExprParser(lexer));
        update = parser.update();
        output = output2MySQL(update, sql);
        Assert.assertEquals("UPDATE LOW_PRIORITY t1, test.t2 SET col2 = DEFAULT, col2 = '123\\'4'", output);

        sql = "upDate LOW_PRIORITY  t1, test.t2 SET col2:=DefaulT , col2='123''4' where id='a'";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDMLUpdateParser(lexer, new MySQLExprParser(lexer));
        update = parser.update();
        output = output2MySQL(update, sql);
        Assert.assertEquals(
                "UPDATE LOW_PRIORITY t1, test.t2 SET col2 = DEFAULT, col2 = '123\\'4' WHERE id = 'a'",
                output);

    }
}
