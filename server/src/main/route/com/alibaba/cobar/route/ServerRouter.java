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
package com.alibaba.cobar.route;

import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLNonTransientException;
import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.log4j.Logger;

import com.alibaba.cobar.config.model.SchemaConfig;
import com.alibaba.cobar.config.model.TableConfig;
import com.alibaba.cobar.config.model.rule.RuleAlgorithm;
import com.alibaba.cobar.config.model.rule.RuleConfig;
import com.alibaba.cobar.config.model.rule.TableRuleConfig;
import com.alibaba.cobar.parser.ast.ASTNode;
import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.ReplacableExpression;
import com.alibaba.cobar.parser.ast.expression.comparison.InExpression;
import com.alibaba.cobar.parser.ast.expression.misc.InExpressionList;
import com.alibaba.cobar.parser.ast.expression.primary.Identifier;
import com.alibaba.cobar.parser.ast.expression.primary.RowExpression;
import com.alibaba.cobar.parser.ast.stmt.SQLStatement;
import com.alibaba.cobar.parser.ast.stmt.dal.DALShowStatement;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLInsertReplaceStatement;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLSelectStatement;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLSelectUnionStatement;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLUpdateStatement;
import com.alibaba.cobar.parser.recognizer.SQLParserDelegate;
import com.alibaba.cobar.parser.recognizer.mysql.syntax.MySQLParser;
import com.alibaba.cobar.parser.util.ArrayUtil;
import com.alibaba.cobar.parser.util.Pair;
import com.alibaba.cobar.parser.visitor.MySQLOutputASTVisitor;
import com.alibaba.cobar.route.hint.CobarHint;
import com.alibaba.cobar.route.visitor.PartitionKeyVisitor;
import com.alibaba.cobar.util.CollectionUtil;

/**
 * @author xianmao.hexm
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public final class ServerRouter {
    private static final Logger LOGGER = Logger.getLogger(ServerRouter.class);

    public static RouteResultset route(SchemaConfig schema, String stmt, String charset, Object info)
            throws SQLNonTransientException {
        RouteResultset rrs = new RouteResultset(stmt);

        // 检查是否含有cobar hint
        int prefixIndex = HintRouter.indexOfPrefix(stmt);
        if (prefixIndex >= 0) {
            HintRouter.routeFromHint(info, schema, rrs, prefixIndex, stmt);
            return rrs;
        }

        // 检查schema是否含有拆分库
        if (schema.isNoSharding()) {
            if (schema.isKeepSqlSchema()) {
                SQLStatement ast = SQLParserDelegate.parse(stmt, charset == null
                        ? MySQLParser.DEFAULT_CHARSET : charset);
                PartitionKeyVisitor visitor = new PartitionKeyVisitor(schema.getTables());
                visitor.setTrimSchema(schema.getName());
                ast.accept(visitor);
                if (visitor.isSchemaTrimmed()) {
                    stmt = genSQL(ast, stmt);
                }
            }
            RouteResultsetNode[] nodes = new RouteResultsetNode[1];
            nodes[0] = new RouteResultsetNode(schema.getDataNode(), stmt);
            rrs.setNodes(nodes);
            return rrs;
        }

        // 生成和展开AST
        SQLStatement ast = SQLParserDelegate.parse(stmt, charset == null ? MySQLParser.DEFAULT_CHARSET : charset);
        PartitionKeyVisitor visitor = new PartitionKeyVisitor(schema.getTables());
        visitor.setTrimSchema(schema.isKeepSqlSchema() ? schema.getName() : null);
        ast.accept(visitor);

        // 如果sql包含用户自定义的schema，则路由到default节点
        if (schema.isKeepSqlSchema() && visitor.isCustomedSchema()) {
            if (visitor.isSchemaTrimmed()) {
                stmt = genSQL(ast, stmt);
            }
            RouteResultsetNode[] nodes = new RouteResultsetNode[1];
            nodes[0] = new RouteResultsetNode(schema.getDataNode(), stmt);
            rrs.setNodes(nodes);
            return rrs;
        }

        // 元数据语句路由
        if (visitor.isTableMetaRead()) {
            MetaRouter.routeForTableMeta(rrs, schema, ast, visitor, stmt);
            if (visitor.isNeedRewriteField()) {
                rrs.setFlag(RouteResultset.REWRITE_FIELD);
            }
            return rrs;
        }

        // 匹配规则
        TableConfig matchedTable = null;
        RuleConfig rule = null;
        Map<String, List<Object>> columnValues = null;
        Map<String, Map<String, List<Object>>> astExt = visitor.getColumnValue();
        Map<String, TableConfig> tables = schema.getTables();
        ft: for (Entry<String, Map<String, List<Object>>> e : astExt.entrySet()) {
            Map<String, List<Object>> col2Val = e.getValue();
            TableConfig tc = tables.get(e.getKey());
            if (tc == null) {
                continue;
            }
            if (matchedTable == null) {
                matchedTable = tc;
            }
            if (col2Val == null || col2Val.isEmpty()) {
                continue;
            }
            TableRuleConfig tr = tc.getRule();
            if (tr != null) {
                for (RuleConfig rc : tr.getRules()) {
                    boolean match = true;
                    for (String ruleColumn : rc.getColumns()) {
                        match &= col2Val.containsKey(ruleColumn);
                    }
                    if (match) {
                        columnValues = col2Val;
                        rule = rc;
                        matchedTable = tc;
                        break ft;
                    }
                }
            }
        }

        // 规则匹配处理，表级别和列级别。
        if (matchedTable == null) {
            String sql = visitor.isSchemaTrimmed() ? genSQL(ast, stmt) : stmt;
            RouteResultsetNode[] rn = new RouteResultsetNode[1];
            if ("".equals(schema.getDataNode()) && isSystemReadSQL(ast)) {
                rn[0] = new RouteResultsetNode(schema.getRandomDataNode(), sql);
            } else {
                rn[0] = new RouteResultsetNode(schema.getDataNode(), sql);
            }
            rrs.setNodes(rn);
            return rrs;
        }
        if (rule == null) {
            if (matchedTable.isRuleRequired()) {
                throw new IllegalArgumentException("route rule for table " + matchedTable.getName() + " is required: "
                        + stmt);
            }
            String[] dataNodes = matchedTable.getDataNodes();
            String sql = visitor.isSchemaTrimmed() ? genSQL(ast, stmt) : stmt;
            RouteResultsetNode[] rn = new RouteResultsetNode[dataNodes.length];
            for (int i = 0; i < dataNodes.length; ++i) {
                rn[i] = new RouteResultsetNode(dataNodes[i], sql);
            }
            rrs.setNodes(rn);
            setGroupFlagAndLimit(rrs, visitor);
            return rrs;
        }

        // 规则计算
        validateAST(ast, matchedTable, rule, visitor);
        Map<Integer, List<Object[]>> dnMap = ruleCalculate(matchedTable, rule, columnValues);
        if (dnMap == null || dnMap.isEmpty()) {
            throw new IllegalArgumentException("No target dataNode for rule " + rule);
        }

        // 判断路由结果是单库还是多库
        if (dnMap.size() == 1) {
            String dataNode = matchedTable.getDataNodes()[dnMap.keySet().iterator().next()];
            String sql = visitor.isSchemaTrimmed() ? genSQL(ast, stmt) : stmt;
            RouteResultsetNode[] rn = new RouteResultsetNode[1];
            rn[0] = new RouteResultsetNode(dataNode, sql);
            rrs.setNodes(rn);
        } else {
            RouteResultsetNode[] rn = new RouteResultsetNode[dnMap.size()];
            if (ast instanceof DMLInsertReplaceStatement) {
                DMLInsertReplaceStatement ir = (DMLInsertReplaceStatement) ast;
                dispatchInsertReplace(rn, ir, rule.getColumns(), dnMap, matchedTable, stmt, visitor);
            } else {
                dispatchWhereBasedStmt(rn, ast, rule.getColumns(), dnMap, matchedTable, stmt, visitor);
            }
            rrs.setNodes(rn);
            setGroupFlagAndLimit(rrs, visitor);
        }

        return rrs;
    }

    private static class HintRouter {
        public static int indexOfPrefix(String sql) {
            int i = 0;
            for (; i < sql.length(); ++i) {
                switch (sql.charAt(i)) {
                case ' ':
                case '\t':
                case '\r':
                case '\n':
                    continue;
                }
                break;
            }
            if (sql.startsWith(CobarHint.COBAR_HINT_PREFIX, i)) {
                return i;
            } else {
                return -1;
            }
        }

        public static void routeFromHint(Object frontConn, SchemaConfig schema, RouteResultset rrs, int prefixIndex,
                                         final String sql) throws SQLSyntaxErrorException {
            CobarHint hint = CobarHint.parserCobarHint(sql, prefixIndex);
            final String outputSql = hint.getOutputSql();
            final int replica = hint.getReplica();
            final String table = hint.getTable();
            final List<Pair<Integer, Integer>> dataNodes = hint.getDataNodes();
            final Pair<String[], Object[][]> partitionOperand = hint.getPartitionOperand();

            TableConfig tableConfig = null;
            if (table == null || schema.getTables() == null || (tableConfig = schema.getTables().get(table)) == null) {
                // table not indicated
                RouteResultsetNode[] nodes = new RouteResultsetNode[1];
                rrs.setNodes(nodes);
                if (dataNodes != null && !dataNodes.isEmpty()) {
                    Integer replicaIndex = dataNodes.get(0).getValue();
                    if (replicaIndex != null
                            && RouteResultsetNode.DEFAULT_REPLICA_INDEX.intValue() != replicaIndex.intValue()) {
                        // replica index indicated in dataNodes references
                        nodes[0] = new RouteResultsetNode(schema.getDataNode(), replicaIndex, outputSql);
                        logExplicitReplicaSet(frontConn, sql, rrs);
                        return;
                    }
                }
                nodes[0] = new RouteResultsetNode(schema.getDataNode(), replica, outputSql);
                if (replica != RouteResultsetNode.DEFAULT_REPLICA_INDEX) {
                    logExplicitReplicaSet(frontConn, sql, rrs);
                }
                return;
            }

            if (dataNodes != null && !dataNodes.isEmpty()) {
                RouteResultsetNode[] nodes = new RouteResultsetNode[dataNodes.size()];
                rrs.setNodes(nodes);
                int i = 0;
                boolean replicaSet = false;
                for (Pair<Integer, Integer> pair : dataNodes) {
                    String dataNodeName = tableConfig.getDataNodes()[pair.getKey()];
                    Integer replicaIndex = dataNodes.get(i).getValue();
                    if (replicaIndex != null
                            && RouteResultsetNode.DEFAULT_REPLICA_INDEX.intValue() != replicaIndex.intValue()) {
                        replicaSet = true;
                        nodes[i] = new RouteResultsetNode(dataNodeName, replicaIndex, outputSql);
                    } else {
                        replicaSet = replicaSet || (replica != RouteResultsetNode.DEFAULT_REPLICA_INDEX);
                        nodes[i] = new RouteResultsetNode(dataNodeName, replica, outputSql);
                    }
                    ++i;
                }
                if (replicaSet) {
                    logExplicitReplicaSet(frontConn, sql, rrs);
                }
                return;
            }

            if (partitionOperand == null) {
                String[] tableDataNodes = tableConfig.getDataNodes();
                RouteResultsetNode[] nodes = new RouteResultsetNode[tableDataNodes.length];
                rrs.setNodes(nodes);
                for (int i = 0; i < nodes.length; ++i) {
                    nodes[i] = new RouteResultsetNode(tableDataNodes[i], replica, outputSql);
                }
                return;
            }

            String[] cols = partitionOperand.getKey();
            Object[][] vals = partitionOperand.getValue();
            if (cols == null || vals == null) {
                throw new SQLSyntaxErrorException("${partitionOperand} is invalid: " + sql);
            }
            RuleConfig rule = null;
            TableRuleConfig tr = tableConfig.getRule();
            List<RuleConfig> rules = tr == null ? null : tr.getRules();
            if (rules != null) {
                for (RuleConfig r : rules) {
                    List<String> ruleCols = r.getColumns();
                    boolean match = true;
                    for (String ruleCol : ruleCols) {
                        match &= ArrayUtil.contains(cols, ruleCol);
                    }
                    if (match) {
                        rule = r;
                        break;
                    }
                }
            }

            String[] tableDataNodes = tableConfig.getDataNodes();
            if (rule == null) {
                RouteResultsetNode[] nodes = new RouteResultsetNode[tableDataNodes.length];
                rrs.setNodes(nodes);
                boolean replicaSet = false;
                for (int i = 0; i < tableDataNodes.length; ++i) {
                    replicaSet = replicaSet || (replica != RouteResultsetNode.DEFAULT_REPLICA_INDEX);
                    nodes[i] = new RouteResultsetNode(tableDataNodes[i], replica, outputSql);
                }
                if (replicaSet) {
                    logExplicitReplicaSet(frontConn, sql, rrs);
                }
                return;
            }

            Set<String> destDataNodes = calcHintDataNodes(rule, cols, vals, tableDataNodes);
            RouteResultsetNode[] nodes = new RouteResultsetNode[destDataNodes.size()];
            rrs.setNodes(nodes);
            int i = 0;
            boolean replicaSet = false;
            for (String dataNode : destDataNodes) {
                replicaSet = replicaSet || (replica != RouteResultsetNode.DEFAULT_REPLICA_INDEX);
                nodes[i++] = new RouteResultsetNode(dataNode, replica, outputSql);
            }
            if (replicaSet) {
                logExplicitReplicaSet(frontConn, sql, rrs);
            }
        }

        private static Set<String> calcHintDataNodes(RuleConfig rule, String[] cols, Object[][] vals, String[] dataNodes) {
            Set<String> destDataNodes = new HashSet<String>(2, 1);
            Map<String, Object> parameter = new HashMap<String, Object>(cols.length, 1);
            for (Object[] val : vals) {
                for (int i = 0; i < cols.length; ++i) {
                    parameter.put(cols[i], val[i]);
                }
                Integer[] dataNodeIndexes = calcDataNodeIndexesByFunction(rule.getRuleAlgorithm(), parameter);
                for (Integer index : dataNodeIndexes) {
                    destDataNodes.add(dataNodes[index]);
                }
            }
            return destDataNodes;
        }

        private static void logExplicitReplicaSet(Object frontConn, String sql, RouteResultset rrs) {
            if (frontConn != null && LOGGER.isInfoEnabled()) {
                StringBuilder s = new StringBuilder();
                s.append(frontConn).append("Explicit data node replica set from, sql=[");
                s.append(sql).append(']');
                LOGGER.info(s.toString());
            }
        }
    }

    private static class MetaRouter {

        public static void routeForTableMeta(RouteResultset rrs, SchemaConfig schema, SQLStatement ast,
                                             PartitionKeyVisitor visitor, String stmt) {
            String sql = stmt;
            if (visitor.isSchemaTrimmed()) {
                sql = genSQL(ast, stmt);
            }
            String[] tables = visitor.getMetaReadTable();
            if (tables == null) {
                throw new IllegalArgumentException("route err: tables[] is null for meta read table: " + stmt);
            }
            String[] dataNodes;
            if (tables.length <= 0) {
                dataNodes = schema.getMetaDataNodes();
            } else if (tables.length == 1) {
                dataNodes = new String[1];
                dataNodes[0] = getMetaReadDataNode(schema, tables[0]);
            } else {
                Set<String> dataNodeSet = new HashSet<String>(tables.length, 1);
                for (String table : tables) {
                    String dataNode = getMetaReadDataNode(schema, table);
                    dataNodeSet.add(dataNode);
                }
                dataNodes = new String[dataNodeSet.size()];
                Iterator<String> iter = dataNodeSet.iterator();
                for (int i = 0; i < dataNodes.length; ++i) {
                    dataNodes[i] = iter.next();
                }
            }

            RouteResultsetNode[] nodes = new RouteResultsetNode[dataNodes.length];
            rrs.setNodes(nodes);
            for (int i = 0; i < dataNodes.length; ++i) {
                nodes[i] = new RouteResultsetNode(dataNodes[i], sql);
            }
        }

        private static String getMetaReadDataNode(SchemaConfig schema, String table) {
            String dataNode = schema.getDataNode();
            Map<String, TableConfig> tables = schema.getTables();
            TableConfig tc;
            if (tables != null && (tc = tables.get(table)) != null) {
                String[] dn = tc.getDataNodes();
                if (dn != null && dn.length > 0) {
                    dataNode = dn[0];
                }
            }
            return dataNode;
        }
    }

    private static Integer[] calcDataNodeIndexesByFunction(RuleAlgorithm algorithm, Map<String, Object> parameter) {
        Integer[] dataNodeIndexes;
        Object calRst = algorithm.calculate(parameter);
        if (calRst instanceof Number) {
            dataNodeIndexes = new Integer[1];
            dataNodeIndexes[0] = ((Number) calRst).intValue();
        } else if (calRst instanceof Integer[]) {
            dataNodeIndexes = (Integer[]) calRst;
        } else if (calRst instanceof int[]) {
            int[] intArray = (int[]) calRst;
            dataNodeIndexes = new Integer[intArray.length];
            for (int i = 0; i < intArray.length; ++i) {
                dataNodeIndexes[i] = intArray[i];
            }
        } else {
            throw new IllegalArgumentException("route err: result of route function is wrong type or null: " + calRst);
        }
        return dataNodeIndexes;
    }

    private static boolean equals(String str1, String str2) {
        if (str1 == null) {
            return str2 == null;
        }
        return str1.equals(str2);
    }

    private static void validateAST(SQLStatement ast, TableConfig tc, RuleConfig rule, PartitionKeyVisitor visitor)
            throws SQLNonTransientException {
        if (ast instanceof DMLUpdateStatement) {
            List<Identifier> columns = null;
            List<String> ruleCols = rule.getColumns();
            DMLUpdateStatement update = (DMLUpdateStatement) ast;
            for (Pair<Identifier, Expression> pair : update.getValues()) {
                for (String ruleCol : ruleCols) {
                    if (equals(pair.getKey().getIdTextUpUnescape(), ruleCol)) {
                        if (columns == null) {
                            columns = new ArrayList<Identifier>(ruleCols.size());
                        }
                        columns.add(pair.getKey());
                    }
                }
            }
            if (columns == null) {
                return;
            }
            Map<String, String> alias = visitor.getTableAlias();
            for (Identifier column : columns) {
                String table = column.getLevelUnescapeUpName(2);
                table = alias.get(table);
                if (table != null && table.equals(tc.getName())) {
                    throw new SQLFeatureNotSupportedException("partition key cannot be changed");
                }
            }
        }
    }

    private static boolean isSystemReadSQL(SQLStatement ast) {
        if (ast instanceof DALShowStatement) {
            return true;
        }
        DMLSelectStatement select = null;
        if (ast instanceof DMLSelectStatement) {
            select = (DMLSelectStatement) ast;
        } else if (ast instanceof DMLSelectUnionStatement) {
            DMLSelectUnionStatement union = (DMLSelectUnionStatement) ast;
            if (union.getSelectStmtList().size() == 1) {
                select = union.getSelectStmtList().get(0);
            } else {
                return false;
            }
        } else {
            return false;
        }
        return select.getTables() == null;
    }

    private static void setGroupFlagAndLimit(RouteResultset rrs, PartitionKeyVisitor visitor) {
        rrs.setLimitSize(visitor.getLimitSize());
        switch (visitor.getGroupFuncType()) {
        case PartitionKeyVisitor.GROUP_SUM:
            rrs.setFlag(RouteResultset.SUM_FLAG);
            break;
        case PartitionKeyVisitor.GROUP_MAX:
            rrs.setFlag(RouteResultset.MAX_FLAG);
            break;
        case PartitionKeyVisitor.GROUP_MIN:
            rrs.setFlag(RouteResultset.MIN_FLAG);
            break;
        }
    }

    /**
     * @return dataNodeIndex -&gt; [partitionKeysValueTuple+]
     */
    private static Map<Integer, List<Object[]>> ruleCalculate(TableConfig matchedTable, RuleConfig rule,
                                                              Map<String, List<Object>> columnValues) {
        Map<Integer, List<Object[]>> map = new HashMap<Integer, List<Object[]>>(1, 1);
        RuleAlgorithm algorithm = rule.getRuleAlgorithm();
        List<String> cols = rule.getColumns();

        Map<String, Object> parameter = new HashMap<String, Object>(cols.size(), 1);
        ArrayList<Iterator<Object>> colsValIter = new ArrayList<Iterator<Object>>(columnValues.size());
        for (String rc : cols) {
            List<Object> list = columnValues.get(rc);
            if (list == null) {
                String msg = "route err: rule column " + rc + " dosn't exist in extract: " + columnValues;
                throw new IllegalArgumentException(msg);
            }
            colsValIter.add(list.iterator());
        }

        try {
            for (Iterator<Object> mainIter = colsValIter.get(0); mainIter.hasNext();) {
                Object[] tuple = new Object[cols.size()];
                for (int i = 0, len = cols.size(); i < len; ++i) {
                    Object value = colsValIter.get(i).next();
                    tuple[i] = value;
                    parameter.put(cols.get(i), value);
                }

                Integer[] dataNodeIndexes = calcDataNodeIndexesByFunction(algorithm, parameter);

                for (int i = 0; i < dataNodeIndexes.length; ++i) {
                    Integer dataNodeIndex = dataNodeIndexes[i];
                    List<Object[]> list = map.get(dataNodeIndex);
                    if (list == null) {
                        list = new LinkedList<Object[]>();
                        map.put(dataNodeIndex, list);
                    }
                    list.add(tuple);
                }
            }
        } catch (NoSuchElementException e) {
            String msg = "route err: different rule columns should have same value number:  " + columnValues;
            throw new IllegalArgumentException(msg, e);
        }

        return map;
    }

    private static void dispatchWhereBasedStmt(RouteResultsetNode[] rn, SQLStatement stmtAST, List<String> ruleColumns,
                                               Map<Integer, List<Object[]>> dataNodeMap, TableConfig matchedTable,
                                               String originalSQL, PartitionKeyVisitor visitor) {
        // [perf tag] 11.617 us: sharding multivalue
        if (ruleColumns.size() > 1) {
            String sql;
            if (visitor.isSchemaTrimmed()) {
                sql = genSQL(stmtAST, originalSQL);
            } else {
                sql = originalSQL;
            }
            int i = -1;
            for (Integer dataNodeId : dataNodeMap.keySet()) {
                String dataNode = matchedTable.getDataNodes()[dataNodeId];
                rn[++i] = new RouteResultsetNode(dataNode, sql);
            }
            return;
        }

        final String table = matchedTable.getName();
        Map<String, Map<Object, Set<Pair<Expression, ASTNode>>>> columnIndex = visitor.getColumnIndex(table);
        Map<Object, Set<Pair<Expression, ASTNode>>> valueMap = columnIndex.get(ruleColumns.get(0));
        replacePartitionKeyOperand(columnIndex, ruleColumns);

        Map<InExpression, Set<Expression>> unreplacedInExpr = new HashMap<InExpression, Set<Expression>>(1, 1);
        Set<ReplacableExpression> unreplacedSingleExprs = new HashSet<ReplacableExpression>();
        // [perf tag] 12.2755 us: sharding multivalue

        int nodeId = -1;
        for (Entry<Integer, List<Object[]>> en : dataNodeMap.entrySet()) {
            List<Object[]> tuples = en.getValue();

            unreplacedSingleExprs.clear();
            unreplacedInExpr.clear();
            for (Object[] tuple : tuples) {
                Object value = tuple[0];
                Set<Pair<Expression, ASTNode>> indexedExpressionPair = getExpressionSet(valueMap, value);
                for (Pair<Expression, ASTNode> pair : indexedExpressionPair) {
                    Expression expr = pair.getKey();
                    ASTNode parent = pair.getValue();
                    if (PartitionKeyVisitor.isPartitionKeyOperandSingle(expr, parent)) {
                        unreplacedSingleExprs.add((ReplacableExpression) expr);
                    } else if (PartitionKeyVisitor.isPartitionKeyOperandIn(expr, parent)) {
                        Set<Expression> newInSet = unreplacedInExpr.get(parent);
                        if (newInSet == null) {
                            newInSet = new HashSet<Expression>(indexedExpressionPair.size(), 1);
                            unreplacedInExpr.put((InExpression) parent, newInSet);
                        }
                        newInSet.add(expr);
                    }
                }
            }
            // [perf tag] 15.3745 us: sharding multivalue

            for (ReplacableExpression expr : unreplacedSingleExprs) {
                expr.clearReplaceExpr();
            }
            for (Entry<InExpression, Set<Expression>> entemp : unreplacedInExpr.entrySet()) {
                InExpression in = entemp.getKey();
                Set<Expression> set = entemp.getValue();
                if (set == null || set.isEmpty()) {
                    in.setReplaceExpr(ReplacableExpression.BOOL_FALSE);
                } else {
                    in.clearReplaceExpr();
                    InExpressionList inlist = in.getInExpressionList();
                    if (inlist != null)
                        inlist.setReplaceExpr(new ArrayList<Expression>(set));
                }
            }
            // [perf tag] 16.506 us: sharding multivalue

            String sql = genSQL(stmtAST, originalSQL);
            // [perf tag] 21.3425 us: sharding multivalue

            String dataNodeName = matchedTable.getDataNodes()[en.getKey()];
            rn[++nodeId] = new RouteResultsetNode(dataNodeName, sql);

            for (ReplacableExpression expr : unreplacedSingleExprs) {
                expr.setReplaceExpr(ReplacableExpression.BOOL_FALSE);
            }
            for (InExpression in : unreplacedInExpr.keySet()) {
                in.setReplaceExpr(ReplacableExpression.BOOL_FALSE);
                InExpressionList list = in.getInExpressionList();
                if (list != null)
                    list.clearReplaceExpr();
            }
            // [perf tag] 22.0965 us: sharding multivalue
        }
    }

    private static void replacePartitionKeyOperand(Map<String, Map<Object, Set<Pair<Expression, ASTNode>>>> index,
                                                   List<String> cols) {
        if (cols == null) {
            return;
        }
        for (String col : cols) {
            Map<Object, Set<Pair<Expression, ASTNode>>> map = index.get(col);
            if (map == null) {
                continue;
            }
            for (Set<Pair<Expression, ASTNode>> set : map.values()) {
                if (set == null) {
                    continue;
                }
                for (Pair<Expression, ASTNode> p : set) {
                    Expression expr = p.getKey();
                    ASTNode parent = p.getValue();
                    if (PartitionKeyVisitor.isPartitionKeyOperandSingle(expr, parent)) {
                        ((ReplacableExpression) expr).setReplaceExpr(ReplacableExpression.BOOL_FALSE);
                    } else if (PartitionKeyVisitor.isPartitionKeyOperandIn(expr, parent)) {
                        ((ReplacableExpression) parent).setReplaceExpr(ReplacableExpression.BOOL_FALSE);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void dispatchInsertReplace(RouteResultsetNode[] rn, DMLInsertReplaceStatement stmt,
                                              List<String> ruleColumns, Map<Integer, List<Object[]>> dataNodeMap,
                                              TableConfig matchedTable, String originalSQL, PartitionKeyVisitor visitor) {
        if (stmt.getSelect() != null) {
            dispatchWhereBasedStmt(rn, stmt, ruleColumns, dataNodeMap, matchedTable, originalSQL, visitor);
            return;
        }
        Map<String, Map<Object, Set<Pair<Expression, ASTNode>>>> colsIndex = visitor.getColumnIndex(stmt.getTable()
                                                                                                        .getIdTextUpUnescape());
        if (colsIndex == null || colsIndex.isEmpty()) {
            throw new IllegalArgumentException("columns index is empty: " + originalSQL);
        }
        ArrayList<Map<Object, Set<Pair<Expression, ASTNode>>>> colsIndexList = new ArrayList<Map<Object, Set<Pair<Expression, ASTNode>>>>(
                ruleColumns.size());
        for (int i = 0, len = ruleColumns.size(); i < len; ++i) {
            colsIndexList.add(colsIndex.get(ruleColumns.get(i)));
        }
        int dataNodeId = -1;
        for (Entry<Integer, List<Object[]>> en : dataNodeMap.entrySet()) {
            List<Object[]> tuples = en.getValue();
            HashSet<RowExpression> replaceRowList = new HashSet<RowExpression>(tuples.size());
            for (Object[] tuple : tuples) {
                Set<Pair<Expression, ASTNode>> tupleExprs = null;
                for (int i = 0; i < tuple.length; ++i) {
                    Map<Object, Set<Pair<Expression, ASTNode>>> valueMap = colsIndexList.get(i);
                    Object value = tuple[i];
                    Set<Pair<Expression, ASTNode>> set = getExpressionSet(valueMap, value);
                    tupleExprs = (Set<Pair<Expression, ASTNode>>) CollectionUtil.intersectSet(tupleExprs, set);
                }
                if (tupleExprs == null || tupleExprs.isEmpty()) {
                    throw new IllegalArgumentException("route: empty expression list for insertReplace stmt: "
                            + originalSQL);
                }
                for (Pair<Expression, ASTNode> p : tupleExprs) {
                    if (p.getValue() == stmt && p.getKey() instanceof RowExpression) {
                        replaceRowList.add((RowExpression) p.getKey());
                    }
                }
            }

            stmt.setReplaceRowList(new ArrayList<RowExpression>(replaceRowList));
            String sql = genSQL(stmt, originalSQL);
            stmt.clearReplaceRowList();
            String dataNodeName = matchedTable.getDataNodes()[en.getKey()];
            rn[++dataNodeId] = new RouteResultsetNode(dataNodeName, sql);
        }
    }

    private static Set<Pair<Expression, ASTNode>> getExpressionSet(Map<Object, Set<Pair<Expression, ASTNode>>> map,
                                                                   Object value) {
        if (map == null || map.isEmpty()) {
            return Collections.emptySet();
        }
        Set<Pair<Expression, ASTNode>> set = map.get(value);
        if (set == null) {
            return Collections.emptySet();
        }
        return set;
    }

    private static String genSQL(SQLStatement ast, String orginalSql) {
        StringBuilder s = new StringBuilder();
        ast.accept(new MySQLOutputASTVisitor(s));
        return s.toString();
    }

}
