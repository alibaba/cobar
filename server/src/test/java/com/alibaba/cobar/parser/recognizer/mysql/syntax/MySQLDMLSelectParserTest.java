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
 * (created at 2011-5-17)
 */
package com.alibaba.cobar.parser.recognizer.mysql.syntax;

import java.sql.SQLSyntaxErrorException;

import org.junit.Assert;

import com.alibaba.cobar.parser.Performance;
import com.alibaba.cobar.parser.ast.expression.misc.QueryExpression;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLSelectStatement;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLSelectUnionStatement;
import com.alibaba.cobar.parser.recognizer.mysql.lexer.MySQLLexer;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class MySQLDMLSelectParserTest extends AbstractSyntaxTest {

    @SuppressWarnings("unused")
    public static void main(String[] ars) throws Exception {
        String sql = Performance.SQL_BENCHMARK_SELECT;
        for (int i = 0; i < 3; ++i) {
            MySQLLexer lexer = new MySQLLexer(sql);
            MySQLExprParser exprParser = new MySQLExprParser(lexer);
            MySQLDMLSelectParser parser = new MySQLDMLSelectParser(lexer, exprParser);
            QueryExpression stmt = parser.select();
            // System.out.println(stmt);
        }
        Thread.sleep(1000);

        long loop = 300 * 10000;
        long t1 = System.currentTimeMillis();
        t1 = System.currentTimeMillis();
        for (long i = 0; i < loop; ++i) {
            MySQLLexer lexer = new MySQLLexer(sql);
            MySQLExprParser exprParser = new MySQLExprParser(lexer);
            MySQLDMLSelectParser parser = new MySQLDMLSelectParser(lexer, exprParser);
            QueryExpression stmt = parser.select();
        }
        long t2 = System.currentTimeMillis();
        System.out.println((t2 - t1) * 1000.0d / loop + " us");
    }

    public void testSelectUnion() throws SQLSyntaxErrorException {
        String sql = "(select id from t1) union all (select id from t2) union all (select id from t3) ordeR By d desC limit 1 offset ?";
        MySQLLexer lexer = new MySQLLexer(sql);
        MySQLDMLSelectParser parser = new MySQLDMLSelectParser(lexer, new MySQLExprParser(lexer));
        DMLSelectUnionStatement select = (DMLSelectUnionStatement) parser.selectUnion();
        Assert.assertEquals(0, select.getFirstDistinctIndex());
        Assert.assertEquals(3, select.getSelectStmtList().size());
        String output = output2MySQL(select, sql);
        Assert.assertEquals(
                "(SELECT id FROM t1) UNION ALL (SELECT id FROM t2) UNION ALL (SELECT id FROM t3) ORDER BY d DESC LIMIT ?, 1",
                output);

        sql = "(select id from t1) union  select id from t2 order by id union aLl (select id from t3) ordeR By d asC";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDMLSelectParser(lexer, new MySQLExprParser(lexer));
        select = (DMLSelectUnionStatement) parser.selectUnion();
        Assert.assertEquals(1, select.getFirstDistinctIndex());
        Assert.assertEquals(3, select.getSelectStmtList().size());
        output = output2MySQL(select, sql);
        Assert.assertEquals(
                "(SELECT id FROM t1) UNION (SELECT id FROM t2 ORDER BY id) UNION ALL (SELECT id FROM t3) ORDER BY d",
                output);

        sql = "(select id from t1) union distInct (select id from t2) union  select id from t3";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDMLSelectParser(lexer, new MySQLExprParser(lexer));
        select = (DMLSelectUnionStatement) parser.selectUnion();
        Assert.assertEquals(2, select.getFirstDistinctIndex());
        Assert.assertEquals(3, select.getSelectStmtList().size());
        output = output2MySQL(select, sql);
        Assert.assertEquals("(SELECT id FROM t1) UNION (SELECT id FROM t2) UNION (SELECT id FROM t3)", output);

    }

    public void testSelect() throws SQLSyntaxErrorException {
        String sql = "SELect t1.id , t2.* from t1, test.t2 where test.t1.id=1 and t1.id=test.t2.id";
        MySQLLexer lexer = new MySQLLexer(sql);
        MySQLDMLSelectParser parser = new MySQLDMLSelectParser(lexer, new MySQLExprParser(lexer));
        DMLSelectStatement select = parser.select();
        Assert.assertNotNull(select);
        String output = output2MySQL(select, sql);
        Assert.assertEquals("SELECT t1.id, t2.* FROM t1, test.t2 WHERE test.t1.id = 1 AND t1.id = test.t2.id", output);

        sql = "select * from  offer  a  straight_join wp_image b use key for join(t1,t2) on a.member_id=b.member_id inner join product_visit c where a.member_id=c.member_id and c.member_id='abc' ";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDMLSelectParser(lexer, new MySQLExprParser(lexer));
        select = parser.select();
        Assert.assertNotNull(select);
        output = output2MySQL(select, sql);
        Assert.assertEquals(
                "SELECT * FROM offer AS A STRAIGHT_JOIN wp_image AS B USE KEY FOR JOIN (t1, t2) ON a.member_id = b.member_id INNER JOIN product_visit AS C WHERE a.member_id = c.member_id AND c.member_id = 'abc'",
                output);

        sql = "SELect all tb1.id,tb2.id from tb1,tb2 where tb1.id2=tb2.id2";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDMLSelectParser(lexer, new MySQLExprParser(lexer));
        select = parser.select();
        Assert.assertNotNull(select);
        output = output2MySQL(select, sql);
        Assert.assertEquals("SELECT tb1.id, tb2.id FROM tb1, tb2 WHERE tb1.id2 = tb2.id2", output);

        sql = "SELect distinct high_priority tb1.id,tb2.id from tb1,tb2 where tb1.id2=tb2.id2";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDMLSelectParser(lexer, new MySQLExprParser(lexer));
        select = parser.select();
        Assert.assertNotNull(select);
        output = output2MySQL(select, sql);
        Assert.assertEquals(
                "SELECT DISTINCT HIGH_PRIORITY tb1.id, tb2.id FROM tb1, tb2 WHERE tb1.id2 = tb2.id2",
                output);

        sql = "SELect distinctrow high_priority sql_small_result tb1.id,tb2.id " + "from tb1,tb2 where tb1.id2=tb2.id2";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDMLSelectParser(lexer, new MySQLExprParser(lexer));
        select = parser.select();
        Assert.assertNotNull(select);
        output = output2MySQL(select, sql);
        Assert.assertEquals(
                "SELECT DISTINCTROW HIGH_PRIORITY SQL_SMALL_RESULT tb1.id, tb2.id FROM tb1, tb2 WHERE tb1.id2 = tb2.id2",
                output);

        sql = "SELect  sql_cache id1,id2 from tb1,tb2 where tb1.id1=tb2.id1 ";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDMLSelectParser(lexer, new MySQLExprParser(lexer));
        select = parser.select();
        Assert.assertNotNull(select);
        output = output2MySQL(select, sql);
        Assert.assertEquals("SELECT SQL_CACHE id1, id2 FROM tb1, tb2 WHERE tb1.id1 = tb2.id1", output);

        sql = "SELect  sql_cache id1,max(id2) from tb1 group by id1 having id1>10 order by id3 desc";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDMLSelectParser(lexer, new MySQLExprParser(lexer));
        select = parser.select();
        Assert.assertNotNull(select);
        output = output2MySQL(select, sql);
        Assert.assertEquals(
                "SELECT SQL_CACHE id1, MAX(id2) FROM tb1 GROUP BY id1 HAVING id1 > 10 ORDER BY id3 DESC",
                output);

        sql = "SELect  SQL_BUFFER_RESULT tb1.id1,id2 from tb1";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDMLSelectParser(lexer, new MySQLExprParser(lexer));
        select = parser.select();
        Assert.assertNotNull(select);
        output = output2MySQL(select, sql);
        Assert.assertEquals("SELECT SQL_BUFFER_RESULT tb1.id1, id2 FROM tb1", output);

        sql = "SELect  SQL_no_cache tb1.id1,id2 from tb1";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDMLSelectParser(lexer, new MySQLExprParser(lexer));
        select = parser.select();
        Assert.assertNotNull(select);
        output = output2MySQL(select, sql);
        Assert.assertEquals("SELECT SQL_NO_CACHE tb1.id1, id2 FROM tb1", output);

        sql = "SELect  SQL_CALC_FOUND_ROWS tb1.id1,id2 from tb1";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDMLSelectParser(lexer, new MySQLExprParser(lexer));
        select = parser.select();
        Assert.assertNotNull(select);
        output = output2MySQL(select, sql);
        Assert.assertEquals("SELECT SQL_CALC_FOUND_ROWS tb1.id1, id2 FROM tb1", output);

        sql = "SELect 1+1 ";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDMLSelectParser(lexer, new MySQLExprParser(lexer));
        select = parser.select();
        Assert.assertNotNull(select);
        output = output2MySQL(select, sql);
        Assert.assertEquals("SELECT 1 + 1", output);

        sql = "SELect t1.* from tb ";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDMLSelectParser(lexer, new MySQLExprParser(lexer));
        select = parser.select();
        Assert.assertNotNull(select);
        output = output2MySQL(select, sql);
        Assert.assertEquals("SELECT t1.* FROM tb", output);

        sql = "SELect distinct high_priority straight_join sql_big_result sql_cache tb1.id,tb2.id "
                + "from tb1,tb2 where tb1.id2=tb2.id2";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDMLSelectParser(lexer, new MySQLExprParser(lexer));
        select = parser.select();
        Assert.assertNotNull(select);
        output = output2MySQL(select, sql);
        Assert.assertEquals("SELECT DISTINCT HIGH_PRIORITY STRAIGHT_JOIN SQL_BIG_RESULT"
                + " SQL_CACHE tb1.id, tb2.id FROM tb1, tb2 WHERE tb1.id2 = tb2.id2", output);

        sql = "SELect distinct id1,id2 from tb1,tb2 where tb1.id1=tb2.id2 for update";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDMLSelectParser(lexer, new MySQLExprParser(lexer));
        select = parser.select();
        Assert.assertNotNull(select);
        output = output2MySQL(select, sql);
        Assert.assertEquals("SELECT DISTINCT id1, id2 FROM tb1, tb2 WHERE tb1.id1 = tb2.id2 FOR UPDATE", output);

        sql = "SELect distinct id1,id2 from tb1,tb2 where tb1.id1=tb2.id2 lock in share mode";
        lexer = new MySQLLexer(sql);
        parser = new MySQLDMLSelectParser(lexer, new MySQLExprParser(lexer));
        select = parser.select();
        Assert.assertNotNull(select);
        output = output2MySQL(select, sql);
        Assert.assertEquals("SELECT DISTINCT id1, id2 FROM tb1, tb2 WHERE tb1.id1 = tb2.id2 LOCK IN SHARE MODE", output);

    }

    public void testSelectChinese() throws SQLSyntaxErrorException {
        String sql = "SELect t1.id , t2.* from t1, test.t2 where test.t1.id='中''‘文' and t1.id=test.t2.id";
        MySQLLexer lexer = new MySQLLexer(sql);
        MySQLDMLSelectParser parser = new MySQLDMLSelectParser(lexer, new MySQLExprParser(lexer));
        DMLSelectStatement select = parser.select();
        Assert.assertNotNull(select);
        String output = output2MySQL(select, sql);
        Assert.assertEquals(
                "SELECT t1.id, t2.* FROM t1, test.t2 WHERE test.t1.id = '中\\'‘文' AND t1.id = test.t2.id",
                output);
    }

}
