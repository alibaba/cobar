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
package com.alibaba.cobar.parser.recognizer.mysql;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public enum MySQLToken {
    EOF,
    PLACE_HOLDER,
    IDENTIFIER,
    SYS_VAR,
    USR_VAR,

    /** number composed purely of digit */
    LITERAL_NUM_PURE_DIGIT,
    /** number composed of digit mixed with <code>.</code> or <code>e</code> */
    LITERAL_NUM_MIX_DIGIT,
    LITERAL_HEX,
    LITERAL_BIT,
    LITERAL_CHARS,
    LITERAL_NCHARS,
    LITERAL_NULL,
    LITERAL_BOOL_TRUE,
    LITERAL_BOOL_FALSE,

    /** ? */
    QUESTION_MARK,

    /** ( */
    PUNC_LEFT_PAREN,
    /** ) */
    PUNC_RIGHT_PAREN,
    /** { */
    PUNC_LEFT_BRACE,
    /** } */
    PUNC_RIGHT_BRACE,
    /** [ */
    PUNC_LEFT_BRACKET,
    /** ] */
    PUNC_RIGHT_BRACKET,
    /** ; */
    PUNC_SEMICOLON,
    /** , */
    PUNC_COMMA,
    /** , */
    PUNC_DOT,
    /** : */
    PUNC_COLON,
    /** <code>*</code><code>/</code> */
    PUNC_C_STYLE_COMMENT_END,

    // /** &#64; */
    // OP_AT,
    /** = */
    OP_EQUALS,
    /** > */
    OP_GREATER_THAN,
    /** < */
    OP_LESS_THAN,
    /** ! */
    OP_EXCLAMATION,
    /** ~ */
    OP_TILDE,
    /** + */
    OP_PLUS,
    /** - */
    OP_MINUS,
    /** * */
    OP_ASTERISK,
    /** / */
    OP_SLASH,
    /** & */
    OP_AMPERSAND,
    /** | */
    OP_VERTICAL_BAR,
    /** ^ */
    OP_CARET,
    /** % */
    OP_PERCENT,
    /** := */
    OP_ASSIGN,
    /** <= */
    OP_LESS_OR_EQUALS,
    /** <> */
    OP_LESS_OR_GREATER,
    /** >= */
    OP_GREATER_OR_EQUALS,
    /** != */
    OP_NOT_EQUALS,
    /** && */
    OP_LOGICAL_AND,
    /** || */
    OP_LOGICAL_OR,
    /** << */
    OP_LEFT_SHIFT,
    /** >> */
    OP_RIGHT_SHIFT,
    /** <=> */
    OP_NULL_SAFE_EQUALS,

    KW_ACCESSIBLE,
    KW_ADD,
    KW_ALL,
    KW_ALTER,
    KW_ANALYZE,
    KW_AND,
    KW_AS,
    KW_ASC,
    KW_ASENSITIVE,
    KW_BEFORE,
    KW_BETWEEN,
    KW_BIGINT,
    KW_BINARY,
    KW_BLOB,
    KW_BOTH,
    KW_BY,
    KW_CALL,
    KW_CASCADE,
    KW_CASE,
    KW_CHANGE,
    KW_CHAR,
    KW_CHARACTER,
    KW_CHECK,
    KW_COLLATE,
    KW_COLUMN,
    KW_CONDITION,
    KW_CONSTRAINT,
    KW_CONTINUE,
    KW_CONVERT,
    KW_CREATE,
    KW_CROSS,
    KW_CURRENT_DATE,
    KW_CURRENT_TIME,
    KW_CURRENT_TIMESTAMP,
    KW_CURRENT_USER,
    KW_CURSOR,
    KW_DATABASE,
    KW_DATABASES,
    KW_DAY_HOUR,
    KW_DAY_MICROSECOND,
    KW_DAY_MINUTE,
    KW_DAY_SECOND,
    KW_DEC,
    KW_DECIMAL,
    KW_DECLARE,
    KW_DEFAULT,
    KW_DELAYED,
    KW_DELETE,
    KW_DESC,
    KW_DESCRIBE,
    KW_DETERMINISTIC,
    KW_DISTINCT,
    KW_DISTINCTROW,
    KW_DIV,
    KW_DOUBLE,
    KW_DROP,
    KW_DUAL,
    KW_EACH,
    KW_ELSE,
    KW_ELSEIF,
    KW_ENCLOSED,
    KW_ESCAPED,
    KW_EXISTS,
    KW_EXIT,
    KW_EXPLAIN,
    KW_FETCH,
    KW_FLOAT,
    KW_FLOAT4,
    KW_FLOAT8,
    KW_FOR,
    KW_FORCE,
    KW_FOREIGN,
    KW_FROM,
    KW_FULLTEXT,
    KW_GENERAL,
    KW_GRANT,
    KW_GROUP,
    KW_HAVING,
    KW_HIGH_PRIORITY,
    KW_HOUR_MICROSECOND,
    KW_HOUR_MINUTE,
    KW_HOUR_SECOND,
    KW_IF,
    KW_IGNORE,
    KW_IGNORE_SERVER_IDS,
    KW_IN,
    KW_INDEX,
    KW_INFILE,
    KW_INNER,
    KW_INOUT,
    KW_INSENSITIVE,
    KW_INSERT,
    KW_INT,
    KW_INT1,
    KW_INT2,
    KW_INT3,
    KW_INT4,
    KW_INT8,
    KW_INTEGER,
    KW_INTERVAL,
    KW_INTO,
    KW_IS,
    KW_ITERATE,
    KW_JOIN,
    KW_KEY,
    KW_KEYS,
    KW_KILL,
    KW_LEADING,
    KW_LEAVE,
    KW_LEFT,
    KW_LIKE,
    KW_LIMIT,
    KW_LINEAR,
    KW_LINES,
    KW_LOAD,
    KW_LOCALTIME,
    KW_LOCALTIMESTAMP,
    KW_LOCK,
    KW_LONG,
    KW_LONGBLOB,
    KW_LONGTEXT,
    KW_LOOP,
    KW_LOW_PRIORITY,
    KW_MASTER_HEARTBEAT_PERIOD,
    KW_MASTER_SSL_VERIFY_SERVER_CERT,
    KW_MATCH,
    KW_MAXVALUE,
    KW_MEDIUMBLOB,
    KW_MEDIUMINT,
    KW_MEDIUMTEXT,
    KW_MIDDLEINT,
    KW_MINUTE_MICROSECOND,
    KW_MINUTE_SECOND,
    KW_MOD,
    KW_MODIFIES,
    KW_NATURAL,
    KW_NOT,
    KW_NO_WRITE_TO_BINLOG,
    KW_NUMERIC,
    KW_ON,
    KW_OPTIMIZE,
    KW_OPTION,
    KW_OPTIONALLY,
    KW_OR,
    KW_ORDER,
    KW_OUT,
    KW_OUTER,
    KW_OUTFILE,
    KW_PRECISION,
    KW_PRIMARY,
    KW_PROCEDURE,
    KW_PURGE,
    KW_RANGE,
    KW_READ,
    KW_READS,
    KW_READ_WRITE,
    KW_REAL,
    KW_REFERENCES,
    KW_REGEXP,
    KW_RELEASE,
    KW_RENAME,
    KW_REPEAT,
    KW_REPLACE,
    KW_REQUIRE,
    KW_RESIGNAL,
    KW_RESTRICT,
    KW_RETURN,
    KW_REVOKE,
    KW_RIGHT,
    KW_RLIKE,
    KW_SCHEMA,
    KW_SCHEMAS,
    KW_SECOND_MICROSECOND,
    KW_SELECT,
    KW_SENSITIVE,
    KW_SEPARATOR,
    KW_SET,
    KW_SHOW,
    KW_SIGNAL,
    KW_SLOW,
    KW_SMALLINT,
    KW_SPATIAL,
    KW_SPECIFIC,
    KW_SQL,
    KW_SQLEXCEPTION,
    KW_SQLSTATE,
    KW_SQLWARNING,
    KW_SQL_BIG_RESULT,
    KW_SQL_CALC_FOUND_ROWS,
    KW_SQL_SMALL_RESULT,
    KW_SSL,
    KW_STARTING,
    KW_STRAIGHT_JOIN,
    KW_TABLE,
    KW_TERMINATED,
    KW_THEN,
    KW_TINYBLOB,
    KW_TINYINT,
    KW_TINYTEXT,
    KW_TO,
    KW_TRAILING,
    KW_TRIGGER,
    KW_UNDO,
    KW_UNION,
    KW_UNIQUE,
    KW_UNLOCK,
    KW_UNSIGNED,
    KW_UPDATE,
    KW_USAGE,
    KW_USE,
    KW_USING,
    KW_UTC_DATE,
    KW_UTC_TIME,
    KW_UTC_TIMESTAMP,
    KW_VALUES,
    KW_VARBINARY,
    KW_VARCHAR,
    KW_VARCHARACTER,
    KW_VARYING,
    KW_WHEN,
    KW_WHERE,
    KW_WHILE,
    KW_WITH,
    KW_WRITE,
    KW_XOR,
    KW_YEAR_MONTH,
    KW_ZEROFILL;

    public static String keyWordToString(MySQLToken token) {
        switch (token) {
        case KW_ACCESSIBLE:
            return "ACCESSIBLE";
        case KW_ADD:
            return "ADD";
        case KW_ALL:
            return "ALL";
        case KW_ALTER:
            return "ALTER";
        case KW_ANALYZE:
            return "ANALYZE";
        case KW_AND:
            return "AND";
        case KW_AS:
            return "AS";
        case KW_ASC:
            return "ASC";
        case KW_ASENSITIVE:
            return "ASENSITIVE";
        case KW_BEFORE:
            return "BEFORE";
        case KW_BETWEEN:
            return "BETWEEN";
        case KW_BIGINT:
            return "BIGINT";
        case KW_BINARY:
            return "BINARY";
        case KW_BLOB:
            return "BLOB";
        case KW_BOTH:
            return "BOTH";
        case KW_BY:
            return "BY";
        case KW_CALL:
            return "CALL";
        case KW_CASCADE:
            return "CASCADE";
        case KW_CASE:
            return "CASE";
        case KW_CHANGE:
            return "CHANGE";
        case KW_CHAR:
            return "CHAR";
        case KW_CHARACTER:
            return "CHARACTER";
        case KW_CHECK:
            return "CHECK";
        case KW_COLLATE:
            return "COLLATE";
        case KW_COLUMN:
            return "COLUMN";
        case KW_CONDITION:
            return "CONDITION";
        case KW_CONSTRAINT:
            return "CONSTRAINT";
        case KW_CONTINUE:
            return "CONTINUE";
        case KW_CONVERT:
            return "CONVERT";
        case KW_CREATE:
            return "CREATE";
        case KW_CROSS:
            return "CROSS";
        case KW_CURRENT_DATE:
            return "CURRENT_DATE";
        case KW_CURRENT_TIME:
            return "CURRENT_TIME";
        case KW_CURRENT_TIMESTAMP:
            return "CURRENT_TIMESTAMP";
        case KW_CURRENT_USER:
            return "CURRENT_USER";
        case KW_CURSOR:
            return "CURSOR";
        case KW_DATABASE:
            return "DATABASE";
        case KW_DATABASES:
            return "DATABASES";
        case KW_DAY_HOUR:
            return "DAY_HOUR";
        case KW_DAY_MICROSECOND:
            return "DAY_MICROSECOND";
        case KW_DAY_MINUTE:
            return "DAY_MINUTE";
        case KW_DAY_SECOND:
            return "DAY_SECOND";
        case KW_DEC:
            return "DEC";
        case KW_DECIMAL:
            return "DECIMAL";
        case KW_DECLARE:
            return "DECLARE";
        case KW_DEFAULT:
            return "DEFAULT";
        case KW_DELAYED:
            return "DELAYED";
        case KW_DELETE:
            return "DELETE";
        case KW_DESC:
            return "DESC";
        case KW_DESCRIBE:
            return "DESCRIBE";
        case KW_DETERMINISTIC:
            return "DETERMINISTIC";
        case KW_DISTINCT:
            return "DISTINCT";
        case KW_DISTINCTROW:
            return "DISTINCTROW";
        case KW_DIV:
            return "DIV";
        case KW_DOUBLE:
            return "DOUBLE";
        case KW_DROP:
            return "DROP";
        case KW_DUAL:
            return "DUAL";
        case KW_EACH:
            return "EACH";
        case KW_ELSE:
            return "ELSE";
        case KW_ELSEIF:
            return "ELSEIF";
        case KW_ENCLOSED:
            return "ENCLOSED";
        case KW_ESCAPED:
            return "ESCAPED";
        case KW_EXISTS:
            return "EXISTS";
        case KW_EXIT:
            return "EXIT";
        case KW_EXPLAIN:
            return "EXPLAIN";
        case KW_FETCH:
            return "FETCH";
        case KW_FLOAT:
            return "FLOAT";
        case KW_FLOAT4:
            return "FLOAT4";
        case KW_FLOAT8:
            return "FLOAT8";
        case KW_FOR:
            return "FOR";
        case KW_FORCE:
            return "FORCE";
        case KW_FOREIGN:
            return "FOREIGN";
        case KW_FROM:
            return "FROM";
        case KW_FULLTEXT:
            return "FULLTEXT";
        case KW_GENERAL:
            return "GENERAL";
        case KW_GRANT:
            return "GRANT";
        case KW_GROUP:
            return "GROUP";
        case KW_HAVING:
            return "HAVING";
        case KW_HIGH_PRIORITY:
            return "HIGH_PRIORITY";
        case KW_HOUR_MICROSECOND:
            return "HOUR_MICROSECOND";
        case KW_HOUR_MINUTE:
            return "HOUR_MINUTE";
        case KW_HOUR_SECOND:
            return "HOUR_SECOND";
        case KW_IF:
            return "IF";
        case KW_IGNORE:
            return "IGNORE";
        case KW_IGNORE_SERVER_IDS:
            return "IGNORE_SERVER_IDS";
        case KW_IN:
            return "IN";
        case KW_INDEX:
            return "INDEX";
        case KW_INFILE:
            return "INFILE";
        case KW_INNER:
            return "INNER";
        case KW_INOUT:
            return "INOUT";
        case KW_INSENSITIVE:
            return "INSENSITIVE";
        case KW_INSERT:
            return "INSERT";
        case KW_INT:
            return "INT";
        case KW_INT1:
            return "INT1";
        case KW_INT2:
            return "INT2";
        case KW_INT3:
            return "INT3";
        case KW_INT4:
            return "INT4";
        case KW_INT8:
            return "INT8";
        case KW_INTEGER:
            return "INTEGER";
        case KW_INTERVAL:
            return "INTERVAL";
        case KW_INTO:
            return "INTO";
        case KW_IS:
            return "IS";
        case KW_ITERATE:
            return "ITERATE";
        case KW_JOIN:
            return "JOIN";
        case KW_KEY:
            return "KEY";
        case KW_KEYS:
            return "KEYS";
        case KW_KILL:
            return "KILL";
        case KW_LEADING:
            return "LEADING";
        case KW_LEAVE:
            return "LEAVE";
        case KW_LEFT:
            return "LEFT";
        case KW_LIKE:
            return "LIKE";
        case KW_LIMIT:
            return "LIMIT";
        case KW_LINEAR:
            return "LINEAR";
        case KW_LINES:
            return "LINES";
        case KW_LOAD:
            return "LOAD";
        case KW_LOCALTIME:
            return "LOCALTIME";
        case KW_LOCALTIMESTAMP:
            return "LOCALTIMESTAMP";
        case KW_LOCK:
            return "LOCK";
        case KW_LONG:
            return "LONG";
        case KW_LONGBLOB:
            return "LONGBLOB";
        case KW_LONGTEXT:
            return "LONGTEXT";
        case KW_LOOP:
            return "LOOP";
        case KW_LOW_PRIORITY:
            return "LOW_PRIORITY";
        case KW_MASTER_HEARTBEAT_PERIOD:
            return "MASTER_HEARTBEAT_PERIOD";
        case KW_MASTER_SSL_VERIFY_SERVER_CERT:
            return "MASTER_SSL_VERIFY_SERVER_CERT";
        case KW_MATCH:
            return "MATCH";
        case KW_MAXVALUE:
            return "MAXVALUE";
        case KW_MEDIUMBLOB:
            return "MEDIUMBLOB";
        case KW_MEDIUMINT:
            return "MEDIUMINT";
        case KW_MEDIUMTEXT:
            return "MEDIUMTEXT";
        case KW_MIDDLEINT:
            return "MIDDLEINT";
        case KW_MINUTE_MICROSECOND:
            return "MINUTE_MICROSECOND";
        case KW_MINUTE_SECOND:
            return "MINUTE_SECOND";
        case KW_MOD:
            return "MOD";
        case KW_MODIFIES:
            return "MODIFIES";
        case KW_NATURAL:
            return "NATURAL";
        case KW_NOT:
            return "NOT";
        case KW_NO_WRITE_TO_BINLOG:
            return "NO_WRITE_TO_BINLOG";
        case KW_NUMERIC:
            return "NUMERIC";
        case KW_ON:
            return "ON";
        case KW_OPTIMIZE:
            return "OPTIMIZE";
        case KW_OPTION:
            return "OPTION";
        case KW_OPTIONALLY:
            return "OPTIONALLY";
        case KW_OR:
            return "OR";
        case KW_ORDER:
            return "ORDER";
        case KW_OUT:
            return "OUT";
        case KW_OUTER:
            return "OUTER";
        case KW_OUTFILE:
            return "OUTFILE";
        case KW_PRECISION:
            return "PRECISION";
        case KW_PRIMARY:
            return "PRIMARY";
        case KW_PROCEDURE:
            return "PROCEDURE";
        case KW_PURGE:
            return "PURGE";
        case KW_RANGE:
            return "RANGE";
        case KW_READ:
            return "READ";
        case KW_READS:
            return "READS";
        case KW_READ_WRITE:
            return "READ_WRITE";
        case KW_REAL:
            return "REAL";
        case KW_REFERENCES:
            return "REFERENCES";
        case KW_REGEXP:
            return "REGEXP";
        case KW_RELEASE:
            return "RELEASE";
        case KW_RENAME:
            return "RENAME";
        case KW_REPEAT:
            return "REPEAT";
        case KW_REPLACE:
            return "REPLACE";
        case KW_REQUIRE:
            return "REQUIRE";
        case KW_RESIGNAL:
            return "RESIGNAL";
        case KW_RESTRICT:
            return "RESTRICT";
        case KW_RETURN:
            return "RETURN";
        case KW_REVOKE:
            return "REVOKE";
        case KW_RIGHT:
            return "RIGHT";
        case KW_RLIKE:
            return "RLIKE";
        case KW_SCHEMA:
            return "SCHEMA";
        case KW_SCHEMAS:
            return "SCHEMAS";
        case KW_SECOND_MICROSECOND:
            return "SECOND_MICROSECOND";
        case KW_SELECT:
            return "SELECT";
        case KW_SENSITIVE:
            return "SENSITIVE";
        case KW_SEPARATOR:
            return "SEPARATOR";
        case KW_SET:
            return "SET";
        case KW_SHOW:
            return "SHOW";
        case KW_SIGNAL:
            return "SIGNAL";
        case KW_SLOW:
            return "SLOW";
        case KW_SMALLINT:
            return "SMALLINT";
        case KW_SPATIAL:
            return "SPATIAL";
        case KW_SPECIFIC:
            return "SPECIFIC";
        case KW_SQL:
            return "SQL";
        case KW_SQLEXCEPTION:
            return "SQLEXCEPTION";
        case KW_SQLSTATE:
            return "SQLSTATE";
        case KW_SQLWARNING:
            return "SQLWARNING";
        case KW_SQL_BIG_RESULT:
            return "SQL_BIG_RESULT";
        case KW_SQL_CALC_FOUND_ROWS:
            return "SQL_CALC_FOUND_ROWS";
        case KW_SQL_SMALL_RESULT:
            return "SQL_SMALL_RESULT";
        case KW_SSL:
            return "SSL";
        case KW_STARTING:
            return "STARTING";
        case KW_STRAIGHT_JOIN:
            return "STRAIGHT_JOIN";
        case KW_TABLE:
            return "TABLE";
        case KW_TERMINATED:
            return "TERMINATED";
        case KW_THEN:
            return "THEN";
        case KW_TINYBLOB:
            return "TINYBLOB";
        case KW_TINYINT:
            return "TINYINT";
        case KW_TINYTEXT:
            return "TINYTEXT";
        case KW_TO:
            return "TO";
        case KW_TRAILING:
            return "TRAILING";
        case KW_TRIGGER:
            return "TRIGGER";
        case KW_UNDO:
            return "UNDO";
        case KW_UNION:
            return "UNION";
        case KW_UNIQUE:
            return "UNIQUE";
        case KW_UNLOCK:
            return "UNLOCK";
        case KW_UNSIGNED:
            return "UNSIGNED";
        case KW_UPDATE:
            return "UPDATE";
        case KW_USAGE:
            return "USAGE";
        case KW_USE:
            return "USE";
        case KW_USING:
            return "USING";
        case KW_UTC_DATE:
            return "UTC_DATE";
        case KW_UTC_TIME:
            return "UTC_TIME";
        case KW_UTC_TIMESTAMP:
            return "UTC_TIMESTAMP";
        case KW_VALUES:
            return "VALUES";
        case KW_VARBINARY:
            return "VARBINARY";
        case KW_VARCHAR:
            return "VARCHAR";
        case KW_VARCHARACTER:
            return "VARCHARACTER";
        case KW_VARYING:
            return "VARYING";
        case KW_WHEN:
            return "WHEN";
        case KW_WHERE:
            return "WHERE";
        case KW_WHILE:
            return "WHILE";
        case KW_WITH:
            return "WITH";
        case KW_WRITE:
            return "WRITE";
        case KW_XOR:
            return "XOR";
        case KW_YEAR_MONTH:
            return "YEAR_MONTH";
        case KW_ZEROFILL:
            return "ZEROFILL";
        default:
            throw new IllegalArgumentException("token is not keyword: " + token);
        }
    }
}
