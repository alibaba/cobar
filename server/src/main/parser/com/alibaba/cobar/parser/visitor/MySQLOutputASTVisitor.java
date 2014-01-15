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
 * (created at 2011-6-1)
 */
package com.alibaba.cobar.parser.visitor;

import static com.alibaba.cobar.parser.ast.expression.comparison.ComparisionIsExpression.IS_FALSE;
import static com.alibaba.cobar.parser.ast.expression.comparison.ComparisionIsExpression.IS_NOT_FALSE;
import static com.alibaba.cobar.parser.ast.expression.comparison.ComparisionIsExpression.IS_NOT_NULL;
import static com.alibaba.cobar.parser.ast.expression.comparison.ComparisionIsExpression.IS_NOT_TRUE;
import static com.alibaba.cobar.parser.ast.expression.comparison.ComparisionIsExpression.IS_NOT_UNKNOWN;
import static com.alibaba.cobar.parser.ast.expression.comparison.ComparisionIsExpression.IS_NULL;
import static com.alibaba.cobar.parser.ast.expression.comparison.ComparisionIsExpression.IS_TRUE;
import static com.alibaba.cobar.parser.ast.expression.comparison.ComparisionIsExpression.IS_UNKNOWN;

import java.util.List;
import java.util.Map;

import com.alibaba.cobar.parser.ast.ASTNode;
import com.alibaba.cobar.parser.ast.expression.BinaryOperatorExpression;
import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.PolyadicOperatorExpression;
import com.alibaba.cobar.parser.ast.expression.TernaryOperatorExpression;
import com.alibaba.cobar.parser.ast.expression.UnaryOperatorExpression;
import com.alibaba.cobar.parser.ast.expression.comparison.BetweenAndExpression;
import com.alibaba.cobar.parser.ast.expression.comparison.ComparisionEqualsExpression;
import com.alibaba.cobar.parser.ast.expression.comparison.ComparisionIsExpression;
import com.alibaba.cobar.parser.ast.expression.comparison.ComparisionNullSafeEqualsExpression;
import com.alibaba.cobar.parser.ast.expression.comparison.InExpression;
import com.alibaba.cobar.parser.ast.expression.logical.LogicalAndExpression;
import com.alibaba.cobar.parser.ast.expression.logical.LogicalOrExpression;
import com.alibaba.cobar.parser.ast.expression.misc.InExpressionList;
import com.alibaba.cobar.parser.ast.expression.misc.QueryExpression;
import com.alibaba.cobar.parser.ast.expression.misc.UserExpression;
import com.alibaba.cobar.parser.ast.expression.primary.CaseWhenOperatorExpression;
import com.alibaba.cobar.parser.ast.expression.primary.DefaultValue;
import com.alibaba.cobar.parser.ast.expression.primary.ExistsPrimary;
import com.alibaba.cobar.parser.ast.expression.primary.Identifier;
import com.alibaba.cobar.parser.ast.expression.primary.MatchExpression;
import com.alibaba.cobar.parser.ast.expression.primary.ParamMarker;
import com.alibaba.cobar.parser.ast.expression.primary.PlaceHolder;
import com.alibaba.cobar.parser.ast.expression.primary.RowExpression;
import com.alibaba.cobar.parser.ast.expression.primary.SysVarPrimary;
import com.alibaba.cobar.parser.ast.expression.primary.UsrDefVarPrimary;
import com.alibaba.cobar.parser.ast.expression.primary.VariableExpression;
import com.alibaba.cobar.parser.ast.expression.primary.function.FunctionExpression;
import com.alibaba.cobar.parser.ast.expression.primary.function.cast.Cast;
import com.alibaba.cobar.parser.ast.expression.primary.function.cast.Convert;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.Extract;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.GetFormat;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.Timestampadd;
import com.alibaba.cobar.parser.ast.expression.primary.function.datetime.Timestampdiff;
import com.alibaba.cobar.parser.ast.expression.primary.function.groupby.Avg;
import com.alibaba.cobar.parser.ast.expression.primary.function.groupby.Count;
import com.alibaba.cobar.parser.ast.expression.primary.function.groupby.GroupConcat;
import com.alibaba.cobar.parser.ast.expression.primary.function.groupby.Max;
import com.alibaba.cobar.parser.ast.expression.primary.function.groupby.Min;
import com.alibaba.cobar.parser.ast.expression.primary.function.groupby.Sum;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.Char;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.Trim;
import com.alibaba.cobar.parser.ast.expression.primary.literal.IntervalPrimary;
import com.alibaba.cobar.parser.ast.expression.primary.literal.LiteralBitField;
import com.alibaba.cobar.parser.ast.expression.primary.literal.LiteralBoolean;
import com.alibaba.cobar.parser.ast.expression.primary.literal.LiteralHexadecimal;
import com.alibaba.cobar.parser.ast.expression.primary.literal.LiteralNull;
import com.alibaba.cobar.parser.ast.expression.primary.literal.LiteralNumber;
import com.alibaba.cobar.parser.ast.expression.primary.literal.LiteralString;
import com.alibaba.cobar.parser.ast.expression.string.LikeExpression;
import com.alibaba.cobar.parser.ast.expression.type.CollateExpression;
import com.alibaba.cobar.parser.ast.fragment.GroupBy;
import com.alibaba.cobar.parser.ast.fragment.Limit;
import com.alibaba.cobar.parser.ast.fragment.OrderBy;
import com.alibaba.cobar.parser.ast.fragment.SortOrder;
import com.alibaba.cobar.parser.ast.fragment.VariableScope;
import com.alibaba.cobar.parser.ast.fragment.ddl.ColumnDefinition;
import com.alibaba.cobar.parser.ast.fragment.ddl.TableOptions;
import com.alibaba.cobar.parser.ast.fragment.ddl.datatype.DataType;
import com.alibaba.cobar.parser.ast.fragment.ddl.index.IndexColumnName;
import com.alibaba.cobar.parser.ast.fragment.ddl.index.IndexOption;
import com.alibaba.cobar.parser.ast.fragment.tableref.Dual;
import com.alibaba.cobar.parser.ast.fragment.tableref.IndexHint;
import com.alibaba.cobar.parser.ast.fragment.tableref.InnerJoin;
import com.alibaba.cobar.parser.ast.fragment.tableref.NaturalJoin;
import com.alibaba.cobar.parser.ast.fragment.tableref.OuterJoin;
import com.alibaba.cobar.parser.ast.fragment.tableref.StraightJoin;
import com.alibaba.cobar.parser.ast.fragment.tableref.SubqueryFactor;
import com.alibaba.cobar.parser.ast.fragment.tableref.TableRefFactor;
import com.alibaba.cobar.parser.ast.fragment.tableref.TableReference;
import com.alibaba.cobar.parser.ast.fragment.tableref.TableReferences;
import com.alibaba.cobar.parser.ast.stmt.dal.DALSetCharacterSetStatement;
import com.alibaba.cobar.parser.ast.stmt.dal.DALSetNamesStatement;
import com.alibaba.cobar.parser.ast.stmt.dal.DALSetStatement;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowAuthors;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowBinLogEvent;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowBinaryLog;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowCharaterSet;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowCollation;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowColumns;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowContributors;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowCreate;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowDatabases;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowEngine;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowEngines;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowErrors;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowEvents;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowFunctionCode;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowFunctionStatus;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowGrants;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowIndex;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowMasterStatus;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowOpenTables;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowPlugins;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowPrivileges;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowProcedureCode;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowProcedureStatus;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowProcesslist;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowProfile;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowProfiles;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowSlaveHosts;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowSlaveStatus;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowStatus;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowTableStatus;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowTables;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowTriggers;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowVariables;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowWarnings;
import com.alibaba.cobar.parser.ast.stmt.ddl.DDLAlterTableStatement;
import com.alibaba.cobar.parser.ast.stmt.ddl.DDLAlterTableStatement.AlterSpecification;
import com.alibaba.cobar.parser.ast.stmt.ddl.DDLCreateIndexStatement;
import com.alibaba.cobar.parser.ast.stmt.ddl.DDLCreateTableStatement;
import com.alibaba.cobar.parser.ast.stmt.ddl.DDLDropIndexStatement;
import com.alibaba.cobar.parser.ast.stmt.ddl.DDLDropTableStatement;
import com.alibaba.cobar.parser.ast.stmt.ddl.DDLRenameTableStatement;
import com.alibaba.cobar.parser.ast.stmt.ddl.DDLTruncateStatement;
import com.alibaba.cobar.parser.ast.stmt.ddl.DescTableStatement;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLCallStatement;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLDeleteStatement;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLInsertStatement;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLReplaceStatement;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLSelectStatement;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLSelectUnionStatement;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLUpdateStatement;
import com.alibaba.cobar.parser.ast.stmt.extension.ExtDDLCreatePolicy;
import com.alibaba.cobar.parser.ast.stmt.extension.ExtDDLDropPolicy;
import com.alibaba.cobar.parser.ast.stmt.mts.MTSReleaseStatement;
import com.alibaba.cobar.parser.ast.stmt.mts.MTSRollbackStatement;
import com.alibaba.cobar.parser.ast.stmt.mts.MTSSavepointStatement;
import com.alibaba.cobar.parser.ast.stmt.mts.MTSSetTransactionStatement;
import com.alibaba.cobar.parser.util.Pair;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public final class MySQLOutputASTVisitor implements SQLASTVisitor {
    private static final Object[] EMPTY_OBJ_ARRAY = new Object[0];
    private static final int[] EMPTY_INT_ARRAY = new int[0];
    private final StringBuilder appendable;
    private final Object[] args;
    private int[] argsIndex;
    private Map<PlaceHolder, Object> placeHolderToString;

    public MySQLOutputASTVisitor(StringBuilder appendable) {
        this(appendable, null);
    }

    /**
     * @param args parameters for {@link java.sql.PreparedStatement
     *            preparedStmt}
     */
    public MySQLOutputASTVisitor(StringBuilder appendable, Object[] args) {
        this.appendable = appendable;
        this.args = args == null ? EMPTY_OBJ_ARRAY : args;
        this.argsIndex = args == null ? EMPTY_INT_ARRAY : new int[args.length];
    }

    public void setPlaceHolderToString(Map<PlaceHolder, Object> map) {
        this.placeHolderToString = map;
    }

    public String getSql() {
        return appendable.toString();
    }

    /**
     * @return never null. rst[i] ≡ {@link #args}[{@link #argsIndex}[i]]
     */
    public Object[] getArguments() {
        final int argsIndexSize = argsIndex.length;
        if (argsIndexSize <= 0)
            return EMPTY_OBJ_ARRAY;

        boolean noChange = true;
        for (int i = 0; i < argsIndexSize; ++i) {
            if (i != argsIndex[i]) {
                noChange = false;
                break;
            }
        }
        if (noChange)
            return args;

        Object[] rst = new Object[argsIndexSize];
        for (int i = 0; i < argsIndexSize; ++i) {
            rst[i] = args[argsIndex[i]];
        }
        return rst;
    }

    /**
     * @param list never null
     */
    private void printList(List<? extends ASTNode> list) {
        printList(list, ", ");
    }

    /**
     * @param list never null
     */
    private void printList(List<? extends ASTNode> list, String sep) {
        boolean isFst = true;
        for (ASTNode arg : list) {
            if (isFst)
                isFst = false;
            else
                appendable.append(sep);
            arg.accept(this);
        }
    }

    @Override
    public void visit(BetweenAndExpression node) {
        Expression comparee = node.getFirst();
        boolean paren = comparee.getPrecedence() <= node.getPrecedence();
        if (paren)
            appendable.append('(');
        comparee.accept(this);
        if (paren)
            appendable.append(')');

        if (node.isNot())
            appendable.append(" NOT BETWEEN ");
        else
            appendable.append(" BETWEEN ");

        Expression start = node.getSecond();
        paren = start.getPrecedence() < node.getPrecedence();
        if (paren)
            appendable.append('(');
        start.accept(this);
        if (paren)
            appendable.append(')');

        appendable.append(" AND ");

        Expression end = node.getThird();
        paren = end.getPrecedence() < node.getPrecedence();
        if (paren)
            appendable.append('(');
        end.accept(this);
        if (paren)
            appendable.append(')');
    }

    @Override
    public void visit(ComparisionIsExpression node) {
        Expression comparee = node.getOperand();
        boolean paren = comparee.getPrecedence() < node.getPrecedence();
        if (paren)
            appendable.append('(');
        comparee.accept(this);
        if (paren)
            appendable.append(')');
        switch (node.getMode()) {
        case IS_NULL:
            appendable.append(" IS NULL");
            break;
        case IS_TRUE:
            appendable.append(" IS TRUE");
            break;
        case IS_FALSE:
            appendable.append(" IS FALSE");
            break;
        case IS_UNKNOWN:
            appendable.append(" IS UNKNOWN");
            break;
        case IS_NOT_NULL:
            appendable.append(" IS NOT NULL");
            break;
        case IS_NOT_TRUE:
            appendable.append(" IS NOT TRUE");
            break;
        case IS_NOT_FALSE:
            appendable.append(" IS NOT FALSE");
            break;
        case IS_NOT_UNKNOWN:
            appendable.append(" IS NOT UNKNOWN");
            break;
        default:
            throw new IllegalArgumentException("unknown mode for IS expression: " + node.getMode());
        }
    }

    @Override
    public void visit(InExpressionList node) {
        appendable.append('(');
        printList(node.getList());
        appendable.append(')');
    }

    @Override
    public void visit(LikeExpression node) {
        Expression comparee = node.getFirst();
        boolean paren = comparee.getPrecedence() < node.getPrecedence();
        if (paren)
            appendable.append('(');
        comparee.accept(this);
        if (paren)
            appendable.append(')');

        if (node.isNot())
            appendable.append(" NOT LIKE ");
        else
            appendable.append(" LIKE ");

        Expression pattern = node.getSecond();
        paren = pattern.getPrecedence() <= node.getPrecedence();
        if (paren)
            appendable.append('(');
        pattern.accept(this);
        if (paren)
            appendable.append(')');

        Expression escape = node.getThird();
        if (escape != null) {
            appendable.append(" ESCAPE ");
            paren = escape.getPrecedence() <= node.getPrecedence();
            if (paren)
                appendable.append('(');
            escape.accept(this);
            if (paren)
                appendable.append(')');
        }
    }

    @Override
    public void visit(CollateExpression node) {
        Expression string = node.getString();
        boolean paren = string.getPrecedence() < node.getPrecedence();
        if (paren)
            appendable.append('(');
        string.accept(this);
        if (paren)
            appendable.append(')');

        appendable.append(" COLLATE ").append(node.getCollateName());
    }

    @Override
    public void visit(UserExpression node) {
        appendable.append(node.getUserAtHost());
    }

    @Override
    public void visit(UnaryOperatorExpression node) {
        appendable.append(node.getOperator()).append(' ');
        boolean paren = node.getOperand().getPrecedence() < node.getPrecedence();
        if (paren)
            appendable.append('(');
        node.getOperand().accept(this);
        if (paren)
            appendable.append(')');
    }

    @Override
    public void visit(BinaryOperatorExpression node) {
        Expression left = node.getLeftOprand();
        boolean paren = node.isLeftCombine()
                ? left.getPrecedence() < node.getPrecedence() : left.getPrecedence() <= node.getPrecedence();
        if (paren)
            appendable.append('(');
        left.accept(this);
        if (paren)
            appendable.append(')');

        appendable.append(' ').append(node.getOperator()).append(' ');

        Expression right = node.getRightOprand();
        paren = node.isLeftCombine()
                ? right.getPrecedence() <= node.getPrecedence() : right.getPrecedence() < node.getPrecedence();
        if (paren)
            appendable.append('(');
        right.accept(this);
        if (paren)
            appendable.append(')');
    }

    @Override
    public void visit(PolyadicOperatorExpression node) {
        for (int i = 0, len = node.getArity(); i < len; ++i) {
            if (i > 0)
                appendable.append(' ').append(node.getOperator()).append(' ');
            Expression operand = node.getOperand(i);
            boolean paren = operand.getPrecedence() < node.getPrecedence();
            if (paren)
                appendable.append('(');
            operand.accept(this);
            if (paren)
                appendable.append(')');
        }
    }

    @Override
    public void visit(LogicalAndExpression node) {
        visit((PolyadicOperatorExpression) node);
    }

    @Override
    public void visit(LogicalOrExpression node) {
        visit((PolyadicOperatorExpression) node);
    }

    @Override
    public void visit(ComparisionEqualsExpression node) {
        visit((BinaryOperatorExpression) node);
    }

    @Override
    public void visit(ComparisionNullSafeEqualsExpression node) {
        visit((BinaryOperatorExpression) node);
    }

    @Override
    public void visit(InExpression node) {
        visit((BinaryOperatorExpression) node);
    }

    @Override
    public void visit(FunctionExpression node) {
        String functionName = node.getFunctionName();
        appendable.append(functionName).append('(');
        printList(node.getArguments());
        appendable.append(')');
    }

    @Override
    public void visit(Char node) {
        String functionName = node.getFunctionName();
        appendable.append(functionName).append('(');
        printList(node.getArguments());
        String charset = node.getCharset();
        if (charset != null) {
            appendable.append(" USING ").append(charset);
        }
        appendable.append(')');
    }

    @Override
    public void visit(Convert node) {
        String functionName = node.getFunctionName();
        appendable.append(functionName).append('(');
        printList(node.getArguments());
        String transcodeName = node.getTranscodeName();
        appendable.append(" USING ").append(transcodeName);
        appendable.append(')');
    }

    @Override
    public void visit(Trim node) {
        String functionName = node.getFunctionName();
        appendable.append(functionName).append('(');
        Expression remStr = node.getRemainString();
        switch (node.getDirection()) {
        case DEFAULT:
            if (remStr != null) {
                remStr.accept(this);
                appendable.append(" FROM ");
            }
            break;
        case BOTH:
            appendable.append("BOTH ");
            if (remStr != null)
                remStr.accept(this);
            appendable.append(" FROM ");
            break;
        case LEADING:
            appendable.append("LEADING ");
            if (remStr != null)
                remStr.accept(this);
            appendable.append(" FROM ");
            break;
        case TRAILING:
            appendable.append("TRAILING ");
            if (remStr != null)
                remStr.accept(this);
            appendable.append(" FROM ");
            break;
        default:
            throw new IllegalArgumentException("unknown trim direction: " + node.getDirection());
        }
        Expression str = node.getString();
        str.accept(this);
        appendable.append(')');
    }

    @Override
    public void visit(Cast node) {
        String functionName = node.getFunctionName();
        appendable.append(functionName).append('(');
        node.getExpr().accept(this);
        appendable.append(" AS ");
        String typeName = node.getTypeName();
        appendable.append(typeName);
        Expression info1 = node.getTypeInfo1();
        if (info1 != null) {
            appendable.append('(');
            info1.accept(this);
            Expression info2 = node.getTypeInfo2();
            if (info2 != null) {
                appendable.append(", ");
                info2.accept(this);
            }
            appendable.append(')');
        }
        appendable.append(')');
    }

    @Override
    public void visit(Avg node) {
        String functionName = node.getFunctionName();
        appendable.append(functionName).append('(');
        if (node.isDistinct()) {
            appendable.append("DISTINCT ");
        }
        printList(node.getArguments());
        appendable.append(')');
    }

    @Override
    public void visit(Max node) {
        String functionName = node.getFunctionName();
        appendable.append(functionName).append('(');
        if (node.isDistinct()) {
            appendable.append("DISTINCT ");
        }
        printList(node.getArguments());
        appendable.append(')');
    }

    @Override
    public void visit(Min node) {
        String functionName = node.getFunctionName();
        appendable.append(functionName).append('(');
        if (node.isDistinct()) {
            appendable.append("DISTINCT ");
        }
        printList(node.getArguments());
        appendable.append(')');
    }

    @Override
    public void visit(Sum node) {
        String functionName = node.getFunctionName();
        appendable.append(functionName).append('(');
        if (node.isDistinct()) {
            appendable.append("DISTINCT ");
        }
        printList(node.getArguments());
        appendable.append(')');
    }

    @Override
    public void visit(Count node) {
        String functionName = node.getFunctionName();
        appendable.append(functionName).append('(');
        if (node.isDistinct()) {
            appendable.append("DISTINCT ");
        }
        printList(node.getArguments());
        appendable.append(')');
    }

    @Override
    public void visit(GroupConcat node) {
        String functionName = node.getFunctionName();
        appendable.append(functionName).append('(');
        if (node.isDistinct()) {
            appendable.append("DISTINCT ");
        }
        printList(node.getArguments());
        Expression orderBy = node.getOrderBy();
        if (orderBy != null) {
            appendable.append(" ORDER BY ");
            orderBy.accept(this);
            if (node.isDesc())
                appendable.append(" DESC");
            else
                appendable.append(" ASC");
            List<Expression> list = node.getAppendedColumnNames();
            if (list != null && !list.isEmpty()) {
                appendable.append(", ");
                printList(list);
            }
        }
        String sep = node.getSeparator();
        if (sep != null) {
            appendable.append(" SEPARATOR ").append(sep);
        }
        appendable.append(')');
    }

    @Override
    public void visit(Extract node) {
        appendable.append("EXTRACT(").append(node.getUnit().name()).append(" FROM ");
        printList(node.getArguments());
        appendable.append(')');
    }

    @Override
    public void visit(Timestampdiff node) {
        appendable.append("TIMESTAMPDIFF(").append(node.getUnit().name()).append(", ");
        printList(node.getArguments());
        appendable.append(')');
    }

    @Override
    public void visit(Timestampadd node) {
        appendable.append("TIMESTAMPADD(").append(node.getUnit().name()).append(", ");
        printList(node.getArguments());
        appendable.append(')');
    }

    @Override
    public void visit(GetFormat node) {
        appendable.append("GET_FORMAT(");
        GetFormat.FormatType type = node.getFormatType();
        appendable.append(type.name()).append(", ");
        printList(node.getArguments());
        appendable.append(')');
    }

    @Override
    public void visit(PlaceHolder node) {
        if (placeHolderToString == null) {
            appendable.append("${").append(node.getName()).append('}');
            return;
        }
        Object toStringer = placeHolderToString.get(node);
        if (toStringer == null) {
            appendable.append("${").append(node.getName()).append('}');
        } else {
            appendable.append(toStringer.toString());
        }
    }

    @Override
    public void visit(IntervalPrimary node) {
        appendable.append("INTERVAL ");
        Expression quantity = node.getQuantity();
        boolean paren = quantity.getPrecedence() < node.getPrecedence();
        if (paren)
            appendable.append('(');
        quantity.accept(this);
        if (paren)
            appendable.append(')');
        IntervalPrimary.Unit unit = node.getUnit();
        appendable.append(' ').append(unit.name());
    }

    @Override
    public void visit(LiteralBitField node) {
        String introducer = node.getIntroducer();
        if (introducer != null)
            appendable.append(introducer).append(' ');
        appendable.append("b'").append(node.getText()).append('\'');
    }

    @Override
    public void visit(LiteralBoolean node) {
        if (node.isTrue()) {
            appendable.append("TRUE");
        } else {
            appendable.append("FALSE");
        }
    }

    @Override
    public void visit(LiteralHexadecimal node) {
        String introducer = node.getIntroducer();
        if (introducer != null)
            appendable.append(introducer).append(' ');
        appendable.append("x'");
        node.appendTo(appendable);
        appendable.append('\'');
    }

    @Override
    public void visit(LiteralNull node) {
        appendable.append("NULL");
    }

    @Override
    public void visit(LiteralNumber node) {
        appendable.append(String.valueOf(node.getNumber()));
    }

    @Override
    public void visit(LiteralString node) {
        String introducer = node.getIntroducer();
        if (introducer != null) {
            appendable.append(introducer);
        } else if (node.isNchars()) {
            appendable.append('N');
        }
        appendable.append('\'').append(node.getString()).append('\'');
    }

    @Override
    public void visit(CaseWhenOperatorExpression node) {
        appendable.append("CASE");
        Expression comparee = node.getComparee();
        if (comparee != null) {
            appendable.append(' ');
            comparee.accept(this);
        }
        List<Pair<Expression, Expression>> whenList = node.getWhenList();
        for (Pair<Expression, Expression> whenthen : whenList) {
            appendable.append(" WHEN ");
            Expression when = whenthen.getKey();
            when.accept(this);
            appendable.append(" THEN ");
            Expression then = whenthen.getValue();
            then.accept(this);
        }
        Expression elseRst = node.getElseResult();
        if (elseRst != null) {
            appendable.append(" ELSE ");
            elseRst.accept(this);
        }
        appendable.append(" END");
    }

    @Override
    public void visit(DefaultValue node) {
        appendable.append("DEFAULT");
    }

    @Override
    public void visit(ExistsPrimary node) {
        appendable.append("EXISTS (");
        node.getSubquery().accept(this);
        appendable.append(')');
    }

    @Override
    public void visit(Identifier node) {
        Expression parent = node.getParent();
        if (parent != null) {
            parent.accept(this);
            appendable.append('.');
        }
        appendable.append(node.getIdText());
    }

    private static boolean containsCompIn(Expression pat) {
        if (pat.getPrecedence() > Expression.PRECEDENCE_COMPARISION)
            return false;
        if (pat instanceof BinaryOperatorExpression) {
            if (pat instanceof InExpression) {
                return true;
            }
            BinaryOperatorExpression bp = (BinaryOperatorExpression) pat;
            if (bp.isLeftCombine()) {
                return containsCompIn(bp.getLeftOprand());
            } else {
                return containsCompIn(bp.getLeftOprand());
            }
        } else if (pat instanceof ComparisionIsExpression) {
            ComparisionIsExpression is = (ComparisionIsExpression) pat;
            return containsCompIn(is.getOperand());
        } else if (pat instanceof TernaryOperatorExpression) {
            TernaryOperatorExpression tp = (TernaryOperatorExpression) pat;
            return containsCompIn(tp.getFirst()) || containsCompIn(tp.getSecond()) || containsCompIn(tp.getThird());
        } else if (pat instanceof UnaryOperatorExpression) {
            UnaryOperatorExpression up = (UnaryOperatorExpression) pat;
            return containsCompIn(up.getOperand());
        } else {
            return false;
        }
    }

    @Override
    public void visit(MatchExpression node) {
        appendable.append("MATCH (");
        printList(node.getColumns());
        appendable.append(") AGAINST (");
        Expression pattern = node.getPattern();
        boolean inparen = containsCompIn(pattern);
        if (inparen)
            appendable.append('(');
        pattern.accept(this);
        if (inparen)
            appendable.append(')');
        switch (node.getModifier()) {
        case IN_BOOLEAN_MODE:
            appendable.append(" IN BOOLEAN MODE");
            break;
        case IN_NATURAL_LANGUAGE_MODE:
            appendable.append(" IN NATURAL LANGUAGE MODE");
            break;
        case IN_NATURAL_LANGUAGE_MODE_WITH_QUERY_EXPANSION:
            appendable.append(" IN NATURAL LANGUAGE MODE WITH QUERY EXPANSION");
            break;
        case WITH_QUERY_EXPANSION:
            appendable.append(" WITH QUERY EXPANSION");
            break;
        case _DEFAULT:
            break;
        default:
            throw new IllegalArgumentException("unkown modifier for match expression: " + node.getModifier());
        }
        appendable.append(')');
    }

    private int index = -1;

    private void appendArgsIndex(int value) {
        int i = ++index;
        if (argsIndex.length <= i) {
            int[] a = new int[i + 1];
            if (i > 0)
                System.arraycopy(argsIndex, 0, a, 0, i);
            argsIndex = a;
        }
        argsIndex[i] = value;
    }

    @Override
    public void visit(ParamMarker node) {
        appendable.append('?');
        appendArgsIndex(node.getParamIndex() - 1);
    }

    @Override
    public void visit(RowExpression node) {
        appendable.append("ROW(");
        printList(node.getRowExprList());
        appendable.append(')');
    }

    @Override
    public void visit(SysVarPrimary node) {
        VariableScope scope = node.getScope();
        switch (scope) {
        case GLOBAL:
            appendable.append("@@global.");
            break;
        case SESSION:
            appendable.append("@@");
            break;
        default:
            throw new IllegalArgumentException("unkown scope for sysVar primary: " + scope);
        }
        appendable.append(node.getVarText());
    }

    @Override
    public void visit(UsrDefVarPrimary node) {
        appendable.append(node.getVarText());
    }

    @Override
    public void visit(IndexHint node) {
        IndexHint.IndexAction action = node.getAction();
        switch (action) {
        case FORCE:
            appendable.append("FORCE ");
            break;
        case IGNORE:
            appendable.append("IGNORE ");
            break;
        case USE:
            appendable.append("USE ");
            break;
        default:
            throw new IllegalArgumentException("unkown index action for index hint: " + action);
        }
        IndexHint.IndexType type = node.getType();
        switch (type) {
        case INDEX:
            appendable.append("INDEX ");
            break;
        case KEY:
            appendable.append("KEY ");
            break;
        default:
            throw new IllegalArgumentException("unkown index type for index hint: " + type);
        }
        IndexHint.IndexScope scope = node.getScope();
        switch (scope) {
        case GROUP_BY:
            appendable.append("FOR GROUP BY ");
            break;
        case ORDER_BY:
            appendable.append("FOR ORDER BY ");
            break;
        case JOIN:
            appendable.append("FOR JOIN ");
            break;
        case ALL:
            break;
        default:
            throw new IllegalArgumentException("unkown index scope for index hint: " + scope);
        }
        appendable.append('(');
        List<String> indexList = node.getIndexList();
        boolean isFst = true;
        for (String indexName : indexList) {
            if (isFst)
                isFst = false;
            else
                appendable.append(", ");
            appendable.append(indexName);
        }
        appendable.append(')');
    }

    @Override
    public void visit(TableReferences node) {
        printList(node.getTableReferenceList());
    }

    @Override
    public void visit(InnerJoin node) {
        TableReference left = node.getLeftTableRef();
        boolean paren = left.getPrecedence() < node.getPrecedence();
        if (paren)
            appendable.append('(');
        left.accept(this);
        if (paren)
            appendable.append(')');

        appendable.append(" INNER JOIN ");
        TableReference right = node.getRightTableRef();
        paren = right.getPrecedence() <= node.getPrecedence();
        if (paren)
            appendable.append('(');
        right.accept(this);
        if (paren)
            appendable.append(')');

        Expression on = node.getOnCond();
        List<String> using = node.getUsing();
        if (on != null) {
            appendable.append(" ON ");
            on.accept(this);
        } else if (using != null) {
            appendable.append(" USING (");
            boolean isFst = true;
            for (String col : using) {
                if (isFst)
                    isFst = false;
                else
                    appendable.append(", ");
                appendable.append(col);
            }
            appendable.append(")");
        }
    }

    @Override
    public void visit(NaturalJoin node) {
        TableReference left = node.getLeftTableRef();
        boolean paren = left.getPrecedence() < node.getPrecedence();
        if (paren)
            appendable.append('(');
        left.accept(this);
        if (paren)
            appendable.append(')');

        appendable.append(" NATURAL ");
        if (node.isOuter()) {
            if (node.isLeft())
                appendable.append("LEFT ");
            else
                appendable.append("RIGHT ");
        }
        appendable.append("JOIN ");

        TableReference right = node.getRightTableRef();
        paren = right.getPrecedence() <= node.getPrecedence();
        if (paren)
            appendable.append('(');
        right.accept(this);
        if (paren)
            appendable.append(')');
    }

    @Override
    public void visit(StraightJoin node) {
        TableReference left = node.getLeftTableRef();
        boolean paren = left.getPrecedence() < node.getPrecedence();
        if (paren)
            appendable.append('(');
        left.accept(this);
        if (paren)
            appendable.append(')');

        appendable.append(" STRAIGHT_JOIN ");

        TableReference right = node.getRightTableRef();
        paren = right.getPrecedence() <= node.getPrecedence();
        if (paren)
            appendable.append('(');
        right.accept(this);
        if (paren)
            appendable.append(')');

        Expression on = node.getOnCond();
        if (on != null) {
            appendable.append(" ON ");
            on.accept(this);
        }
    }

    @Override
    public void visit(OuterJoin node) {
        TableReference left = node.getLeftTableRef();
        boolean paren = left.getPrecedence() < node.getPrecedence();
        if (paren)
            appendable.append('(');
        left.accept(this);
        if (paren)
            appendable.append(')');

        if (node.isLeftJoin())
            appendable.append(" LEFT JOIN ");
        else
            appendable.append(" RIGHT JOIN ");

        TableReference right = node.getRightTableRef();
        paren = right.getPrecedence() <= node.getPrecedence();
        if (paren)
            appendable.append('(');
        right.accept(this);
        if (paren)
            appendable.append(')');

        Expression on = node.getOnCond();
        List<String> using = node.getUsing();
        if (on != null) {
            appendable.append(" ON ");
            on.accept(this);
        } else if (using != null) {
            appendable.append(" USING (");
            boolean isFst = true;
            for (String col : using) {
                if (isFst)
                    isFst = false;
                else
                    appendable.append(", ");
                appendable.append(col);
            }
            appendable.append(")");
        } else {
            throw new IllegalArgumentException("either ON or USING must be included for OUTER JOIN");
        }
    }

    @Override
    public void visit(SubqueryFactor node) {
        appendable.append('(');
        QueryExpression query = node.getSubquery();
        query.accept(this);
        appendable.append(") AS ").append(node.getAlias());
    }

    @Override
    public void visit(TableRefFactor node) {
        Identifier table = node.getTable();
        table.accept(this);
        String alias = node.getAlias();
        if (alias != null) {
            appendable.append(" AS ").append(alias);
        }
        List<IndexHint> list = node.getHintList();
        if (list != null && !list.isEmpty()) {
            appendable.append(' ');
            printList(list, " ");
        }
    }

    @Override
    public void visit(Dual dual) {
        appendable.append("DUAL");
    }

    @Override
    public void visit(GroupBy node) {
        appendable.append("GROUP BY ");
        boolean isFst = true;
        for (Pair<Expression, SortOrder> p : node.getOrderByList()) {
            if (isFst)
                isFst = false;
            else
                appendable.append(", ");
            Expression col = p.getKey();
            col.accept(this);
            switch (p.getValue()) {
            case DESC:
                appendable.append(" DESC");
                break;
            }
        }
        if (node.isWithRollup()) {
            appendable.append(" WITH ROLLUP");
        }
    }

    @Override
    public void visit(OrderBy node) {
        appendable.append("ORDER BY ");
        boolean isFst = true;
        for (Pair<Expression, SortOrder> p : node.getOrderByList()) {
            if (isFst)
                isFst = false;
            else
                appendable.append(", ");
            Expression col = p.getKey();
            col.accept(this);
            switch (p.getValue()) {
            case DESC:
                appendable.append(" DESC");
                break;
            }
        }
    }

    @Override
    public void visit(Limit node) {
        appendable.append("LIMIT ");
        Object offset = node.getOffset();
        if (offset instanceof ParamMarker) {
            ((ParamMarker) offset).accept(this);
        } else {
            appendable.append(String.valueOf(offset));
        }
        appendable.append(", ");
        Object size = node.getSize();
        if (size instanceof ParamMarker) {
            ((ParamMarker) size).accept(this);
        } else {
            appendable.append(String.valueOf(size));
        }
    }

    @Override
    public void visit(ColumnDefinition node) {
        throw new UnsupportedOperationException("col_def in CREATE TABLE is partially parsed");
    }

    @Override
    public void visit(IndexOption node) {
        if (node.getKeyBlockSize() != null) {
            appendable.append("KEY_BLOCK_SIZE = ");
            node.getKeyBlockSize().accept(this);
        } else if (node.getIndexType() != null) {
            appendable.append("USING ");
            switch (node.getIndexType()) {// USING {BTREE | HASH}
            case BTREE:
                appendable.append("BTREE");
                break;
            case HASH:
                appendable.append("HASH");
                break;
            }
        } else if (node.getParserName() != null) {
            appendable.append("WITH PARSER ");
            node.getParserName().accept(this);
        } else if (node.getComment() != null) {
            appendable.append("COMMENT ");
            node.getComment().accept(this);
        }
    }

    @Override
    public void visit(IndexColumnName node) {
        // QS_TODO
    }

    @Override
    public void visit(TableOptions node) {
        // QS_TODO

    }

    @Override
    public void visit(AlterSpecification node) {
        throw new UnsupportedOperationException("subclass have not implement visit");
    }

    @Override
    public void visit(DataType node) {
        throw new UnsupportedOperationException("subclass have not implement visit");
    }

    private void printSimpleShowStmt(String attName) {
        appendable.append("SHOW ").append(attName);
    }

    @Override
    public void visit(ShowAuthors node) {
        printSimpleShowStmt("AUTHORS");
    }

    @Override
    public void visit(ShowBinaryLog node) {
        printSimpleShowStmt("BINARY LOGS");
    }

    @Override
    public void visit(ShowBinLogEvent node) {
        appendable.append("SHOW BINLOG EVENTS");
        String logName = node.getLogName();
        if (logName != null)
            appendable.append(" IN ").append(logName);
        Expression pos = node.getPos();
        if (pos != null) {
            appendable.append(" FROM ");
            pos.accept(this);
        }
        Limit limit = node.getLimit();
        if (limit != null) {
            appendable.append(' ');
            limit.accept(this);
        }
    }

    /**
     * ' ' will be prepended
     */
    private void printLikeOrWhere(String like, Expression where) {
        if (like != null) {
            appendable.append(" LIKE ").append(like);
        } else if (where != null) {
            appendable.append(" WHERE ");
            where.accept(this);
        }
    }

    @Override
    public void visit(ShowCharaterSet node) {
        appendable.append("SHOW CHARACTER SET");
        printLikeOrWhere(node.getPattern(), node.getWhere());
    }

    @Override
    public void visit(ShowCollation node) {
        appendable.append("SHOW COLLATION");
        printLikeOrWhere(node.getPattern(), node.getWhere());
    }

    @Override
    public void visit(ShowColumns node) {
        appendable.append("SHOW ");
        if (node.isFull())
            appendable.append("FULL ");
        appendable.append("COLUMNS FROM ");
        node.getTable().accept(this);
        printLikeOrWhere(node.getPattern(), node.getWhere());
    }

    @Override
    public void visit(ShowContributors node) {
        printSimpleShowStmt("CONTRIBUTORS");
    }

    @Override
    public void visit(ShowCreate node) {
        appendable.append("SHOW CREATE ").append(node.getType().name()).append(' ');
        node.getId().accept(this);
    }

    @Override
    public void visit(ShowDatabases node) {
        appendable.append("SHOW DATABASES");
        printLikeOrWhere(node.getPattern(), node.getWhere());
    }

    @Override
    public void visit(ShowEngine node) {
        appendable.append("SHOW ENGINE ");
        switch (node.getType()) {
        case INNODB_MUTEX:
            appendable.append("INNODB MUTEX");
            break;
        case INNODB_STATUS:
            appendable.append("INNODB STATUS");
            break;
        case PERFORMANCE_SCHEMA_STATUS:
            appendable.append("PERFORMANCE SCHEMA STATUS");
            break;
        default:
            throw new IllegalArgumentException("unrecognized type for SHOW ENGINE: " + node.getType());
        }
    }

    @Override
    public void visit(ShowEngines node) {
        printSimpleShowStmt("ENGINES");
    }

    @Override
    public void visit(ShowErrors node) {
        appendable.append("SHOW ");
        if (node.isCount()) {
            appendable.append("COUNT(*) ERRORS");
        } else {
            appendable.append("ERRORS");
            Limit limit = node.getLimit();
            if (node.getLimit() != null) {
                appendable.append(' ');
                limit.accept(this);
            }
        }
    }

    @Override
    public void visit(ShowEvents node) {
        appendable.append("SHOW EVENTS");
        Identifier schema = node.getSchema();
        if (schema != null) {
            appendable.append(" FROM ");
            schema.accept(this);
        }
        printLikeOrWhere(node.getPattern(), node.getWhere());
    }

    @Override
    public void visit(ShowFunctionCode node) {
        appendable.append("SHOW FUNCTION CODE ");
        node.getFunctionName().accept(this);
    }

    @Override
    public void visit(ShowFunctionStatus node) {
        appendable.append("SHOW FUNCTION STATUS");
        printLikeOrWhere(node.getPattern(), node.getWhere());
    }

    @Override
    public void visit(ShowGrants node) {
        appendable.append("SHOW GRANTS");
        Expression user = node.getUser();
        if (user != null) {
            appendable.append(" FOR ");
            user.accept(this);
        }
    }

    @Override
    public void visit(ShowIndex node) {
        appendable.append("SHOW ");
        switch (node.getType()) {
        case INDEX:
            appendable.append("INDEX ");
            break;
        case INDEXES:
            appendable.append("INDEXES ");
            break;
        case KEYS:
            appendable.append("KEYS ");
            break;
        default:
            throw new IllegalArgumentException("unrecognized type for SHOW INDEX: " + node.getType());
        }
        appendable.append("IN ");
        node.getTable().accept(this);
    }

    @Override
    public void visit(ShowMasterStatus node) {
        printSimpleShowStmt("MASTER STATUS");
    }

    @Override
    public void visit(ShowOpenTables node) {
        appendable.append("SHOW OPEN TABLES");
        Identifier db = node.getSchema();
        if (db != null) {
            appendable.append(" FROM ");
            db.accept(this);
        }
        printLikeOrWhere(node.getPattern(), node.getWhere());
    }

    @Override
    public void visit(ShowPlugins node) {
        printSimpleShowStmt("PLUGINS");
    }

    @Override
    public void visit(ShowPrivileges node) {
        printSimpleShowStmt("PRIVILEGES");
    }

    @Override
    public void visit(ShowProcedureCode node) {
        appendable.append("SHOW PROCEDURE CODE ");
        node.getProcedureName().accept(this);
    }

    @Override
    public void visit(ShowProcedureStatus node) {
        appendable.append("SHOW PROCEDURE STATUS");
        printLikeOrWhere(node.getPattern(), node.getWhere());
    }

    @Override
    public void visit(ShowProcesslist node) {
        appendable.append("SHOW ");
        if (node.isFull())
            appendable.append("FULL ");
        appendable.append("PROCESSLIST");
    }

    @Override
    public void visit(ShowProfile node) {
        appendable.append("SHOW PROFILE");
        List<ShowProfile.Type> types = node.getTypes();
        boolean isFst = true;
        for (ShowProfile.Type type : types) {
            if (isFst) {
                isFst = false;
                appendable.append(' ');
            } else {
                appendable.append(", ");
            }
            appendable.append(type.name().replace('_', ' '));
        }
        Expression query = node.getForQuery();
        if (query != null) {
            appendable.append(" FOR QUERY ");
            query.accept(this);
        }
        Limit limit = node.getLimit();
        if (limit != null) {
            appendable.append(' ');
            limit.accept(this);
        }
    }

    @Override
    public void visit(ShowProfiles node) {
        printSimpleShowStmt("PROFILES");
    }

    @Override
    public void visit(ShowSlaveHosts node) {
        printSimpleShowStmt("SLAVE HOSTS");
    }

    @Override
    public void visit(ShowSlaveStatus node) {
        printSimpleShowStmt("SLAVE STATUS");
    }

    @Override
    public void visit(ShowStatus node) {
        appendable.append("SHOW ").append(node.getScope().name().replace('_', ' ')).append(" STATUS");
        printLikeOrWhere(node.getPattern(), node.getWhere());
    }

    @Override
    public void visit(ShowTables node) {
        appendable.append("SHOW");
        if (node.isFull())
            appendable.append(" FULL");
        appendable.append(" TABLES");
        Identifier schema = node.getSchema();
        if (schema != null) {
            appendable.append(" FROM ");
            schema.accept(this);
        }
        printLikeOrWhere(node.getPattern(), node.getWhere());
    }

    @Override
    public void visit(ShowTableStatus node) {
        appendable.append("SHOW TABLE STATUS");
        Identifier schema = node.getDatabase();
        if (schema != null) {
            appendable.append(" FROM ");
            schema.accept(this);
        }
        printLikeOrWhere(node.getPattern(), node.getWhere());
    }

    @Override
    public void visit(ShowTriggers node) {
        appendable.append("SHOW TRIGGERS");
        Identifier schema = node.getSchema();
        if (schema != null) {
            appendable.append(" FROM ");
            schema.accept(this);
        }
        printLikeOrWhere(node.getPattern(), node.getWhere());
    }

    @Override
    public void visit(ShowVariables node) {
        appendable.append("SHOW ").append(node.getScope().name().replace('_', ' ')).append(" VARIABLES");
        printLikeOrWhere(node.getPattern(), node.getWhere());
    }

    @Override
    public void visit(ShowWarnings node) {
        appendable.append("SHOW ");
        if (node.isCount()) {
            appendable.append("COUNT(*) WARNINGS");
        } else {
            appendable.append("WARNINGS");
            Limit limit = node.getLimit();
            if (limit != null) {
                appendable.append(' ');
                limit.accept(this);
            }
        }
    }

    @Override
    public void visit(DescTableStatement node) {
        appendable.append("DESC ");
        node.getTable().accept(this);
    }

    @Override
    public void visit(DALSetStatement node) {
        appendable.append("SET ");
        boolean isFst = true;
        for (Pair<VariableExpression, Expression> p : node.getAssignmentList()) {
            if (isFst)
                isFst = false;
            else
                appendable.append(", ");
            p.getKey().accept(this);
            appendable.append(" = ");
            p.getValue().accept(this);
        }
    }

    @Override
    public void visit(DALSetNamesStatement node) {
        appendable.append("SET NAMES ");
        if (node.isDefault()) {
            appendable.append("DEFAULT");
        } else {
            appendable.append(node.getCharsetName());
            String collate = node.getCollationName();
            if (collate != null) {
                appendable.append(" COLLATE ");
                appendable.append(collate);
            }
        }
    }

    @Override
    public void visit(DALSetCharacterSetStatement node) {
        appendable.append("SET CHARACTER SET ");
        if (node.isDefault()) {
            appendable.append("DEFAULT");
        } else {
            appendable.append(node.getCharset());
        }
    }

    @Override
    public void visit(MTSSetTransactionStatement node) {
        appendable.append("SET ");
        VariableScope scope = node.getScope();
        if (scope != null) {
            switch (scope) {
            case SESSION:
                appendable.append("SESSION ");
                break;
            case GLOBAL:
                appendable.append("GLOBAL ");
                break;
            default:
                throw new IllegalArgumentException("unknown scope for SET TRANSACTION ISOLATION LEVEL: " + scope);
            }
        }
        appendable.append("TRANSACTION ISOLATION LEVEL ");
        switch (node.getLevel()) {
        case READ_COMMITTED:
            appendable.append("READ COMMITTED");
            break;
        case READ_UNCOMMITTED:
            appendable.append("READ UNCOMMITTED");
            break;
        case REPEATABLE_READ:
            appendable.append("REPEATABLE READ");
            break;
        case SERIALIZABLE:
            appendable.append("SERIALIZABLE");
            break;
        default:
            throw new IllegalArgumentException("unknown level for SET TRANSACTION ISOLATION LEVEL: " + node.getLevel());
        }
    }

    @Override
    public void visit(MTSSavepointStatement node) {
        appendable.append("SAVEPOINT ");
        node.getSavepoint().accept(this);
    }

    @Override
    public void visit(MTSReleaseStatement node) {
        appendable.append("RELEASE SAVEPOINT ");
        node.getSavepoint().accept(this);
    }

    @Override
    public void visit(MTSRollbackStatement node) {
        appendable.append("ROLLBACK");
        Identifier savepoint = node.getSavepoint();
        if (savepoint == null) {
            MTSRollbackStatement.CompleteType type = node.getCompleteType();
            switch (type) {
            case CHAIN:
                appendable.append(" AND CHAIN");
                break;
            case NO_CHAIN:
                appendable.append(" AND NO CHAIN");
                break;
            case NO_RELEASE:
                appendable.append(" NO RELEASE");
                break;
            case RELEASE:
                appendable.append(" RELEASE");
                break;
            case UN_DEF:
                break;
            default:
                throw new IllegalArgumentException("unrecgnized complete type: " + type);
            }
        } else {
            appendable.append(" TO SAVEPOINT ");
            savepoint.accept(this);
        }
    }

    @Override
    public void visit(DMLCallStatement node) {
        appendable.append("CALL ");
        node.getProcedure().accept(this);
        appendable.append('(');
        printList(node.getArguments());
        appendable.append(')');
    }

    @Override
    public void visit(DMLDeleteStatement node) {
        appendable.append("DELETE ");
        if (node.isLowPriority())
            appendable.append("LOW_PRIORITY ");
        if (node.isQuick())
            appendable.append("QUICK ");
        if (node.isIgnore())
            appendable.append("IGNORE ");
        TableReferences tableRefs = node.getTableRefs();
        if (tableRefs == null) {
            appendable.append("FROM ");
            node.getTableNames().get(0).accept(this);
        } else {
            printList(node.getTableNames());
            appendable.append(" FROM ");
            node.getTableRefs().accept(this);
        }
        Expression where = node.getWhereCondition();
        if (where != null) {
            appendable.append(" WHERE ");
            where.accept(this);
        }
        OrderBy orderBy = node.getOrderBy();
        if (orderBy != null) {
            appendable.append(' ');
            orderBy.accept(this);
        }
        Limit limit = node.getLimit();
        if (limit != null) {
            appendable.append(' ');
            limit.accept(this);
        }
    }

    @Override
    public void visit(DMLInsertStatement node) {
        appendable.append("INSERT ");
        switch (node.getMode()) {
        case DELAY:
            appendable.append("DELAYED ");
            break;
        case HIGH:
            appendable.append("HIGH_PRIORITY ");
            break;
        case LOW:
            appendable.append("LOW_PRIORITY ");
            break;
        case UNDEF:
            break;
        default:
            throw new IllegalArgumentException("unknown mode for INSERT: " + node.getMode());
        }
        if (node.isIgnore())
            appendable.append("IGNORE ");
        appendable.append("INTO ");
        node.getTable().accept(this);
        appendable.append(' ');

        List<Identifier> cols = node.getColumnNameList();
        if (cols != null && !cols.isEmpty()) {
            appendable.append('(');
            printList(cols);
            appendable.append(") ");
        }

        QueryExpression select = node.getSelect();
        if (select == null) {
            appendable.append("VALUES ");
            List<RowExpression> rows = node.getRowList();
            if (rows != null && !rows.isEmpty()) {
                boolean isFst = true;
                for (RowExpression row : rows) {
                    if (row == null || row.getRowExprList().isEmpty())
                        continue;
                    if (isFst)
                        isFst = false;
                    else
                        appendable.append(", ");
                    appendable.append('(');
                    printList(row.getRowExprList());
                    appendable.append(')');
                }
            } else {
                throw new IllegalArgumentException("at least one row for INSERT");
            }
        } else {
            select.accept(this);
        }

        List<Pair<Identifier, Expression>> dup = node.getDuplicateUpdate();
        if (dup != null && !dup.isEmpty()) {
            appendable.append(" ON DUPLICATE KEY UPDATE ");
            boolean isFst = true;
            for (Pair<Identifier, Expression> p : dup) {
                if (isFst)
                    isFst = false;
                else
                    appendable.append(", ");
                p.getKey().accept(this);
                appendable.append(" = ");
                p.getValue().accept(this);
            }
        }
    }

    @Override
    public void visit(DMLReplaceStatement node) {
        appendable.append("REPLACE ");
        switch (node.getMode()) {
        case DELAY:
            appendable.append("DELAYED ");
            break;
        case LOW:
            appendable.append("LOW_PRIORITY ");
            break;
        case UNDEF:
            break;
        default:
            throw new IllegalArgumentException("unknown mode for INSERT: " + node.getMode());
        }
        appendable.append("INTO ");
        node.getTable().accept(this);
        appendable.append(' ');

        List<Identifier> cols = node.getColumnNameList();
        if (cols != null && !cols.isEmpty()) {
            appendable.append('(');
            printList(cols);
            appendable.append(") ");
        }

        QueryExpression select = node.getSelect();
        if (select == null) {
            appendable.append("VALUES ");
            List<RowExpression> rows = node.getRowList();
            if (rows != null && !rows.isEmpty()) {
                boolean isFst = true;
                for (RowExpression row : rows) {
                    if (row == null || row.getRowExprList().isEmpty())
                        continue;
                    if (isFst)
                        isFst = false;
                    else
                        appendable.append(", ");
                    appendable.append('(');
                    printList(row.getRowExprList());
                    appendable.append(')');
                }
            } else {
                throw new IllegalArgumentException("at least one row for REPLACE");
            }
        } else {
            select.accept(this);
        }
    }

    @Override
    public void visit(DMLSelectStatement node) {
        appendable.append("SELECT ");
        final DMLSelectStatement.SelectOption option = node.getOption();
        switch (option.resultDup) {
        case ALL:
            break;
        case DISTINCT:
            appendable.append("DISTINCT ");
            break;
        case DISTINCTROW:
            appendable.append("DISTINCTROW ");
            break;
        default:
            throw new IllegalArgumentException("unknown option for SELECT: " + option);
        }
        if (option.highPriority) {
            appendable.append("HIGH_PRIORITY ");
        }
        if (option.straightJoin) {
            appendable.append("STRAIGHT_JOIN ");
        }
        switch (option.resultSize) {
        case SQL_BIG_RESULT:
            appendable.append("SQL_BIG_RESULT ");
            break;
        case SQL_SMALL_RESULT:
            appendable.append("SQL_SMALL_RESULT ");
            break;
        case UNDEF:
            break;
        default:
            throw new IllegalArgumentException("unknown option for SELECT: " + option);
        }
        if (option.sqlBufferResult) {
            appendable.append("SQL_BUFFER_RESULT ");
        }
        switch (option.queryCache) {
        case SQL_CACHE:
            appendable.append("SQL_CACHE ");
            break;
        case SQL_NO_CACHE:
            appendable.append("SQL_NO_CACHE ");
            break;
        case UNDEF:
            break;
        default:
            throw new IllegalArgumentException("unknown option for SELECT: " + option);
        }
        if (option.sqlCalcFoundRows) {
            appendable.append("SQL_CALC_FOUND_ROWS ");
        }

        boolean isFst = true;
        List<Pair<Expression, String>> exprList = node.getSelectExprList();
        for (Pair<Expression, String> p : exprList) {
            if (isFst)
                isFst = false;
            else
                appendable.append(", ");
            p.getKey().accept(this);
            String alias = p.getValue();
            if (alias != null) {
                appendable.append(" AS ").append(alias);
            }
        }

        TableReferences from = node.getTables();
        if (from != null) {
            appendable.append(" FROM ");
            from.accept(this);
        }

        Expression where = node.getWhere();
        if (where != null) {
            appendable.append(" WHERE ");
            where.accept(this);
        }

        GroupBy group = node.getGroup();
        if (group != null) {
            appendable.append(' ');
            group.accept(this);
        }

        Expression having = node.getHaving();
        if (having != null) {
            appendable.append(" HAVING ");
            having.accept(this);
        }

        OrderBy order = node.getOrder();
        if (order != null) {
            appendable.append(' ');
            order.accept(this);
        }

        Limit limit = node.getLimit();
        if (limit != null) {
            appendable.append(' ');
            limit.accept(this);
        }

        switch (option.lockMode) {
        case FOR_UPDATE:
            appendable.append(" FOR UPDATE");
            break;
        case LOCK_IN_SHARE_MODE:
            appendable.append(" LOCK IN SHARE MODE");
            break;
        case UNDEF:
            break;
        default:
            throw new IllegalArgumentException("unknown option for SELECT: " + option);
        }
    }

    @Override
    public void visit(DMLSelectUnionStatement node) {
        List<DMLSelectStatement> list = node.getSelectStmtList();
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("SELECT UNION must have at least one SELECT");
        }
        final int fstDist = node.getFirstDistinctIndex();
        int i = 0;
        for (DMLSelectStatement select : list) {
            if (i > 0) {
                appendable.append(" UNION ");
                if (i > fstDist) {
                    appendable.append("ALL ");
                }
            }
            appendable.append('(');
            select.accept(this);
            appendable.append(')');
            ++i;
        }
        OrderBy order = node.getOrderBy();
        if (order != null) {
            appendable.append(' ');
            order.accept(this);
        }
        Limit limit = node.getLimit();
        if (limit != null) {
            appendable.append(' ');
            limit.accept(this);
        }
    }

    @Override
    public void visit(DMLUpdateStatement node) {
        appendable.append("UPDATE ");
        if (node.isLowPriority()) {
            appendable.append("LOW_PRIORITY ");
        }
        if (node.isIgnore()) {
            appendable.append("IGNORE ");
        }
        node.getTableRefs().accept(this);
        appendable.append(" SET ");
        boolean isFst = true;
        for (Pair<Identifier, Expression> p : node.getValues()) {
            if (isFst)
                isFst = false;
            else
                appendable.append(", ");
            p.getKey().accept(this);
            appendable.append(" = ");
            p.getValue().accept(this);
        }
        Expression where = node.getWhere();
        if (where != null) {
            appendable.append(" WHERE ");
            where.accept(this);
        }
        OrderBy order = node.getOrderBy();
        if (order != null) {
            appendable.append(' ');
            order.accept(this);
        }
        Limit limit = node.getLimit();
        if (limit != null) {
            appendable.append(' ');
            limit.accept(this);
        }
    }

    @Override
    public void visit(DDLTruncateStatement node) {
        appendable.append("TRUNCATE TABLE ");
        node.getTable().accept(this);
    }

    @Override
    public void visit(DDLAlterTableStatement node) {
        throw new UnsupportedOperationException("ALTER TABLE is partially parsed");
    }

    @Override
    public void visit(DDLCreateIndexStatement node) {
        throw new UnsupportedOperationException("CREATE INDEX is partially parsed");
    }

    @Override
    public void visit(DDLCreateTableStatement node) {
        throw new UnsupportedOperationException("CREATE TABLE is partially parsed");
    }

    @Override
    public void visit(DDLRenameTableStatement node) {
        appendable.append("RENAME TABLE ");
        boolean isFst = true;
        for (Pair<Identifier, Identifier> p : node.getList()) {
            if (isFst)
                isFst = false;
            else
                appendable.append(", ");
            p.getKey().accept(this);
            appendable.append(" TO ");
            p.getValue().accept(this);
        }
    }

    @Override
    public void visit(DDLDropIndexStatement node) {
        appendable.append("DROP INDEX ");
        node.getIndexName().accept(this);
        appendable.append(" ON ");
        node.getTable().accept(this);
    }

    @Override
    public void visit(DDLDropTableStatement node) {
        appendable.append("DROP ");
        if (node.isTemp()) {
            appendable.append("TEMPORARY ");
        }
        appendable.append("TABLE ");
        if (node.isIfExists()) {
            appendable.append("IF EXISTS ");
        }
        printList(node.getTableNames());
        switch (node.getMode()) {
        case CASCADE:
            appendable.append(" CASCADE");
            break;
        case RESTRICT:
            appendable.append(" RESTRICT");
            break;
        case UNDEF:
            break;
        default:
            throw new IllegalArgumentException("unsupported mode for DROP TABLE: " + node.getMode());
        }
    }

    @Override
    public void visit(ExtDDLCreatePolicy node) {
        appendable.append("CREATE POLICY ");
        node.getName().accept(this);
        appendable.append(" (");
        boolean first = true;
        for (Pair<Integer, Expression> p : node.getProportion()) {
            if (first)
                first = false;
            else
                appendable.append(", ");
            appendable.append(p.getKey()).append(' ');
            p.getValue().accept(this);
        }
        appendable.append(')');
    }

    @Override
    public void visit(ExtDDLDropPolicy node) {
        appendable.append("DROP POLICY ");
        node.getPolicyName().accept(this);
    }

}
