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
package com.alibaba.cobar.route.perf;

import com.alibaba.cobar.config.model.SchemaConfig;
import com.alibaba.cobar.parser.ast.stmt.SQLStatement;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLInsertStatement;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLSelectStatement;
import com.alibaba.cobar.parser.recognizer.SQLParserDelegate;
import com.alibaba.cobar.parser.recognizer.mysql.lexer.MySQLLexer;
import com.alibaba.cobar.parser.recognizer.mysql.syntax.MySQLDMLInsertParser;
import com.alibaba.cobar.parser.recognizer.mysql.syntax.MySQLDMLSelectParser;
import com.alibaba.cobar.parser.recognizer.mysql.syntax.MySQLExprParser;
import com.alibaba.cobar.parser.visitor.MySQLOutputASTVisitor;
import com.alibaba.cobar.route.ServerRouter;
import com.alibaba.cobar.route.visitor.PartitionKeyVisitor;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class ServerRoutePerformance {

    private static abstract class TestProvider {
        public abstract String getSql() throws Exception;

        public abstract void route(SchemaConfig schema, int loop, String sql) throws Exception;
    }

    private static class ShardingDefaultSpace extends TestProvider {
        private SQLStatement stmt;

        @Override
        public void route(SchemaConfig schema, int loop, String sql) throws Exception {
            for (int i = 0; i < loop; ++i) {
                // SQLLexer lexer = new SQLLexer(sql);
                // DMLSelectStatement select = new DMLSelectParser(lexer, new
                // SQLExprParser(lexer)).select();
                // PartitionKeyVisitor visitor = new
                // PartitionKeyVisitor(schema.getTablesSpace());
                // select.accept(visitor);
                // visitor.getColumnValue();
                ServerRouter.route(schema, sql, null, null);

                // StringBuilder s = new StringBuilder();
                // stmt.accept(new MySQLOutputASTVisitor(s));
                // s.toString();
            }
        }

        @Override
        public String getSql() throws Exception {
            String sql = "insert into xoffer (member_id, gmt_create) values ('1','2001-09-13 20:20:33')";
            stmt = SQLParserDelegate.parse(sql);
            return "insert into xoffer (member_id, gmt_create) values ('1','2001-09-13 20:20:33')";
        }
    }

    private static class ShardingTableSpace extends TestProvider {
        private SQLStatement stmt;

        @Override
        public void route(SchemaConfig schema, int loop, String sql) throws Exception {
            for (int i = 0; i < loop; ++i) {
                // SQLLexer lexer = new SQLLexer(sql);
                // DMLSelectStatement select = new DMLSelectParser(lexer, new
                // SQLExprParser(lexer)).select();
                // PartitionKeyVisitor visitor = new
                // PartitionKeyVisitor(schema.getTablesSpace());
                // select.accept(visitor);
                // visitor.getColumnValue();
                ServerRouter.route(schema, sql, null, null);

                // StringBuilder s = new StringBuilder();
                // stmt.accept(new MySQLOutputASTVisitor(s));
                // s.toString();
            }
        }

        @Override
        public String getSql() throws Exception {
            String sql = "insert into offer (member_id, gmt_create) values ('1','2001-09-13 20:20:33')";
            stmt = SQLParserDelegate.parse(sql);
            return "insert into offer (member_id, gmt_create) values ('1','2001-09-13 20:20:33'),('1','2001-09-13 20:20:34')";
        }
    }

    private static class ShardingMultiTableSpace extends TestProvider {
        private SQLStatement stmt;

        @Override
        public void route(SchemaConfig schema, int loop, String sql) throws Exception {
            for (int i = 0; i < loop * 5; ++i) {
                // SQLLexer lexer = new SQLLexer(sql);
                // DMLSelectStatement select = new DMLSelectParser(lexer, new
                // SQLExprParser(lexer)).select();
                // PartitionKeyVisitor visitor = new
                // PartitionKeyVisitor(schema.getTablesSpace());
                // select.accept(visitor);
                // visitor.getColumnValue();
                ServerRouter.route(schema, sql, null, null);

                // StringBuilder s = new StringBuilder();
                // stmt.accept(new MySQLOutputASTVisitor(s));
                // s.toString();
            }
        }

        @Override
        public String getSql() throws Exception {
            String sql = "select id,member_id,gmt_create from offer where member_id in ('22')";
            stmt = SQLParserDelegate.parse(sql);
            return "select id,member_id,gmt_create from offer where member_id in ('1','22','333','1124','4525')";
        }
    }

    private static class SelectShort extends TestProvider {
        @Override
        public void route(SchemaConfig schema, int loop, String sql) throws Exception {
            for (int i = 0; i < loop; ++i) {
                MySQLLexer lexer = new MySQLLexer(sql);
                DMLSelectStatement select = new MySQLDMLSelectParser(lexer, new MySQLExprParser(lexer)).select();
                PartitionKeyVisitor visitor = new PartitionKeyVisitor(schema.getTables());
                select.accept(visitor);
                // visitor.getColumnValue();
                // ServerRoute.route(schema, sql);
            }
        }

        @Override
        public String getSql() throws Exception {
            return " seLEcT id, member_id , image_path  \t , image_size , STATUS,   gmt_modified from    offer_detail wheRe \t\t\n offer_id =  123 AND member_id\t=\t-123.456";
        }
    }

    private static class SelectLongIn extends TestProvider {
        @Override
        public void route(SchemaConfig schema, int loop, String sql) throws Exception {
            for (int i = 0; i < loop; ++i) {
                MySQLLexer lexer = new MySQLLexer(sql);
                DMLSelectStatement select = new MySQLDMLSelectParser(lexer, new MySQLExprParser(lexer)).select();
                PartitionKeyVisitor visitor = new PartitionKeyVisitor(schema.getTables());
                select.accept(visitor);
                // visitor.getColumnValue();
                // ServerRoute.route(schema, sql);
            }
        }

        @Override
        public String getSql() throws Exception {
            StringBuilder sb = new StringBuilder();
            sb.append(" seLEcT id, member_id , image_path  \t , image_size , STATUS,   gmt_modified from").append(
                    "    offer_detail wheRe \t\t\n offer_id in (");
            for (int i = 0; i < 1024; ++i) {
                if (i > 0)
                    sb.append(", ");
                sb.append(i);
            }
            sb.append(") AND member_id\t=\t-123.456");// System.out.println(sb.length());
            return sb.toString();
        }
    }

    private static class InsertLong extends TestProvider {

        @Override
        public String getSql() throws Exception {
            StringBuilder sb = new StringBuilder("insert into offer_detail (offer_id, gmt) values ");
            for (int i = 0; i < 1024; ++i) {
                if (i > 0)
                    sb.append(", ");
                sb.append("(" + i + ", now())");
            }
            // System.out.println(sb.length());
            return sb.toString();
        }

        @Override
        public void route(SchemaConfig schema, int loop, String sql) throws Exception {
            for (int i = 0; i < loop; ++i) {
                MySQLLexer lexer = new MySQLLexer(sql);
                DMLInsertStatement insert = new MySQLDMLInsertParser(lexer, new MySQLExprParser(lexer)).insert();
                // PartitionKeyVisitor visitor = new
                // PartitionKeyVisitor(schema.getTablesSpace());
                // insert.accept(visitor);
                // visitor.getColumnValue();
                // SQLLexer lexer = new SQLLexer(sql);
                // new DMLInsertParser(lexer, new
                // SQLExprParser(lexer)).insert();
                // RouteResultset rrs = ServerRoute.route(schema, sql);
                // System.out.println(rrs);
            }
        }
    }

    private static class InsertLongSQLGen extends TestProvider {
        private DMLInsertStatement insert;
        private int sqlSize;

        @Override
        public String getSql() throws Exception {
            String sql = new InsertLong().getSql();
            MySQLLexer lexer = new MySQLLexer(sql);
            insert = new MySQLDMLInsertParser(lexer, new MySQLExprParser(lexer)).insert();
            return sql;
        }

        @Override
        public void route(SchemaConfig schema, int loop, String sql) throws Exception {
            for (int i = 0; i < loop; ++i) {
                StringBuilder sb = new StringBuilder(sqlSize);
                insert.accept(new MySQLOutputASTVisitor(sb));
                sb.toString();
            }
        }
    }

    private static class InsertLongSQLGenShort extends TestProvider {
        private DMLInsertStatement insert;
        private int sqlSize;

        @Override
        public String getSql() throws Exception {
            StringBuilder sb = new StringBuilder("insert into offer_detail (offer_id, gmt) values ");
            for (int i = 0; i < 8; ++i) {
                if (i > 0)
                    sb.append(", ");
                sb.append("(" + (i + 100) + ", now())");
            }
            String sql = sb.toString();
            MySQLLexer lexer = new MySQLLexer(sql);
            insert = new MySQLDMLInsertParser(lexer, new MySQLExprParser(lexer)).insert();
            sqlSize = new InsertLong().getSql().length();
            return sql;
        }

        @Override
        public void route(SchemaConfig schema, int loop, String sql) throws Exception {
            for (int i = 0; i < loop; ++i) {
                for (int j = 0; j < 128; ++j) {
                    StringBuilder sb = new StringBuilder();
                    insert.accept(new MySQLOutputASTVisitor(sb));
                    sb.toString();
                }
            }
        }
    }

    public void perf() throws Exception {
        TestProvider provider;
        provider = new InsertLongSQLGen();
        provider = new InsertLongSQLGenShort();
        provider = new SelectShort();
        provider = new InsertLong();
        provider = new SelectLongIn();
        provider = new ShardingMultiTableSpace();
        provider = new ShardingDefaultSpace();
        provider = new ShardingTableSpace();

        SchemaConfig schema = getSchema();
        String sql = provider.getSql();
        System.out.println(ServerRouter.route(schema, sql, null, null));
        long start = System.currentTimeMillis();
        provider.route(schema, 1, sql);
        long end;
        int loop = 200 * 10000;
        start = System.currentTimeMillis();
        provider.route(schema, loop, sql);
        end = System.currentTimeMillis();
        System.out.println((end - start) * 1000.0d / loop + " us");
    }

    private SchemaConfig schema;

    public ServerRoutePerformance() {
        // CobarConfig conf = CobarServer.getInstance().getConfig();
        // schema = conf.getSchemas().get("cndb");
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        ServerRoutePerformance perf = new ServerRoutePerformance();
        perf.perf();
    }

    protected SchemaConfig getSchema() {
        return schema;
    }

}
