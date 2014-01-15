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
 * (created at 2011-5-12)
 */
package com.alibaba.cobar.parser.recognizer.mysql.syntax;

import java.sql.SQLSyntaxErrorException;
import java.util.List;

import junit.framework.Assert;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.comparison.ComparisionEqualsExpression;
import com.alibaba.cobar.parser.ast.expression.logical.LogicalAndExpression;
import com.alibaba.cobar.parser.ast.expression.primary.Identifier;
import com.alibaba.cobar.parser.ast.expression.primary.ParamMarker;
import com.alibaba.cobar.parser.ast.fragment.GroupBy;
import com.alibaba.cobar.parser.ast.fragment.Limit;
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
import com.alibaba.cobar.parser.ast.stmt.dml.DMLSelectStatement;
import com.alibaba.cobar.parser.recognizer.mysql.lexer.MySQLLexer;
import com.alibaba.cobar.parser.util.ListUtil;
import com.alibaba.cobar.parser.util.Pair;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class MySQLDMLParserTest extends AbstractSyntaxTest {

    protected MySQLDMLParser getDMLParser(MySQLLexer lexer) {
        MySQLExprParser exp = new MySQLExprParser(lexer);
        MySQLDMLParser parser = new MySQLDMLSelectParser(lexer, exp);
        return parser;
    }

    public void testOrderBy() throws SQLSyntaxErrorException {
        String sql = "order by c1 asc, c2 desc  , c3 ";
        MySQLLexer lexer = new MySQLLexer(sql);
        MySQLDMLParser parser = getDMLParser(lexer);
        OrderBy orderBy = parser.orderBy();
        String output = output2MySQL(orderBy, sql);
        ListUtil.isEquals(ListUtil.createList(
                new Pair<Expression, SortOrder>(new Identifier(null, "c1"), SortOrder.ASC),
                new Pair<Expression, SortOrder>(new Identifier(null, "c2"), SortOrder.DESC),
                new Pair<Expression, SortOrder>(new Identifier(null, "c3"), SortOrder.ASC)), orderBy.getOrderByList());
        Assert.assertEquals("ORDER BY c1, c2 DESC, c3", output);

        sql = "order by c1   ";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        orderBy = parser.orderBy();
        output = output2MySQL(orderBy, sql);
        ListUtil.isEquals(
                ListUtil.createList(new Pair<Expression, SortOrder>(new Identifier(null, "c1"), SortOrder.ASC)),
                orderBy.getOrderByList());
        Assert.assertEquals("ORDER BY c1", output);

        sql = "order by c1 asc  ";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        orderBy = parser.orderBy();
        output = output2MySQL(orderBy, sql);
        ListUtil.isEquals(
                ListUtil.createList(new Pair<Expression, SortOrder>(new Identifier(null, "c1"), SortOrder.ASC)),
                orderBy.getOrderByList());
        Assert.assertEquals("ORDER BY c1", output);

        sql = "order by c1 desc  ";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        orderBy = parser.orderBy();
        output = output2MySQL(orderBy, sql);
        ListUtil.isEquals(
                ListUtil.createList(new Pair<Expression, SortOrder>(new Identifier(null, "c1"), SortOrder.DESC)),
                orderBy.getOrderByList());
        Assert.assertEquals("ORDER BY c1 DESC", output);
    }

    public void testLimit() throws SQLSyntaxErrorException {
        String sql = "limit 1,2";
        MySQLLexer lexer = new MySQLLexer(sql);
        MySQLDMLParser parser = getDMLParser(lexer);
        Limit limit = parser.limit();
        String output = output2MySQL(limit, sql);
        Assert.assertEquals(1, limit.getOffset());
        Assert.assertEquals(2, limit.getSize());
        Assert.assertEquals("LIMIT 1, 2", output);

        sql = "limit 1,?";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        limit = parser.limit();
        output = output2MySQL(limit, sql);
        Assert.assertEquals(1, limit.getOffset());
        Assert.assertEquals(new ParamMarker(1), limit.getSize());
        Assert.assertEquals("LIMIT 1, ?", output);

        sql = "limit ?,9";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        limit = parser.limit();
        output = output2MySQL(limit, sql);
        Assert.assertEquals(new ParamMarker(1), limit.getOffset());
        Assert.assertEquals(9, limit.getSize());
        Assert.assertEquals("LIMIT ?, 9", output);

        sql = "limit ?,?";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        limit = parser.limit();
        output = output2MySQL(limit, sql);
        Assert.assertEquals(new ParamMarker(1), limit.getOffset());
        Assert.assertEquals(new ParamMarker(2), limit.getSize());
        Assert.assertEquals("LIMIT ?, ?", output);

        sql = "limit ? d";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        limit = parser.limit();
        output = output2MySQL(limit, sql);
        Assert.assertEquals(0, limit.getOffset());
        Assert.assertEquals(new ParamMarker(1), limit.getSize());
        Assert.assertEquals("LIMIT 0, ?", output);

        sql = "limit 9 f";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        limit = parser.limit();
        output = output2MySQL(limit, sql);
        Assert.assertEquals(0, limit.getOffset());
        Assert.assertEquals(9, limit.getSize());
        Assert.assertEquals("LIMIT 0, 9", output);

        sql = "limit 9 ofFset 0";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        limit = parser.limit();
        output = output2MySQL(limit, sql);
        Assert.assertEquals(0, limit.getOffset());
        Assert.assertEquals(9, limit.getSize());
        Assert.assertEquals("LIMIT 0, 9", output);

        sql = "limit ? offset 0";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        limit = parser.limit();
        output = output2MySQL(limit, sql);
        Assert.assertEquals(0, limit.getOffset());
        Assert.assertEquals(new ParamMarker(1), limit.getSize());
        Assert.assertEquals("LIMIT 0, ?", output);

        sql = "limit ? offset ?";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        limit = parser.limit();
        output = output2MySQL(limit, sql);
        Assert.assertEquals(new ParamMarker(2), limit.getOffset());
        Assert.assertEquals(new ParamMarker(1), limit.getSize());
        Assert.assertEquals("LIMIT ?, ?", output);

        sql = "limit 9 offset ?";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        limit = parser.limit();
        output = output2MySQL(limit, sql);
        Assert.assertEquals(new ParamMarker(1), limit.getOffset());
        Assert.assertEquals(9, limit.getSize());
        Assert.assertEquals("LIMIT ?, 9", output);

    }

    public void testGroupBy() throws SQLSyntaxErrorException {
        String sql = "group by c1 asc, c2 desc  , c3 with rollup";
        MySQLLexer lexer = new MySQLLexer(sql);
        MySQLDMLParser parser = getDMLParser(lexer);
        GroupBy groupBy = parser.groupBy();
        String output = output2MySQL(groupBy, sql);
        ListUtil.isEquals(ListUtil.createList(
                new Pair<Expression, SortOrder>(new Identifier(null, "c1"), SortOrder.ASC),
                new Pair<Expression, SortOrder>(new Identifier(null, "c2"), SortOrder.DESC),
                new Pair<Expression, SortOrder>(new Identifier(null, "c3"), SortOrder.ASC)), groupBy.getOrderByList());
        Assert.assertEquals("GROUP BY c1, c2 DESC, c3 WITH ROLLUP", output);

        sql = "group by c1 asc, c2 desc  , c3 ";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        groupBy = parser.groupBy();
        output = output2MySQL(groupBy, sql);
        ListUtil.isEquals(ListUtil.createList(
                new Pair<Expression, SortOrder>(new Identifier(null, "c1"), SortOrder.ASC),
                new Pair<Expression, SortOrder>(new Identifier(null, "c2"), SortOrder.DESC),
                new Pair<Expression, SortOrder>(new Identifier(null, "c3"), SortOrder.ASC)), groupBy.getOrderByList());
        Assert.assertEquals("GROUP BY c1, c2 DESC, c3", output);

        sql = "group by c1   ";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        groupBy = parser.groupBy();
        output = output2MySQL(groupBy, sql);
        ListUtil.isEquals(
                ListUtil.createList(new Pair<Expression, SortOrder>(new Identifier(null, "c1"), SortOrder.ASC)),
                groupBy.getOrderByList());
        Assert.assertEquals("GROUP BY c1", output);

        sql = "group by c1 asc  ";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        groupBy = parser.groupBy();
        output = output2MySQL(groupBy, sql);
        ListUtil.isEquals(
                ListUtil.createList(new Pair<Expression, SortOrder>(new Identifier(null, "c1"), SortOrder.ASC)),
                groupBy.getOrderByList());
        Assert.assertEquals("GROUP BY c1", output);

        sql = "group by c1 desc  ";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        groupBy = parser.groupBy();
        output = output2MySQL(groupBy, sql);
        ListUtil.isEquals(
                ListUtil.createList(new Pair<Expression, SortOrder>(new Identifier(null, "c1"), SortOrder.DESC)),
                groupBy.getOrderByList());
        Assert.assertEquals("GROUP BY c1 DESC", output);

        sql = "group by c1 with rollup  ";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        groupBy = parser.groupBy();
        output = output2MySQL(groupBy, sql);
        ListUtil.isEquals(
                ListUtil.createList(new Pair<Expression, SortOrder>(new Identifier(null, "c1"), SortOrder.ASC)),
                groupBy.getOrderByList());
        Assert.assertEquals("GROUP BY c1 WITH ROLLUP", output);
    }

    public void testTR1() throws SQLSyntaxErrorException {
        String sql = "(select * from `select`) as `select`";
        MySQLLexer lexer = new MySQLLexer(sql);
        MySQLDMLParser parser = getDMLParser(lexer);
        TableReferences trs = parser.tableRefs();
        String output = output2MySQL(trs, sql);
        List<TableReference> list = trs.getTableReferenceList();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(SubqueryFactor.class, list.get(0).getClass());
        Assert.assertEquals("(SELECT * FROM `select`) AS `SELECT`", output);

        sql = "(((selecT * from any)union select `select` from `from` order by dd) as 'a1', (((t2)))), t3";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        list = trs.getTableReferenceList();
        Assert.assertEquals(2, list.size());
        Assert.assertEquals(TableReferences.class, list.get(0).getClass());
        Assert.assertEquals(TableRefFactor.class, list.get(1).getClass());
        list = ((TableReferences) list.get(0)).getTableReferenceList();
        Assert.assertEquals(2, list.size());
        Assert.assertEquals(SubqueryFactor.class, list.get(0).getClass());
        Assert.assertEquals(TableReferences.class, list.get(1).getClass());
        Assert.assertEquals(
                "((SELECT * FROM any) UNION (SELECT `select` FROM `from` ORDER BY dd)) AS 'a1', t2, t3",
                output);

        sql = "(t1)";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        list = trs.getTableReferenceList();
        Assert.assertEquals(1, list.size());
        TableReference tr = list.get(0);
        Assert.assertEquals(TableReferences.class, tr.getClass());
        list = ((TableReferences) tr).getTableReferenceList();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(TableRefFactor.class, list.get(0).getClass());
        Assert.assertEquals("t1", output);

        sql = "(t1,t2,(t3))";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        list = trs.getTableReferenceList();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(TableReferences.class, list.get(0).getClass());
        tr = (TableReferences) list.get(0);
        Assert.assertEquals(TableReferences.class, tr.getClass());
        list = ((TableReferences) tr).getTableReferenceList();
        Assert.assertEquals(3, list.size());
        Assert.assertEquals(TableRefFactor.class, list.get(0).getClass());
        Assert.assertEquals(TableRefFactor.class, list.get(1).getClass());
        Assert.assertEquals(TableRefFactor.class, list.get(1).getClass());
        Assert.assertEquals("t1, t2, t3", output);

        sql = "(tb1 as t1)inner join (tb2 as t2) on t1.name=t2.name";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        list = trs.getTableReferenceList();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(InnerJoin.class, list.get(0).getClass());
        tr = ((InnerJoin) list.get(0)).getLeftTableRef();
        Assert.assertEquals(TableReferences.class, tr.getClass());
        list = ((TableReferences) tr).getTableReferenceList();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(TableRefFactor.class, list.get(0).getClass());
        Expression ex = ((InnerJoin) (trs.getTableReferenceList()).get(0)).getOnCond();
        Assert.assertEquals(ex.getClass(), ComparisionEqualsExpression.class);
        Assert.assertEquals("(tb1 AS T1) INNER JOIN (tb2 AS T2) ON t1.name = t2.name", output);

        sql = "(tb1 as t1)inner join tb2 as t2 using (c1)";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        List<String> using_list = ((InnerJoin) (trs.getTableReferenceList()).get(0)).getUsing();
        Assert.assertEquals(1, using_list.size());
        Assert.assertEquals("(tb1 AS T1) INNER JOIN tb2 AS T2 USING (c1)", output);

        sql = "(tb1 as t1)inner join tb2 as t2 using (c1,c2)";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        using_list = ((InnerJoin) (trs.getTableReferenceList()).get(0)).getUsing();
        Assert.assertEquals(2, using_list.size());
        Assert.assertEquals("(tb1 AS T1) INNER JOIN tb2 AS T2 USING (c1, c2)", output);

        sql = "tb1 as t1 use index (i1,i2,i3)";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        list = trs.getTableReferenceList();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(TableRefFactor.class, list.get(0).getClass());
        List<IndexHint> hintlist = ((TableRefFactor) (trs.getTableReferenceList()).get(0)).getHintList();
        Assert.assertEquals(1, hintlist.size());
        IndexHint indexhint = hintlist.get(0);
        Assert.assertEquals(3, indexhint.getIndexList().size());
        Assert.assertEquals("USE", indexhint.getAction().name());
        Assert.assertEquals("INDEX", indexhint.getType().name());
        Assert.assertEquals("tb1 AS T1 USE INDEX (i1, i2, i3)", output);

        sql = "tb1 as t1 use index (i1,i2,i3),tb2 as t2 use index (i1,i2,i3)";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        list = trs.getTableReferenceList();
        Assert.assertEquals(2, list.size());
        Assert.assertEquals(TableRefFactor.class, list.get(0).getClass());
        hintlist = ((TableRefFactor) (trs.getTableReferenceList()).get(0)).getHintList();
        Assert.assertEquals(1, hintlist.size());
        indexhint = hintlist.get(0);
        Assert.assertEquals(3, indexhint.getIndexList().size());
        Assert.assertEquals("USE", indexhint.getAction().name());
        Assert.assertEquals("INDEX", indexhint.getType().name());
        hintlist = ((TableRefFactor) (trs.getTableReferenceList()).get(1)).getHintList();
        Assert.assertEquals(1, hintlist.size());
        indexhint = hintlist.get(0);
        Assert.assertEquals(3, indexhint.getIndexList().size());
        Assert.assertEquals("USE", indexhint.getAction().name());
        Assert.assertEquals("INDEX", indexhint.getType().name());
        Assert.assertEquals("tb1 AS T1 USE INDEX (i1, i2, i3), tb2 AS T2 USE INDEX (i1, i2, i3)", output);

        sql = "tb1 as t1";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        list = trs.getTableReferenceList();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(TableRefFactor.class, list.get(0).getClass());
        Assert.assertEquals("tb1", ((TableRefFactor) (trs.getTableReferenceList()).get(0)).getTable().getIdText());
        Assert.assertEquals("T1", ((TableRefFactor) (trs.getTableReferenceList()).get(0)).getAlias());
        Assert.assertEquals("tb1 AS T1", output);

        sql = "tb1 t1";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        list = trs.getTableReferenceList();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(TableRefFactor.class, list.get(0).getClass());
        Assert.assertEquals("tb1", ((TableRefFactor) (trs.getTableReferenceList()).get(0)).getTable().getIdText());
        Assert.assertEquals("T1", ((TableRefFactor) (trs.getTableReferenceList()).get(0)).getAlias());
        Assert.assertEquals("tb1 AS T1", output);

        sql = "tb1,tb2,tb3";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        list = trs.getTableReferenceList();
        Assert.assertEquals(3, list.size());
        Assert.assertEquals(TableRefFactor.class, list.get(0).getClass());
        Assert.assertEquals("tb1", ((TableRefFactor) (trs.getTableReferenceList()).get(0)).getTable().getIdText());
        Assert.assertEquals(null, ((TableRefFactor) (trs.getTableReferenceList()).get(0)).getAlias());
        Assert.assertEquals("tb2", ((TableRefFactor) (trs.getTableReferenceList()).get(1)).getTable().getIdText());
        Assert.assertEquals("tb3", ((TableRefFactor) (trs.getTableReferenceList()).get(2)).getTable().getIdText());
        Assert.assertEquals("tb1, tb2, tb3", output);

        sql = "tb1 use key for join (i1,i2)";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        list = trs.getTableReferenceList();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(TableRefFactor.class, list.get(0).getClass());
        hintlist = ((TableRefFactor) (trs.getTableReferenceList()).get(0)).getHintList();
        Assert.assertEquals(1, hintlist.size());
        indexhint = hintlist.get(0);
        Assert.assertEquals(2, indexhint.getIndexList().size());
        Assert.assertEquals("USE", indexhint.getAction().name());
        Assert.assertEquals("KEY", indexhint.getType().name());
        Assert.assertEquals("JOIN", indexhint.getScope().name());
        Assert.assertEquals("tb1 USE KEY FOR JOIN (i1, i2)", output);

        sql = "tb1 use index for group by(i1,i2)";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        list = trs.getTableReferenceList();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(TableRefFactor.class, list.get(0).getClass());
        hintlist = ((TableRefFactor) (trs.getTableReferenceList()).get(0)).getHintList();
        Assert.assertEquals(1, hintlist.size());
        indexhint = hintlist.get(0);
        Assert.assertEquals(2, indexhint.getIndexList().size());
        Assert.assertEquals("USE", indexhint.getAction().name());
        Assert.assertEquals("INDEX", indexhint.getType().name());
        Assert.assertEquals("GROUP_BY", indexhint.getScope().name());
        Assert.assertEquals("tb1 USE INDEX FOR GROUP BY (i1, i2)", output);

        sql = "tb1 use key for order by (i1,i2) use key for group by () " + "ignore index for group by (i1,i2)";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        list = trs.getTableReferenceList();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(TableRefFactor.class, list.get(0).getClass());
        Assert.assertEquals("tb1", ((TableRefFactor) (trs.getTableReferenceList()).get(0)).getTable().getIdText());
        Assert.assertEquals(null, ((TableRefFactor) (trs.getTableReferenceList()).get(0)).getAlias());
        hintlist = ((TableRefFactor) (trs.getTableReferenceList()).get(0)).getHintList();
        Assert.assertEquals(3, hintlist.size());
        indexhint = hintlist.get(0);
        Assert.assertEquals(2, indexhint.getIndexList().size());
        Assert.assertEquals("USE", indexhint.getAction().name());
        Assert.assertEquals("KEY", indexhint.getType().name());
        Assert.assertEquals("ORDER_BY", indexhint.getScope().name());
        indexhint = hintlist.get(1);
        Assert.assertEquals(0, indexhint.getIndexList().size());
        Assert.assertEquals("USE", indexhint.getAction().name());
        Assert.assertEquals("KEY", indexhint.getType().name());
        Assert.assertEquals("GROUP_BY", indexhint.getScope().name());
        indexhint = hintlist.get(2);
        Assert.assertEquals(2, indexhint.getIndexList().size());
        Assert.assertEquals("IGNORE", indexhint.getAction().name());
        Assert.assertEquals("INDEX", indexhint.getType().name());
        Assert.assertEquals("GROUP_BY", indexhint.getScope().name());
        Assert.assertEquals("tb1 USE KEY FOR ORDER BY (i1, i2) "
                + "USE KEY FOR GROUP BY () IGNORE INDEX FOR GROUP BY (i1, i2)", output);

        sql = "tb1 use index for order by (i1,i2) force index for group by (i1)";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        list = trs.getTableReferenceList();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(TableRefFactor.class, list.get(0).getClass());
        Assert.assertEquals("tb1", ((TableRefFactor) (trs.getTableReferenceList()).get(0)).getTable().getIdText());
        Assert.assertEquals(null, ((TableRefFactor) (trs.getTableReferenceList()).get(0)).getAlias());
        hintlist = ((TableRefFactor) (trs.getTableReferenceList()).get(0)).getHintList();
        Assert.assertEquals(2, hintlist.size());
        indexhint = hintlist.get(0);
        Assert.assertEquals(2, indexhint.getIndexList().size());
        Assert.assertEquals("i1", indexhint.getIndexList().get(0));
        Assert.assertEquals("i2", indexhint.getIndexList().get(1));
        Assert.assertEquals("USE", indexhint.getAction().name());
        Assert.assertEquals("INDEX", indexhint.getType().name());
        Assert.assertEquals("ORDER_BY", indexhint.getScope().name());
        indexhint = hintlist.get(1);
        Assert.assertEquals(1, indexhint.getIndexList().size());
        Assert.assertEquals("FORCE", indexhint.getAction().name());
        Assert.assertEquals("INDEX", indexhint.getType().name());
        Assert.assertEquals("GROUP_BY", indexhint.getScope().name());
        Assert.assertEquals("tb1 USE INDEX FOR ORDER BY (i1, i2) FORCE INDEX FOR GROUP BY (i1)", output);

        sql = "tb1 ignore key for join (i1,i2)";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        list = trs.getTableReferenceList();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(TableRefFactor.class, list.get(0).getClass());
        Assert.assertEquals("tb1", ((TableRefFactor) (trs.getTableReferenceList()).get(0)).getTable().getIdText());
        Assert.assertEquals(null, ((TableRefFactor) (trs.getTableReferenceList()).get(0)).getAlias());
        hintlist = ((TableRefFactor) (trs.getTableReferenceList()).get(0)).getHintList();
        Assert.assertEquals(1, hintlist.size());
        indexhint = hintlist.get(0);
        Assert.assertEquals(2, indexhint.getIndexList().size());
        Assert.assertEquals("i1", indexhint.getIndexList().get(0));
        Assert.assertEquals("i2", indexhint.getIndexList().get(1));
        Assert.assertEquals("IGNORE", indexhint.getAction().name());
        Assert.assertEquals("KEY", indexhint.getType().name());
        Assert.assertEquals("JOIN", indexhint.getScope().name());
        Assert.assertEquals("tb1 IGNORE KEY FOR JOIN (i1, i2)", output);

        sql = "tb1 ignore index for group by (i1,i2)";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        list = trs.getTableReferenceList();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(TableRefFactor.class, list.get(0).getClass());
        Assert.assertEquals("tb1", ((TableRefFactor) (trs.getTableReferenceList()).get(0)).getTable().getIdText());
        Assert.assertEquals(null, ((TableRefFactor) (trs.getTableReferenceList()).get(0)).getAlias());
        hintlist = ((TableRefFactor) (trs.getTableReferenceList()).get(0)).getHintList();
        Assert.assertEquals(1, hintlist.size());
        indexhint = hintlist.get(0);
        Assert.assertEquals(2, indexhint.getIndexList().size());
        Assert.assertEquals("i1", indexhint.getIndexList().get(0));
        Assert.assertEquals("i2", indexhint.getIndexList().get(1));
        Assert.assertEquals("IGNORE", indexhint.getAction().name());
        Assert.assertEquals("INDEX", indexhint.getType().name());
        Assert.assertEquals("GROUP_BY", indexhint.getScope().name());
        Assert.assertEquals("tb1 IGNORE INDEX FOR GROUP BY (i1, i2)", output);

        sql = "(offer  a  straight_join wp_image b use key for join(t1,t2) on a.member_id=b.member_id inner join product_visit c )";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        Assert.assertEquals(
                "offer AS A STRAIGHT_JOIN wp_image AS B USE KEY FOR JOIN (t1, t2) ON a.member_id = b.member_id INNER JOIN product_visit AS C",
                output);

        sql = "tb1 ignore index for order by(i1)";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        list = trs.getTableReferenceList();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(TableRefFactor.class, list.get(0).getClass());
        Assert.assertEquals("tb1", ((TableRefFactor) (trs.getTableReferenceList()).get(0)).getTable().getIdText());
        Assert.assertEquals(null, ((TableRefFactor) (trs.getTableReferenceList()).get(0)).getAlias());
        hintlist = ((TableRefFactor) (trs.getTableReferenceList()).get(0)).getHintList();
        Assert.assertEquals(1, hintlist.size());
        indexhint = hintlist.get(0);
        Assert.assertEquals(1, indexhint.getIndexList().size());
        Assert.assertEquals("i1", indexhint.getIndexList().get(0));
        Assert.assertEquals("IGNORE", indexhint.getAction().name());
        Assert.assertEquals("INDEX", indexhint.getType().name());
        Assert.assertEquals("ORDER_BY", indexhint.getScope().name());
        Assert.assertEquals("tb1 IGNORE INDEX FOR ORDER BY (i1)", output);

        sql = "tb1 force key for group by (i1,i2)";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        list = trs.getTableReferenceList();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(TableRefFactor.class, list.get(0).getClass());
        Assert.assertEquals("tb1", ((TableRefFactor) (trs.getTableReferenceList()).get(0)).getTable().getIdText());
        Assert.assertEquals(null, ((TableRefFactor) (trs.getTableReferenceList()).get(0)).getAlias());
        hintlist = ((TableRefFactor) (trs.getTableReferenceList()).get(0)).getHintList();
        Assert.assertEquals(1, hintlist.size());
        indexhint = hintlist.get(0);
        Assert.assertEquals(2, indexhint.getIndexList().size());
        Assert.assertEquals("i1", indexhint.getIndexList().get(0));
        Assert.assertEquals("i2", indexhint.getIndexList().get(1));
        Assert.assertEquals("FORCE", indexhint.getAction().name());
        Assert.assertEquals("KEY", indexhint.getType().name());
        Assert.assertEquals("GROUP_BY", indexhint.getScope().name());
        Assert.assertEquals("tb1 FORCE KEY FOR GROUP BY (i1, i2)", output);

        sql = "tb1 force index for group by (i1,i2)";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        list = trs.getTableReferenceList();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(TableRefFactor.class, list.get(0).getClass());
        Assert.assertEquals("tb1", ((TableRefFactor) (trs.getTableReferenceList()).get(0)).getTable().getIdText());
        Assert.assertEquals(null, ((TableRefFactor) (trs.getTableReferenceList()).get(0)).getAlias());
        hintlist = ((TableRefFactor) (trs.getTableReferenceList()).get(0)).getHintList();
        Assert.assertEquals(1, hintlist.size());
        indexhint = hintlist.get(0);
        Assert.assertEquals(2, indexhint.getIndexList().size());
        Assert.assertEquals("i1", indexhint.getIndexList().get(0));
        Assert.assertEquals("i2", indexhint.getIndexList().get(1));
        Assert.assertEquals("FORCE", indexhint.getAction().name());
        Assert.assertEquals("INDEX", indexhint.getType().name());
        Assert.assertEquals("GROUP_BY", indexhint.getScope().name());
        Assert.assertEquals("tb1 FORCE INDEX FOR GROUP BY (i1, i2)", output);

        sql = "tb1 force index for join (i1,i2)";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        list = trs.getTableReferenceList();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(TableRefFactor.class, list.get(0).getClass());
        Assert.assertEquals("tb1", ((TableRefFactor) (trs.getTableReferenceList()).get(0)).getTable().getIdText());
        Assert.assertEquals(null, ((TableRefFactor) (trs.getTableReferenceList()).get(0)).getAlias());
        hintlist = ((TableRefFactor) (trs.getTableReferenceList()).get(0)).getHintList();
        Assert.assertEquals(1, hintlist.size());
        indexhint = hintlist.get(0);
        Assert.assertEquals(2, indexhint.getIndexList().size());
        Assert.assertEquals("i1", indexhint.getIndexList().get(0));
        Assert.assertEquals("i2", indexhint.getIndexList().get(1));
        Assert.assertEquals("FORCE", indexhint.getAction().name());
        Assert.assertEquals("INDEX", indexhint.getType().name());
        Assert.assertEquals("JOIN", indexhint.getScope().name());
        Assert.assertEquals("tb1 FORCE INDEX FOR JOIN (i1, i2)", output);

        sql = "(tb1 force index for join (i1,i2) )left outer join tb2 as t2 " + "use index (i1,i2,i3) on t1.id1=t2.id1";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        list = trs.getTableReferenceList();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(OuterJoin.class, list.get(0).getClass());
        Assert.assertEquals(true, (((OuterJoin) list.get(0)).isLeftJoin()));
        TableReferences ltr = (TableReferences) ((OuterJoin) list.get(0)).getLeftTableRef();
        Assert.assertEquals(1, ltr.getTableReferenceList().size());
        Assert.assertEquals(TableRefFactor.class, ltr.getTableReferenceList().get(0).getClass());
        Assert.assertEquals(null, ((TableRefFactor) (ltr.getTableReferenceList().get(0))).getAlias());
        Assert.assertEquals("tb1", ((TableRefFactor) (ltr.getTableReferenceList().get(0))).getTable().getIdText());
        hintlist = ((TableRefFactor) (ltr.getTableReferenceList()).get(0)).getHintList();
        Assert.assertEquals(1, hintlist.size());
        indexhint = hintlist.get(0);
        Assert.assertEquals(2, indexhint.getIndexList().size());
        Assert.assertEquals("i1", indexhint.getIndexList().get(0));
        Assert.assertEquals("i2", indexhint.getIndexList().get(1));
        Assert.assertEquals("FORCE", indexhint.getAction().name());
        Assert.assertEquals("INDEX", indexhint.getType().name());
        Assert.assertEquals("JOIN", indexhint.getScope().name());
        TableRefFactor rtf = (TableRefFactor) ((OuterJoin) list.get(0)).getRightTableRef();
        Assert.assertEquals("T2", rtf.getAlias());
        Assert.assertEquals("tb2", rtf.getTable().getIdText());
        hintlist = rtf.getHintList();
        Assert.assertEquals(1, hintlist.size());
        indexhint = hintlist.get(0);
        Assert.assertEquals(3, indexhint.getIndexList().size());
        Assert.assertEquals("i1", indexhint.getIndexList().get(0));
        Assert.assertEquals("i2", indexhint.getIndexList().get(1));
        Assert.assertEquals("USE", indexhint.getAction().name());
        Assert.assertEquals("INDEX", indexhint.getType().name());
        Assert.assertEquals("ALL", indexhint.getScope().name());
        Assert.assertEquals(ComparisionEqualsExpression.class, ((OuterJoin) list.get(0)).getOnCond().getClass());
        Assert.assertEquals("(tb1 FORCE INDEX FOR JOIN (i1, i2)) "
                + "LEFT JOIN tb2 AS T2 USE INDEX (i1, i2, i3) ON t1.id1 = t2.id1", output);

        sql = " (((tb1 force index for join (i1,i2),tb3),tb4),tb5) "
                + "left outer join (tb2 as t2 use index (i1,i2,i3)) using(id1)";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        list = trs.getTableReferenceList();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(OuterJoin.class, list.get(0).getClass());
        Assert.assertEquals(true, (((OuterJoin) list.get(0)).isLeftJoin()));
        ltr = (TableReferences) ((OuterJoin) list.get(0)).getLeftTableRef();
        Assert.assertEquals(2, ltr.getTableReferenceList().size());
        Assert.assertEquals(TableReferences.class, ltr.getTableReferenceList().get(0).getClass());
        TableReferences ltr1 = (TableReferences) (ltr.getTableReferenceList()).get(0);
        Assert.assertEquals(2, ltr1.getTableReferenceList().size());
        Assert.assertEquals(TableReferences.class, ltr1.getTableReferenceList().get(0).getClass());
        TableReferences ltr2 = (TableReferences) (ltr1.getTableReferenceList()).get(0);
        Assert.assertEquals(2, ltr2.getTableReferenceList().size());
        Assert.assertEquals(TableRefFactor.class, ltr2.getTableReferenceList().get(0).getClass());
        Assert.assertEquals(null, ((TableRefFactor) (ltr2.getTableReferenceList().get(0))).getAlias());
        Assert.assertEquals("tb1", ((TableRefFactor) (ltr2.getTableReferenceList().get(0))).getTable().getIdText());
        hintlist = ((TableRefFactor) (ltr2.getTableReferenceList()).get(0)).getHintList();
        Assert.assertEquals(1, hintlist.size());
        indexhint = hintlist.get(0);
        Assert.assertEquals(2, indexhint.getIndexList().size());
        Assert.assertEquals("i1", indexhint.getIndexList().get(0));
        Assert.assertEquals("i2", indexhint.getIndexList().get(1));
        Assert.assertEquals("FORCE", indexhint.getAction().name());
        Assert.assertEquals("INDEX", indexhint.getType().name());
        Assert.assertEquals("JOIN", indexhint.getScope().name());
        Assert.assertEquals(TableRefFactor.class, ltr2.getTableReferenceList().get(1).getClass());
        Assert.assertEquals("tb3", ((TableRefFactor) (ltr2.getTableReferenceList().get(1))).getTable().getIdText());
        Assert.assertEquals(TableRefFactor.class, ltr1.getTableReferenceList().get(1).getClass());
        Assert.assertEquals("tb4", ((TableRefFactor) (ltr1.getTableReferenceList().get(1))).getTable().getIdText());
        Assert.assertEquals(TableRefFactor.class, ltr.getTableReferenceList().get(1).getClass());
        Assert.assertEquals("tb5", ((TableRefFactor) (ltr.getTableReferenceList().get(1))).getTable().getIdText());
        TableReferences rtr = (TableReferences) ((OuterJoin) list.get(0)).getRightTableRef();
        Assert.assertEquals("T2", ((TableRefFactor) rtr.getTableReferenceList().get(0)).getAlias());
        Assert.assertEquals("tb2", ((TableRefFactor) rtr.getTableReferenceList().get(0)).getTable().getIdText());
        hintlist = ((TableRefFactor) rtr.getTableReferenceList().get(0)).getHintList();
        Assert.assertEquals(1, hintlist.size());
        indexhint = hintlist.get(0);
        Assert.assertEquals(3, indexhint.getIndexList().size());
        Assert.assertEquals("i1", indexhint.getIndexList().get(0));
        Assert.assertEquals("i2", indexhint.getIndexList().get(1));
        Assert.assertEquals("USE", indexhint.getAction().name());
        Assert.assertEquals("INDEX", indexhint.getType().name());
        Assert.assertEquals("ALL", indexhint.getScope().name());
        using_list = ((OuterJoin) (trs.getTableReferenceList()).get(0)).getUsing();
        Assert.assertEquals(1, using_list.size());
        Assert.assertEquals("(tb1 FORCE INDEX FOR JOIN (i1, i2), tb3, tb4, tb5) "
                + "LEFT JOIN (tb2 AS T2 USE INDEX (i1, i2, i3)) USING (id1)", output);

        sql = "(tb1 force index for join (i1,i2),tb3) "
                + "left outer join tb2 as t2 use index (i1,i2,i3) using(id1,id2)";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        Assert.assertEquals("(tb1 FORCE INDEX FOR JOIN (i1, i2), tb3) "
                + "LEFT JOIN tb2 AS T2 USE INDEX (i1, i2, i3) USING (id1, id2)", output);

        sql = "(tb1 force index for join (i1,i2),tb3) left outer join (tb2 as t2 use index (i1,i2,i3)) using(id1,id2)";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        Assert.assertEquals("(tb1 FORCE INDEX FOR JOIN (i1, i2), tb3) "
                + "LEFT JOIN (tb2 AS T2 USE INDEX (i1, i2, i3)) USING (id1, id2)", output);

        sql = "tb1 as t1 cross join tb2 as t2 use index(i1)using(id1)";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        Assert.assertEquals("tb1 AS T1 INNER JOIN tb2 AS T2 USE INDEX (i1) USING (id1)", output);

        sql = "(tb1 as t1) cross join tb2 as t2 use index(i1)using(id1)";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        Assert.assertEquals("(tb1 AS T1) INNER JOIN tb2 AS T2 USE INDEX (i1) USING (id1)", output);

        sql = "tb1 as _latin't1' cross join tb2 as t2 use index(i1)";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        Assert.assertEquals("tb1 AS _LATIN't1' INNER JOIN tb2 AS T2 USE INDEX (i1)", output);

        sql = "((select '  @  from' from `from`)) as t1 cross join tb2 as t2 use index()";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        list = trs.getTableReferenceList();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(InnerJoin.class, list.get(0).getClass());
        SubqueryFactor lsf = (SubqueryFactor) ((InnerJoin) list.get(0)).getLeftTableRef();
        Assert.assertEquals("T1", lsf.getAlias());
        Assert.assertEquals(DMLSelectStatement.class, lsf.getSubquery().getClass());
        rtf = (TableRefFactor) ((InnerJoin) list.get(0)).getRightTableRef();
        Assert.assertEquals("T2", rtf.getAlias());
        hintlist = rtf.getHintList();
        Assert.assertEquals(1, hintlist.size());
        indexhint = hintlist.get(0);
        Assert.assertEquals("USE", indexhint.getAction().name());
        Assert.assertEquals("INDEX", indexhint.getType().name());
        Assert.assertEquals("ALL", indexhint.getScope().name());
        Assert.assertEquals("tb2", rtf.getTable().getIdText());
        Assert.assertEquals("(SELECT '  @  from' FROM `from`) AS T1 " + "INNER JOIN tb2 AS T2 USE INDEX ()", output);

        sql = "(tb1 as t1) straight_join (tb2 as t2)";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        Assert.assertEquals("(tb1 AS T1) STRAIGHT_JOIN (tb2 AS T2)", output);

        sql = "tb1 straight_join tb2 as t2 on tb1.id=tb2.id";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        Assert.assertEquals("tb1 STRAIGHT_JOIN tb2 AS T2 ON tb1.id = tb2.id", output);

        sql = "tb1 left outer join tb2 on tb1.id=tb2.id";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        Assert.assertEquals("tb1 LEFT JOIN tb2 ON tb1.id = tb2.id", output);

        sql = "tb1 left outer join tb2 using(id)";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        Assert.assertEquals("tb1 LEFT JOIN tb2 USING (id)", output);

        sql = "(tb1 right outer join tb2 using()) join tb3 on tb1.id=tb2.id and tb2.id=tb3.id";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        list = trs.getTableReferenceList();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(InnerJoin.class, list.get(0).getClass());
        ltr = (TableReferences) ((InnerJoin) list.get(0)).getLeftTableRef();
        Assert.assertEquals(1, ltr.getTableReferenceList().size());
        TableRefFactor lltrf = (TableRefFactor) ((OuterJoin) ltr.getTableReferenceList().get(0)).getLeftTableRef();
        Assert.assertEquals(null, lltrf.getAlias());
        Assert.assertEquals("tb1", lltrf.getTable().getIdText());
        using_list = ((OuterJoin) ltr.getTableReferenceList().get(0)).getUsing();
        Assert.assertEquals(0, using_list.size());
        rtf = (TableRefFactor) ((InnerJoin) list.get(0)).getRightTableRef();
        Assert.assertEquals(null, rtf.getAlias());
        hintlist = rtf.getHintList();
        Assert.assertEquals(0, hintlist.size());
        Assert.assertEquals("tb3", rtf.getTable().getIdText());
        Assert.assertEquals(LogicalAndExpression.class, ((InnerJoin) list.get(0)).getOnCond().getClass());
        Assert.assertEquals(
                "(tb1 RIGHT JOIN tb2 USING ()) " + "INNER JOIN tb3 ON tb1.id = tb2.id AND tb2.id = tb3.id",
                output);

        sql = "tb1 right outer join tb2 using(id1,id2) " + "join (tb3,tb4) on tb1.id=tb2.id and tb2.id=tb3.id";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        list = trs.getTableReferenceList();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(InnerJoin.class, list.get(0).getClass());
        OuterJoin loj = (OuterJoin) ((InnerJoin) list.get(0)).getLeftTableRef();
        lltrf = (TableRefFactor) loj.getLeftTableRef();
        Assert.assertEquals(null, lltrf.getAlias());
        Assert.assertEquals("tb1", lltrf.getTable().getIdText());
        using_list = loj.getUsing();
        Assert.assertEquals(2, using_list.size());
        rtr = (TableReferences) ((InnerJoin) list.get(0)).getRightTableRef();
        Assert.assertEquals(2, rtr.getTableReferenceList().size());
        Assert.assertEquals("tb3", ((TableRefFactor) (rtr.getTableReferenceList().get(0))).getTable().getIdText());
        Assert.assertEquals("tb4", ((TableRefFactor) (rtr.getTableReferenceList().get(1))).getTable().getIdText());
        Assert.assertEquals(LogicalAndExpression.class, ((InnerJoin) list.get(0)).getOnCond().getClass());
        Assert.assertEquals("tb1 RIGHT JOIN tb2 USING (id1, id2) "
                + "INNER JOIN (tb3, tb4) ON tb1.id = tb2.id AND tb2.id = tb3.id", output);

        sql = "tb1 left outer join tb2 join tb3 using(id)";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        Assert.assertEquals("tb1 LEFT JOIN (tb2 INNER JOIN tb3) USING (id)", output);

        sql = "tb1 right join tb2 on tb1.id=tb2.id";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        Assert.assertEquals("tb1 RIGHT JOIN tb2 ON tb1.id = tb2.id", output);

        sql = "tb1 natural right join tb2 ";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        Assert.assertEquals("tb1 NATURAL RIGHT JOIN tb2", output);

        sql = "tb1 natural right outer join tb2 natural left outer join tb3";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        list = trs.getTableReferenceList();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(NaturalJoin.class, list.get(0).getClass());
        NaturalJoin lnj = (NaturalJoin) ((NaturalJoin) list.get(0)).getLeftTableRef();
        lltrf = (TableRefFactor) lnj.getLeftTableRef();
        Assert.assertEquals(null, lltrf.getAlias());
        Assert.assertEquals("tb1", lltrf.getTable().getIdText());
        TableRefFactor rltrf = (TableRefFactor) lnj.getRightTableRef();
        Assert.assertEquals(null, rltrf.getAlias());
        Assert.assertEquals("tb2", rltrf.getTable().getIdText());
        rtf = (TableRefFactor) ((NaturalJoin) list.get(0)).getRightTableRef();
        Assert.assertEquals(null, rtf.getAlias());
        Assert.assertEquals("tb3", rtf.getTable().getIdText());
        Assert.assertEquals("tb1 NATURAL RIGHT JOIN tb2 NATURAL LEFT JOIN tb3", output);

        sql = "tb1 natural left outer join tb2 ";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        Assert.assertEquals("tb1 NATURAL LEFT JOIN tb2", output);

        sql = "(tb1  t1) natural  join (tb2 as t2) ";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        Assert.assertEquals("(tb1 AS T1) NATURAL JOIN (tb2 AS T2)", output);

        sql = "(select (select * from tb1) from `select` " + "where `any`=any(select id2 from tb2))any  ";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        list = trs.getTableReferenceList();
        Assert.assertEquals(SubqueryFactor.class, list.get(0).getClass());
        Assert.assertEquals("ANY", ((SubqueryFactor) list.get(0)).getAlias());
        Assert.assertEquals("(SELECT SELECT * FROM tb1 FROM `select` "
                + "WHERE `any` = ANY (SELECT id2 FROM tb2)) AS ANY", output);

        sql = "((tb1),(tb3 as t3,`select`),tb2 use key for join (i1,i2))" + " left join tb4 join tb5 using ()";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        list = trs.getTableReferenceList();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(OuterJoin.class, list.get(0).getClass());
        Assert.assertEquals(TableReferences.class, ((OuterJoin) list.get(0)).getLeftTableRef().getClass());
        Assert.assertEquals(InnerJoin.class, ((OuterJoin) list.get(0)).getRightTableRef().getClass());
        list = ((TableReferences) ((OuterJoin) list.get(0)).getLeftTableRef()).getTableReferenceList();
        list = ((TableReferences) list.get(1)).getTableReferenceList();
        Assert.assertEquals(2, list.size());
        Assert.assertEquals("(tb1, tb3 AS T3, `select`, tb2 USE KEY FOR JOIN (i1, i2))"
                + " LEFT JOIN (tb4 INNER JOIN tb5) USING ()", output);

        sql = "((select `select` from `from` ) tb1),(tb3 as t3,`select`),tb2 use key for join (i1,i2) "
                + "left join tb4 using (i1,i2)straight_join tb5";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        list = trs.getTableReferenceList();
        Assert.assertEquals(3, list.size());
        Assert.assertEquals(TableReferences.class, list.get(0).getClass());
        Assert.assertEquals(TableReferences.class, list.get(1).getClass());
        Assert.assertEquals(StraightJoin.class, list.get(2).getClass());
        list = ((TableReferences) list.get(0)).getTableReferenceList();
        Assert.assertEquals(SubqueryFactor.class, list.get(0).getClass());
        list = trs.getTableReferenceList();
        list = ((TableReferences) list.get(1)).getTableReferenceList();
        Assert.assertEquals(TableRefFactor.class, list.get(0).getClass());
        Assert.assertEquals(TableRefFactor.class, list.get(1).getClass());
        list = trs.getTableReferenceList();
        StraightJoin sj = (StraightJoin) list.get(2);
        Assert.assertEquals(OuterJoin.class, sj.getLeftTableRef().getClass());
        Assert.assertEquals(TableRefFactor.class, sj.getRightTableRef().getClass());
        OuterJoin oj = (OuterJoin) sj.getLeftTableRef();
        using_list = oj.getUsing();
        Assert.assertEquals(2, using_list.size());
        Assert.assertEquals(
                "(SELECT `select` FROM `from`) AS TB1, tb3 AS T3, `select`, tb2 USE KEY FOR JOIN (i1, i2) LEFT JOIN tb4 USING (i1, i2) STRAIGHT_JOIN tb5",
                output);

        sql = "(`select`,(tb1 as t1 use index for join()ignore key for group by (i1)))" + "join tb2 on cd1=any "
                + "right join " + "tb3 straight_join "
                + "(tb4 use index() left outer join (tb6,tb7) on id3=all(select `all` from `all`)) "
                + " on id2=any(select * from any) using  (i1)";
        lexer = new MySQLLexer(sql);
        parser = getDMLParser(lexer);
        trs = parser.tableRefs();
        output = output2MySQL(trs, sql);
        list = trs.getTableReferenceList();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(OuterJoin.class, list.get(0).getClass());
        using_list = ((OuterJoin) list.get(0)).getUsing();
        Assert.assertEquals(1, using_list.size());
        Assert.assertEquals(InnerJoin.class, ((OuterJoin) (list.get(0))).getLeftTableRef().getClass());
        Assert.assertEquals(StraightJoin.class, ((OuterJoin) (list.get(0))).getRightTableRef().getClass());
        StraightJoin rsj = (StraightJoin) ((OuterJoin) (list.get(0))).getRightTableRef();
        Assert.assertEquals(TableRefFactor.class, rsj.getLeftTableRef().getClass());
        Assert.assertEquals(TableReferences.class, rsj.getRightTableRef().getClass());
        list = ((TableReferences) rsj.getRightTableRef()).getTableReferenceList();
        Assert.assertEquals(OuterJoin.class, list.get(0).getClass());
        Assert.assertEquals("(`select`, tb1 AS T1 USE INDEX FOR JOIN () IGNORE KEY FOR GROUP BY (i1)) "
                + "INNER JOIN tb2 ON cd1 = any RIGHT JOIN (tb3 STRAIGHT_JOIN (tb4 USE INDEX () "
                + "LEFT JOIN (tb6, tb7) ON id3 = ALL (SELECT `all` FROM `all`)) ON id2 = ANY (SELECT * FROM any))"
                + " USING (i1)", output);
    }

}
