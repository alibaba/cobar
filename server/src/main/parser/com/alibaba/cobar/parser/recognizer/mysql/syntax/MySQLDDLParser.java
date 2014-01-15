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
 * (created at 2011-7-4)
 */
package com.alibaba.cobar.parser.recognizer.mysql.syntax;

import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.EOF;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.IDENTIFIER;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_AS;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_BINARY;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_CHARACTER;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_COLLATE;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_COLUMN;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_DEFAULT;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_EXISTS;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_IF;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_IGNORE;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_INDEX;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_KEY;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_KEYS;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_NOT;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_ON;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_SET;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_TABLE;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_TO;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_UNSIGNED;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_USING;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.KW_ZEROFILL;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.LITERAL_NULL;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.LITERAL_NUM_PURE_DIGIT;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.OP_EQUALS;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.PUNC_COMMA;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.PUNC_LEFT_PAREN;
import static com.alibaba.cobar.parser.recognizer.mysql.MySQLToken.PUNC_RIGHT_PAREN;

import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.primary.Identifier;
import com.alibaba.cobar.parser.ast.expression.primary.literal.Literal;
import com.alibaba.cobar.parser.ast.expression.primary.literal.LiteralString;
import com.alibaba.cobar.parser.ast.fragment.ddl.ColumnDefinition;
import com.alibaba.cobar.parser.ast.fragment.ddl.TableOptions;
import com.alibaba.cobar.parser.ast.fragment.ddl.datatype.DataType;
import com.alibaba.cobar.parser.ast.fragment.ddl.index.IndexColumnName;
import com.alibaba.cobar.parser.ast.fragment.ddl.index.IndexDefinition;
import com.alibaba.cobar.parser.ast.fragment.ddl.index.IndexOption;
import com.alibaba.cobar.parser.ast.stmt.ddl.DDLAlterTableStatement;
import com.alibaba.cobar.parser.ast.stmt.ddl.DDLCreateIndexStatement;
import com.alibaba.cobar.parser.ast.stmt.ddl.DDLCreateTableStatement;
import com.alibaba.cobar.parser.ast.stmt.ddl.DDLDropIndexStatement;
import com.alibaba.cobar.parser.ast.stmt.ddl.DDLDropTableStatement;
import com.alibaba.cobar.parser.ast.stmt.ddl.DDLRenameTableStatement;
import com.alibaba.cobar.parser.ast.stmt.ddl.DDLStatement;
import com.alibaba.cobar.parser.ast.stmt.ddl.DDLTruncateStatement;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLSelectStatement;
import com.alibaba.cobar.parser.ast.stmt.extension.ExtDDLCreatePolicy;
import com.alibaba.cobar.parser.ast.stmt.extension.ExtDDLDropPolicy;
import com.alibaba.cobar.parser.recognizer.mysql.lexer.MySQLLexer;
import com.alibaba.cobar.parser.util.Pair;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class MySQLDDLParser extends MySQLParser {
    protected MySQLExprParser exprParser;

    public MySQLDDLParser(MySQLLexer lexer, MySQLExprParser exprParser) {
        super(lexer);
        this.exprParser = exprParser;
    }

    private static enum SpecialIdentifier {
        TRUNCATE,
        TEMPORARY,
        DEFINER,
        KEY_BLOCK_SIZE,
        COMMENT,
        DYNAMIC,
        FIXED,
        BIT,
        DATE,
        TIME,
        TIMESTAMP,
        DATETIME,
        YEAR,
        TEXT,
        ENUM,
        ENGINE,
        AUTO_INCREMENT,
        AVG_ROW_LENGTH,
        CHECKSUM,
        CONNECTION,
        DATA,
        DELAY_KEY_WRITE,
        INSERT_METHOD,
        MAX_ROWS,
        MIN_ROWS,
        PACK_KEYS,
        PASSWORD,
        ROW_FORMAT,
        COMPRESSED,
        REDUNDANT,
        COMPACT,
        MODIFY,
        DISABLE,
        ENABLE,
        DISCARD,
        IMPORT,
        /** MySQL 5.1 legacy syntax */
        CHARSET,
        /** EXTENSION syntax */
        POLICY
    }

    private static final Map<String, SpecialIdentifier> specialIdentifiers = new HashMap<String, SpecialIdentifier>(
            1,
            1);
    static {
        specialIdentifiers.put("TRUNCATE", SpecialIdentifier.TRUNCATE);
        specialIdentifiers.put("TEMPORARY", SpecialIdentifier.TEMPORARY);
        specialIdentifiers.put("DEFINER", SpecialIdentifier.DEFINER);
        specialIdentifiers.put("KEY_BLOCK_SIZE", SpecialIdentifier.KEY_BLOCK_SIZE);
        specialIdentifiers.put("COMMENT", SpecialIdentifier.COMMENT);
        specialIdentifiers.put("DYNAMIC", SpecialIdentifier.DYNAMIC);
        specialIdentifiers.put("FIXED", SpecialIdentifier.FIXED);
        specialIdentifiers.put("BIT", SpecialIdentifier.BIT);
        specialIdentifiers.put("DATE", SpecialIdentifier.DATE);
        specialIdentifiers.put("TIME", SpecialIdentifier.TIME);
        specialIdentifiers.put("TIMESTAMP", SpecialIdentifier.TIMESTAMP);
        specialIdentifiers.put("DATETIME", SpecialIdentifier.DATETIME);
        specialIdentifiers.put("YEAR", SpecialIdentifier.YEAR);
        specialIdentifiers.put("TEXT", SpecialIdentifier.TEXT);
        specialIdentifiers.put("ENUM", SpecialIdentifier.ENUM);
        specialIdentifiers.put("ENGINE", SpecialIdentifier.ENGINE);
        specialIdentifiers.put("AUTO_INCREMENT", SpecialIdentifier.AUTO_INCREMENT);
        specialIdentifiers.put("AVG_ROW_LENGTH", SpecialIdentifier.AVG_ROW_LENGTH);
        specialIdentifiers.put("CHECKSUM", SpecialIdentifier.CHECKSUM);
        specialIdentifiers.put("CONNECTION", SpecialIdentifier.CONNECTION);
        specialIdentifiers.put("DATA", SpecialIdentifier.DATA);
        specialIdentifiers.put("DELAY_KEY_WRITE", SpecialIdentifier.DELAY_KEY_WRITE);
        specialIdentifiers.put("INSERT_METHOD", SpecialIdentifier.INSERT_METHOD);
        specialIdentifiers.put("MAX_ROWS", SpecialIdentifier.MAX_ROWS);
        specialIdentifiers.put("MIN_ROWS", SpecialIdentifier.MIN_ROWS);
        specialIdentifiers.put("PACK_KEYS", SpecialIdentifier.PACK_KEYS);
        specialIdentifiers.put("PASSWORD", SpecialIdentifier.PASSWORD);
        specialIdentifiers.put("ROW_FORMAT", SpecialIdentifier.ROW_FORMAT);
        specialIdentifiers.put("COMPRESSED", SpecialIdentifier.COMPRESSED);
        specialIdentifiers.put("REDUNDANT", SpecialIdentifier.REDUNDANT);
        specialIdentifiers.put("COMPACT", SpecialIdentifier.COMPACT);
        specialIdentifiers.put("MODIFY", SpecialIdentifier.MODIFY);
        specialIdentifiers.put("DISABLE", SpecialIdentifier.DISABLE);
        specialIdentifiers.put("ENABLE", SpecialIdentifier.ENABLE);
        specialIdentifiers.put("DISCARD", SpecialIdentifier.DISCARD);
        specialIdentifiers.put("IMPORT", SpecialIdentifier.IMPORT);
        specialIdentifiers.put("CHARSET", SpecialIdentifier.CHARSET);
        specialIdentifiers.put("POLICY", SpecialIdentifier.POLICY);
    }

    public DDLTruncateStatement truncate() throws SQLSyntaxErrorException {
        matchIdentifier("TRUNCATE");
        if (lexer.token() == KW_TABLE) {
            lexer.nextToken();
        }
        Identifier tb = identifier();
        return new DDLTruncateStatement(tb);
    }

    /**
     * nothing has been pre-consumed
     */
    public DDLStatement ddlStmt() throws SQLSyntaxErrorException {
        Identifier idTemp1;
        Identifier idTemp2;
        SpecialIdentifier siTemp;
        switch (lexer.token()) {
        case KW_ALTER:
            boolean ignore = false;
            if (lexer.nextToken() == KW_IGNORE) {
                ignore = true;
                lexer.nextToken();
            }
            switch (lexer.token()) {
            case KW_TABLE:
                lexer.nextToken();
                idTemp1 = identifier();
                DDLAlterTableStatement alterTableStatement = new DDLAlterTableStatement(ignore, idTemp1);
                return alterTable(alterTableStatement);
            default:
                throw err("Only ALTER TABLE is supported");
            }
        case KW_CREATE:
            switch (lexer.nextToken()) {
            case KW_UNIQUE:
            case KW_FULLTEXT:
            case KW_SPATIAL:
                lexer.nextToken();
            case KW_INDEX:
                lexer.nextToken();
                idTemp1 = identifier();
                for (; lexer.token() != KW_ON; lexer.nextToken());
                lexer.nextToken();
                idTemp2 = identifier();
                return new DDLCreateIndexStatement(idTemp1, idTemp2);
            case KW_TABLE:
                lexer.nextToken();
                return createTable(false);
            case IDENTIFIER:
                siTemp = specialIdentifiers.get(lexer.stringValueUppercase());
                if (siTemp != null) {
                    switch (siTemp) {
                    case TEMPORARY:
                        lexer.nextToken();
                        match(KW_TABLE);
                        return createTable(true);
                    case POLICY:
                        lexer.nextToken();
                        Identifier policyName = identifier();
                        match(PUNC_LEFT_PAREN);
                        ExtDDLCreatePolicy policy = new ExtDDLCreatePolicy(policyName);
                        for (int j = 0; lexer.token() != PUNC_RIGHT_PAREN; ++j) {
                            if (j > 0) {
                                match(PUNC_COMMA);
                            }
                            Integer id = lexer.integerValue().intValue();
                            match(LITERAL_NUM_PURE_DIGIT);
                            Expression val = exprParser.expression();
                            policy.addProportion(id, val);
                        }
                        match(PUNC_RIGHT_PAREN);
                        return policy;
                    }
                }
            default:
                throw err("unsupported DDL for CREATE");
            }
        case KW_DROP:
            switch (lexer.nextToken()) {
            case KW_INDEX:
                lexer.nextToken();
                idTemp1 = identifier();
                match(KW_ON);
                idTemp2 = identifier();
                return new DDLDropIndexStatement(idTemp1, idTemp2);
            case KW_TABLE:
                lexer.nextToken();
                return dropTable(false);
            case IDENTIFIER:
                siTemp = specialIdentifiers.get(lexer.stringValueUppercase());
                if (siTemp != null) {
                    switch (siTemp) {
                    case TEMPORARY:
                        lexer.nextToken();
                        match(KW_TABLE);
                        return dropTable(true);
                    case POLICY:
                        lexer.nextToken();
                        Identifier policyName = identifier();
                        return new ExtDDLDropPolicy(policyName);
                    }
                }
            default:
                throw err("unsupported DDL for DROP");
            }
        case KW_RENAME:
            lexer.nextToken();
            match(KW_TABLE);
            idTemp1 = identifier();
            match(KW_TO);
            idTemp2 = identifier();
            List<Pair<Identifier, Identifier>> list;
            if (lexer.token() != PUNC_COMMA) {
                list = new ArrayList<Pair<Identifier, Identifier>>(1);
                list.add(new Pair<Identifier, Identifier>(idTemp1, idTemp2));
                return new DDLRenameTableStatement(list);
            }
            list = new LinkedList<Pair<Identifier, Identifier>>();
            list.add(new Pair<Identifier, Identifier>(idTemp1, idTemp2));
            for (; lexer.token() == PUNC_COMMA;) {
                lexer.nextToken();
                idTemp1 = identifier();
                match(KW_TO);
                idTemp2 = identifier();
                list.add(new Pair<Identifier, Identifier>(idTemp1, idTemp2));
            }
            return new DDLRenameTableStatement(list);
        case IDENTIFIER:
            SpecialIdentifier si = specialIdentifiers.get(lexer.stringValueUppercase());
            if (si != null) {
                switch (si) {
                case TRUNCATE:
                    return truncate();
                }
            }
        default:
            throw err("unsupported DDL");
        }
    }

    /**
     * <code>TABLE</code> has been consumed
     */
    private DDLDropTableStatement dropTable(boolean temp) throws SQLSyntaxErrorException {
        boolean ifExists = false;
        if (lexer.token() == KW_IF) {
            lexer.nextToken();
            match(KW_EXISTS);
            ifExists = true;
        }
        Identifier tb = identifier();
        List<Identifier> list;
        if (lexer.token() != PUNC_COMMA) {
            list = new ArrayList<Identifier>(1);
            list.add(tb);
        } else {
            list = new LinkedList<Identifier>();
            list.add(tb);
            for (; lexer.token() == PUNC_COMMA;) {
                lexer.nextToken();
                tb = identifier();
                list.add(tb);
            }
        }
        DDLDropTableStatement.Mode mode = DDLDropTableStatement.Mode.UNDEF;
        switch (lexer.token()) {
        case KW_RESTRICT:
            lexer.nextToken();
            mode = DDLDropTableStatement.Mode.RESTRICT;
            break;
        case KW_CASCADE:
            lexer.nextToken();
            mode = DDLDropTableStatement.Mode.CASCADE;
            break;
        }
        return new DDLDropTableStatement(list, temp, ifExists, mode);
    }

    /**
     * token of table name has been consumed
     * 
     * @throws SQLSyntaxErrorException
     */
    private DDLAlterTableStatement alterTable(DDLAlterTableStatement stmt) throws SQLSyntaxErrorException {
        TableOptions options = new TableOptions();
        stmt.setTableOptions(options);
        Identifier id = null;
        Identifier id2 = null;
        Identifier id3 = null;
        ColumnDefinition colDef = null;
        IndexDefinition indexDef = null;
        Expression expr = null;
        for (int i = 0; lexer.token() != EOF; ++i) {
            if (i > 0) {
                match(PUNC_COMMA);
            }
            if (tableOptions(options)) {
                continue;
            }
            main_switch: switch (lexer.token()) {
            case KW_CONVERT:
                // | CONVERT TO CHARACTER SET charset_name [COLLATE
                // collation_name]
                lexer.nextToken();
                match(KW_TO);
                match(KW_CHARACTER);
                match(KW_SET);
                id = identifier();
                id2 = null;
                if (lexer.token() == KW_COLLATE) {
                    lexer.nextToken();
                    id2 = identifier();
                }
                stmt.setConvertCharset(new Pair<Identifier, Identifier>(id, id2));
                break main_switch;
            case KW_RENAME:
                // | RENAME [TO] new_tbl_name
                if (lexer.nextToken() == KW_TO) {
                    lexer.nextToken();
                }
                id = identifier();
                stmt.setRenameTo(id);
                break main_switch;
            case KW_DROP:
                drop_switch: switch (lexer.nextToken()) {
                case KW_INDEX:
                case KW_KEY:
                    // | DROP {INDEX|KEY} index_name
                    lexer.nextToken();
                    id = identifier();
                    stmt.addAlterSpecification(new DDLAlterTableStatement.DropIndex(id));
                    break drop_switch;
                case KW_PRIMARY:
                    // | DROP PRIMARY KEY
                    lexer.nextToken();
                    match(KW_KEY);
                    stmt.addAlterSpecification(new DDLAlterTableStatement.DropPrimaryKey());
                    break drop_switch;
                case IDENTIFIER:
                    // | DROP [COLUMN] col_name
                    id = identifier();
                    stmt.addAlterSpecification(new DDLAlterTableStatement.DropColumn(id));
                    break drop_switch;
                case KW_COLUMN:
                    // | DROP [COLUMN] col_name
                    lexer.nextToken();
                    id = identifier();
                    stmt.addAlterSpecification(new DDLAlterTableStatement.DropColumn(id));
                    break drop_switch;
                default:
                    throw new SQLSyntaxErrorException("ALTER TABLE error for DROP");
                }
                break main_switch;
            case KW_CHANGE:
                // | CHANGE [COLUMN] old_col_name new_col_name column_definition
                // [FIRST|AFTER col_name]
                if (lexer.nextToken() == KW_COLUMN) {
                    lexer.nextToken();
                }
                id = identifier();
                id2 = identifier();
                colDef = columnDefinition();
                if (lexer.token() == IDENTIFIER) {
                    if ("FIRST".equals(lexer.stringValueUppercase())) {
                        lexer.nextToken();
                        stmt.addAlterSpecification(new DDLAlterTableStatement.ChangeColumn(id, id2, colDef, null));
                    } else if ("AFTER".equals(lexer.stringValueUppercase())) {
                        lexer.nextToken();
                        id3 = identifier();
                        stmt.addAlterSpecification(new DDLAlterTableStatement.ChangeColumn(id, id2, colDef, id3));
                    } else {
                        stmt.addAlterSpecification(new DDLAlterTableStatement.ChangeColumn(id, id2, colDef));
                    }
                } else {
                    stmt.addAlterSpecification(new DDLAlterTableStatement.ChangeColumn(id, id2, colDef));
                }
                break main_switch;
            case KW_ALTER:
                // | ALTER [COLUMN] col_name {SET DEFAULT literal | DROP
                // DEFAULT}
                if (lexer.nextToken() == KW_COLUMN) {
                    lexer.nextToken();
                }
                id = identifier();
                switch (lexer.token()) {
                case KW_SET:
                    lexer.nextToken();
                    match(KW_DEFAULT);
                    expr = exprParser.expression();
                    stmt.addAlterSpecification(new DDLAlterTableStatement.AlterColumnDefaultVal(id, expr));
                    break;
                case KW_DROP:
                    lexer.nextToken();
                    match(KW_DEFAULT);
                    stmt.addAlterSpecification(new DDLAlterTableStatement.AlterColumnDefaultVal(id));
                    break;
                default:
                    throw new SQLSyntaxErrorException("ALTER TABLE error for ALTER");
                }
                break main_switch;
            case KW_ADD:
                add_switch: switch (lexer.nextToken()) {
                case IDENTIFIER:
                    // | ADD [COLUMN] col_name column_definition [FIRST | AFTER
                    // col_name ]
                    id = identifier();
                    colDef = columnDefinition();
                    if (lexer.token() == IDENTIFIER) {
                        if ("FIRST".equals(lexer.stringValueUppercase())) {
                            lexer.nextToken();
                            stmt.addAlterSpecification(new DDLAlterTableStatement.AddColumn(id, colDef, null));
                        } else if ("AFTER".equals(lexer.stringValueUppercase())) {
                            lexer.nextToken();
                            id2 = identifier();
                            stmt.addAlterSpecification(new DDLAlterTableStatement.AddColumn(id, colDef, id2));
                        } else {
                            stmt.addAlterSpecification(new DDLAlterTableStatement.AddColumn(id, colDef));
                        }
                    } else {
                        stmt.addAlterSpecification(new DDLAlterTableStatement.AddColumn(id, colDef));
                    }
                    break add_switch;
                case PUNC_LEFT_PAREN:
                    // | ADD [COLUMN] (col_name column_definition,...)
                    lexer.nextToken();
                    for (int j = 0; lexer.token() != PUNC_RIGHT_PAREN; ++j) {
                        DDLAlterTableStatement.AddColumns addColumns = new DDLAlterTableStatement.AddColumns();
                        stmt.addAlterSpecification(addColumns);
                        if (j > 0) {
                            match(PUNC_COMMA);
                        }
                        id = identifier();
                        colDef = columnDefinition();
                        addColumns.addColumn(id, colDef);
                    }
                    match(PUNC_RIGHT_PAREN);
                    break add_switch;
                case KW_COLUMN:
                    if (lexer.nextToken() == PUNC_LEFT_PAREN) {
                        // | ADD [COLUMN] (col_name column_definition,...)
                        lexer.nextToken();
                        for (int j = 0; lexer.token() != PUNC_RIGHT_PAREN; ++j) {
                            DDLAlterTableStatement.AddColumns addColumns = new DDLAlterTableStatement.AddColumns();
                            stmt.addAlterSpecification(addColumns);
                            if (j > 0) {
                                match(PUNC_COMMA);
                            }
                            id = identifier();
                            colDef = columnDefinition();
                            addColumns.addColumn(id, colDef);
                        }
                        match(PUNC_RIGHT_PAREN);
                    } else {
                        // | ADD [COLUMN] col_name column_definition [FIRST |
                        // AFTER col_name ]
                        id = identifier();
                        colDef = columnDefinition();
                        if (lexer.token() == IDENTIFIER) {
                            if ("FIRST".equals(lexer.stringValueUppercase())) {
                                lexer.nextToken();
                                stmt.addAlterSpecification(new DDLAlterTableStatement.AddColumn(id, colDef, null));
                            } else if ("AFTER".equals(lexer.stringValueUppercase())) {
                                lexer.nextToken();
                                id2 = identifier();
                                stmt.addAlterSpecification(new DDLAlterTableStatement.AddColumn(id, colDef, id2));
                            } else {
                                stmt.addAlterSpecification(new DDLAlterTableStatement.AddColumn(id, colDef));
                            }
                        } else {
                            stmt.addAlterSpecification(new DDLAlterTableStatement.AddColumn(id, colDef));
                        }
                    }
                    break add_switch;
                case KW_INDEX:
                case KW_KEY:
                    // | ADD {INDEX|KEY} [index_name] [index_type]
                    // (index_col_name,...) [index_option] ...
                    id = null;
                    if (lexer.nextToken() == IDENTIFIER) {
                        id = identifier();
                    }
                    indexDef = indexDefinition();
                    stmt.addAlterSpecification(new DDLAlterTableStatement.AddIndex(id, indexDef));
                    break add_switch;
                case KW_PRIMARY:
                    // | ADD PRIMARY KEY [index_type] (index_col_name,...)
                    // [index_option] ...
                    lexer.nextToken();
                    match(KW_KEY);
                    indexDef = indexDefinition();
                    stmt.addAlterSpecification(new DDLAlterTableStatement.AddPrimaryKey(indexDef));
                    break add_switch;
                case KW_UNIQUE:
                    // | ADD UNIQUE [INDEX|KEY] [index_name] [index_type]
                    // (index_col_name,...) [index_option] ...
                    switch (lexer.nextToken()) {
                    case KW_INDEX:
                    case KW_KEY:
                        lexer.nextToken();
                    }
                    id = null;
                    if (lexer.token() == IDENTIFIER) {
                        id = identifier();
                    }
                    indexDef = indexDefinition();
                    stmt.addAlterSpecification(new DDLAlterTableStatement.AddUniqueKey(id, indexDef));
                    break add_switch;
                case KW_FULLTEXT:
                    // | ADD FULLTEXT [INDEX|KEY] [index_name]
                    // (index_col_name,...) [index_option] ...
                    switch (lexer.nextToken()) {
                    case KW_INDEX:
                    case KW_KEY:
                        lexer.nextToken();
                    }
                    id = null;
                    if (lexer.token() == IDENTIFIER) {
                        id = identifier();
                    }
                    indexDef = indexDefinition();
                    stmt.addAlterSpecification(new DDLAlterTableStatement.AddFullTextIndex(id, indexDef));
                    break add_switch;
                case KW_SPATIAL:
                    // | ADD SPATIAL [INDEX|KEY] [index_name]
                    // (index_col_name,...) [index_option] ...
                    switch (lexer.nextToken()) {
                    case KW_INDEX:
                    case KW_KEY:
                        lexer.nextToken();
                    }
                    id = null;
                    if (lexer.token() == IDENTIFIER) {
                        id = identifier();
                    }
                    indexDef = indexDefinition();
                    stmt.addAlterSpecification(new DDLAlterTableStatement.AddSpatialIndex(id, indexDef));
                    break add_switch;
                default:
                    throw new SQLSyntaxErrorException("ALTER TABLE error for ADD");
                }
                break main_switch;
            case IDENTIFIER:
                SpecialIdentifier si = specialIdentifiers.get(lexer.stringValueUppercase());
                if (si != null) {
                    switch (si) {
                    case IMPORT:
                        // | IMPORT TABLESPACE
                        lexer.nextToken();
                        matchIdentifier("TABLESPACE");
                        stmt.setImportTableSpace(true);
                        break main_switch;
                    case DISCARD:
                        // | DISCARD TABLESPACE
                        lexer.nextToken();
                        matchIdentifier("TABLESPACE");
                        stmt.setDiscardTableSpace(true);
                        break main_switch;
                    case ENABLE:
                        // | ENABLE KEYS
                        lexer.nextToken();
                        match(KW_KEYS);
                        stmt.setEnableKeys(true);
                        break main_switch;
                    case DISABLE:
                        // | DISABLE KEYS
                        lexer.nextToken();
                        match(KW_KEYS);
                        stmt.setDisableKeys(true);
                        break main_switch;
                    case MODIFY:
                        // | MODIFY [COLUMN] col_name column_definition [FIRST |
                        // AFTER col_name]
                        if (lexer.nextToken() == KW_COLUMN) {
                            lexer.nextToken();
                        }
                        id = identifier();
                        colDef = columnDefinition();
                        if (lexer.token() == IDENTIFIER) {
                            if ("FIRST".equals(lexer.stringValueUppercase())) {
                                lexer.nextToken();
                                stmt.addAlterSpecification(new DDLAlterTableStatement.ModifyColumn(id, colDef, null));
                            } else if ("AFTER".equals(lexer.stringValueUppercase())) {
                                lexer.nextToken();
                                id2 = identifier();
                                stmt.addAlterSpecification(new DDLAlterTableStatement.ModifyColumn(id, colDef, id2));
                            } else {
                                stmt.addAlterSpecification(new DDLAlterTableStatement.ModifyColumn(id, colDef));
                            }
                        } else {
                            stmt.addAlterSpecification(new DDLAlterTableStatement.ModifyColumn(id, colDef));
                        }
                        break main_switch;
                    }
                }
            default:
                throw new SQLSyntaxErrorException("unknown ALTER specification");
            }
        }
        return stmt;
    }

    /**
     * <code>TABLE</code> has been consumed
     */
    private DDLCreateTableStatement createTable(boolean temp) throws SQLSyntaxErrorException {
        boolean ifNotExists = false;
        if (lexer.token() == KW_IF) {
            lexer.nextToken();
            match(KW_NOT);
            match(KW_EXISTS);
            ifNotExists = true;
        }
        Identifier table = identifier();
        DDLCreateTableStatement stmt = new DDLCreateTableStatement(temp, ifNotExists, table);
        createTableDefs(stmt);

        TableOptions options = new TableOptions();
        stmt.setTableOptions(options);
        tableOptions(options);

        DDLCreateTableStatement.SelectOption selectOpt = null;
        switch (lexer.token()) {
        case KW_IGNORE:
            selectOpt = DDLCreateTableStatement.SelectOption.IGNORED;
            if (lexer.nextToken() == KW_AS) {
                lexer.nextToken();
            }
            break;
        case KW_REPLACE:
            selectOpt = DDLCreateTableStatement.SelectOption.REPLACE;
            if (lexer.nextToken() == KW_AS) {
                lexer.nextToken();
            }
            break;
        case KW_AS:
            lexer.nextToken();
        case KW_SELECT:
            break;
        case EOF:
            return stmt;
        default:
            throw new SQLSyntaxErrorException("DDL CREATE TABLE statement not end properly");
        }
        DMLSelectStatement select = new MySQLDMLSelectParser(lexer, exprParser).select();
        stmt.setSelect(selectOpt, select);
        match(EOF);
        return stmt;
    }

    private void createTableDefs(DDLCreateTableStatement stmt) throws SQLSyntaxErrorException {
        if (lexer.token() != PUNC_LEFT_PAREN) {
            return;
        }
        match(PUNC_LEFT_PAREN);
        IndexDefinition indexDef;
        Identifier id;
        for (int i = 0; lexer.token() != PUNC_RIGHT_PAREN; ++i) {
            if (i > 0) {
                match(PUNC_COMMA);
            }
            switch (lexer.token()) {
            case KW_PRIMARY:
                lexer.nextToken();
                match(KW_KEY);
                indexDef = indexDefinition();
                stmt.setPrimaryKey(indexDef);
                break;
            case KW_INDEX:
            case KW_KEY:
                lexer.nextToken();
                if (lexer.token() == IDENTIFIER) {
                    id = identifier();
                } else {
                    id = null;
                }
                indexDef = indexDefinition();
                stmt.addIndex(id, indexDef);
                break;
            case KW_UNIQUE:
                switch (lexer.nextToken()) {
                case KW_INDEX:
                case KW_KEY:
                    lexer.nextToken();
                    break;
                }
                if (lexer.token() == IDENTIFIER) {
                    id = identifier();
                } else {
                    id = null;
                }
                indexDef = indexDefinition();
                stmt.addUniqueIndex(id, indexDef);
                break;
            case KW_FULLTEXT:
                switch (lexer.nextToken()) {
                case KW_INDEX:
                case KW_KEY:
                    lexer.nextToken();
                    break;
                }
                if (lexer.token() == IDENTIFIER) {
                    id = identifier();
                } else {
                    id = null;
                }
                indexDef = indexDefinition();
                if (indexDef.getIndexType() != null) {
                    throw new SQLSyntaxErrorException("FULLTEXT INDEX can specify no index_type");
                }
                stmt.addFullTextIndex(id, indexDef);
                break;
            case KW_SPATIAL:
                switch (lexer.nextToken()) {
                case KW_INDEX:
                case KW_KEY:
                    lexer.nextToken();
                    break;
                }
                if (lexer.token() == IDENTIFIER) {
                    id = identifier();
                } else {
                    id = null;
                }
                indexDef = indexDefinition();
                if (indexDef.getIndexType() != null) {
                    throw new SQLSyntaxErrorException("SPATIAL INDEX can specify no index_type");
                }
                stmt.addSpatialIndex(id, indexDef);
                break;
            case KW_CHECK:
                lexer.nextToken();
                match(PUNC_LEFT_PAREN);
                Expression expr = exprParser.expression();
                match(PUNC_RIGHT_PAREN);
                stmt.addCheck(expr);
                break;
            case IDENTIFIER:
                Identifier columnName = identifier();
                ColumnDefinition columnDef = columnDefinition();
                stmt.addColumnDefinition(columnName, columnDef);
                break;
            default:
                throw new SQLSyntaxErrorException("unsupportted column definition");
            }
        }
        match(PUNC_RIGHT_PAREN);
    }

    // col_name column_definition
    // | [CONSTRAINT [symbol]] PRIMARY KEY [index_type] (index_col_name,...)
    // [index_option] ...
    // | {INDEX|KEY} [index_name] [index_type] (index_col_name,...)
    // [index_option] ...
    // | [CONSTRAINT [symbol]] UNIQUE [INDEX|KEY] [index_name] [index_type]
    // (index_col_name,...) [index_option] ...
    // | {FULLTEXT|SPATIAL} [INDEX|KEY] [index_name] (index_col_name,...)
    // [index_option] ...
    // | [CONSTRAINT [symbol]] FOREIGN KEY [index_name] (index_col_name,...)
    // reference_definition
    // | CHECK (expr)
    private IndexDefinition indexDefinition() throws SQLSyntaxErrorException {
        IndexDefinition.IndexType indexType = null;
        List<IndexColumnName> columns = new ArrayList<IndexColumnName>(1);
        if (lexer.token() == KW_USING) {
            lexer.nextToken();
            int tp = matchIdentifier("BTREE", "HASH");
            indexType = tp == 0 ? IndexDefinition.IndexType.BTREE : IndexDefinition.IndexType.HASH;
        }
        match(PUNC_LEFT_PAREN);
        for (int i = 0; lexer.token() != PUNC_RIGHT_PAREN; ++i) {
            if (i > 0)
                match(PUNC_COMMA);
            IndexColumnName indexColumnName = indexColumnName();
            columns.add(indexColumnName);
        }
        match(PUNC_RIGHT_PAREN);
        List<IndexOption> options = indexOptions();
        return new IndexDefinition(indexType, columns, options);
    }

    private List<IndexOption> indexOptions() throws SQLSyntaxErrorException {
        List<IndexOption> list = null;
        for (;;) {
            main_switch: switch (lexer.token()) {
            case KW_USING:
                lexer.nextToken();
                IndexOption.IndexType indexType = matchIdentifier("BTREE", "HASH") == 0
                        ? IndexOption.IndexType.BTREE : IndexOption.IndexType.HASH;
                if (list == null) {
                    list = new ArrayList<IndexOption>(1);
                }
                list.add(new IndexOption(indexType));
                break main_switch;
            case KW_WITH:
                lexer.nextToken();
                matchIdentifier("PARSER");
                Identifier id = identifier();
                if (list == null) {
                    list = new ArrayList<IndexOption>(1);
                }
                list.add(new IndexOption(id));
                break main_switch;
            case IDENTIFIER:
                SpecialIdentifier si = specialIdentifiers.get(lexer.stringValueUppercase());
                if (si != null) {
                    switch (si) {
                    case KEY_BLOCK_SIZE:
                        lexer.nextToken();
                        if (lexer.token() == OP_EQUALS) {
                            lexer.nextToken();
                        }
                        Expression val = exprParser.expression();
                        if (list == null) {
                            list = new ArrayList<IndexOption>(1);
                        }
                        list.add(new IndexOption(val));
                        break main_switch;
                    case COMMENT:
                        lexer.nextToken();
                        LiteralString string = (LiteralString) exprParser.expression();
                        if (list == null) {
                            list = new ArrayList<IndexOption>(1);
                        }
                        list.add(new IndexOption(string));
                        break main_switch;
                    }
                }
            default:
                return list;
            }
        }
    }

    private IndexColumnName indexColumnName() throws SQLSyntaxErrorException {
        // col_name [(length)] [ASC | DESC]
        Identifier colName = identifier();
        Expression len = null;
        if (lexer.token() == PUNC_LEFT_PAREN) {
            lexer.nextToken();
            len = exprParser.expression();
            match(PUNC_RIGHT_PAREN);
        }
        switch (lexer.token()) {
        case KW_ASC:
            lexer.nextToken();
            return new IndexColumnName(colName, len, true);
        case KW_DESC:
            lexer.nextToken();
            return new IndexColumnName(colName, len, false);
        default:
            return new IndexColumnName(colName, len, true);
        }
    }

    // data_type:
    // | DATE
    // | TIME
    // | TIMESTAMP
    // | DATETIME
    // | YEAR

    // | spatial_type
    private DataType dataType() throws SQLSyntaxErrorException {
        DataType.DataTypeName typeName = null;
        boolean unsigned = false;
        boolean zerofill = false;
        /** for text only */
        boolean binary = false;
        Expression length = null;
        Expression decimals = null;
        Identifier charSet = null;
        Identifier collation = null;
        List<Expression> collectionVals = null;
        typeName: switch (lexer.token()) {
        case KW_TINYINT:
            // | TINYINT[(length)] [UNSIGNED] [ZEROFILL]
            typeName = DataType.DataTypeName.TINYINT;
            if (lexer.nextToken() == PUNC_LEFT_PAREN) {
                lexer.nextToken();
                length = exprParser.expression();
                match(PUNC_RIGHT_PAREN);
            }
            if (lexer.token() == KW_UNSIGNED) {
                unsigned = true;
                lexer.nextToken();
            }
            if (lexer.token() == KW_ZEROFILL) {
                zerofill = true;
                lexer.nextToken();
            }
            break typeName;
        case KW_SMALLINT:
            // | SMALLINT[(length)] [UNSIGNED] [ZEROFILL]
            typeName = DataType.DataTypeName.SMALLINT;
            if (lexer.nextToken() == PUNC_LEFT_PAREN) {
                lexer.nextToken();
                length = exprParser.expression();
                match(PUNC_RIGHT_PAREN);
            }
            if (lexer.token() == KW_UNSIGNED) {
                unsigned = true;
                lexer.nextToken();
            }
            if (lexer.token() == KW_ZEROFILL) {
                zerofill = true;
                lexer.nextToken();
            }
            break typeName;
        case KW_MEDIUMINT:
            // | MEDIUMINT[(length)] [UNSIGNED] [ZEROFILL]
            typeName = DataType.DataTypeName.MEDIUMINT;
            if (lexer.nextToken() == PUNC_LEFT_PAREN) {
                lexer.nextToken();
                length = exprParser.expression();
                match(PUNC_RIGHT_PAREN);
            }
            if (lexer.token() == KW_UNSIGNED) {
                unsigned = true;
                lexer.nextToken();
            }
            if (lexer.token() == KW_ZEROFILL) {
                zerofill = true;
                lexer.nextToken();
            }
            break typeName;
        case KW_INTEGER:
        case KW_INT:
            // | INT[(length)] [UNSIGNED] [ZEROFILL]
            // | INTEGER[(length)] [UNSIGNED] [ZEROFILL]
            typeName = DataType.DataTypeName.INT;
            if (lexer.nextToken() == PUNC_LEFT_PAREN) {
                lexer.nextToken();
                length = exprParser.expression();
                match(PUNC_RIGHT_PAREN);
            }
            if (lexer.token() == KW_UNSIGNED) {
                unsigned = true;
                lexer.nextToken();
            }
            if (lexer.token() == KW_ZEROFILL) {
                zerofill = true;
                lexer.nextToken();
            }
            break typeName;
        case KW_BIGINT:
            // | BIGINT[(length)] [UNSIGNED] [ZEROFILL]
            typeName = DataType.DataTypeName.BIGINT;
            if (lexer.nextToken() == PUNC_LEFT_PAREN) {
                lexer.nextToken();
                length = exprParser.expression();
                match(PUNC_RIGHT_PAREN);
            }
            if (lexer.token() == KW_UNSIGNED) {
                unsigned = true;
                lexer.nextToken();
            }
            if (lexer.token() == KW_ZEROFILL) {
                zerofill = true;
                lexer.nextToken();
            }
            break typeName;
        case KW_REAL:
            // | REAL[(length,decimals)] [UNSIGNED] [ZEROFILL]
            typeName = DataType.DataTypeName.REAL;
            if (lexer.nextToken() == PUNC_LEFT_PAREN) {
                lexer.nextToken();
                length = exprParser.expression();
                match(PUNC_COMMA);
                decimals = exprParser.expression();
                match(PUNC_RIGHT_PAREN);
            }
            if (lexer.token() == KW_UNSIGNED) {
                unsigned = true;
                lexer.nextToken();
            }
            if (lexer.token() == KW_ZEROFILL) {
                zerofill = true;
                lexer.nextToken();
            }
            break typeName;
        case KW_DOUBLE:
            // | DOUBLE[(length,decimals)] [UNSIGNED] [ZEROFILL]
            typeName = DataType.DataTypeName.DOUBLE;
            if (lexer.nextToken() == PUNC_LEFT_PAREN) {
                lexer.nextToken();
                length = exprParser.expression();
                match(PUNC_COMMA);
                decimals = exprParser.expression();
                match(PUNC_RIGHT_PAREN);
            }
            if (lexer.token() == KW_UNSIGNED) {
                unsigned = true;
                lexer.nextToken();
            }
            if (lexer.token() == KW_ZEROFILL) {
                zerofill = true;
                lexer.nextToken();
            }
            break typeName;
        case KW_FLOAT:
            // | FLOAT[(length,decimals)] [UNSIGNED] [ZEROFILL]
            typeName = DataType.DataTypeName.FLOAT;
            if (lexer.nextToken() == PUNC_LEFT_PAREN) {
                lexer.nextToken();
                length = exprParser.expression();
                match(PUNC_COMMA);
                decimals = exprParser.expression();
                match(PUNC_RIGHT_PAREN);
            }
            if (lexer.token() == KW_UNSIGNED) {
                unsigned = true;
                lexer.nextToken();
            }
            if (lexer.token() == KW_ZEROFILL) {
                zerofill = true;
                lexer.nextToken();
            }
            break typeName;
        case KW_NUMERIC:
        case KW_DECIMAL:
        case KW_DEC:
            // | DECIMAL[(length[,decimals])] [UNSIGNED] [ZEROFILL]
            // | NUMERIC[(length[,decimals])] [UNSIGNED] [ZEROFILL]
            typeName = DataType.DataTypeName.DECIMAL;
            if (lexer.nextToken() == PUNC_LEFT_PAREN) {
                lexer.nextToken();
                length = exprParser.expression();
                if (lexer.token() == PUNC_COMMA) {
                    match(PUNC_COMMA);
                    decimals = exprParser.expression();
                }
                match(PUNC_RIGHT_PAREN);
            }
            if (lexer.token() == KW_UNSIGNED) {
                unsigned = true;
                lexer.nextToken();
            }
            if (lexer.token() == KW_ZEROFILL) {
                zerofill = true;
                lexer.nextToken();
            }
            break typeName;
        case KW_CHAR:
            // | CHAR[(length)] [CHARACTER SET charset_name] [COLLATE
            // collation_name]
            typeName = DataType.DataTypeName.CHAR;
            if (lexer.nextToken() == PUNC_LEFT_PAREN) {
                lexer.nextToken();
                length = exprParser.expression();
                match(PUNC_RIGHT_PAREN);
            }
            if (lexer.token() == KW_CHARACTER) {
                lexer.nextToken();
                match(KW_SET);
                charSet = identifier();
            }
            if (lexer.token() == KW_COLLATE) {
                lexer.nextToken();
                collation = identifier();
            }
            break typeName;
        case KW_VARCHAR:
            // | VARCHAR(length) [CHARACTER SET charset_name] [COLLATE
            // collation_name]
            typeName = DataType.DataTypeName.VARCHAR;
            lexer.nextToken();
            match(PUNC_LEFT_PAREN);
            length = exprParser.expression();
            match(PUNC_RIGHT_PAREN);
            if (lexer.token() == KW_CHARACTER) {
                lexer.nextToken();
                match(KW_SET);
                charSet = identifier();
            }
            if (lexer.token() == KW_COLLATE) {
                lexer.nextToken();
                collation = identifier();
            }
            break typeName;
        case KW_BINARY:
            // | BINARY[(length)]
            typeName = DataType.DataTypeName.BINARY;
            if (lexer.nextToken() == PUNC_LEFT_PAREN) {
                lexer.nextToken();
                length = exprParser.expression();
                match(PUNC_RIGHT_PAREN);
            }
            break typeName;
        case KW_VARBINARY:
            // | VARBINARY(length)
            typeName = DataType.DataTypeName.VARBINARY;
            lexer.nextToken();
            match(PUNC_LEFT_PAREN);
            length = exprParser.expression();
            match(PUNC_RIGHT_PAREN);
            break typeName;
        case KW_TINYBLOB:
            typeName = DataType.DataTypeName.TINYBLOB;
            lexer.nextToken();
            break typeName;
        case KW_BLOB:
            typeName = DataType.DataTypeName.BLOB;
            lexer.nextToken();
            break typeName;
        case KW_MEDIUMBLOB:
            typeName = DataType.DataTypeName.MEDIUMBLOB;
            lexer.nextToken();
            break typeName;
        case KW_LONGBLOB:
            typeName = DataType.DataTypeName.LONGBLOB;
            lexer.nextToken();
            break typeName;
        case KW_TINYTEXT:
            // | TINYTEXT [BINARY] [CHARACTER SET charset_name] [COLLATE
            // collation_name]
            typeName = DataType.DataTypeName.TINYTEXT;
            if (lexer.nextToken() == KW_BINARY) {
                lexer.nextToken();
                binary = true;
            }
            if (lexer.token() == KW_CHARACTER) {
                lexer.nextToken();
                match(KW_SET);
                charSet = identifier();
            }
            if (lexer.token() == KW_COLLATE) {
                lexer.nextToken();
                collation = identifier();
            }
            break typeName;
        case KW_MEDIUMTEXT:
            // | MEDIUMTEXT [BINARY] [CHARACTER SET charset_name] [COLLATE
            // collation_name]
            typeName = DataType.DataTypeName.MEDIUMTEXT;
            if (lexer.nextToken() == KW_BINARY) {
                lexer.nextToken();
                binary = true;
            }
            if (lexer.token() == KW_CHARACTER) {
                lexer.nextToken();
                match(KW_SET);
                charSet = identifier();
            }
            if (lexer.token() == KW_COLLATE) {
                lexer.nextToken();
                collation = identifier();
            }
            break typeName;
        case KW_LONGTEXT:
            // | LONGTEXT [BINARY] [CHARACTER SET charset_name] [COLLATE
            // collation_name]
            typeName = DataType.DataTypeName.LONGTEXT;
            if (lexer.nextToken() == KW_BINARY) {
                lexer.nextToken();
                binary = true;
            }
            if (lexer.token() == KW_CHARACTER) {
                lexer.nextToken();
                match(KW_SET);
                charSet = identifier();
            }
            if (lexer.token() == KW_COLLATE) {
                lexer.nextToken();
                collation = identifier();
            }
            break typeName;
        case KW_SET:
            // | SET(value1,value2,value3,...) [CHARACTER SET charset_name]
            // [COLLATE collation_name]
            typeName = DataType.DataTypeName.SET;
            lexer.nextToken();
            match(PUNC_LEFT_PAREN);
            for (int i = 0; lexer.token() != PUNC_RIGHT_PAREN; ++i) {
                if (i > 0)
                    match(PUNC_COMMA);
                else
                    collectionVals = new ArrayList<Expression>(2);
                collectionVals.add(exprParser.expression());
            }
            match(PUNC_RIGHT_PAREN);
            if (lexer.token() == KW_CHARACTER) {
                lexer.nextToken();
                match(KW_SET);
                charSet = identifier();
            }
            if (lexer.token() == KW_COLLATE) {
                lexer.nextToken();
                collation = identifier();
            }
            break typeName;
        case IDENTIFIER:
            SpecialIdentifier si = specialIdentifiers.get(lexer.stringValueUppercase());
            if (si != null) {
                switch (si) {
                case BIT:
                    // BIT[(length)]
                    typeName = DataType.DataTypeName.BIT;
                    if (lexer.nextToken() == PUNC_LEFT_PAREN) {
                        lexer.nextToken();
                        length = exprParser.expression();
                        match(PUNC_RIGHT_PAREN);
                    }
                    break typeName;
                case DATE:
                    typeName = DataType.DataTypeName.DATE;
                    lexer.nextToken();
                    break typeName;
                case TIME:
                    typeName = DataType.DataTypeName.TIME;
                    lexer.nextToken();
                    break typeName;
                case TIMESTAMP:
                    typeName = DataType.DataTypeName.TIMESTAMP;
                    lexer.nextToken();
                    break typeName;
                case DATETIME:
                    typeName = DataType.DataTypeName.DATETIME;
                    lexer.nextToken();
                    break typeName;
                case YEAR:
                    typeName = DataType.DataTypeName.YEAR;
                    lexer.nextToken();
                    break typeName;
                case TEXT:
                    // | TEXT [BINARY] [CHARACTER SET charset_name] [COLLATE
                    // collation_name]
                    typeName = DataType.DataTypeName.TEXT;
                    if (lexer.nextToken() == KW_BINARY) {
                        lexer.nextToken();
                        binary = true;
                    }
                    if (lexer.token() == KW_CHARACTER) {
                        lexer.nextToken();
                        match(KW_SET);
                        charSet = identifier();
                    }
                    if (lexer.token() == KW_COLLATE) {
                        lexer.nextToken();
                        collation = identifier();
                    }
                    break typeName;
                case ENUM:
                    // | ENUM(value1,value2,value3,...) [CHARACTER SET
                    // charset_name] [COLLATE collation_name]
                    typeName = DataType.DataTypeName.ENUM;
                    lexer.nextToken();
                    match(PUNC_LEFT_PAREN);
                    for (int i = 0; lexer.token() != PUNC_RIGHT_PAREN; ++i) {
                        if (i > 0)
                            match(PUNC_COMMA);
                        else
                            collectionVals = new ArrayList<Expression>(2);
                        collectionVals.add(exprParser.expression());
                    }
                    match(PUNC_RIGHT_PAREN);
                    if (lexer.token() == KW_CHARACTER) {
                        lexer.nextToken();
                        match(KW_SET);
                        charSet = identifier();
                    }
                    if (lexer.token() == KW_COLLATE) {
                        lexer.nextToken();
                        collation = identifier();
                    }
                    break typeName;
                }
            }
        default:
            return null;
        }
        return new DataType(typeName, unsigned, zerofill, binary, length, decimals, charSet, collation, collectionVals);
    }

    // column_definition:
    // data_type [NOT NULL | NULL] [DEFAULT default_value]
    // [AUTO_INCREMENT] [UNIQUE [KEY] | [PRIMARY] KEY]
    // [COMMENT 'string']
    // [COLUMN_FORMAT {FIXED|DYNAMIC|DEFAULT}]
    // [reference_definition]
    private ColumnDefinition columnDefinition() throws SQLSyntaxErrorException {
        DataType dataType = dataType();
        boolean notNull = false;
        Expression defaultVal = null;
        boolean autoIncrement = false;
        ColumnDefinition.SpecialIndex sindex = null;
        ColumnDefinition.ColumnFormat format = null;
        LiteralString comment = null;
        if (lexer.token() == KW_NOT) {
            lexer.nextToken();
            match(LITERAL_NULL);
            notNull = true;
        } else if (lexer.token() == LITERAL_NULL) {
            lexer.nextToken();
        }
        if (lexer.token() == KW_DEFAULT) {
            lexer.nextToken();
            defaultVal = exprParser.expression();
            if (!(defaultVal instanceof Literal)) {
                throw new SQLSyntaxErrorException("default value of column must be a literal: " + defaultVal);
            }
        }
        if (lexer.token() == IDENTIFIER && "AUTO_INCREMENT".equals(lexer.stringValueUppercase())) {
            lexer.nextToken();
            autoIncrement = true;
        }
        switch (lexer.token()) {
        case KW_UNIQUE:
            if (lexer.nextToken() == KW_KEY) {
                lexer.nextToken();
            }
            sindex = ColumnDefinition.SpecialIndex.UNIQUE;
            break;
        case KW_PRIMARY:
            lexer.nextToken();
        case KW_KEY:
            match(KW_KEY);
            sindex = ColumnDefinition.SpecialIndex.PRIMARY;
            break;
        }
        if (lexer.token() == IDENTIFIER && "COMMENT".equals(lexer.stringValueUppercase())) {
            lexer.nextToken();
            comment = (LiteralString) exprParser.expression();
        }
        if (lexer.token() == IDENTIFIER && "COLUMN_FORMAT".equals(lexer.stringValueUppercase())) {
            switch (lexer.nextToken()) {
            case KW_DEFAULT:
                lexer.nextToken();
                format = ColumnDefinition.ColumnFormat.DEFAULT;
                break;
            case IDENTIFIER:
                SpecialIdentifier si = specialIdentifiers.get(lexer.stringValueUppercase());
                if (si != null) {
                    switch (si) {
                    case FIXED:
                        lexer.nextToken();
                        format = ColumnDefinition.ColumnFormat.FIXED;
                        break;
                    case DYNAMIC:
                        lexer.nextToken();
                        format = ColumnDefinition.ColumnFormat.DYNAMIC;
                        break;
                    }
                }
            }
        }
        return new ColumnDefinition(dataType, notNull, defaultVal, autoIncrement, sindex, comment, format);
    }

    private boolean tableOptions(TableOptions options) throws SQLSyntaxErrorException {
        boolean matched = false;
        for (int i = 0;; ++i) {
            boolean comma = false;
            if (i > 0 && lexer.token() == PUNC_COMMA) {
                lexer.nextToken();
                comma = true;
            }
            if (!tableOption(options)) {
                if (comma) {
                    lexer.addCacheToke(PUNC_COMMA);
                }
                break;
            } else {
                matched = true;
            }
        }
        return matched;
    }

    private boolean tableOption(TableOptions options) throws SQLSyntaxErrorException {
        Identifier id = null;
        Expression expr = null;
        os: switch (lexer.token()) {
        case KW_CHARACTER:
            lexer.nextToken();
            match(KW_SET);
            if (lexer.token() == OP_EQUALS) {
                lexer.nextToken();
            }
            id = identifier();
            options.setCharSet(id);
            break;
        case KW_COLLATE:
            lexer.nextToken();
            if (lexer.token() == OP_EQUALS) {
                lexer.nextToken();
            }
            id = identifier();
            options.setCollation(id);
            break;
        case KW_DEFAULT:
            // | [DEFAULT] CHARSET [=] charset_name { MySQL 5.1 legacy}
            // | [DEFAULT] CHARACTER SET [=] charset_name
            // | [DEFAULT] COLLATE [=] collation_name
            switch (lexer.nextToken()) {
            case KW_CHARACTER:
                lexer.nextToken();
                match(KW_SET);
                if (lexer.token() == OP_EQUALS) {
                    lexer.nextToken();
                }
                id = identifier();
                options.setCharSet(id);
                break os;
            case KW_COLLATE:
                lexer.nextToken();
                if (lexer.token() == OP_EQUALS) {
                    lexer.nextToken();
                }
                id = identifier();
                options.setCollation(id);
                break os;
            case IDENTIFIER:
                SpecialIdentifier si = specialIdentifiers.get(lexer.stringValueUppercase());
                if (si != null) {
                    switch (si) {
                    case CHARSET:
                        lexer.nextToken();
                        if (lexer.token() == OP_EQUALS) {
                            lexer.nextToken();
                        }
                        id = identifier();
                        options.setCharSet(id);
                        break os;
                    }
                }
            default:
                lexer.addCacheToke(KW_DEFAULT);
                return false;
            }
        case KW_INDEX:
            // | INDEX DIRECTORY [=] 'absolute path to directory'
            lexer.nextToken();
            if (lexer.token() == IDENTIFIER && "DIRECTORY".equals(lexer.stringValueUppercase())) {
                if (lexer.nextToken() == OP_EQUALS) {
                    lexer.nextToken();
                }
                options.setIndexDir((LiteralString) exprParser.expression());
                break;
            }
            lexer.addCacheToke(KW_INDEX);
            return true;
        case KW_UNION:
            // | UNION [=] (tbl_name[,tbl_name]...)
            if (lexer.nextToken() == OP_EQUALS) {
                lexer.nextToken();
            }
            match(PUNC_LEFT_PAREN);
            List<Identifier> union = new ArrayList<Identifier>(2);
            for (int j = 0; lexer.token() != PUNC_RIGHT_PAREN; ++j) {
                if (j > 0)
                    match(PUNC_COMMA);
                id = identifier();
                union.add(id);
            }
            match(PUNC_RIGHT_PAREN);
            options.setUnion(union);
            break os;
        case IDENTIFIER:
            SpecialIdentifier si = specialIdentifiers.get(lexer.stringValueUppercase());
            if (si != null) {
                switch (si) {
                case CHARSET:
                    // CHARSET [=] charset_name
                    lexer.nextToken();
                    if (lexer.token() == OP_EQUALS) {
                        lexer.nextToken();
                    }
                    id = identifier();
                    options.setCharSet(id);
                    break os;
                case ENGINE:
                    // ENGINE [=] engine_name
                    if (lexer.nextToken() == OP_EQUALS) {
                        lexer.nextToken();
                    }
                    id = identifier();
                    options.setEngine(id);
                    break os;
                case AUTO_INCREMENT:
                    // | AUTO_INCREMENT [=] value
                    if (lexer.nextToken() == OP_EQUALS) {
                        lexer.nextToken();
                    }
                    expr = exprParser.expression();
                    options.setAutoIncrement(expr);
                    break os;
                case AVG_ROW_LENGTH:
                    // | AVG_ROW_LENGTH [=] value
                    if (lexer.nextToken() == OP_EQUALS) {
                        lexer.nextToken();
                    }
                    expr = exprParser.expression();
                    options.setAvgRowLength(expr);
                    break os;
                case CHECKSUM:
                    // | CHECKSUM [=] {0 | 1}
                    if (lexer.nextToken() == OP_EQUALS) {
                        lexer.nextToken();
                    }
                    switch (lexer.token()) {
                    case LITERAL_BOOL_FALSE:
                        lexer.nextToken();
                        options.setCheckSum(false);
                    case LITERAL_BOOL_TRUE:
                        lexer.nextToken();
                        options.setCheckSum(true);
                        break;
                    case LITERAL_NUM_PURE_DIGIT:
                        int intVal = lexer.integerValue().intValue();
                        lexer.nextToken();
                        if (intVal == 0) {
                            options.setCheckSum(false);
                        } else {
                            options.setCheckSum(true);
                        }
                        break;
                    default:
                        throw new SQLSyntaxErrorException("table option of CHECKSUM error");
                    }
                    break os;
                case DELAY_KEY_WRITE:
                    // | DELAY_KEY_WRITE [=] {0 | 1}
                    if (lexer.nextToken() == OP_EQUALS) {
                        lexer.nextToken();
                    }
                    switch (lexer.token()) {
                    case LITERAL_BOOL_FALSE:
                        lexer.nextToken();
                        options.setDelayKeyWrite(false);
                    case LITERAL_BOOL_TRUE:
                        lexer.nextToken();
                        options.setDelayKeyWrite(true);
                        break;
                    case LITERAL_NUM_PURE_DIGIT:
                        int intVal = lexer.integerValue().intValue();
                        lexer.nextToken();
                        if (intVal == 0) {
                            options.setDelayKeyWrite(false);
                        } else {
                            options.setDelayKeyWrite(true);
                        }
                        break;
                    default:
                        throw new SQLSyntaxErrorException("table option of DELAY_KEY_WRITE error");
                    }
                    break os;
                case COMMENT:
                    // | COMMENT [=] 'string'
                    if (lexer.nextToken() == OP_EQUALS) {
                        lexer.nextToken();
                    }
                    options.setComment((LiteralString) exprParser.expression());
                    break os;
                case CONNECTION:
                    // | CONNECTION [=] 'connect_string'
                    if (lexer.nextToken() == OP_EQUALS) {
                        lexer.nextToken();
                    }
                    options.setConnection((LiteralString) exprParser.expression());
                    break os;
                case DATA:
                    // | DATA DIRECTORY [=] 'absolute path to directory'
                    lexer.nextToken();
                    matchIdentifier("DIRECTORY");
                    if (lexer.token() == OP_EQUALS) {
                        lexer.nextToken();
                    }
                    options.setDataDir((LiteralString) exprParser.expression());
                    break os;
                case INSERT_METHOD:
                    // | INSERT_METHOD [=] { NO | FIRST | LAST }
                    if (lexer.nextToken() == OP_EQUALS) {
                        lexer.nextToken();
                    }
                    switch (matchIdentifier("NO", "FIRST", "LAST")) {
                    case 0:
                        options.setInsertMethod(TableOptions.InsertMethod.NO);
                        break;
                    case 1:
                        options.setInsertMethod(TableOptions.InsertMethod.FIRST);
                        break;
                    case 2:
                        options.setInsertMethod(TableOptions.InsertMethod.LAST);
                        break;
                    }
                    break os;
                case KEY_BLOCK_SIZE:
                    // | KEY_BLOCK_SIZE [=] value
                    if (lexer.nextToken() == OP_EQUALS) {
                        lexer.nextToken();
                    }
                    options.setKeyBlockSize(exprParser.expression());
                    break os;
                case MAX_ROWS:
                    // | MAX_ROWS [=] value
                    if (lexer.nextToken() == OP_EQUALS) {
                        lexer.nextToken();
                    }
                    options.setMaxRows(exprParser.expression());
                    break os;
                case MIN_ROWS:
                    // | MIN_ROWS [=] value
                    if (lexer.nextToken() == OP_EQUALS) {
                        lexer.nextToken();
                    }
                    options.setMinRows(exprParser.expression());
                    break os;
                case PACK_KEYS:
                    // | PACK_KEYS [=] {0 | 1 | DEFAULT}
                    if (lexer.nextToken() == OP_EQUALS) {
                        lexer.nextToken();
                    }
                    switch (lexer.token()) {
                    case LITERAL_BOOL_FALSE:
                        lexer.nextToken();
                        options.setPackKeys(TableOptions.PackKeys.FALSE);
                        break;
                    case LITERAL_BOOL_TRUE:
                        lexer.nextToken();
                        options.setPackKeys(TableOptions.PackKeys.TRUE);
                        break;
                    case LITERAL_NUM_PURE_DIGIT:
                        int intVal = lexer.integerValue().intValue();
                        lexer.nextToken();
                        if (intVal == 0) {
                            options.setPackKeys(TableOptions.PackKeys.FALSE);
                        } else {
                            options.setPackKeys(TableOptions.PackKeys.TRUE);
                        }
                        break;
                    case KW_DEFAULT:
                        lexer.nextToken();
                        options.setPackKeys(TableOptions.PackKeys.DEFAULT);
                        break;
                    default:
                        throw new SQLSyntaxErrorException("table option of PACK_KEYS error");
                    }
                    break os;
                case PASSWORD:
                    // | PASSWORD [=] 'string'
                    if (lexer.nextToken() == OP_EQUALS) {
                        lexer.nextToken();
                    }
                    options.setPassword((LiteralString) exprParser.expression());
                    break os;
                case ROW_FORMAT:
                    // | ROW_FORMAT [=]
                    // {DEFAULT|DYNAMIC|FIXED|COMPRESSED|REDUNDANT|COMPACT}
                    if (lexer.nextToken() == OP_EQUALS) {
                        lexer.nextToken();
                    }
                    switch (lexer.token()) {
                    case KW_DEFAULT:
                        lexer.nextToken();
                        options.setRowFormat(TableOptions.RowFormat.DEFAULT);
                        break os;
                    case IDENTIFIER:
                        SpecialIdentifier sid = specialIdentifiers.get(lexer.stringValueUppercase());
                        if (sid != null) {
                            switch (sid) {
                            case DYNAMIC:
                                lexer.nextToken();
                                options.setRowFormat(TableOptions.RowFormat.DYNAMIC);
                                break os;
                            case FIXED:
                                lexer.nextToken();
                                options.setRowFormat(TableOptions.RowFormat.FIXED);
                                break os;
                            case COMPRESSED:
                                lexer.nextToken();
                                options.setRowFormat(TableOptions.RowFormat.COMPRESSED);
                                break os;
                            case REDUNDANT:
                                lexer.nextToken();
                                options.setRowFormat(TableOptions.RowFormat.REDUNDANT);
                                break os;
                            case COMPACT:
                                lexer.nextToken();
                                options.setRowFormat(TableOptions.RowFormat.COMPACT);
                                break os;
                            }
                        }
                    default:
                        throw new SQLSyntaxErrorException("table option of ROW_FORMAT error");
                    }
                }
            }
        default:
            return false;
        }
        return true;
    }
}
