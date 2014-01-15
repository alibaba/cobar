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
 * (created at 2012-5-30)
 */
package com.alibaba.cobar.route;

import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import junit.framework.Assert;

import com.alibaba.cobar.config.loader.SchemaLoader;
import com.alibaba.cobar.config.loader.xml.XMLSchemaLoader;
import com.alibaba.cobar.config.model.SchemaConfig;
import com.alibaba.cobar.config.util.ConfigException;
import com.alibaba.cobar.parser.ast.expression.primary.RowExpression;
import com.alibaba.cobar.parser.ast.stmt.SQLStatement;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLInsertStatement;
import com.alibaba.cobar.parser.recognizer.SQLParserDelegate;
import com.alibaba.cobar.route.config.RouteRuleInitializer;
import com.alibaba.cobar.route.util.PermutationUtil.PermutationGenerator;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class ServerRouteTest extends AbstractAliasConvert {

    protected Map<String, SchemaConfig> schemaMap;

    public ServerRouteTest() {
        String schemaFile = "/route/schema.xml";
        String ruleFile = "/route/rule.xml";
        SchemaLoader schemaLoader = new XMLSchemaLoader(schemaFile, ruleFile);
        try {
            RouteRuleInitializer.initRouteRule(schemaLoader);
        } catch (SQLSyntaxErrorException e) {
            throw new ConfigException(e);
        } catch (RuntimeException ee) {
            throw ee;
        }
        schemaMap = schemaLoader.getSchemas();
    }

    protected void setUp() throws Exception {
        // super.setUp();
        // schemaMap = CobarServer.getInstance().getConfig().getSchemas();
    }

    public void testRouteInsertShort() throws Exception {
        String sql = "inSErt into offer_detail (`offer_id`, gmt) values (123,now())";
        SchemaConfig schema = schemaMap.get("cndb");
        RouteResultset rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, rrs.getNodes()[0].getReplicaIndex());
        Assert.assertEquals("detail_dn[15]", rrs.getNodes()[0].getName());
        Assert.assertEquals(
                "inSErt into offer_detail (`offer_id`, gmt) values (123,now())",
                rrs.getNodes()[0].getStatement());

        sql = "inSErt into offer_detail ( gmt) values (now())";
        schema = schemaMap.get("cndb");
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(128, rrs.getNodes().length);

        sql = "inSErt into offer_detail (offer_id, gmt) values (123,now())";
        schema = schemaMap.get("cndb");
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, rrs.getNodes()[0].getReplicaIndex());
        Assert.assertEquals("detail_dn[15]", rrs.getNodes()[0].getName());
        Assert.assertEquals(
                "inSErt into offer_detail (offer_id, gmt) values (123,now())",
                rrs.getNodes()[0].getStatement());

        sql = "insert into offer(group_id,offer_id,member_id)values(234,123,'abc')";
        schema = schemaMap.get("cndb");
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, rrs.getNodes()[0].getReplicaIndex());
        Assert.assertEquals("offer_dn[12]", rrs.getNodes()[0].getName());
        Assert.assertEquals(
                "insert into offer(group_id,offer_id,member_id)values(234,123,'abc')",
                rrs.getNodes()[0].getStatement());

        sql = "insert into offer (group_id, offer_id, gmt) values (234,123,now())";
        schema = schemaMap.get("cndb");
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, rrs.getNodes()[0].getReplicaIndex());
        Assert.assertEquals("offer_dn[44]", rrs.getNodes()[0].getName());
        Assert.assertEquals(
                "insert into offer (group_id, offer_id, gmt) values (234,123,now())",
                rrs.getNodes()[0].getStatement());

        sql = "insert into offer (offer_id, group_id, gmt) values (123,234,now())";
        schema = schemaMap.get("cndb");
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, rrs.getNodes()[0].getReplicaIndex());
        Assert.assertEquals("offer_dn[44]", rrs.getNodes()[0].getName());
        Assert.assertEquals(
                "insert into offer (offer_id, group_id, gmt) values (123,234,now())",
                rrs.getNodes()[0].getStatement());

        sql = "insert into offer (offer_id, group_id, gmt) values (234,123,now())";
        schema = schemaMap.get("cndb");
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, rrs.getNodes()[0].getReplicaIndex());
        Assert.assertEquals("offer_dn[44]", rrs.getNodes()[0].getName());
        Assert.assertEquals(
                "insert into offer (offer_id, group_id, gmt) values (234,123,now())",
                rrs.getNodes()[0].getStatement());

        sql = "insert into wp_image (member_id,gmt) values ('pavarotti17',now())";
        schema = schemaMap.get("cndb");
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, rrs.getNodes()[0].getReplicaIndex());
        Assert.assertEquals("offer_dn[123]", rrs.getNodes()[0].getName());
        Assert.assertEquals(
                "insert into wp_image (member_id,gmt) values ('pavarotti17',now())",
                rrs.getNodes()[0].getStatement());

        sql = "insert low_priority into offer set offer_id=123,  group_id=234,gmt=now() on duplicate key update `dual`=1";
        schema = schemaMap.get("cndb");
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, rrs.getNodes()[0].getReplicaIndex());
        Assert.assertEquals("offer_dn[44]", rrs.getNodes()[0].getName());
        Assert.assertEquals(
                "insert low_priority into offer set offer_id=123,  group_id=234,gmt=now() on duplicate key update `dual`=1",
                rrs.getNodes()[0].getStatement());

        sql = "update ignore wp_image set name='abc',gmt=now()where `select`='abc'";
        schema = schemaMap.get("cndb");
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, rrs.getNodes()[0].getReplicaIndex());
        Assert.assertEquals("offer_dn[12]", rrs.getNodes()[0].getName());
        Assert.assertEquals(
                "update ignore wp_image set name='abc',gmt=now()where `select`='abc'",
                rrs.getNodes()[0].getStatement());

        sql = "delete from offer.*,wp_image.* using offer a,wp_image b where a.member_id=b.member_id and a.member_id='abc' ";
        schema = schemaMap.get("cndb");
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, rrs.getNodes()[0].getReplicaIndex());
        Assert.assertEquals("offer_dn[12]", rrs.getNodes()[0].getName());
        Assert.assertEquals(
                "delete from offer.*,wp_image.* using offer a,wp_image b where a.member_id=b.member_id and a.member_id='abc' ",
                rrs.getNodes()[0].getStatement());

    }

    private static Map<String, RouteResultsetNode> getNodeMap(RouteResultset rrs, int expectSize) {
        RouteResultsetNode[] routeNodes = rrs.getNodes();
        Assert.assertEquals(expectSize, routeNodes.length);
        Map<String, RouteResultsetNode> nodeMap = new HashMap<String, RouteResultsetNode>(expectSize, 1);
        for (int i = 0; i < expectSize; i++) {
            RouteResultsetNode routeNode = routeNodes[i];
            nodeMap.put(routeNode.getName(), routeNode);
        }
        Assert.assertEquals(expectSize, nodeMap.size());
        return nodeMap;
    }

    private static interface NodeNameDeconstructor {
        public int getNodeIndex(String name);
    }

    private static class NodeNameAsserter implements NodeNameDeconstructor {
        private String[] expectNames;

        public NodeNameAsserter() {
        }

        public NodeNameAsserter(String... expectNames) {
            Assert.assertNotNull(expectNames);
            this.expectNames = expectNames;
        }

        protected void setNames(String[] expectNames) {
            Assert.assertNotNull(expectNames);
            this.expectNames = expectNames;
        }

        public void assertRouteNodeNames(Collection<String> nodeNames) {
            Assert.assertNotNull(nodeNames);
            Assert.assertEquals(expectNames.length, nodeNames.size());
            for (String name : expectNames) {
                Assert.assertTrue(nodeNames.contains(name));
            }
        }

        @Override
        public int getNodeIndex(String name) {
            for (int i = 0; i < expectNames.length; ++i) {
                if (name.equals(expectNames[i])) {
                    return i;
                }
            }
            throw new NoSuchElementException("route node " + name + " dosn't exist!");
        }
    }

    private static class IndexedNodeNameAsserter extends NodeNameAsserter {
        /**
         * @param from included
         * @param to excluded
         */
        public IndexedNodeNameAsserter(String prefix, int from, int to) {
            super();
            String[] names = new String[to - from];
            for (int i = 0; i < names.length; ++i) {
                names[i] = prefix + "[" + (i + from) + "]";
            }
            setNames(names);
        }
    }

    private static interface ReplicaAsserter {
        public void assertReplica(Integer nodeIndex, Integer replica);
    }

    private static class RouteNodeAsserter {
        private NodeNameDeconstructor deconstructor;
        private SQLAsserter sqlAsserter;
        private ReplicaAsserter replicaAsserter;

        public RouteNodeAsserter(NodeNameDeconstructor deconstructor, SQLAsserter sqlAsserter) {
            this.deconstructor = deconstructor;
            this.sqlAsserter = sqlAsserter;
            this.replicaAsserter = new ReplicaAsserter() {
                @Override
                public void assertReplica(Integer nodeIndex, Integer replica) {
                    Assert.assertEquals(RouteResultsetNode.DEFAULT_REPLICA_INDEX, replica);
                }
            };
        }

        public RouteNodeAsserter(NodeNameDeconstructor deconstructor, SQLAsserter sqlAsserter,
                                 ReplicaAsserter replicaAsserter) {
            this.deconstructor = deconstructor;
            this.sqlAsserter = sqlAsserter;
            this.replicaAsserter = replicaAsserter;
        }

        public void assertNode(RouteResultsetNode node) throws Exception {
            int nodeIndex = deconstructor.getNodeIndex(node.getName());
            sqlAsserter.assertSQL(node.getStatement(), nodeIndex);
            replicaAsserter.assertReplica(nodeIndex, node.getReplicaIndex());
        }
    }

    private static interface SQLAsserter {
        public void assertSQL(String sql, int nodeIndex) throws Exception;
    }

    private static class SimpleSQLAsserter implements SQLAsserter {
        private Map<Integer, Set<String>> map = new HashMap<Integer, Set<String>>();

        public SimpleSQLAsserter addExpectSQL(int nodeIndex, String sql) {
            Set<String> set = map.get(nodeIndex);
            if (set == null) {
                set = new HashSet<String>();
                map.put(nodeIndex, set);
            }
            set.add(sql);
            return this;
        }

        public SimpleSQLAsserter addExpectSQL(int nodeIndex, String... sql) {
            for (String s : sql) {
                addExpectSQL(nodeIndex, s);
            }
            return this;
        }

        public SimpleSQLAsserter addExpectSQL(int nodeIndex, String prefix, PermutationGenerator pg, String suffix) {
            Set<String> ss = pg.permutateSQL();
            for (String s : ss) {
                addExpectSQL(nodeIndex, prefix + s + suffix);
            }
            return this;
        }

        @Override
        public void assertSQL(String sql, int nodeIndex) throws Exception {
            Assert.assertNotNull(map.get(nodeIndex));
            Assert.assertTrue(map.get(nodeIndex).contains(sql));
        }
    }

    private static abstract class ParseredSQLAsserter implements SQLAsserter {
        @Override
        public void assertSQL(String sql, int nodeIndex) throws Exception {
            SQLStatement stmt = SQLParserDelegate.parse(sql);
            assertAST(stmt, nodeIndex);
        }

        protected abstract void assertAST(SQLStatement stmt, int nodeIndex);
    }

    public void testRouteInsertLong() throws Exception {
        StringBuilder sb = new StringBuilder("insert into offer_detail (offer_id, gmt) values ");
        for (int i = 0; i < 1024; ++i) {
            if (i > 0)
                sb.append(", ");
            sb.append("(" + i + ", now())");
        }
        SchemaConfig schema = schemaMap.get("cndb");
        RouteResultset rrs = ServerRouter.route(schema, sb.toString(), null, null);
        Assert.assertEquals(-1l, rrs.getLimitSize());

        Map<String, RouteResultsetNode> nodeMap = getNodeMap(rrs, 128);
        IndexedNodeNameAsserter nameAsserter = new IndexedNodeNameAsserter("detail_dn", 0, 128);
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        RouteNodeAsserter asserter = new RouteNodeAsserter(nameAsserter, new ParseredSQLAsserter() {
            @Override
            protected void assertAST(SQLStatement stmt, int nodeIndex) {
                DMLInsertStatement insert = (DMLInsertStatement) stmt;
                List<RowExpression> rows = insert.getRowList();
                Assert.assertNotNull(rows);
                Assert.assertEquals(8, rows.size());
                List<Integer> vals = new ArrayList<Integer>(8);
                for (RowExpression row : rows) {
                    int val = ((Number) row.getRowExprList().get(0).evaluation(null)).intValue();
                    vals.add(val);
                }
                Assert.assertEquals(8, vals.size());
                for (int i = 8 * nodeIndex; i < 8 * nodeIndex + 8; ++i) {
                    Assert.assertTrue(vals.contains(i));
                }
            }
        });
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }
    }

    public void testRoute() throws Exception {
        String sql = "select * from offer.wp_image where member_id='pavarotti17' or member_id='1qq'";
        SchemaConfig schema = schemaMap.get("cndb");
        RouteResultset rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, rrs.getNodes()[0].getReplicaIndex());

        Map<String, RouteResultsetNode> nodeMap = getNodeMap(rrs, 2);
        NodeNameAsserter nameAsserter = new NodeNameAsserter("offer_dn[123]", "offer_dn[66]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        SimpleSQLAsserter sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(0, "SELECT * FROM wp_image WHERE member_id = 'pavarotti17' OR FALSE").addExpectSQL(
                1,
                "SELECT * FROM wp_image WHERE FALSE OR member_id = '1qq'");
        RouteNodeAsserter asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "select * from independent where member='abc'";
        schema = schemaMap.get("cndb");
        rrs = ServerRouter.route(schema, sql, null, null);
        nodeMap = getNodeMap(rrs, 128);
        nameAsserter = new IndexedNodeNameAsserter("independent_dn", 0, 128);
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        for (int i = 0; i < 128; ++i) {
            sqlAsserter.addExpectSQL(i, "select * from independent where member='abc'");
        }
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "select * from independent A where cndb.a.member='abc'";
        schema = schemaMap.get("cndb");
        rrs = ServerRouter.route(schema, sql, null, null);
        nodeMap = getNodeMap(rrs, 128);
        nameAsserter = new IndexedNodeNameAsserter("independent_dn", 0, 128);
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        for (int i = 0; i < 128; ++i) {
            sqlAsserter.addExpectSQL(i, "SELECT * FROM independent AS A WHERE a.member = 'abc'");
        }
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "select * from tb where member='abc'";
        schema = schemaMap.get("cndb");
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, rrs.getNodes()[0].getReplicaIndex());
        Assert.assertEquals("cndb_dn", rrs.getNodes()[0].getName());
        Assert.assertEquals("select * from tb where member='abc'", rrs.getNodes()[0].getStatement());

        sql = "select * from offer.wp_image where member_id is null";
        schema = schemaMap.get("cndb");
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, rrs.getNodes()[0].getReplicaIndex());
        Assert.assertEquals("offer_dn[48]", rrs.getNodes()[0].getName());
        Assert.assertEquals("SELECT * FROM wp_image WHERE member_id IS NULL", rrs.getNodes()[0].getStatement());

        sql = "select * from offer.wp_image where member_id between 'pavarotti17' and 'pavarotti17'";
        schema = schemaMap.get("cndb");
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, rrs.getNodes()[0].getReplicaIndex());
        Assert.assertEquals("offer_dn[123]", rrs.getNodes()[0].getName());
        Assert.assertEquals(
                "SELECT * FROM wp_image WHERE member_id BETWEEN 'pavarotti17' AND 'pavarotti17'",
                rrs.getNodes()[0].getStatement());

        sql = "select * from  offer A where a.member_id='abc' union select * from product_visit b where B.offer_id =123";
        schema = schemaMap.get("cndb");
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(128, rrs.getNodes().length);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        for (int i = 0; i < 128; i++) {
            Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, rrs.getNodes()[i].getReplicaIndex());
            Assert.assertEquals("offer_dn[" + i + "]", rrs.getNodes()[i].getName());
            Assert.assertEquals(
                    "select * from  offer A where a.member_id='abc' union select * from product_visit b where B.offer_id =123",
                    rrs.getNodes()[i].getStatement());
        }

        sql = "update offer.offer a join offer_detail b set id=123 where a.offer_id=b.offer_id and a.offer_id=123 and group_id=234";
        schema = schemaMap.get("cndb");
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, rrs.getNodes()[0].getReplicaIndex());
        Assert.assertEquals("offer_dn[44]", rrs.getNodes()[0].getName());
        Assert.assertEquals(
                "UPDATE offer AS " + aliasConvert("a") + " INNER JOIN offer_detail AS " + aliasConvert("b")
                        + " SET id = 123 WHERE a.offer_id = b.offer_id AND a.offer_id = 123 AND group_id = 234",
                rrs.getNodes()[0].getStatement());

        sql = "update    offer./*kjh*/offer a join offer_detail B set id:=123 where A.offer_id=b.offer_id and b.offer_id=123 and group_id=234";
        schema = schemaMap.get("cndb");
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, rrs.getNodes()[0].getReplicaIndex());
        Assert.assertEquals("detail_dn[15]", rrs.getNodes()[0].getName());
        Assert.assertEquals(
                "UPDATE offer AS " + aliasConvert("a") + " INNER JOIN offer_detail AS " + aliasConvert("b")
                        + " SET id = 123 WHERE A.offer_id = b.offer_id AND b.offer_id = 123 AND group_id = 234",
                rrs.getNodes()[0].getStatement());

        sql = "select * from offer.wp_image where member_id in ('pavarotti17', 'qaa') or offer.wp_image.member_id='1qq'";
        schema = schemaMap.get("cndb");
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        nodeMap = getNodeMap(rrs, 3);
        nameAsserter = new NodeNameAsserter("offer_dn[123]", "offer_dn[10]", "offer_dn[66]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(0, "SELECT * FROM wp_image WHERE member_id IN ('pavarotti17') OR FALSE")
                   .addExpectSQL(1, "SELECT * FROM wp_image WHERE member_id IN ('qaa') OR FALSE")
                   .addExpectSQL(2, "SELECT * FROM wp_image WHERE FALSE OR wp_image.member_id = '1qq'");
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "select * from offer.wp_image,tb2 as t2 where member_id in ('pavarotti17', 'qaa') or offer.wp_image.member_id='1qq'";
        schema = schemaMap.get("cndb");
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(3, rrs.getNodes().length);
        Assert.assertEquals(-1l, rrs.getLimitSize());

        nodeMap = getNodeMap(rrs, 3);
        nameAsserter = new NodeNameAsserter("offer_dn[123]", "offer_dn[10]", "offer_dn[66]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(
                           0,
                           "SELECT * FROM wp_image, tb2 AS " + aliasConvert("t2")
                                   + " WHERE member_id IN ('pavarotti17') OR FALSE")
                   .addExpectSQL(
                           1,
                           "SELECT * FROM wp_image, tb2 AS " + aliasConvert("t2")
                                   + " WHERE member_id IN ('qaa') OR FALSE")
                   .addExpectSQL(
                           2,
                           "SELECT * FROM wp_image, tb2 AS " + aliasConvert("t2")
                                   + " WHERE FALSE OR wp_image.member_id = '1qq'");
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "select * from offer.wp_image,tb2 as t2 where member_id in ('pavarotti17', 'sf', 's22f', 'sdddf', 'sd') ";
        schema = schemaMap.get("cndb");
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        nodeMap = getNodeMap(rrs, 4);
        nameAsserter = new NodeNameAsserter("offer_dn[123]", "offer_dn[126]", "offer_dn[74]", "offer_dn[26]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(
                           0,
                           "SELECT * FROM wp_image, tb2 AS " + aliasConvert("t2")
                                   + " WHERE member_id IN ('pavarotti17')")
                   .addExpectSQL(
                           1,
                           "SELECT * FROM wp_image, tb2 AS " + aliasConvert("t2") + " WHERE member_id IN ('sdddf')")
                   .addExpectSQL(
                           2,
                           "SELECT * FROM wp_image, tb2 AS " + aliasConvert("t2") + " WHERE member_id IN ('sf', 'sd')",
                           "SELECT * FROM wp_image, tb2 AS " + aliasConvert("t2") + " WHERE member_id IN ('sd', 'sf')")
                   .addExpectSQL(
                           3,
                           "SELECT * FROM wp_image, tb2 AS " + aliasConvert("t2") + " WHERE member_id IN ('s22f')");
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "select * from tb2 as t2 ,offer.wp_image where member_id in ('pavarotti17', 'qaa') or offer.wp_image.member_id='1qq'";
        schema = schemaMap.get("cndb");
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        nodeMap = getNodeMap(rrs, 3);
        nameAsserter = new NodeNameAsserter("offer_dn[123]", "offer_dn[10]", "offer_dn[66]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(
                           0,
                           "SELECT * FROM tb2 AS " + aliasConvert("t2")
                                   + ", wp_image WHERE member_id IN ('pavarotti17') OR FALSE")
                   .addExpectSQL(
                           1,
                           "SELECT * FROM tb2 AS " + aliasConvert("t2")
                                   + ", wp_image WHERE member_id IN ('qaa') OR FALSE")
                   .addExpectSQL(
                           2,
                           "SELECT * FROM tb2 AS " + aliasConvert("t2")
                                   + ", wp_image WHERE FALSE OR wp_image.member_id = '1qq'");
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "select * from tb2 as t2 ,offer.wp_image where member_id in ('pavarotti17', 'qaa') or offer.wp_image.member_id='1qq' and t2.member_id='123'";
        schema = schemaMap.get("cndb");
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        nodeMap = getNodeMap(rrs, 3);
        nameAsserter = new NodeNameAsserter("offer_dn[123]", "offer_dn[10]", "offer_dn[66]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(
                           0,
                           "SELECT * FROM tb2 AS " + aliasConvert("t2")
                                   + ", wp_image WHERE member_id IN ('pavarotti17') OR FALSE AND t2.member_id = '123'")
                   .addExpectSQL(
                           1,
                           "SELECT * FROM tb2 AS " + aliasConvert("t2")
                                   + ", wp_image WHERE member_id IN ('qaa') OR FALSE AND t2.member_id = '123'")
                   .addExpectSQL(
                           2,
                           "SELECT * FROM tb2 AS " + aliasConvert("t2")
                                   + ", wp_image WHERE FALSE OR wp_image.member_id = '1qq' AND t2.member_id = '123'");
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "select * from wp_image wB inner join offer.offer o on wB.member_id=O.member_ID where wB.member_iD='pavarotti17' and o.id=3";
        schema = schemaMap.get("cndb");
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, rrs.getNodes()[0].getReplicaIndex());
        Assert.assertEquals("offer_dn[123]", rrs.getNodes()[0].getName());
        Assert.assertEquals(
                "SELECT * FROM wp_image AS " + aliasConvert("wB") + " INNER JOIN offer AS " + aliasConvert("o")
                        + " ON wB.member_id = O.member_ID WHERE wB.member_iD = 'pavarotti17' AND o.id = 3",
                rrs.getNodes()[0].getStatement());

        sql = "select * from wp_image w inner join offer o on w.member_id=O.member_ID where w.member_iD in ('pavarotti17','13') and o.id=3";
        schema = schemaMap.get("cndb");
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        nodeMap = getNodeMap(rrs, 2);
        nameAsserter = new NodeNameAsserter("offer_dn[123]", "offer_dn[68]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(
                           0,
                           "SELECT * FROM wp_image AS " + aliasConvert("w") + " INNER JOIN offer AS "
                                   + aliasConvert("o")
                                   + " ON w.member_id = O.member_ID WHERE w.member_iD IN ('pavarotti17') AND o.id = 3")
                   .addExpectSQL(
                           1,
                           "SELECT * FROM wp_image AS " + aliasConvert("w") + " INNER JOIN offer AS "
                                   + aliasConvert("o")
                                   + " ON w.member_id = O.member_ID WHERE w.member_iD IN ('13') AND o.id = 3");
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "insert into wp_image (member_id,gmt) values ('pavarotti17',now()),('123',now())";
        schema = schemaMap.get("cndb");
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        nodeMap = getNodeMap(rrs, 2);
        nameAsserter = new NodeNameAsserter("offer_dn[123]", "offer_dn[70]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(0, "INSERT INTO wp_image (member_id, gmt) VALUES ('pavarotti17', NOW())")
                   .addExpectSQL(1, "INSERT INTO wp_image (member_id, gmt) VALUES ('123', NOW())");
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }
    }

    public void testDuplicatePartitionKey() throws Exception {
        String sql = "select * from offer.wp_image where member_id in ('pavarotti17', 'qaa') or offer.wp_image.member_id='1qq' or member_id='1qq'";
        SchemaConfig schema = schemaMap.get("cndb");
        RouteResultset rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        Map<String, RouteResultsetNode> nodeMap = getNodeMap(rrs, 3);
        NodeNameAsserter nameAsserter = new NodeNameAsserter("offer_dn[123]", "offer_dn[10]", "offer_dn[66]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        SimpleSQLAsserter sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(0, "SELECT * FROM wp_image WHERE member_id IN ('pavarotti17') OR FALSE OR FALSE")
                   .addExpectSQL(1, "SELECT * FROM wp_image WHERE member_id IN ('qaa') OR FALSE OR FALSE")
                   .addExpectSQL(
                           2,
                           "SELECT * FROM wp_image WHERE FALSE OR wp_image.member_id = '1qq' OR member_id = '1qq'");
        RouteNodeAsserter asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "insert into wp_image (id, member_id, gmt) values (1,'pavarotti17',now()),(2,'pavarotti17',now()),(3,'qaa',now())";
        schema = schemaMap.get("cndb");
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        nodeMap = getNodeMap(rrs, 2);
        nameAsserter = new NodeNameAsserter("offer_dn[123]", "offer_dn[10]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(
                           0,
                           "INSERT INTO wp_image (id, member_id, gmt) VALUES (2, 'pavarotti17', NOW()), (1, 'pavarotti17', NOW())",
                           "INSERT INTO wp_image (id, member_id, gmt) VALUES (1, 'pavarotti17', NOW()), (2, 'pavarotti17', NOW())")
                   .addExpectSQL(1, "INSERT INTO wp_image (id, member_id, gmt) VALUES (3, 'qaa', NOW())");
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "select * from offer.wp_image where member_id in ('pavarotti17','pavarotti17', 'qaa') or offer.wp_image.member_id='pavarotti17'";
        schema = schemaMap.get("cndb");
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        Assert.assertEquals(2, rrs.getNodes().length);
        nodeMap = getNodeMap(rrs, 2);
        nameAsserter = new NodeNameAsserter("offer_dn[123]", "offer_dn[10]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(
                           0,
                           "SELECT * FROM wp_image WHERE member_id IN ('pavarotti17', 'pavarotti17') OR wp_image.member_id = 'pavarotti17'")
                   .addExpectSQL(1, "SELECT * FROM wp_image WHERE member_id IN ('qaa') OR FALSE");
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "select * from offer.`wp_image` where `member_id` in ('pavarotti17','pavarotti17', 'qaa') or member_id in ('pavarotti17','1qq','pavarotti17') or offer.wp_image.member_id='pavarotti17'";
        schema = schemaMap.get("cndb");
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        Assert.assertEquals(3, rrs.getNodes().length);
        nodeMap = getNodeMap(rrs, 3);
        nameAsserter = new NodeNameAsserter("offer_dn[123]", "offer_dn[10]", "offer_dn[66]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(
                           0,
                           "SELECT * FROM `wp_image` WHERE `member_id` IN ('pavarotti17', 'pavarotti17') OR member_id IN ('pavarotti17', 'pavarotti17') OR wp_image.member_id = 'pavarotti17'")
                   .addExpectSQL(1, "SELECT * FROM `wp_image` WHERE `member_id` IN ('qaa') OR FALSE OR FALSE")
                   .addExpectSQL(2, "SELECT * FROM `wp_image` WHERE FALSE OR member_id IN ('1qq') OR FALSE");
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "insert into offer_detail (offer_id, gmt) values (123,now()),(123,now()+1),(234,now()),(123,now()),(345,now()),(122+1,now()),(456,now())";
        schema = schemaMap.get("cndb");
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        nodeMap = getNodeMap(rrs, 4);
        nameAsserter = new NodeNameAsserter("detail_dn[29]", "detail_dn[43]", "detail_dn[57]", "detail_dn[15]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(0, "INSERT INTO offer_detail (offer_id, gmt) VALUES (234, NOW())")
                   .addExpectSQL(1, "INSERT INTO offer_detail (offer_id, gmt) VALUES (345, NOW())")
                   .addExpectSQL(2, "INSERT INTO offer_detail (offer_id, gmt) VALUES (456, NOW())")
                   .addExpectSQL(
                           3,
                           "INSERT INTO offer_detail (offer_id, gmt) VALUES ",
                           new PermutationGenerator(
                                   "(123, NOW())",
                                   "(123, NOW() + 1)",
                                   "(122 + 1, NOW())",
                                   "(123, NOW())").setDelimiter(", "),
                           "");
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "insert into offer (offer_id, group_id, gmt) values "
                + "(123, 123, now()),(123, 234, now()),(123, 345, now()),(123, 456, now())"
                + ",(234, 123, now()),(234, 234, now()),(234, 345, now()),(234, 456, now())"
                + ",(345, 123, now()),(345, 234, now()),(345, 345, now()),(345, 456, now())"
                + ",(456, 123, now()),(456, 234, now()),(456, 345, now()),(456, 456, now())";
        schema = schemaMap.get("cndb");
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        nodeMap = getNodeMap(rrs, 7);
        nameAsserter = new NodeNameAsserter(
                "offer_dn[58]",
                "offer_dn[100]",
                "offer_dn[86]",
                "offer_dn[72]",
                "offer_dn[114]",
                "offer_dn[44]",
                "offer_dn[30]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(
                           0,
                           "INSERT INTO offer (offer_id, group_id, gmt) VALUES ",
                           new PermutationGenerator("(345, 123, NOW())", "(123, 345, NOW())", "(234, 234, NOW())").setDelimiter(", "),
                           "")
                   .addExpectSQL(
                           1,
                           "INSERT INTO offer (offer_id, group_id, gmt) VALUES ",
                           new PermutationGenerator("(345, 456, NOW())", "(456, 345, NOW())").setDelimiter(", "),
                           "")
                   .addExpectSQL(
                           2,
                           "INSERT INTO offer (offer_id, group_id, gmt) VALUES ",
                           new PermutationGenerator("(456, 234, NOW())", "(234, 456, NOW())", "(345, 345, NOW())").setDelimiter(", "),
                           "")
                   .addExpectSQL(
                           3,
                           "INSERT INTO offer (offer_id, group_id, gmt) VALUES ",
                           new PermutationGenerator(
                                   "(123, 456, NOW())",
                                   "(345, 234, NOW())",
                                   "(234, 345, NOW())",
                                   "(456, 123, NOW())").setDelimiter(", "),
                           "")
                   .addExpectSQL(
                           4,
                           "INSERT INTO offer (offer_id, group_id, gmt) VALUES ",
                           new PermutationGenerator("(456, 456, NOW())").setDelimiter(", "),
                           "")
                   .addExpectSQL(
                           5,
                           "INSERT INTO offer (offer_id, group_id, gmt) VALUES ",
                           new PermutationGenerator("(234, 123, NOW())", "(123, 234, NOW())").setDelimiter(", "),
                           "")
                   .addExpectSQL(
                           6,
                           "INSERT INTO offer (offer_id, group_id, gmt) VALUES ",
                           new PermutationGenerator("(123, 123, NOW())").setDelimiter(", "),
                           "");
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "select * from offer where (offer_id, group_id ) = (123,234)";
        schema = schemaMap.get("cndb");
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        Assert.assertEquals(128, rrs.getNodes().length);
        for (int i = 0; i < 128; i++) {
            Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, rrs.getNodes()[i].getReplicaIndex());
            Assert.assertEquals("offer_dn[" + i + "]", rrs.getNodes()[i].getName());
            Assert.assertEquals(
                    "select * from offer where (offer_id, group_id ) = (123,234)",
                    rrs.getNodes()[i].getStatement());
        }

        sql = "select * from offer where offer_id=123 and group_id=234";
        schema = schemaMap.get("cndb");
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, rrs.getNodes()[0].getReplicaIndex());
        Assert.assertEquals("offer_dn[44]", rrs.getNodes()[0].getName());
        Assert.assertEquals("select * from offer where offer_id=123 and group_id=234", rrs.getNodes()[0].getStatement());

        // WITHOUT SQL CHANGE unless schema is appeared
        sql = "select * from  cndb.offer where false"
                + " or offer_id=123 and group_id=123 or offer_id=123 and group_id=234 or offer_id=123 and group_id=345 or offer_id=123 and group_id=456  "
                + " or offer_id=234 and group_id=123 or offer_id=234 and group_id=234 or offer_id=234 and group_id=345 or offer_id=234 and group_id=456  "
                + " or offer_id=345 and group_id=123 or offer_id=345 and group_id=234 or offer_id=345 and group_id=345 or offer_id=345 and group_id=456  "
                + " or offer_id=456 and group_id=123 or offer_id=456 and group_id=234 or offer_id=456 and group_id=345 or offer_id=456 and group_id=456  ";
        schema = schemaMap.get("cndb");
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        String sqlTemp = "SELECT * FROM offer WHERE FALSE OR offer_id = 123 AND group_id = 123 OR offer_id = 123 AND group_id = 234 OR offer_id = 123 AND group_id = 345 OR offer_id = 123 AND group_id = 456 OR offer_id = 234 AND group_id = 123 OR offer_id = 234 AND group_id = 234 OR offer_id = 234 AND group_id = 345 OR offer_id = 234 AND group_id = 456 OR offer_id = 345 AND group_id = 123 OR offer_id = 345 AND group_id = 234 OR offer_id = 345 AND group_id = 345 OR offer_id = 345 AND group_id = 456 OR offer_id = 456 AND group_id = 123 OR offer_id = 456 AND group_id = 234 OR offer_id = 456 AND group_id = 345 OR offer_id = 456 AND group_id = 456";
        nodeMap = getNodeMap(rrs, 7);
        nameAsserter = new NodeNameAsserter(
                "offer_dn[58]",
                "offer_dn[100]",
                "offer_dn[86]",
                "offer_dn[72]",
                "offer_dn[114]",
                "offer_dn[44]",
                "offer_dn[30]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(0, sqlTemp)
                   .addExpectSQL(1, sqlTemp)
                   .addExpectSQL(2, sqlTemp)
                   .addExpectSQL(3, sqlTemp)
                   .addExpectSQL(4, sqlTemp)
                   .addExpectSQL(5, sqlTemp)
                   .addExpectSQL(6, sqlTemp);
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "select * from  offer where false" + " or offer_id=123 and group_id=123"
                + " or group_id=123 and offer_id=234" + " or offer_id=123 and group_id=345"
                + " or offer_id=123 and group_id=456  ";
        schema = schemaMap.get("cndb");
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        sqlTemp = "select * from  offer where false or offer_id=123 and group_id=123 or group_id=123 and offer_id=234 or offer_id=123 and group_id=345 or offer_id=123 and group_id=456  ";
        nodeMap = getNodeMap(rrs, 4);
        nameAsserter = new NodeNameAsserter("offer_dn[72]", "offer_dn[58]", "offer_dn[44]", "offer_dn[30]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(0, sqlTemp).addExpectSQL(1, sqlTemp).addExpectSQL(2, sqlTemp).addExpectSQL(3, sqlTemp);
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }
    }

    public void testGroupLimit() throws Exception {
        final SchemaConfig schema = schemaMap.get("cndb");

        String sql = "select count(*) from wp_image where member_id = 'pavarotti17'";
        RouteResultset rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1L, rrs.getLimitSize());
        Assert.assertEquals(0, rrs.getFlag());
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, rrs.getNodes()[0].getReplicaIndex());
        Assert.assertEquals("offer_dn[123]", rrs.getNodes()[0].getName());
        Assert.assertEquals(
                "select count(*) from wp_image where member_id = 'pavarotti17'",
                rrs.getNodes()[0].getStatement());

        sql = "select count(*) from wp_image where member_id in ('pavarotti17','qaa')";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1L, rrs.getLimitSize());
        if (rrs.getNodes().length > 1)
            Assert.assertEquals(RouteResultset.SUM_FLAG, rrs.getFlag());
        Map<String, RouteResultsetNode> nodeMap = getNodeMap(rrs, 2);
        NodeNameAsserter nameAsserter = new NodeNameAsserter("offer_dn[123]", "offer_dn[10]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        SimpleSQLAsserter sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(0, "SELECT COUNT(*) FROM wp_image WHERE member_id IN ('pavarotti17')").addExpectSQL(
                1,
                "SELECT COUNT(*) FROM wp_image WHERE member_id IN ('qaa')");
        RouteNodeAsserter asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "select min(id) from wp_image where member_id in ('pavarotti17','qaa') limit 99";
        rrs = ServerRouter.route(schema, sql, null, null);
        if (rrs.getNodes().length > 1)
            Assert.assertEquals(RouteResultset.MIN_FLAG, rrs.getFlag());
        if (rrs.getNodes().length > 1)
            Assert.assertEquals(99L, rrs.getLimitSize());
        nodeMap = getNodeMap(rrs, 2);
        nameAsserter = new NodeNameAsserter("offer_dn[123]", "offer_dn[10]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(0, "SELECT MIN(id) FROM wp_image WHERE member_id IN ('pavarotti17') LIMIT 0, 99")
                   .addExpectSQL(1, "SELECT MIN(id) FROM wp_image WHERE member_id IN ('qaa') LIMIT 0, 99");
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "select max(offer_id) from offer.wp_image where member_id in ('pavarotti17','pavarotti17', 'qaa') or member_id in ('pavarotti17','1qq','pavarotti17') or offer.wp_image.member_id='pavarotti17' limit 99 offset 1";
        rrs = ServerRouter.route(schema, sql, null, null);
        if (rrs.getNodes().length > 1)
            Assert.assertEquals(RouteResultset.MAX_FLAG, rrs.getFlag());
        if (rrs.getNodes().length > 1)
            Assert.assertEquals(99L, rrs.getLimitSize());
        nodeMap = getNodeMap(rrs, 3);
        nameAsserter = new NodeNameAsserter("offer_dn[123]", "offer_dn[10]", "offer_dn[66]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(
                           0,
                           "SELECT MAX(offer_id) FROM wp_image WHERE member_id IN ('pavarotti17', 'pavarotti17') OR member_id IN ('pavarotti17', 'pavarotti17') OR wp_image.member_id = 'pavarotti17' LIMIT 1, 99")
                   .addExpectSQL(
                           1,
                           "SELECT MAX(offer_id) FROM wp_image WHERE member_id IN ('qaa') OR FALSE OR FALSE LIMIT 1, 99")
                   .addExpectSQL(
                           2,
                           "SELECT MAX(offer_id) FROM wp_image WHERE FALSE OR member_id IN ('1qq') OR FALSE LIMIT 1, 99");
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "select count(*) from (select * from wp_image) w, (select * from offer) o "
                + " where o.member_id=w.member_id and o.member_id='pavarotti17' limit 99";
        rrs = ServerRouter.route(schema, sql, null, null);
        if (rrs.getNodes().length > 1)
            Assert.assertEquals(RouteResultset.SUM_FLAG, rrs.getFlag());
        if (rrs.getNodes().length > 1)
            Assert.assertEquals(99L, rrs.getLimitSize());
        nodeMap = getNodeMap(rrs, 128);
        nameAsserter = new IndexedNodeNameAsserter("offer_dn", 0, 128);
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        for (int i = 0; i < 128; ++i) {
            sqlAsserter.addExpectSQL(
                    i,
                    "select count(*) from (select * from wp_image) w, (select * from offer) o  where o.member_id=w.member_id and o.member_id='pavarotti17' limit 99");
        }
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "select count(*) from (select * from wp_image) w, (select * from offer limit 99) o "
                + " where o.member_id=w.member_id and o.member_id='pavarotti17' ";
        rrs = ServerRouter.route(schema, sql, null, null);
        if (rrs.getNodes().length > 1)
            Assert.assertEquals(RouteResultset.SUM_FLAG, rrs.getFlag());
        if (rrs.getNodes().length > 1)
            Assert.assertEquals(99L, rrs.getLimitSize());
        nodeMap = getNodeMap(rrs, 128);
        nameAsserter = new IndexedNodeNameAsserter("offer_dn", 0, 128);
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        for (int i = 0; i < 128; ++i) {
            sqlAsserter.addExpectSQL(
                    i,
                    "select count(*) from (select * from wp_image) w, (select * from offer limit 99) o  where o.member_id=w.member_id and o.member_id='pavarotti17' ");
        }
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "select count(*) from (select * from wp_image where member_id='abc' or member_id='pavarotti17' limit 100) w, (select * from offer_detail where offer_id='123') o "
                + " where o.member_id=w.member_id and o.member_id='pavarotti17' limit 99";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(RouteResultset.SUM_FLAG, rrs.getFlag());
        Assert.assertEquals(100L, rrs.getLimitSize());
        nodeMap = getNodeMap(rrs, 2);
        nameAsserter = new NodeNameAsserter("offer_dn[12]", "offer_dn[123]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(
                0,
                "SELECT COUNT(*) FROM (SELECT * FROM wp_image WHERE member_id = 'abc' OR FALSE LIMIT 0, 100) AS "
                        + aliasConvert("w") + ", (SELECT * FROM offer_detail WHERE offer_id = '123') AS "
                        + aliasConvert("o")
                        + " WHERE o.member_id = w.member_id AND o.member_id = 'pavarotti17' LIMIT 0, 99").addExpectSQL(
                1,
                "SELECT COUNT(*) FROM (SELECT * FROM wp_image WHERE FALSE OR member_id = 'pavarotti17' LIMIT 0, 100) AS "
                        + aliasConvert("w") + ", (SELECT * FROM offer_detail WHERE offer_id = '123') AS "
                        + aliasConvert("o")
                        + " WHERE o.member_id = w.member_id AND o.member_id = 'pavarotti17' LIMIT 0, 99");
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "select count(*) from (select * from(select * from offer_detail where offer_id='123' or offer_id='234' limit 88)offer  where offer.member_id='abc' limit 60) w "
                + " where w.member_id ='pavarotti17' limit 99";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(88L, rrs.getLimitSize());
        Assert.assertEquals(RouteResultset.SUM_FLAG, rrs.getFlag());
        nodeMap = getNodeMap(rrs, 2);
        nameAsserter = new NodeNameAsserter("detail_dn[29]", "detail_dn[15]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(
                0,
                "SELECT COUNT(*) FROM (SELECT * FROM (SELECT * FROM offer_detail WHERE FALSE OR offer_id = '234' LIMIT 0, 88) AS "
                        + aliasConvert("offer") + " WHERE offer.member_id = 'abc' LIMIT 0, 60) AS " + aliasConvert("w")
                        + " WHERE w.member_id = 'pavarotti17' LIMIT 0, 99").addExpectSQL(
                1,
                "SELECT COUNT(*) FROM (SELECT * FROM (SELECT * FROM offer_detail WHERE offer_id = '123' OR FALSE LIMIT 0, 88) AS "
                        + aliasConvert("offer") + " WHERE offer.member_id = 'abc' LIMIT 0, 60) AS " + aliasConvert("w")
                        + " WHERE w.member_id = 'pavarotti17' LIMIT 0, 99");
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "select count(*) from (select * from(select max(id) from offer_detail where offer_id='123' or offer_id='234' limit 88)offer  where offer.member_id='abc' limit 60) w "
                + " where w.member_id ='pavarotti17' limit 99";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(88L, rrs.getLimitSize());
        Assert.assertEquals(0, rrs.getFlag());
        nodeMap = getNodeMap(rrs, 2);
        nameAsserter = new NodeNameAsserter("detail_dn[29]", "detail_dn[15]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(
                           0,
                           "SELECT COUNT(*) FROM (SELECT * FROM (SELECT MAX(id) FROM offer_detail WHERE FALSE OR offer_id = '234' LIMIT 0, 88) AS "
                                   + aliasConvert("offer")
                                   + " WHERE offer.member_id = 'abc' LIMIT 0, 60) AS "
                                   + aliasConvert("w") + " WHERE w.member_id = 'pavarotti17' LIMIT 0, 99")
                   .addExpectSQL(
                           1,
                           "SELECT COUNT(*) FROM (SELECT * FROM (SELECT MAX(id) FROM offer_detail WHERE offer_id = '123' OR FALSE LIMIT 0, 88) AS "
                                   + aliasConvert("offer")
                                   + " WHERE offer.member_id = 'abc' LIMIT 0, 60) AS "
                                   + aliasConvert("w") + " WHERE w.member_id = 'pavarotti17' LIMIT 0, 99");
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "select * from (select * from(select max(id) from offer_detail where offer_id='123' or offer_id='234' limit 88)offer  where offer.member_id='abc' limit 60) w "
                + " where w.member_id ='pavarotti17' limit 99";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(88L, rrs.getLimitSize());
        Assert.assertEquals(RouteResultset.MAX_FLAG, rrs.getFlag());
        nodeMap = getNodeMap(rrs, 2);
        nameAsserter = new NodeNameAsserter("detail_dn[29]", "detail_dn[15]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(
                0,
                "SELECT * FROM (SELECT * FROM (SELECT MAX(id) FROM offer_detail WHERE FALSE OR offer_id = '234' LIMIT 0, 88) AS "
                        + aliasConvert("offer") + " WHERE offer.member_id = 'abc' LIMIT 0, 60) AS " + aliasConvert("w")
                        + " WHERE w.member_id = 'pavarotti17' LIMIT 0, 99").addExpectSQL(
                1,
                "SELECT * FROM (SELECT * FROM (SELECT MAX(id) FROM offer_detail WHERE offer_id = '123' OR FALSE LIMIT 0, 88) AS "
                        + aliasConvert("offer") + " WHERE offer.member_id = 'abc' LIMIT 0, 60) AS " + aliasConvert("w")
                        + " WHERE w.member_id = 'pavarotti17' LIMIT 0, 99");
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "select * from (select count(*) from(select * from offer_detail where offer_id='123' or offer_id='234' limit 88)offer  where offer.member_id='abc' limit 60) w "
                + " where w.member_id ='pavarotti17' limit 99";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(88L, rrs.getLimitSize());
        Assert.assertEquals(RouteResultset.SUM_FLAG, rrs.getFlag());
        nodeMap = getNodeMap(rrs, 2);
        nameAsserter = new NodeNameAsserter("detail_dn[29]", "detail_dn[15]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(
                0,
                "SELECT * FROM (SELECT COUNT(*) FROM (SELECT * FROM offer_detail WHERE FALSE OR offer_id = '234' LIMIT 0, 88) AS "
                        + aliasConvert("offer") + " WHERE offer.member_id = 'abc' LIMIT 0, 60) AS " + aliasConvert("w")
                        + " WHERE w.member_id = 'pavarotti17' LIMIT 0, 99").addExpectSQL(
                1,
                "SELECT * FROM (SELECT COUNT(*) FROM (SELECT * FROM offer_detail WHERE offer_id = '123' OR FALSE LIMIT 0, 88) AS "
                        + aliasConvert("offer") + " WHERE offer.member_id = 'abc' LIMIT 0, 60) AS " + aliasConvert("w")
                        + " WHERE w.member_id = 'pavarotti17' LIMIT 0, 99");
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }
    }

    public void testDimension2Route() throws Exception {
        final SchemaConfig schema = schemaMap.get("cndb");
        String sql = "select * from product_visit where member_id='pavarotti17' and product_id=2345";
        RouteResultset rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, rrs.getNodes()[0].getReplicaIndex());
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals("offer_dn[9]", rrs.getNodes()[0].getName());
        Assert.assertEquals(
                "select * from product_visit where member_id='pavarotti17' and product_id=2345",
                rrs.getNodes()[0].getStatement());

        sql = "select * from product_visit where member_id='pavarotti17' ";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        Map<String, RouteResultsetNode> nodeMap = getNodeMap(rrs, 8);
        NodeNameAsserter nameAsserter = new NodeNameAsserter(
                "offer_dn[25]",
                "offer_dn[17]",
                "offer_dn[9]",
                "offer_dn[1]",
                "offer_dn[29]",
                "offer_dn[21]",
                "offer_dn[5]",
                "offer_dn[13]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        SimpleSQLAsserter sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(0, "SELECT * FROM product_visit WHERE member_id = 'pavarotti17'")
                   .addExpectSQL(1, "SELECT * FROM product_visit WHERE member_id = 'pavarotti17'")
                   .addExpectSQL(2, "SELECT * FROM product_visit WHERE member_id = 'pavarotti17'")
                   .addExpectSQL(3, "SELECT * FROM product_visit WHERE member_id = 'pavarotti17'")
                   .addExpectSQL(4, "SELECT * FROM product_visit WHERE member_id = 'pavarotti17'")
                   .addExpectSQL(5, "SELECT * FROM product_visit WHERE member_id = 'pavarotti17'")
                   .addExpectSQL(6, "SELECT * FROM product_visit WHERE member_id = 'pavarotti17'")
                   .addExpectSQL(7, "SELECT * FROM product_visit WHERE member_id = 'pavarotti17'");
        RouteNodeAsserter asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "select * from product_visit where member_id='abc' ";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        nodeMap = getNodeMap(rrs, 8);
        nameAsserter = new NodeNameAsserter(
                "offer_dn[0]",
                "offer_dn[4]",
                "offer_dn[8]",
                "offer_dn[12]",
                "offer_dn[16]",
                "offer_dn[20]",
                "offer_dn[24]",
                "offer_dn[28]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(0, "SELECT * FROM product_visit WHERE member_id = 'abc'")
                   .addExpectSQL(1, "SELECT * FROM product_visit WHERE member_id = 'abc'")
                   .addExpectSQL(2, "SELECT * FROM product_visit WHERE member_id = 'abc'")
                   .addExpectSQL(3, "SELECT * FROM product_visit WHERE member_id = 'abc'")
                   .addExpectSQL(4, "SELECT * FROM product_visit WHERE member_id = 'abc'")
                   .addExpectSQL(5, "SELECT * FROM product_visit WHERE member_id = 'abc'")
                   .addExpectSQL(6, "SELECT * FROM product_visit WHERE member_id = 'abc'")
                   .addExpectSQL(7, "SELECT * FROM product_visit WHERE member_id = 'abc'");
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "delete from product_visit where member_id='pavarotti17' or Member_id between 'abc' and 'abc'";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        nodeMap = getNodeMap(rrs, 16);
        nameAsserter = new NodeNameAsserter(
                "offer_dn[0]",
                "offer_dn[4]",
                "offer_dn[8]",
                "offer_dn[12]",
                "offer_dn[16]",
                "offer_dn[20]",
                "offer_dn[24]",
                "offer_dn[28]",
                "offer_dn[1]",
                "offer_dn[5]",
                "offer_dn[9]",
                "offer_dn[13]",
                "offer_dn[17]",
                "offer_dn[21]",
                "offer_dn[25]",
                "offer_dn[29]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(0, "DELETE FROM product_visit WHERE FALSE OR Member_id BETWEEN 'abc' AND 'abc'")
                   .addExpectSQL(1, "DELETE FROM product_visit WHERE FALSE OR Member_id BETWEEN 'abc' AND 'abc'")
                   .addExpectSQL(2, "DELETE FROM product_visit WHERE FALSE OR Member_id BETWEEN 'abc' AND 'abc'")
                   .addExpectSQL(3, "DELETE FROM product_visit WHERE FALSE OR Member_id BETWEEN 'abc' AND 'abc'")
                   .addExpectSQL(4, "DELETE FROM product_visit WHERE FALSE OR Member_id BETWEEN 'abc' AND 'abc'")
                   .addExpectSQL(5, "DELETE FROM product_visit WHERE FALSE OR Member_id BETWEEN 'abc' AND 'abc'")
                   .addExpectSQL(6, "DELETE FROM product_visit WHERE FALSE OR Member_id BETWEEN 'abc' AND 'abc'")
                   .addExpectSQL(7, "DELETE FROM product_visit WHERE FALSE OR Member_id BETWEEN 'abc' AND 'abc'")
                   .addExpectSQL(8, "DELETE FROM product_visit WHERE member_id = 'pavarotti17' OR FALSE")
                   .addExpectSQL(9, "DELETE FROM product_visit WHERE member_id = 'pavarotti17' OR FALSE")
                   .addExpectSQL(10, "DELETE FROM product_visit WHERE member_id = 'pavarotti17' OR FALSE")
                   .addExpectSQL(11, "DELETE FROM product_visit WHERE member_id = 'pavarotti17' OR FALSE")
                   .addExpectSQL(12, "DELETE FROM product_visit WHERE member_id = 'pavarotti17' OR FALSE")
                   .addExpectSQL(13, "DELETE FROM product_visit WHERE member_id = 'pavarotti17' OR FALSE")
                   .addExpectSQL(14, "DELETE FROM product_visit WHERE member_id = 'pavarotti17' OR FALSE")
                   .addExpectSQL(15, "DELETE FROM product_visit WHERE member_id = 'pavarotti17' OR FALSE");
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "select * from product_visit where  product_id=2345 ";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        nodeMap = getNodeMap(rrs, 4);
        nameAsserter = new NodeNameAsserter("offer_dn[8]", "offer_dn[9]", "offer_dn[10]", "offer_dn[11]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(0, "SELECT * FROM product_visit WHERE product_id = 2345")
                   .addExpectSQL(1, "SELECT * FROM product_visit WHERE product_id = 2345")
                   .addExpectSQL(2, "SELECT * FROM product_visit WHERE product_id = 2345")
                   .addExpectSQL(3, "SELECT * FROM product_visit WHERE product_id = 2345");
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "select * from product_visit where  product_id=1234 ";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        nodeMap = getNodeMap(rrs, 4);
        nameAsserter = new NodeNameAsserter("offer_dn[4]", "offer_dn[5]", "offer_dn[6]", "offer_dn[7]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(0, "SELECT * FROM product_visit WHERE product_id = 1234")
                   .addExpectSQL(1, "SELECT * FROM product_visit WHERE product_id = 1234")
                   .addExpectSQL(2, "SELECT * FROM product_visit WHERE product_id = 1234")
                   .addExpectSQL(3, "SELECT * FROM product_visit WHERE product_id = 1234");
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "select * from product_visit where  product_id=1234 or product_id=2345 ";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        nodeMap = getNodeMap(rrs, 8);
        nameAsserter = new NodeNameAsserter(
                "offer_dn[4]",
                "offer_dn[5]",
                "offer_dn[6]",
                "offer_dn[7]",
                "offer_dn[8]",
                "offer_dn[9]",
                "offer_dn[10]",
                "offer_dn[11]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(0, "SELECT * FROM product_visit WHERE product_id = 1234 OR FALSE")
                   .addExpectSQL(1, "SELECT * FROM product_visit WHERE product_id = 1234 OR FALSE")
                   .addExpectSQL(2, "SELECT * FROM product_visit WHERE product_id = 1234 OR FALSE")
                   .addExpectSQL(3, "SELECT * FROM product_visit WHERE product_id = 1234 OR FALSE")
                   .addExpectSQL(4, "SELECT * FROM product_visit WHERE FALSE OR product_id = 2345")
                   .addExpectSQL(5, "SELECT * FROM product_visit WHERE FALSE OR product_id = 2345")
                   .addExpectSQL(6, "SELECT * FROM product_visit WHERE FALSE OR product_id = 2345")
                   .addExpectSQL(7, "SELECT * FROM product_visit WHERE FALSE OR product_id = 2345");
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "select * from product_visit where  product_id in (1234,2345) ";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        nodeMap = getNodeMap(rrs, 8);
        nameAsserter = new NodeNameAsserter(
                "offer_dn[4]",
                "offer_dn[5]",
                "offer_dn[6]",
                "offer_dn[7]",
                "offer_dn[8]",
                "offer_dn[9]",
                "offer_dn[10]",
                "offer_dn[11]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(0, "SELECT * FROM product_visit WHERE product_id IN (1234)")
                   .addExpectSQL(1, "SELECT * FROM product_visit WHERE product_id IN (1234)")
                   .addExpectSQL(2, "SELECT * FROM product_visit WHERE product_id IN (1234)")
                   .addExpectSQL(3, "SELECT * FROM product_visit WHERE product_id IN (1234)")
                   .addExpectSQL(4, "SELECT * FROM product_visit WHERE product_id IN (2345)")
                   .addExpectSQL(5, "SELECT * FROM product_visit WHERE product_id IN (2345)")
                   .addExpectSQL(6, "SELECT * FROM product_visit WHERE product_id IN (2345)")
                   .addExpectSQL(7, "SELECT * FROM product_visit WHERE product_id IN (2345)");
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }
    }

    public void testBackquotedColumn() throws Exception {
        final SchemaConfig schema = schemaMap.get("cndb");
        String sql = "select * from wp_image where `seLect`='pavarotti17' ";
        RouteResultset rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, rrs.getNodes()[0].getReplicaIndex());
        Assert.assertEquals("offer_dn[123]", rrs.getNodes()[0].getName());
        Assert.assertEquals("select * from wp_image where `seLect`='pavarotti17' ", rrs.getNodes()[0].getStatement());

    }

    public void testTableMetaRead() throws Exception {
        final SchemaConfig schema = schemaMap.get("cndb");

        String sql = "desc offer";
        RouteResultset rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1L, rrs.getLimitSize());
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, rrs.getNodes()[0].getReplicaIndex());
        Assert.assertEquals("offer_dn[0]", rrs.getNodes()[0].getName());
        Assert.assertEquals("desc offer", rrs.getNodes()[0].getStatement());

        sql = "desc cndb.offer";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1L, rrs.getLimitSize());
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, rrs.getNodes()[0].getReplicaIndex());
        Assert.assertEquals("offer_dn[0]", rrs.getNodes()[0].getName());
        Assert.assertEquals("DESC offer", rrs.getNodes()[0].getStatement());

        sql = "SHOW FULL COLUMNS FROM  offer  IN db_name WHERE true";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1L, rrs.getLimitSize());
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, rrs.getNodes()[0].getReplicaIndex());
        Assert.assertEquals("offer_dn[0]", rrs.getNodes()[0].getName());
        Assert.assertEquals("SHOW FULL COLUMNS FROM offer WHERE TRUE", rrs.getNodes()[0].getStatement());

        sql = "SHOW FULL COLUMNS FROM  db.offer  IN db_name WHERE true";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1L, rrs.getLimitSize());
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, rrs.getNodes()[0].getReplicaIndex());
        Assert.assertEquals("offer_dn[0]", rrs.getNodes()[0].getName());
        Assert.assertEquals("SHOW FULL COLUMNS FROM offer WHERE TRUE", rrs.getNodes()[0].getStatement());

        sql = "SHOW INDEX  IN offer FROM  db_name";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1L, rrs.getLimitSize());
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, rrs.getNodes()[0].getReplicaIndex());
        Assert.assertEquals("offer_dn[0]", rrs.getNodes()[0].getName());
        Assert.assertEquals("SHOW INDEX IN offer", rrs.getNodes()[0].getStatement());

        sql = "SHOW TABLES from db_name like 'solo'";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1L, rrs.getLimitSize());
        Map<String, RouteResultsetNode> nodeMap = getNodeMap(rrs, 4);
        NodeNameAsserter nameAsserter = new NodeNameAsserter(
                "detail_dn[0]",
                "offer_dn[0]",
                "cndb_dn",
                "independent_dn[0]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        SimpleSQLAsserter sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(0, "SHOW TABLES LIKE 'solo'")
                   .addExpectSQL(1, "SHOW TABLES LIKE 'solo'")
                   .addExpectSQL(2, "SHOW TABLES LIKE 'solo'")
                   .addExpectSQL(3, "SHOW TABLES LIKE 'solo'");
        RouteNodeAsserter asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "SHOW TABLES in db_name ";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1L, rrs.getLimitSize());
        nodeMap = getNodeMap(rrs, 4);
        nameAsserter = new NodeNameAsserter("detail_dn[0]", "offer_dn[0]", "cndb_dn", "independent_dn[0]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(0, "SHOW TABLES")
                   .addExpectSQL(1, "SHOW TABLES")
                   .addExpectSQL(2, "SHOW TABLES")
                   .addExpectSQL(3, "SHOW TABLES");
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "SHOW TABLeS ";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1L, rrs.getLimitSize());
        nodeMap = getNodeMap(rrs, 4);
        nameAsserter = new NodeNameAsserter("detail_dn[0]", "offer_dn[0]", "cndb_dn", "independent_dn[0]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(0, "SHOW TABLeS ")
                   .addExpectSQL(1, "SHOW TABLeS ")
                   .addExpectSQL(2, "SHOW TABLeS ")
                   .addExpectSQL(3, "SHOW TABLeS ");
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }
    }

    public void testCobarHint() throws Exception {
        SchemaConfig schema = schemaMap.get("cndb");
        String sql = "  /*!cobar: $dataNodeId=2.1, $table='offer'*/ select * from `dual`";
        RouteResultset rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1L, rrs.getLimitSize());
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals(1, rrs.getNodes()[0].getReplicaIndex());
        Assert.assertEquals("offer_dn[2]", rrs.getNodes()[0].getName());
        Assert.assertEquals(" select * from `dual`", rrs.getNodes()[0].getStatement());

        sql = "/*!cobar: $dataNodeId=2.1, $table='offer', $replica =2*/ select * from `dual`";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1L, rrs.getLimitSize());
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals(1, rrs.getNodes()[0].getReplicaIndex());
        Assert.assertEquals("offer_dn[2]", rrs.getNodes()[0].getName());
        Assert.assertEquals(" select * from `dual`", rrs.getNodes()[0].getStatement());

        sql = "/*!cobar: $dataNodeId   = [ 1,2,5.2]  , $table =  'offer'   */ select * from `dual`";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1L, rrs.getLimitSize());
        Map<String, RouteResultsetNode> nodeMap = getNodeMap(rrs, 3);
        NodeNameAsserter nameAsserter = new NodeNameAsserter("offer_dn[1]", "offer_dn[2]", "offer_dn[5]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        SimpleSQLAsserter sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(0, " select * from `dual`")
                   .addExpectSQL(1, " select * from `dual`")
                   .addExpectSQL(2, " select * from `dual`");
        RouteNodeAsserter asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter, new ReplicaAsserter() {
            @Override
            public void assertReplica(Integer nodeIndex, Integer replica) {
                if (nodeIndex.equals(2))
                    Assert.assertEquals(2, replica.intValue());
                else
                    Assert.assertEquals(RouteResultsetNode.DEFAULT_REPLICA_INDEX, replica);
            }
        });
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "/*!cobar: $dataNodeId   = [ 1,2,5.2]  , $table =  'offer'  , $replica =1 */ select * from `dual`";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1L, rrs.getLimitSize());
        Assert.assertEquals(3, rrs.getNodes().length);
        nodeMap = getNodeMap(rrs, 3);
        nameAsserter = new NodeNameAsserter("offer_dn[1]", "offer_dn[2]", "offer_dn[5]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(0, " select * from `dual`")
                   .addExpectSQL(1, " select * from `dual`")
                   .addExpectSQL(2, " select * from `dual`");
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter, new ReplicaAsserter() {
            @Override
            public void assertReplica(Integer nodeIndex, Integer replica) {
                if (nodeIndex.equals(2))
                    Assert.assertEquals(2, replica.intValue());
                else
                    Assert.assertEquals(1, replica.intValue());
            }
        });
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "/*!cobar: $partitionOperand=( 'member_id' = 'pavarotti17'), $table='offer'*/ select * from `dual`";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1L, rrs.getLimitSize());
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, rrs.getNodes()[0].getReplicaIndex());
        Assert.assertEquals("offer_dn[123]", rrs.getNodes()[0].getName());
        Assert.assertEquals(" select * from `dual`", rrs.getNodes()[0].getStatement());

        sql = "/*!cobar:$partitionOperand =   ( 'member_id' = ['pavarotti17'  ,   'qaa' ]  ), $table='offer'  , $replica =  2*/  select * from `dual`";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1L, rrs.getLimitSize());
        nodeMap = getNodeMap(rrs, 2);
        nameAsserter = new NodeNameAsserter("offer_dn[123]", "offer_dn[10]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(0, "  select * from `dual`").addExpectSQL(1, "  select * from `dual`");
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter, new ReplicaAsserter() {
            @Override
            public void assertReplica(Integer nodeIndex, Integer replica) {
                Assert.assertEquals(2, replica.intValue());
            }
        });
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "/*!cobar:$partitionOperand = ( ['group_id','offer_id'] = [234,4]), $table='offer'*/ select * from `dual`";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1L, rrs.getLimitSize());
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, rrs.getNodes()[0].getReplicaIndex());
        Assert.assertEquals("offer_dn[29]", rrs.getNodes()[0].getName());
        Assert.assertEquals(" select * from `dual`", rrs.getNodes()[0].getStatement());

        sql = "/*!cobar:$partitionOperand=(['offer_id','group_id']=[[123,3],[234,4]]), $table='offer'  , $replica =2*/ select * from `dual`";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1L, rrs.getLimitSize());
        nodeMap = getNodeMap(rrs, 2);
        nameAsserter = new NodeNameAsserter("offer_dn[29]", "offer_dn[15]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(0, " select * from `dual`").addExpectSQL(1, " select * from `dual`");
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter, new ReplicaAsserter() {
            @Override
            public void assertReplica(Integer nodeIndex, Integer replica) {
                Assert.assertEquals(2, replica.intValue());
            }
        });
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "/*!cobar:$partitionOperand=(['group_id','offer_id']=[[123,3], [ 234,4 ] ]), $table='offer'  */ select * from `dual`";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1L, rrs.getLimitSize());
        nodeMap = getNodeMap(rrs, 2);
        nameAsserter = new NodeNameAsserter("offer_dn[29]", "offer_dn[15]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(0, " select * from `dual`").addExpectSQL(1, " select * from `dual`");
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter, new ReplicaAsserter() {
            @Override
            public void assertReplica(Integer nodeIndex, Integer replica) {
                Assert.assertEquals(RouteResultsetNode.DEFAULT_REPLICA_INDEX, replica);
            }
        });
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "/*!cobar:$partitionOperand=(['offer_id','NON_EXistence']=[[123,3],[234,4]]), $table='offer'  , $replica =2*/ select * from `dual`";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1L, rrs.getLimitSize());
        nodeMap = getNodeMap(rrs, 128);
        nameAsserter = new IndexedNodeNameAsserter("offer_dn", 0, 128);
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        for (int i = 0; i < 128; i++) {
            sqlAsserter.addExpectSQL(i, " select * from `dual`");
        }
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter, new ReplicaAsserter() {
            @Override
            public void assertReplica(Integer nodeIndex, Integer replica) {
                Assert.assertEquals(2, replica.intValue());
            }
        });
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "/*!cobar:  $dataNodeId   = 1  ,$table =  'wp_image'*/ select * from `dual`";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1L, rrs.getLimitSize());
        nodeMap = getNodeMap(rrs, 1);
        nameAsserter = new NodeNameAsserter("offer_dn[1]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(0, " select * from `dual`");
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "/*!cobar:  $dataNodeId   = [0,3]  ,$table =  'wp_image'*/ select * from `dual`";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1L, rrs.getLimitSize());
        nodeMap = getNodeMap(rrs, 2);
        nameAsserter = new NodeNameAsserter("offer_dn[0]", "offer_dn[3]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(0, " select * from `dual`").addExpectSQL(1, " select * from `dual`");
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "/*!cobar:  $table =  'wp_image'*/ select * from `dual`";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1L, rrs.getLimitSize());
        nodeMap = getNodeMap(rrs, 128);
        nameAsserter = new IndexedNodeNameAsserter("offer_dn", 0, 128);
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        for (int i = 0; i < 128; i++) {
            sqlAsserter.addExpectSQL(i, " select * from `dual`");
        }
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "/*!cobar:  $dataNodeId   = 0  ,$table =  'independent'*/ select * from `dual`";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1L, rrs.getLimitSize());
        nodeMap = getNodeMap(rrs, 1);
        nameAsserter = new NodeNameAsserter("independent_dn[0]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(0, " select * from `dual`");
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "/*!cobar:  $dataNodeId   = [ 1,2,5]  ,$table =  'independent'*/ select * from `dual`";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1L, rrs.getLimitSize());
        nodeMap = getNodeMap(rrs, 3);
        nameAsserter = new NodeNameAsserter("independent_dn[1]", "independent_dn[2]", "independent_dn[5]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(0, " select * from `dual`")
                   .addExpectSQL(1, " select * from `dual`")
                   .addExpectSQL(2, " select * from `dual`");
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "/*!cobar:  $table =  'independent'*/ select * from `dual`";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1L, rrs.getLimitSize());
        nodeMap = getNodeMap(rrs, 128);
        nameAsserter = new IndexedNodeNameAsserter("independent_dn", 0, 128);
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        for (int i = 0; i < 128; i++) {
            sqlAsserter.addExpectSQL(i, " select * from `dual`");
        }
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "/*!cobar:$partitionOperand=(['member_id','NON_EXistence']=[['pavarotti17'],['qaa',4]]), $table='offer'  , $replica=2*/ select * from `dual`";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1L, rrs.getLimitSize());
        nodeMap = getNodeMap(rrs, 2);
        nameAsserter = new NodeNameAsserter("offer_dn[123]", "offer_dn[10]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(0, " select * from `dual`").addExpectSQL(1, " select * from `dual`");
        asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter, new ReplicaAsserter() {
            @Override
            public void assertReplica(Integer nodeIndex, Integer replica) {
                Assert.assertEquals(2, replica.intValue());
            }
        });
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }

        sql = "/*!cobar:$partitionOperand=(['offer_id','NON_EXistence']=[[123,3],[234,4]]), $table='non_existence'  , $replica=2*/ select * from `dual`";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1L, rrs.getLimitSize());
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals(2, rrs.getNodes()[0].getReplicaIndex());
        Assert.assertEquals("cndb_dn", rrs.getNodes()[0].getName());
        Assert.assertEquals(" select * from `dual`", rrs.getNodes()[0].getStatement());

        sql = "/*!cobar:$partitionOperand=(['offer_id','group_id']=[[123,3],[234,4]]), $table='non_existence'  , $replica=2*/ select * from `dual`";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1L, rrs.getLimitSize());
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals(2, rrs.getNodes()[0].getReplicaIndex());
        Assert.assertEquals("cndb_dn", rrs.getNodes()[0].getName());
        Assert.assertEquals(" select * from `dual`", rrs.getNodes()[0].getStatement());

        schema = schemaMap.get("dubbo");
        sql = "/*!cobar: $replica=2*/ select * from `dual`";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1L, rrs.getLimitSize());
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals(2, rrs.getNodes()[0].getReplicaIndex());
        Assert.assertEquals("dubbo_dn", rrs.getNodes()[0].getName());
        Assert.assertEquals(" select * from `dual`", rrs.getNodes()[0].getStatement());

        schema = schemaMap.get("dubbo");
        sql = "/*!cobar: $dataNodeId = [ 0.1],$replica=2*/ select * from `dual`";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals(1, rrs.getNodes()[0].getReplicaIndex());
        Assert.assertEquals("dubbo_dn", rrs.getNodes()[0].getName());
        Assert.assertEquals(" select * from `dual`", rrs.getNodes()[0].getStatement());

        schema = schemaMap.get("dubbo");
        sql = "/*!cobar: */ select * from `dual`";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, rrs.getNodes()[0].getReplicaIndex());
        Assert.assertEquals("dubbo_dn", rrs.getNodes()[0].getName());
        Assert.assertEquals(" select * from `dual`", rrs.getNodes()[0].getStatement());

    }

    public void testConfigSchema() throws Exception {
        try {
            SchemaConfig schema = schemaMap.get("config");
            String sql = "select * from offer where offer_id=1";
            ServerRouter.route(schema, sql, null, null);
            Assert.assertFalse(true);
        } catch (Exception e) {
        }
        try {
            SchemaConfig schema = schemaMap.get("config");
            String sql = "select * from offer where col11111=1";
            ServerRouter.route(schema, sql, null, null);
            Assert.assertFalse(true);
        } catch (Exception e) {
        }
        try {
            SchemaConfig schema = schemaMap.get("config");
            String sql = "select * from offer ";
            ServerRouter.route(schema, sql, null, null);
            Assert.assertFalse(true);
        } catch (Exception e) {
        }
    }

    public void testIgnoreSchema() throws Exception {
        SchemaConfig schema = schemaMap.get("ignoreSchemaTest");
        String sql = "select * from offer where offer_id=1";
        RouteResultset rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals("cndb_dn", rrs.getNodes()[0].getName());
        Assert.assertEquals(sql, rrs.getNodes()[0].getStatement());
        sql = "select * from ignoreSchemaTest.offer where ignoreSchemaTest.offer.offer_id=1";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals("SELECT * FROM offer WHERE offer.offer_id = 1", rrs.getNodes()[0].getStatement());
        sql = "select * from ignoreSchemaTest2.offer where ignoreSchemaTest2.offer.offer_id=1";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(sql, rrs.getNodes()[0].getStatement());
        sql = "select * from ignoreSchemaTest2.offer a,ignoreSchemaTest.offer b  where ignoreSchemaTest2.offer.offer_id=1";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals("SELECT * FROM ignoreSchemaTest2.offer AS " + aliasConvert("a") + ", offer AS "
                + aliasConvert("b") + " WHERE ignoreSchemaTest2.offer.offer_id = 1", rrs.getNodes()[0].getStatement());

        schema = schemaMap.get("ignoreSchemaTest0");
        sql = "select * from offer where offer_id=1";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(sql, rrs.getNodes()[0].getStatement());
        sql = "select * from ignoreSchemaTest0.offer where ignoreSchemaTest.offer.offer_id=1";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(
                "SELECT * FROM offer WHERE ignoreSchemaTest.offer.offer_id = 1",
                rrs.getNodes()[0].getStatement());
        sql = "insert into offer (group_id, offer_id, gmt) values (234,123,now())";
        schema = schemaMap.get("ignoreSchemaTest0");
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, rrs.getNodes()[0].getReplicaIndex());
        Assert.assertEquals("offer_dn[44]", rrs.getNodes()[0].getName());
        Assert.assertEquals(
                "insert into offer (group_id, offer_id, gmt) values (234,123,now())",
                rrs.getNodes()[0].getStatement());
        sql = "insert into ignoreSchemaTest0.offer (group_id, offer_id, gmt) values (234,123,now())";
        schema = schemaMap.get("ignoreSchemaTest0");
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals(-1l, rrs.getLimitSize());
        Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, rrs.getNodes()[0].getReplicaIndex());
        Assert.assertEquals("offer_dn[44]", rrs.getNodes()[0].getName());
        Assert.assertEquals(
                "INSERT INTO offer (group_id, offer_id, gmt) VALUES (234, 123, NOW())",
                rrs.getNodes()[0].getStatement());
        sql = "insert into ignoreSchemaTest2.offer (group_id, offer_id, gmt) values (234,123,now())";
        schema = schemaMap.get("ignoreSchemaTest0");
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals("cndb_dn", rrs.getNodes()[0].getName());
        Assert.assertEquals(sql, rrs.getNodes()[0].getStatement());
        sql = "insert into ignoreSchemaTest2.offer (ignoreSchemaTest0.offer.group_id, offer_id, gmt) values (234,123,now())";
        schema = schemaMap.get("ignoreSchemaTest0");
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals("cndb_dn", rrs.getNodes()[0].getName());
        Assert.assertEquals(
                "INSERT INTO ignoreSchemaTest2.offer (offer.group_id, offer_id, gmt) VALUES (234, 123, NOW())",
                rrs.getNodes()[0].getStatement());

    }

    public void testNonPartitionSQL() throws Exception {

        SchemaConfig schema = schemaMap.get("cndb");
        String sql = "  select * from `dual`";
        RouteResultset rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, rrs.getNodes()[0].getReplicaIndex());
        Assert.assertEquals("cndb_dn", rrs.getNodes()[0].getName());
        Assert.assertEquals("  select * from `dual`", rrs.getNodes()[0].getStatement());

        schema = schemaMap.get("dubbo");
        sql = "  select * from `dual`";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, rrs.getNodes()[0].getReplicaIndex());
        Assert.assertEquals("dubbo_dn", rrs.getNodes()[0].getName());
        Assert.assertEquals("  select * from `dual`", rrs.getNodes()[0].getStatement());

        schema = schemaMap.get("dubbo");
        sql = "  select * from dubbo.`dual`";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, rrs.getNodes()[0].getReplicaIndex());
        Assert.assertEquals("dubbo_dn", rrs.getNodes()[0].getName());
        Assert.assertEquals("  select * from dubbo.`dual`", rrs.getNodes()[0].getStatement());

        sql = "SHOW TABLES from db_name like 'solo'";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1L, rrs.getLimitSize());
        Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, rrs.getNodes()[0].getReplicaIndex());
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals("dubbo_dn", rrs.getNodes()[0].getName());
        Assert.assertEquals("SHOW TABLES from db_name like 'solo'", rrs.getNodes()[0].getStatement());

        sql = "desc cndb.offer";
        rrs = ServerRouter.route(schema, sql, null, null);
        Assert.assertEquals(-1L, rrs.getLimitSize());
        Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, rrs.getNodes()[0].getReplicaIndex());
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals("dubbo_dn", rrs.getNodes()[0].getName());
        Assert.assertEquals("desc cndb.offer", rrs.getNodes()[0].getStatement());

        schema = schemaMap.get("cndb");
        sql = "SHOW fulL TaBLES from db_name like 'solo'";
        rrs = ServerRouter.route(schema, sql, null, null);
        Map<String, RouteResultsetNode> nodeMap = getNodeMap(rrs, 4);
        NodeNameAsserter nameAsserter = new NodeNameAsserter(
                "detail_dn[0]",
                "offer_dn[0]",
                "cndb_dn",
                "independent_dn[0]");
        nameAsserter.assertRouteNodeNames(nodeMap.keySet());
        SimpleSQLAsserter sqlAsserter = new SimpleSQLAsserter();
        sqlAsserter.addExpectSQL(0, "SHOW FULL TABLES LIKE 'solo'")
                   .addExpectSQL(1, "SHOW FULL TABLES LIKE 'solo'")
                   .addExpectSQL(2, "SHOW FULL TABLES LIKE 'solo'")
                   .addExpectSQL(3, "SHOW FULL TABLES LIKE 'solo'");
        RouteNodeAsserter asserter = new RouteNodeAsserter(nameAsserter, sqlAsserter);
        for (RouteResultsetNode node : nodeMap.values()) {
            asserter.assertNode(node);
        }
    }

}
