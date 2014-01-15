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

import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.IDENTIFIER;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_CHARACTER;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_COLLATE;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_DEFAULT;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_DESC;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_DESCRIBE;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_FOR;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_FROM;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_IN;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_LIKE;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_LIMIT;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_OPTION;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_READ;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_SET;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_SHOW;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_WHERE;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.OP_ASSIGN;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.OP_ASTERISK;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.OP_EQUALS;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.PUNC_COMMA;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.PUNC_LEFT_PAREN;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.PUNC_RIGHT_PAREN;

import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.primary.Identifier;
import com.alibaba.cobar.parser.ast.expression.primary.SysVarPrimary;
import com.alibaba.cobar.parser.ast.expression.primary.UsrDefVarPrimary;
import com.alibaba.cobar.parser.ast.expression.primary.VariableExpression;
import com.alibaba.cobar.parser.ast.expression.primary.literal.LiteralString;
import com.alibaba.cobar.parser.ast.fragment.Limit;
import com.alibaba.cobar.parser.ast.fragment.VariableScope;
import com.alibaba.cobar.parser.ast.stmt.SQLStatement;
import com.alibaba.cobar.parser.ast.stmt.dal.DALSetCharacterSetStatement;
import com.alibaba.cobar.parser.ast.stmt.dal.DALSetNamesStatement;
import com.alibaba.cobar.parser.ast.stmt.dal.DALSetStatement;
import com.alibaba.cobar.parser.ast.stmt.dal.DALShowStatement;
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
import com.alibaba.cobar.parser.ast.stmt.ddl.DescTableStatement;
import com.alibaba.cobar.parser.ast.stmt.mts.MTSSetTransactionStatement;
import com.alibaba.cobar.parser.recognizer.mysql.lexer.MySQLLexer;
import com.alibaba.cobar.parser.util.Pair;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class MySQLDALParser extends MySQLParser {
    protected MySQLExprParser exprParser;

    public MySQLDALParser(MySQLLexer lexer, MySQLExprParser exprParser) {
        super(lexer);
        this.exprParser = exprParser;
    }

    private static enum SpecialIdentifier {
        AUTHORS,
        BINLOG,
        BLOCK,
        CODE,
        COLLATION,
        COLUMNS,
        COMMITTED,
        CONTEXT,
        CONTRIBUTORS,
        COUNT,
        CPU,
        ENGINE,
        ENGINES,
        ERRORS,
        EVENT,
        EVENTS,
        FULL,
        FUNCTION,
        GLOBAL,
        GRANTS,
        HOSTS,
        INDEXES,
        INNODB,
        IPC,
        LOCAL,
        MASTER,
        MEMORY,
        MUTEX,
        NAMES,
        OPEN,
        PAGE,
        PERFORMANCE_SCHEMA,
        PLUGINS,
        PRIVILEGES,
        PROCESSLIST,
        PROFILE,
        PROFILES,
        REPEATABLE,
        SERIALIZABLE,
        SESSION,
        SLAVE,
        SOURCE,
        STATUS,
        STORAGE,
        SWAPS,
        TABLES,
        TRANSACTION,
        TRIGGERS,
        UNCOMMITTED,
        VARIABLES,
        VIEW,
        WARNINGS
    }

    private static final Map<String, SpecialIdentifier> specialIdentifiers = new HashMap<String, SpecialIdentifier>();
    static {
        specialIdentifiers.put("AUTHORS", SpecialIdentifier.AUTHORS);
        specialIdentifiers.put("BINLOG", SpecialIdentifier.BINLOG);
        specialIdentifiers.put("COLLATION", SpecialIdentifier.COLLATION);
        specialIdentifiers.put("COLUMNS", SpecialIdentifier.COLUMNS);
        specialIdentifiers.put("CONTRIBUTORS", SpecialIdentifier.CONTRIBUTORS);
        specialIdentifiers.put("EVENT", SpecialIdentifier.EVENT);
        specialIdentifiers.put("FUNCTION", SpecialIdentifier.FUNCTION);
        specialIdentifiers.put("VIEW", SpecialIdentifier.VIEW);
        specialIdentifiers.put("ENGINE", SpecialIdentifier.ENGINE);
        specialIdentifiers.put("ENGINES", SpecialIdentifier.ENGINES);
        specialIdentifiers.put("ERRORS", SpecialIdentifier.ERRORS);
        specialIdentifiers.put("EVENTS", SpecialIdentifier.EVENTS);
        specialIdentifiers.put("FULL", SpecialIdentifier.FULL);
        specialIdentifiers.put("GLOBAL", SpecialIdentifier.GLOBAL);
        specialIdentifiers.put("GRANTS", SpecialIdentifier.GRANTS);
        specialIdentifiers.put("MASTER", SpecialIdentifier.MASTER);
        specialIdentifiers.put("OPEN", SpecialIdentifier.OPEN);
        specialIdentifiers.put("PLUGINS", SpecialIdentifier.PLUGINS);
        specialIdentifiers.put("CODE", SpecialIdentifier.CODE);
        specialIdentifiers.put("STATUS", SpecialIdentifier.STATUS);
        specialIdentifiers.put("PRIVILEGES", SpecialIdentifier.PRIVILEGES);
        specialIdentifiers.put("PROCESSLIST", SpecialIdentifier.PROCESSLIST);
        specialIdentifiers.put("PROFILE", SpecialIdentifier.PROFILE);
        specialIdentifiers.put("PROFILES", SpecialIdentifier.PROFILES);
        specialIdentifiers.put("SESSION", SpecialIdentifier.SESSION);
        specialIdentifiers.put("SLAVE", SpecialIdentifier.SLAVE);
        specialIdentifiers.put("STORAGE", SpecialIdentifier.STORAGE);
        specialIdentifiers.put("TABLES", SpecialIdentifier.TABLES);
        specialIdentifiers.put("TRIGGERS", SpecialIdentifier.TRIGGERS);
        specialIdentifiers.put("VARIABLES", SpecialIdentifier.VARIABLES);
        specialIdentifiers.put("WARNINGS", SpecialIdentifier.WARNINGS);
        specialIdentifiers.put("INNODB", SpecialIdentifier.INNODB);
        specialIdentifiers.put("PERFORMANCE_SCHEMA", SpecialIdentifier.PERFORMANCE_SCHEMA);
        specialIdentifiers.put("MUTEX", SpecialIdentifier.MUTEX);
        specialIdentifiers.put("COUNT", SpecialIdentifier.COUNT);
        specialIdentifiers.put("BLOCK", SpecialIdentifier.BLOCK);
        specialIdentifiers.put("CONTEXT", SpecialIdentifier.CONTEXT);
        specialIdentifiers.put("CPU", SpecialIdentifier.CPU);
        specialIdentifiers.put("MEMORY", SpecialIdentifier.MEMORY);
        specialIdentifiers.put("PAGE", SpecialIdentifier.PAGE);
        specialIdentifiers.put("SOURCE", SpecialIdentifier.SOURCE);
        specialIdentifiers.put("SWAPS", SpecialIdentifier.SWAPS);
        specialIdentifiers.put("IPC", SpecialIdentifier.IPC);
        specialIdentifiers.put("LOCAL", SpecialIdentifier.LOCAL);
        specialIdentifiers.put("HOSTS", SpecialIdentifier.HOSTS);
        specialIdentifiers.put("INDEXES", SpecialIdentifier.INDEXES);
        specialIdentifiers.put("TRANSACTION", SpecialIdentifier.TRANSACTION);
        specialIdentifiers.put("UNCOMMITTED", SpecialIdentifier.UNCOMMITTED);
        specialIdentifiers.put("COMMITTED", SpecialIdentifier.COMMITTED);
        specialIdentifiers.put("REPEATABLE", SpecialIdentifier.REPEATABLE);
        specialIdentifiers.put("SERIALIZABLE", SpecialIdentifier.SERIALIZABLE);
        specialIdentifiers.put("NAMES", SpecialIdentifier.NAMES);
    }

    public DescTableStatement desc() throws SQLSyntaxErrorException {
        match(KW_DESC, KW_DESCRIBE);
        Identifier table = identifier();
        return new DescTableStatement(table);
    }

    public DALShowStatement show() throws SQLSyntaxErrorException {
        match(KW_SHOW);
        String tempStr;
        String tempStrUp;
        Expression tempExpr;
        Identifier tempId;
        SpecialIdentifier tempSi;
        Limit tempLimit;
        switch (lexer.token()) {
        case KW_BINARY:
            lexer.nextToken();
            matchIdentifier("LOGS");
            return new ShowBinaryLog();
        case KW_CHARACTER:
            lexer.nextToken();
            match(KW_SET);
            switch (lexer.token()) {
            case KW_LIKE:
                tempStr = like();
                return new ShowCharaterSet(tempStr);
            case KW_WHERE:
                tempExpr = where();
                return new ShowCharaterSet(tempExpr);
            default:
                return new ShowCharaterSet();
            }
        case KW_CREATE:
            ShowCreate.Type showCreateType;
            switch1: switch (lexer.nextToken()) {
            case KW_DATABASE:
                showCreateType = ShowCreate.Type.DATABASE;
                break;
            case KW_PROCEDURE:
                showCreateType = ShowCreate.Type.PROCEDURE;
                break;
            case KW_TABLE:
                showCreateType = ShowCreate.Type.TABLE;
                break;
            case KW_TRIGGER:
                showCreateType = ShowCreate.Type.TRIGGER;
                break;
            case IDENTIFIER:
                tempSi = specialIdentifiers.get(lexer.stringValueUppercase());
                if (tempSi != null) {
                    switch (tempSi) {
                    case EVENT:
                        showCreateType = ShowCreate.Type.EVENT;
                        break switch1;
                    case FUNCTION:
                        showCreateType = ShowCreate.Type.FUNCTION;
                        break switch1;
                    case VIEW:
                        showCreateType = ShowCreate.Type.VIEW;
                        break switch1;
                    }
                }
            default:
                throw err("unexpect token for SHOW CREATE");
            }
            lexer.nextToken();
            tempId = identifier();
            return new ShowCreate(showCreateType, tempId);
        case KW_SCHEMAS:
        case KW_DATABASES:
            lexer.nextToken();
            switch (lexer.token()) {
            case KW_LIKE:
                tempStr = like();
                return new ShowDatabases(tempStr);
            case KW_WHERE:
                tempExpr = where();
                return new ShowDatabases(tempExpr);
            }
            return new ShowDatabases();
        case KW_KEYS:
            return showIndex(ShowIndex.Type.KEYS);
        case KW_INDEX:
            return showIndex(ShowIndex.Type.INDEX);
        case KW_PROCEDURE:
            lexer.nextToken();
            tempStrUp = lexer.stringValueUppercase();
            tempSi = specialIdentifiers.get(tempStrUp);
            if (tempSi != null) {
                switch (tempSi) {
                case CODE:
                    lexer.nextToken();
                    tempId = identifier();
                    return new ShowProcedureCode(tempId);
                case STATUS:
                    switch (lexer.nextToken()) {
                    case KW_LIKE:
                        tempStr = like();
                        return new ShowProcedureStatus(tempStr);
                    case KW_WHERE:
                        tempExpr = where();
                        return new ShowProcedureStatus(tempExpr);
                    default:
                        return new ShowProcedureStatus();
                    }
                }
            }
            throw err("unexpect token for SHOW PROCEDURE");
        case KW_TABLE:
            lexer.nextToken();
            matchIdentifier("STATUS");
            tempId = null;
            if (lexer.token() == KW_FROM || lexer.token() == KW_IN) {
                lexer.nextToken();
                tempId = identifier();
            }
            switch (lexer.token()) {
            case KW_LIKE:
                tempStr = like();
                return new ShowTableStatus(tempId, tempStr);
            case KW_WHERE:
                tempExpr = where();
                return new ShowTableStatus(tempId, tempExpr);
            }
            return new ShowTableStatus(tempId);
        case IDENTIFIER:
            tempStrUp = lexer.stringValueUppercase();
            tempSi = specialIdentifiers.get(tempStrUp);
            if (tempSi == null) {
                break;
            }
            switch (tempSi) {
            case INDEXES:
                return showIndex(ShowIndex.Type.INDEXES);
            case GRANTS:
                if (lexer.nextToken() == KW_FOR) {
                    lexer.nextToken();
                    tempExpr = exprParser.expression();
                    return new ShowGrants(tempExpr);
                }
                return new ShowGrants();
            case AUTHORS:
                lexer.nextToken();
                return new ShowAuthors();
            case BINLOG:
                lexer.nextToken();
                matchIdentifier("EVENTS");
                tempStr = null;
                tempExpr = null;
                tempLimit = null;
                if (lexer.token() == KW_IN) {
                    lexer.nextToken();
                    tempStr = lexer.stringValue();
                    lexer.nextToken();
                }
                if (lexer.token() == KW_FROM) {
                    lexer.nextToken();
                    tempExpr = exprParser.expression();
                }
                if (lexer.token() == KW_LIMIT) {
                    tempLimit = limit();
                }
                return new ShowBinLogEvent(tempStr, tempExpr, tempLimit);
            case COLLATION:
                switch (lexer.nextToken()) {
                case KW_LIKE:
                    tempStr = like();
                    return new ShowCollation(tempStr);
                case KW_WHERE:
                    tempExpr = where();
                    return new ShowCollation(tempExpr);
                }
                return new ShowCollation();
            case COLUMNS:
                return showColumns(false);
            case CONTRIBUTORS:
                lexer.nextToken();
                return new ShowContributors();
            case ENGINE:
                switch (lexer.nextToken()) {
                case IDENTIFIER:
                    tempStrUp = lexer.stringValueUppercase();
                    tempSi = specialIdentifiers.get(tempStrUp);
                    if (tempSi != null) {
                        switch (tempSi) {
                        case INNODB:
                            lexer.nextToken();
                            tempStrUp = lexer.stringValueUppercase();
                            tempSi = specialIdentifiers.get(tempStrUp);
                            if (tempSi != null) {
                                switch (tempSi) {
                                case STATUS:
                                    lexer.nextToken();
                                    return new ShowEngine(ShowEngine.Type.INNODB_STATUS);
                                case MUTEX:
                                    lexer.nextToken();
                                    return new ShowEngine(ShowEngine.Type.INNODB_MUTEX);
                                }
                            }
                        case PERFORMANCE_SCHEMA:
                            lexer.nextToken();
                            matchIdentifier("STATUS");
                            return new ShowEngine(ShowEngine.Type.PERFORMANCE_SCHEMA_STATUS);
                        }
                    }
                default:
                    throw err("unexpect token for SHOW ENGINE");
                }
            case ENGINES:
                lexer.nextToken();
                return new ShowEngines();
            case ERRORS:
                lexer.nextToken();
                tempLimit = limit();
                return new ShowErrors(false, tempLimit);
            case COUNT:
                lexer.nextToken();
                match(PUNC_LEFT_PAREN);
                match(OP_ASTERISK);
                match(PUNC_RIGHT_PAREN);
                switch (matchIdentifier("ERRORS", "WARNINGS")) {
                case 0:
                    return new ShowErrors(true, null);
                case 1:
                    return new ShowWarnings(true, null);
                }
            case EVENTS:
                tempId = null;
                switch (lexer.nextToken()) {
                case KW_IN:
                case KW_FROM:
                    lexer.nextToken();
                    tempId = identifier();
                }
                switch (lexer.token()) {
                case KW_LIKE:
                    tempStr = like();
                    return new ShowEvents(tempId, tempStr);
                case KW_WHERE:
                    tempExpr = where();
                    return new ShowEvents(tempId, tempExpr);
                default:
                    return new ShowEvents(tempId);
                }
            case FULL:
                lexer.nextToken();
                tempStrUp = lexer.stringValueUppercase();
                tempSi = specialIdentifiers.get(tempStrUp);
                if (tempSi != null) {
                    switch (tempSi) {
                    case COLUMNS:
                        return showColumns(true);
                    case PROCESSLIST:
                        lexer.nextToken();
                        return new ShowProcesslist(true);
                    case TABLES:
                        tempId = null;
                        switch (lexer.nextToken()) {
                        case KW_IN:
                        case KW_FROM:
                            lexer.nextToken();
                            tempId = identifier();
                        }
                        switch (lexer.token()) {
                        case KW_LIKE:
                            tempStr = like();
                            return new ShowTables(true, tempId, tempStr);
                        case KW_WHERE:
                            tempExpr = where();
                            return new ShowTables(true, tempId, tempExpr);
                        default:
                            return new ShowTables(true, tempId);
                        }
                    }
                }
                throw err("unexpected token for SHOW FULL");
            case FUNCTION:
                lexer.nextToken();
                tempStrUp = lexer.stringValueUppercase();
                tempSi = specialIdentifiers.get(tempStrUp);
                if (tempSi != null) {
                    switch (tempSi) {
                    case CODE:
                        lexer.nextToken();
                        tempId = identifier();
                        return new ShowFunctionCode(tempId);
                    case STATUS:
                        switch (lexer.nextToken()) {
                        case KW_LIKE:
                            tempStr = like();
                            return new ShowFunctionStatus(tempStr);
                        case KW_WHERE:
                            tempExpr = where();
                            return new ShowFunctionStatus(tempExpr);
                        default:
                            return new ShowFunctionStatus();
                        }
                    }
                }
                throw err("unexpected token for SHOW FUNCTION");
            case GLOBAL:
                lexer.nextToken();
                tempStrUp = lexer.stringValueUppercase();
                tempSi = specialIdentifiers.get(tempStrUp);
                if (tempSi != null) {
                    switch (tempSi) {
                    case STATUS:
                        switch (lexer.nextToken()) {
                        case KW_LIKE:
                            tempStr = like();
                            return new ShowStatus(VariableScope.GLOBAL, tempStr);
                        case KW_WHERE:
                            tempExpr = where();
                            return new ShowStatus(VariableScope.GLOBAL, tempExpr);
                        default:
                            return new ShowStatus(VariableScope.GLOBAL);
                        }
                    case VARIABLES:
                        switch (lexer.nextToken()) {
                        case KW_LIKE:
                            tempStr = like();
                            return new ShowVariables(VariableScope.GLOBAL, tempStr);
                        case KW_WHERE:
                            tempExpr = where();
                            return new ShowVariables(VariableScope.GLOBAL, tempExpr);
                        default:
                            return new ShowVariables(VariableScope.GLOBAL);
                        }
                    }
                }
                throw err("unexpected token for SHOW GLOBAL");
            case MASTER:
                lexer.nextToken();
                tempStrUp = lexer.stringValueUppercase();
                tempSi = specialIdentifiers.get(tempStrUp);
                if (tempSi != null && tempSi == SpecialIdentifier.STATUS) {
                    lexer.nextToken();
                    return new ShowMasterStatus();
                }
                matchIdentifier("LOGS");
                return new ShowBinaryLog();
            case OPEN:
                lexer.nextToken();
                matchIdentifier("TABLES");
                tempId = null;
                switch (lexer.token()) {
                case KW_IN:
                case KW_FROM:
                    lexer.nextToken();
                    tempId = identifier();
                }
                switch (lexer.token()) {
                case KW_LIKE:
                    tempStr = like();
                    return new ShowOpenTables(tempId, tempStr);
                case KW_WHERE:
                    tempExpr = where();
                    return new ShowOpenTables(tempId, tempExpr);
                default:
                    return new ShowOpenTables(tempId);
                }
            case PLUGINS:
                lexer.nextToken();
                return new ShowPlugins();
            case PRIVILEGES:
                lexer.nextToken();
                return new ShowPrivileges();
            case PROCESSLIST:
                lexer.nextToken();
                return new ShowProcesslist(false);
            case PROFILE:
                return showProfile();
            case PROFILES:
                lexer.nextToken();
                return new ShowProfiles();
            case LOCAL:
            case SESSION:
                lexer.nextToken();
                tempStrUp = lexer.stringValueUppercase();
                tempSi = specialIdentifiers.get(tempStrUp);
                if (tempSi != null) {
                    switch (tempSi) {
                    case STATUS:
                        switch (lexer.nextToken()) {
                        case KW_LIKE:
                            tempStr = like();
                            return new ShowStatus(VariableScope.SESSION, tempStr);
                        case KW_WHERE:
                            tempExpr = where();
                            return new ShowStatus(VariableScope.SESSION, tempExpr);
                        default:
                            return new ShowStatus(VariableScope.SESSION);
                        }
                    case VARIABLES:
                        switch (lexer.nextToken()) {
                        case KW_LIKE:
                            tempStr = like();
                            return new ShowVariables(VariableScope.SESSION, tempStr);
                        case KW_WHERE:
                            tempExpr = where();
                            return new ShowVariables(VariableScope.SESSION, tempExpr);
                        default:
                            return new ShowVariables(VariableScope.SESSION);
                        }
                    }
                }
                throw err("unexpected token for SHOW SESSION");
            case SLAVE:
                lexer.nextToken();
                tempStrUp = lexer.stringValueUppercase();
                tempSi = specialIdentifiers.get(tempStrUp);
                if (tempSi != null) {
                    switch (tempSi) {
                    case HOSTS:
                        lexer.nextToken();
                        return new ShowSlaveHosts();
                    case STATUS:
                        lexer.nextToken();
                        return new ShowSlaveStatus();
                    }
                }
                throw err("unexpected token for SHOW SLAVE");
            case STATUS:
                switch (lexer.nextToken()) {
                case KW_LIKE:
                    tempStr = like();
                    return new ShowStatus(VariableScope.SESSION, tempStr);
                case KW_WHERE:
                    tempExpr = where();
                    return new ShowStatus(VariableScope.SESSION, tempExpr);
                default:
                    return new ShowStatus(VariableScope.SESSION);
                }
            case STORAGE:
                lexer.nextToken();
                matchIdentifier("ENGINES");
                return new ShowEngines();
            case TABLES:
                tempId = null;
                switch (lexer.nextToken()) {
                case KW_IN:
                case KW_FROM:
                    lexer.nextToken();
                    tempId = identifier();
                }
                switch (lexer.token()) {
                case KW_LIKE:
                    tempStr = like();
                    return new ShowTables(false, tempId, tempStr);
                case KW_WHERE:
                    tempExpr = where();
                    return new ShowTables(false, tempId, tempExpr);
                default:
                    return new ShowTables(false, tempId);
                }
            case TRIGGERS:
                tempId = null;
                switch (lexer.nextToken()) {
                case KW_IN:
                case KW_FROM:
                    lexer.nextToken();
                    tempId = identifier();
                }
                switch (lexer.token()) {
                case KW_LIKE:
                    tempStr = like();
                    return new ShowTriggers(tempId, tempStr);
                case KW_WHERE:
                    tempExpr = where();
                    return new ShowTriggers(tempId, tempExpr);
                default:
                    return new ShowTriggers(tempId);
                }
            case VARIABLES:
                switch (lexer.nextToken()) {
                case KW_LIKE:
                    tempStr = like();
                    return new ShowVariables(VariableScope.SESSION, tempStr);
                case KW_WHERE:
                    tempExpr = where();
                    return new ShowVariables(VariableScope.SESSION, tempExpr);
                default:
                    return new ShowVariables(VariableScope.SESSION);
                }
            case WARNINGS:
                lexer.nextToken();
                tempLimit = limit();
                return new ShowWarnings(false, tempLimit);
            }
            break;
        }
        throw err("unexpect token for SHOW");
    }

    private ShowIndex showIndex(ShowIndex.Type type) throws SQLSyntaxErrorException {
        lexer.nextToken();
        match(KW_FROM, KW_IN);
        Identifier tempId = identifier();
        if (lexer.token() == KW_FROM || lexer.token() == KW_IN) {
            lexer.nextToken();
            Identifier tempId2 = identifier();
            return new ShowIndex(type, tempId, tempId2);
        }
        return new ShowIndex(type, tempId);
    }

    private ShowProfile showProfile() throws SQLSyntaxErrorException {
        lexer.nextToken();
        List<ShowProfile.Type> types = new LinkedList<ShowProfile.Type>();
        ShowProfile.Type type = showPrifileType();
        if (type == null) {
            types = Collections.emptyList();
        } else if (lexer.token() == PUNC_COMMA) {
            types = new LinkedList<ShowProfile.Type>();
            types.add(type);
            for (; lexer.token() == PUNC_COMMA;) {
                lexer.nextToken();
                type = showPrifileType();
                types.add(type);
            }
        } else {
            types = new ArrayList<ShowProfile.Type>();
            types.add(type);
        }
        Expression forQuery = null;
        if (lexer.token() == KW_FOR) {
            lexer.nextToken();
            matchIdentifier("QUERY");
            forQuery = exprParser.expression();
        }
        Limit limit = limit();
        return new ShowProfile(types, forQuery, limit);
    }

    /**
     * @return null if not a type
     */
    private ShowProfile.Type showPrifileType() throws SQLSyntaxErrorException {
        switch (lexer.token()) {
        case KW_ALL:
            lexer.nextToken();
            return ShowProfile.Type.ALL;
        case IDENTIFIER:
            String strUp = lexer.stringValueUppercase();
            SpecialIdentifier si = specialIdentifiers.get(strUp);
            if (si != null) {
                switch (si) {
                case BLOCK:
                    lexer.nextToken();
                    matchIdentifier("IO");
                    return ShowProfile.Type.BLOCK_IO;
                case CONTEXT:
                    lexer.nextToken();
                    matchIdentifier("SWITCHES");
                    return ShowProfile.Type.CONTEXT_SWITCHES;
                case CPU:
                    lexer.nextToken();
                    return ShowProfile.Type.CPU;
                case IPC:
                    lexer.nextToken();
                    return ShowProfile.Type.IPC;
                case MEMORY:
                    lexer.nextToken();
                    return ShowProfile.Type.MEMORY;
                case PAGE:
                    lexer.nextToken();
                    matchIdentifier("FAULTS");
                    return ShowProfile.Type.PAGE_FAULTS;
                case SOURCE:
                    lexer.nextToken();
                    return ShowProfile.Type.SOURCE;
                case SWAPS:
                    lexer.nextToken();
                    return ShowProfile.Type.SWAPS;
                }
            }
        default:
            return null;
        }
    }

    /**
     * First token is {@link SpecialIdentifier#COLUMNS}
     * 
     * <pre>
     * SHOW [FULL] <code>COLUMNS {FROM | IN} tbl_name [{FROM | IN} db_name] [LIKE 'pattern' | WHERE expr] </code>
     * </pre>
     */
    private ShowColumns showColumns(boolean full) throws SQLSyntaxErrorException {
        lexer.nextToken();
        match(KW_FROM, KW_IN);
        Identifier table = identifier();
        Identifier database = null;
        switch (lexer.token()) {
        case KW_FROM:
        case KW_IN:
            lexer.nextToken();
            database = identifier();
        }
        switch (lexer.token()) {
        case KW_LIKE:
            String like = like();
            return new ShowColumns(full, table, database, like);
        case KW_WHERE:
            Expression where = where();
            return new ShowColumns(full, table, database, where);
        }
        return new ShowColumns(full, table, database);
    }

    private String like() throws SQLSyntaxErrorException {
        match(KW_LIKE);
        String pattern = lexer.stringValue();
        lexer.nextToken();
        return pattern;
    }

    private Expression where() throws SQLSyntaxErrorException {
        match(KW_WHERE);
        Expression where = exprParser.expression();
        return where;
    }

    private String getStringValue() throws SQLSyntaxErrorException {
        String name;
        switch (lexer.token()) {
        case IDENTIFIER:
            name = Identifier.unescapeName(lexer.stringValue());
            lexer.nextToken();
            return name;
        case LITERAL_CHARS:
            name = lexer.stringValue();
            name = LiteralString.getUnescapedString(name.substring(1, name.length() - 1));
            lexer.nextToken();
            return name;
        default:
            throw err("unexpected token: " + lexer.token());
        }
    }

    /**
     * @return {@link DALSetStatement} or {@link MTSSetTransactionStatement}
     */
    @SuppressWarnings("unchecked")
    public SQLStatement set() throws SQLSyntaxErrorException {
        match(KW_SET);
        if (lexer.token() == KW_OPTION) {
            lexer.nextToken();
        }
        if (lexer.token() == IDENTIFIER
                && SpecialIdentifier.NAMES == specialIdentifiers.get(lexer.stringValueUppercase())) {
            if (lexer.nextToken() == KW_DEFAULT) {
                lexer.nextToken();
                return new DALSetNamesStatement();
            }
            String charsetName = getStringValue();
            String collationName = null;
            if (lexer.token() == KW_COLLATE) {
                lexer.nextToken();
                collationName = getStringValue();
            }
            return new DALSetNamesStatement(charsetName, collationName);
        } else if (lexer.token() == KW_CHARACTER) {
            lexer.nextToken();
            match(KW_SET);
            if (lexer.token() == KW_DEFAULT) {
                lexer.nextToken();
                return new DALSetCharacterSetStatement();
            }
            String charsetName = getStringValue();
            return new DALSetCharacterSetStatement(charsetName);
        }

        List<Pair<VariableExpression, Expression>> assignmentList;
        Object obj = varAssign();
        if (obj instanceof MTSSetTransactionStatement) {
            return (MTSSetTransactionStatement) obj;
        }
        Pair<VariableExpression, Expression> pair = (Pair<VariableExpression, Expression>) obj;
        if (lexer.token() != PUNC_COMMA) {
            assignmentList = new ArrayList<Pair<VariableExpression, Expression>>(1);
            assignmentList.add(pair);
            return new DALSetStatement(assignmentList);
        }
        assignmentList = new LinkedList<Pair<VariableExpression, Expression>>();
        assignmentList.add(pair);
        for (; lexer.token() == PUNC_COMMA;) {
            lexer.nextToken();
            pair = (Pair<VariableExpression, Expression>) varAssign();
            assignmentList.add(pair);
        }
        return new DALSetStatement(assignmentList);
    }

    /**
     * first token is <code>TRANSACTION</code>
     */
    private MTSSetTransactionStatement setMTSSetTransactionStatement(VariableScope scope)
            throws SQLSyntaxErrorException {
        lexer.nextToken();
        matchIdentifier("ISOLATION");
        matchIdentifier("LEVEL");

        SpecialIdentifier si;
        switch (lexer.token()) {
        case KW_READ:
            lexer.nextToken();
            si = specialIdentifiers.get(lexer.stringValueUppercase());
            if (si != null) {
                switch (si) {
                case COMMITTED:
                    lexer.nextToken();
                    return new MTSSetTransactionStatement(
                            scope,
                            MTSSetTransactionStatement.IsolationLevel.READ_COMMITTED);
                case UNCOMMITTED:
                    lexer.nextToken();
                    return new MTSSetTransactionStatement(
                            scope,
                            MTSSetTransactionStatement.IsolationLevel.READ_UNCOMMITTED);
                }
            }
            throw err("unknown isolation read level: " + lexer.stringValue());
        case IDENTIFIER:
            si = specialIdentifiers.get(lexer.stringValueUppercase());
            if (si != null) {
                switch (si) {
                case REPEATABLE:
                    lexer.nextToken();
                    match(KW_READ);
                    return new MTSSetTransactionStatement(
                            scope,
                            MTSSetTransactionStatement.IsolationLevel.REPEATABLE_READ);
                case SERIALIZABLE:
                    lexer.nextToken();
                    return new MTSSetTransactionStatement(scope, MTSSetTransactionStatement.IsolationLevel.SERIALIZABLE);
                }
            }
        }
        throw err("unknown isolation level: " + lexer.stringValue());
    }

    private Object varAssign() throws SQLSyntaxErrorException {
        VariableExpression var;
        Expression expr;
        VariableScope scope = VariableScope.SESSION;
        switch (lexer.token()) {
        case IDENTIFIER:
            boolean explictScope = false;
            SpecialIdentifier si = specialIdentifiers.get(lexer.stringValueUppercase());
            if (si != null) {
                switch (si) {
                case TRANSACTION:
                    return setMTSSetTransactionStatement(null);
                case GLOBAL:
                    scope = VariableScope.GLOBAL;
                case SESSION:
                case LOCAL:
                    explictScope = true;
                    lexer.nextToken();
                default:
                    break;
                }
            }
            if (explictScope && specialIdentifiers.get(lexer.stringValueUppercase()) == SpecialIdentifier.TRANSACTION) {
                return setMTSSetTransactionStatement(scope);
            }
            var = new SysVarPrimary(scope, lexer.stringValue(), lexer.stringValueUppercase());
            match(IDENTIFIER);
            break;
        case SYS_VAR:
            var = systemVariale();
            break;
        case USR_VAR:
            var = new UsrDefVarPrimary(lexer.stringValue());
            lexer.nextToken();
            break;
        default:
            throw err("unexpected token for SET statement");
        }
        match(OP_EQUALS, OP_ASSIGN);
        expr = exprParser.expression();
        return new Pair<VariableExpression, Expression>(var, expr);
    }
}
