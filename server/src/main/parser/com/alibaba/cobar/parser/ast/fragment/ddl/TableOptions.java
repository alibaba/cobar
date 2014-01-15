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
 * (created at 2012-8-14)
 */
package com.alibaba.cobar.parser.ast.fragment.ddl;

import java.util.List;

import com.alibaba.cobar.parser.ast.ASTNode;
import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.primary.Identifier;
import com.alibaba.cobar.parser.ast.expression.primary.literal.LiteralString;
import com.alibaba.cobar.parser.visitor.SQLASTVisitor;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class TableOptions implements ASTNode {
    public static enum InsertMethod {
        NO,
        FIRST,
        LAST
    }

    public static enum PackKeys {
        FALSE,
        TRUE,
        DEFAULT
    }

    public static enum RowFormat {
        DEFAULT,
        DYNAMIC,
        FIXED,
        COMPRESSED,
        REDUNDANT,
        COMPACT
    }

    private Identifier engine;
    private Expression autoIncrement;
    private Expression avgRowLength;
    private Identifier charSet;
    private Identifier collation;
    private Boolean checkSum;
    private LiteralString comment;
    private LiteralString connection;
    private LiteralString dataDir;
    private LiteralString indexDir;
    private Boolean delayKeyWrite;
    private InsertMethod insertMethod;
    private Expression keyBlockSize;
    private Expression maxRows;
    private Expression minRows;
    private PackKeys packKeys;
    private LiteralString password;
    private RowFormat rowFormat;
    private List<Identifier> union;

    // table_option:
    // ENGINE [=] engine_name
    // | AUTO_INCREMENT [=] value
    // | AVG_ROW_LENGTH [=] value
    // | [DEFAULT] CHARACTER SET [=] charset_name
    // | CHECKSUM [=] {0 | 1}
    // | [DEFAULT] COLLATE [=] collation_name
    // | COMMENT [=] 'string'
    // | CONNECTION [=] 'connect_string'
    // | DATA DIRECTORY [=] 'absolute path to directory'
    // | DELAY_KEY_WRITE [=] {0 | 1}
    // | INDEX DIRECTORY [=] 'absolute path to directory'
    // | INSERT_METHOD [=] { NO | FIRST | LAST }
    // | KEY_BLOCK_SIZE [=] value
    // | MAX_ROWS [=] value
    // | MIN_ROWS [=] value
    // | PACK_KEYS [=] {0 | 1 | DEFAULT}
    // | PASSWORD [=] 'string'
    // | ROW_FORMAT [=] {DEFAULT|DYNAMIC|FIXED|COMPRESSED|REDUNDANT|COMPACT}
    // | UNION [=] (tbl_name[,tbl_name]...)
    public TableOptions() {
    }

    public Identifier getEngine() {
        return engine;
    }

    public void setEngine(Identifier engine) {
        this.engine = engine;
    }

    public Expression getAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(Expression autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    public Expression getAvgRowLength() {
        return avgRowLength;
    }

    public void setAvgRowLength(Expression avgRowLength) {
        this.avgRowLength = avgRowLength;
    }

    public Identifier getCharSet() {
        return charSet;
    }

    public void setCharSet(Identifier charSet) {
        this.charSet = charSet;
    }

    public Identifier getCollation() {
        return collation;
    }

    public void setCollation(Identifier collation) {
        this.collation = collation;
    }

    public Boolean getCheckSum() {
        return checkSum;
    }

    public void setCheckSum(Boolean checkSum) {
        this.checkSum = checkSum;
    }

    public LiteralString getComment() {
        return comment;
    }

    public void setComment(LiteralString comment) {
        this.comment = comment;
    }

    public LiteralString getConnection() {
        return connection;
    }

    public void setConnection(LiteralString connection) {
        this.connection = connection;
    }

    public LiteralString getDataDir() {
        return dataDir;
    }

    public void setDataDir(LiteralString dataDir) {
        this.dataDir = dataDir;
    }

    public LiteralString getIndexDir() {
        return indexDir;
    }

    public void setIndexDir(LiteralString indexDir) {
        this.indexDir = indexDir;
    }

    public Boolean getDelayKeyWrite() {
        return delayKeyWrite;
    }

    public void setDelayKeyWrite(Boolean delayKeyWrite) {
        this.delayKeyWrite = delayKeyWrite;
    }

    public InsertMethod getInsertMethod() {
        return insertMethod;
    }

    public void setInsertMethod(InsertMethod insertMethod) {
        this.insertMethod = insertMethod;
    }

    public Expression getKeyBlockSize() {
        return keyBlockSize;
    }

    public void setKeyBlockSize(Expression keyBlockSize) {
        this.keyBlockSize = keyBlockSize;
    }

    public Expression getMaxRows() {
        return maxRows;
    }

    public void setMaxRows(Expression maxRows) {
        this.maxRows = maxRows;
    }

    public Expression getMinRows() {
        return minRows;
    }

    public void setMinRows(Expression minRows) {
        this.minRows = minRows;
    }

    public PackKeys getPackKeys() {
        return packKeys;
    }

    public void setPackKeys(PackKeys packKeys) {
        this.packKeys = packKeys;
    }

    public LiteralString getPassword() {
        return password;
    }

    public void setPassword(LiteralString password) {
        this.password = password;
    }

    public RowFormat getRowFormat() {
        return rowFormat;
    }

    public void setRowFormat(RowFormat rowFormat) {
        this.rowFormat = rowFormat;
    }

    public List<Identifier> getUnion() {
        return union;
    }

    public void setUnion(List<Identifier> union) {
        this.union = union;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
