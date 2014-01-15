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
package com.alibaba.cobar.parser.ast.stmt.ddl;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.cobar.parser.ast.ASTNode;
import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.primary.Identifier;
import com.alibaba.cobar.parser.ast.fragment.ddl.ColumnDefinition;
import com.alibaba.cobar.parser.ast.fragment.ddl.TableOptions;
import com.alibaba.cobar.parser.ast.fragment.ddl.index.IndexDefinition;
import com.alibaba.cobar.parser.util.Pair;
import com.alibaba.cobar.parser.visitor.SQLASTVisitor;

/**
 * NOT FULL AST: partition options, foreign key, ORDER BY not supported
 * 
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class DDLAlterTableStatement implements DDLStatement {
    public static interface AlterSpecification extends ASTNode {
    }

    // | ADD [COLUMN] col_name column_definition [FIRST | AFTER col_name ]
    public static class AddColumn implements AlterSpecification {
        private final Identifier columnName;
        private final ColumnDefinition columnDefine;
        private final boolean first;
        private final Identifier afterColumn;

        /**
         * @param columnName
         * @param columnDefine
         * @param afterColumn null means fisrt
         */
        public AddColumn(Identifier columnName, ColumnDefinition columnDefine, Identifier afterColumn) {
            this.columnName = columnName;
            this.columnDefine = columnDefine;
            this.afterColumn = afterColumn;
            this.first = afterColumn == null;
        }

        /**
         * @param columnName
         * @param columnDefine
         * @param afterColumn null means fisrt
         */
        public AddColumn(Identifier columnName, ColumnDefinition columnDefine) {
            this.columnName = columnName;
            this.columnDefine = columnDefine;
            this.afterColumn = null;
            this.first = false;
        }

        public Identifier getColumnName() {
            return columnName;
        }

        public ColumnDefinition getColumnDefine() {
            return columnDefine;
        }

        public boolean isFirst() {
            return first;
        }

        public Identifier getAfterColumn() {
            return afterColumn;
        }

        @Override
        public void accept(SQLASTVisitor visitor) {
            visitor.visit(this);
        }
    }

    // | ADD [COLUMN] (col_name column_definition,...)
    public static class AddColumns implements AlterSpecification {
        private final List<Pair<Identifier, ColumnDefinition>> columns;

        public AddColumns() {
            this.columns = new ArrayList<Pair<Identifier, ColumnDefinition>>(2);
        }

        public AddColumns addColumn(Identifier name, ColumnDefinition colDef) {
            this.columns.add(new Pair<Identifier, ColumnDefinition>(name, colDef));
            return this;
        }

        public List<Pair<Identifier, ColumnDefinition>> getColumns() {
            return columns;
        }

        @Override
        public void accept(SQLASTVisitor visitor) {
            visitor.visit(this);
        }
    }

    // | ADD {INDEX|KEY} [index_name] [index_type] (index_col_name,...)
    // [index_option] ...
    public static class AddIndex implements AlterSpecification {
        private final Identifier indexName;
        private final IndexDefinition indexDef;

        /**
         * @param indexName
         * @param indexType
         */
        public AddIndex(Identifier indexName, IndexDefinition indexDef) {
            this.indexName = indexName;
            this.indexDef = indexDef;
        }

        public Identifier getIndexName() {
            return indexName;
        }

        public IndexDefinition getIndexDef() {
            return indexDef;
        }

        @Override
        public void accept(SQLASTVisitor visitor) {
            visitor.visit(this);
        }
    }

    // | ADD [CONSTRAINT [symbol]] PRIMARY KEY [index_type] (index_col_name,...)
    // [index_option] ...
    public static class AddPrimaryKey implements AlterSpecification {
        private final IndexDefinition indexDef;

        public AddPrimaryKey(IndexDefinition indexDef) {
            this.indexDef = indexDef;
        }

        public IndexDefinition getIndexDef() {
            return indexDef;
        }

        @Override
        public void accept(SQLASTVisitor visitor) {
            visitor.visit(this);
        }
    }

    // | ADD [CONSTRAINT [symbol]] UNIQUE [INDEX|KEY] [index_name] [index_type]
    // (index_col_name,...) [index_option] ...
    public static class AddUniqueKey implements AlterSpecification {
        private final Identifier indexName;
        private final IndexDefinition indexDef;

        public AddUniqueKey(Identifier indexName, IndexDefinition indexDef) {
            this.indexDef = indexDef;
            this.indexName = indexName;
        }

        public Identifier getIndexName() {
            return indexName;
        }

        public IndexDefinition getIndexDef() {
            return indexDef;
        }

        @Override
        public void accept(SQLASTVisitor visitor) {
            visitor.visit(this);

        }
    }

    // | ADD FULLTEXT [INDEX|KEY] [index_name] (index_col_name,...)
    // [index_option] ...
    public static class AddFullTextIndex implements AlterSpecification {
        private final Identifier indexName;
        private final IndexDefinition indexDef;

        public AddFullTextIndex(Identifier indexName, IndexDefinition indexDef) {
            this.indexDef = indexDef;
            this.indexName = indexName;
        }

        public Identifier getIndexName() {
            return indexName;
        }

        public IndexDefinition getIndexDef() {
            return indexDef;
        }

        @Override
        public void accept(SQLASTVisitor visitor) {
            visitor.visit(this);
        }
    }

    // | ADD SPATIAL [INDEX|KEY] [index_name] (index_col_name,...)
    // [index_option] ...
    public static class AddSpatialIndex implements AlterSpecification {
        private final Identifier indexName;
        private final IndexDefinition indexDef;

        public AddSpatialIndex(Identifier indexName, IndexDefinition indexDef) {
            this.indexDef = indexDef;
            this.indexName = indexName;
        }

        public Identifier getIndexName() {
            return indexName;
        }

        public IndexDefinition getIndexDef() {
            return indexDef;
        }

        @Override
        public void accept(SQLASTVisitor visitor) {
            visitor.visit(this);
        }
    }

    // | ALTER [COLUMN] col_name {SET DEFAULT literal | DROP DEFAULT}
    public static class AlterColumnDefaultVal implements AlterSpecification {
        private final Identifier columnName;
        private final Expression defaultValue;
        private final boolean dropDefault;

        /**
         * @param columnName
         * @param defaultValue
         */
        public AlterColumnDefaultVal(Identifier columnName, Expression defaultValue) {
            this.columnName = columnName;
            this.defaultValue = defaultValue;
            this.dropDefault = false;
        }

        /**
         * DROP DEFAULT
         * 
         * @param columnName
         */
        public AlterColumnDefaultVal(Identifier columnName) {
            this.columnName = columnName;
            this.defaultValue = null;
            this.dropDefault = true;
        }

        public Identifier getColumnName() {
            return columnName;
        }

        public Expression getDefaultValue() {
            return defaultValue;
        }

        public boolean isDropDefault() {
            return dropDefault;
        }

        @Override
        public void accept(SQLASTVisitor visitor) {
            visitor.visit(this);
        }
    }

    // | CHANGE [COLUMN] old_col_name new_col_name column_definition
    // [FIRST|AFTER col_name]
    public static class ChangeColumn implements AlterSpecification {
        private final Identifier oldName;
        private final Identifier newName;
        private final ColumnDefinition colDef;
        private final boolean first;
        private final Identifier afterColumn;

        public ChangeColumn(Identifier oldName, Identifier newName, ColumnDefinition colDef, Identifier afterColumn) {
            this.oldName = oldName;
            this.newName = newName;
            this.colDef = colDef;
            this.first = afterColumn == null;
            this.afterColumn = afterColumn;
        }

        /**
         * without column position specification
         */
        public ChangeColumn(Identifier oldName, Identifier newName, ColumnDefinition colDef) {
            this.oldName = oldName;
            this.newName = newName;
            this.colDef = colDef;
            this.first = false;
            this.afterColumn = null;
        }

        public Identifier getOldName() {
            return oldName;
        }

        public Identifier getNewName() {
            return newName;
        }

        public ColumnDefinition getColDef() {
            return colDef;
        }

        public boolean isFirst() {
            return first;
        }

        public Identifier getAfterColumn() {
            return afterColumn;
        }

        @Override
        public void accept(SQLASTVisitor visitor) {
            visitor.visit(this);
        }
    }

    // | MODIFY [COLUMN] col_name column_definition [FIRST | AFTER col_name]
    public static class ModifyColumn implements AlterSpecification {
        private final Identifier colName;
        private final ColumnDefinition colDef;
        private final boolean first;
        private final Identifier afterColumn;

        public ModifyColumn(Identifier colName, ColumnDefinition colDef, Identifier afterColumn) {
            this.colName = colName;
            this.colDef = colDef;
            this.first = afterColumn == null;
            this.afterColumn = afterColumn;
        }

        /**
         * without column position specification
         */
        public ModifyColumn(Identifier colName, ColumnDefinition colDef) {
            this.colName = colName;
            this.colDef = colDef;
            this.first = false;
            this.afterColumn = null;
        }

        public Identifier getColName() {
            return colName;
        }

        public ColumnDefinition getColDef() {
            return colDef;
        }

        public boolean isFirst() {
            return first;
        }

        public Identifier getAfterColumn() {
            return afterColumn;
        }

        @Override
        public void accept(SQLASTVisitor visitor) {
            visitor.visit(this);
        }
    }

    // | DROP [COLUMN] col_name
    public static class DropColumn implements AlterSpecification {
        private final Identifier colName;

        public DropColumn(Identifier colName) {
            this.colName = colName;
        }

        public Identifier getColName() {
            return colName;
        }

        @Override
        public void accept(SQLASTVisitor visitor) {
            visitor.visit(this);
        }
    }

    // | DROP PRIMARY KEY
    public static class DropPrimaryKey implements AlterSpecification {
        @Override
        public void accept(SQLASTVisitor visitor) {
            visitor.visit(this);
        }
    }

    // | DROP {INDEX|KEY} index_name
    public static class DropIndex implements AlterSpecification {
        private final Identifier indexName;

        public DropIndex(Identifier indexName) {
            this.indexName = indexName;
        }

        public Identifier getIndexName() {
            return indexName;
        }

        @Override
        public void accept(SQLASTVisitor visitor) {
            visitor.visit(this);
        }
    }

    // | DISABLE KEYS
    // | ENABLE KEYS
    // | RENAME [TO] new_tbl_name
    // | ORDER BY col_name [, col_name] ...
    // | CONVERT TO CHARACTER SET charset_name [COLLATE collation_name]
    // | DISCARD TABLESPACE
    // | IMPORT TABLESPACE
    // /// | ADD [CONSTRAINT [symbol]] FOREIGN KEY [index_name]
    // (index_col_name,...) reference_definition
    // /// | DROP FOREIGN KEY fk_symbol
    // /// | ADD PARTITION (partition_definition)
    // /// | DROP PARTITION partition_names
    // /// | TRUNCATE PARTITION {partition_names | ALL }
    // /// | COALESCE PARTITION number
    // /// | REORGANIZE PARTITION partition_names INTO (partition_definitions)
    // /// | ANALYZE PARTITION {partition_names | ALL }
    // /// | CHECK PARTITION {partition_names | ALL }
    // /// | OPTIMIZE PARTITION {partition_names | ALL }
    // /// | REBUILD PARTITION {partition_names | ALL }
    // /// | REPAIR PARTITION {partition_names | ALL }
    // /// | REMOVE PARTITIONING

    // ADD, ALTER, DROP, and CHANGE can be multiple

    private final boolean ignore;
    private final Identifier table;
    private TableOptions tableOptions;
    private final List<AlterSpecification> alters;
    private boolean disableKeys;
    private boolean enableKeys;
    private boolean discardTableSpace;
    private boolean importTableSpace;
    private Identifier renameTo;
    /** charsetName -> collate */
    private Pair<Identifier, Identifier> convertCharset;

    public DDLAlterTableStatement(boolean ignore, Identifier table) {
        this.ignore = ignore;
        this.table = table;
        this.alters = new ArrayList<DDLAlterTableStatement.AlterSpecification>(1);
    }

    public DDLAlterTableStatement addAlterSpecification(AlterSpecification alter) {
        alters.add(alter);
        return this;
    }

    public boolean isDisableKeys() {
        return disableKeys;
    }

    public void setDisableKeys(boolean disableKeys) {
        this.disableKeys = disableKeys;
    }

    public boolean isEnableKeys() {
        return enableKeys;
    }

    public void setEnableKeys(boolean enableKeys) {
        this.enableKeys = enableKeys;
    }

    public boolean isDiscardTableSpace() {
        return discardTableSpace;
    }

    public void setDiscardTableSpace(boolean discardTableSpace) {
        this.discardTableSpace = discardTableSpace;
    }

    public boolean isImportTableSpace() {
        return importTableSpace;
    }

    public void setImportTableSpace(boolean importTableSpace) {
        this.importTableSpace = importTableSpace;
    }

    public Identifier getRenameTo() {
        return renameTo;
    }

    public void setRenameTo(Identifier renameTo) {
        this.renameTo = renameTo;
    }

    public Pair<Identifier, Identifier> getConvertCharset() {
        return convertCharset;
    }

    public void setConvertCharset(Pair<Identifier, Identifier> convertCharset) {
        this.convertCharset = convertCharset;
    }

    public List<AlterSpecification> getAlters() {
        return alters;
    }

    public void setTableOptions(TableOptions tableOptions) {
        this.tableOptions = tableOptions;
    }

    public TableOptions getTableOptions() {
        return tableOptions;
    }

    public boolean isIgnore() {
        return ignore;
    }

    public Identifier getTable() {
        return table;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

}
