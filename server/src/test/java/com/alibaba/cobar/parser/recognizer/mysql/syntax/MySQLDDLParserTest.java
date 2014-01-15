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

import org.junit.Assert;

import com.alibaba.cobar.parser.ast.stmt.ddl.DDLStatement;
import com.alibaba.cobar.parser.ast.stmt.ddl.DDLTruncateStatement;
import com.alibaba.cobar.parser.recognizer.mysql.MySQLToken;
import com.alibaba.cobar.parser.recognizer.mysql.lexer.MySQLLexer;

/**
 * @author <a href="mailto:danping.yudp@alibaba-inc.com">YU Danping</a>
 */
public class MySQLDDLParserTest extends AbstractSyntaxTest {

    public void testTruncate() throws Exception {
        String sql = "Truncate table tb1";
        MySQLLexer lexer = new MySQLLexer(sql);
        MySQLDDLParser parser = new MySQLDDLParser(lexer, new MySQLExprParser(lexer));
        DDLStatement trun = (DDLTruncateStatement) parser.truncate();
        parser.match(MySQLToken.EOF);
        String output = output2MySQL(trun, sql);
        Assert.assertEquals("TRUNCATE TABLE tb1", output);

        sql = "Truncate tb1";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDDLParser(lexer, new MySQLExprParser(lexer));
        trun = (DDLTruncateStatement) parser.truncate();
        parser.match(MySQLToken.EOF);
        output = output2MySQL(trun, sql);
        Assert.assertEquals("TRUNCATE TABLE tb1", output);
    }

    public void testDDLStmt() throws Exception {
        String sql = "alTer ignore table tb_name";
        MySQLLexer lexer = new MySQLLexer(sql);
        MySQLDDLParser parser = new MySQLDDLParser(lexer, new MySQLExprParser(lexer));
        DDLStatement dst = parser.ddlStmt();

        sql = "alTeR table tb_name";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDDLParser(lexer, new MySQLExprParser(lexer));
        dst = parser.ddlStmt();

        sql = "crEate temporary tabLe if not exists tb_name";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDDLParser(lexer, new MySQLExprParser(lexer));
        dst = parser.ddlStmt();

        sql = "crEate tabLe if not exists tb_name";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDDLParser(lexer, new MySQLExprParser(lexer));
        dst = parser.ddlStmt();

        sql = "crEate temporary tabLe tb_name";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDDLParser(lexer, new MySQLExprParser(lexer));
        dst = parser.ddlStmt();

        sql = "crEate unique index index_name on tb(col(id)) desc";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDDLParser(lexer, new MySQLExprParser(lexer));
        dst = parser.ddlStmt();

        sql = "crEate fulltext index index_name on tb(col(id))";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDDLParser(lexer, new MySQLExprParser(lexer));
        dst = parser.ddlStmt();

        sql = "crEate spatial index index_name on tb(col(id))";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDDLParser(lexer, new MySQLExprParser(lexer));
        dst = parser.ddlStmt();

        sql = "crEate index index_name using hash on tb(col(id))";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDDLParser(lexer, new MySQLExprParser(lexer));
        dst = parser.ddlStmt();

        sql = "drop index index_name on tb1";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDDLParser(lexer, new MySQLExprParser(lexer));
        dst = parser.ddlStmt();
        String output = output2MySQL(dst, sql);
        Assert.assertEquals("DROP INDEX index_name ON tb1", output);

        sql = "drop temporary tabLe if exists tb1,tb2,tb3 restrict";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDDLParser(lexer, new MySQLExprParser(lexer));
        dst = parser.ddlStmt();
        output = output2MySQL(dst, sql);
        Assert.assertEquals("DROP TEMPORARY TABLE IF EXISTS tb1, tb2, tb3 RESTRICT", output);

        sql = "drop temporary tabLe if exists tb1,tb2,tb3 cascade";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDDLParser(lexer, new MySQLExprParser(lexer));
        dst = parser.ddlStmt();
        output = output2MySQL(dst, sql);
        Assert.assertEquals("DROP TEMPORARY TABLE IF EXISTS tb1, tb2, tb3 CASCADE", output);

        sql = "drop temporary tabLe if exists tb1 cascade";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDDLParser(lexer, new MySQLExprParser(lexer));
        dst = parser.ddlStmt();
        output = output2MySQL(dst, sql);
        Assert.assertEquals("DROP TEMPORARY TABLE IF EXISTS tb1 CASCADE", output);

        sql = "drop tabLe if exists tb1 cascade";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDDLParser(lexer, new MySQLExprParser(lexer));
        dst = parser.ddlStmt();
        output = output2MySQL(dst, sql);
        Assert.assertEquals("DROP TABLE IF EXISTS tb1 CASCADE", output);

        sql = "drop temporary tabLe tb1 cascade";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDDLParser(lexer, new MySQLExprParser(lexer));
        dst = parser.ddlStmt();
        output = output2MySQL(dst, sql);
        Assert.assertEquals("DROP TEMPORARY TABLE tb1 CASCADE", output);

        sql = "rename table tb1 to ntb1,tb2 to ntb2";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDDLParser(lexer, new MySQLExprParser(lexer));
        dst = parser.ddlStmt();
        output = output2MySQL(dst, sql);
        Assert.assertEquals("RENAME TABLE tb1 TO ntb1, tb2 TO ntb2", output);

        sql = "rename table tb1 to ntb1";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDDLParser(lexer, new MySQLExprParser(lexer));
        dst = parser.ddlStmt();
        output = output2MySQL(dst, sql);
        Assert.assertEquals("RENAME TABLE tb1 TO ntb1", output);
    }
}
