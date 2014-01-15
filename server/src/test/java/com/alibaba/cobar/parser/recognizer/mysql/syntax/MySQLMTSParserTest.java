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
 * (created at 2011-9-12)
 */
package com.alibaba.cobar.parser.recognizer.mysql.syntax;

import java.sql.SQLSyntaxErrorException;

import junit.framework.Assert;

import com.alibaba.cobar.parser.ast.stmt.mts.MTSReleaseStatement;
import com.alibaba.cobar.parser.ast.stmt.mts.MTSRollbackStatement;
import com.alibaba.cobar.parser.ast.stmt.mts.MTSSavepointStatement;
import com.alibaba.cobar.parser.recognizer.mysql.lexer.MySQLLexer;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class MySQLMTSParserTest extends AbstractSyntaxTest {
    public void testSavepint() throws SQLSyntaxErrorException {
        String sql = "  savepoint 123e123e";
        MySQLMTSParser parser = new MySQLMTSParser(new MySQLLexer(sql));
        MTSSavepointStatement savepoint = parser.savepoint();
        String output = output2MySQL(savepoint, sql);
        Assert.assertEquals("SAVEPOINT 123e123e", output);
        Assert.assertEquals("123e123e", savepoint.getSavepoint().getIdText());

        sql = "  savepoint SAVEPOINT";
        parser = new MySQLMTSParser(new MySQLLexer(sql));
        savepoint = parser.savepoint();
        output = output2MySQL(savepoint, sql);
        Assert.assertEquals("SAVEPOINT SAVEPOINT", output);
        Assert.assertEquals("SAVEPOINT", savepoint.getSavepoint().getIdText());

        sql = "  savepoInt `select`";
        parser = new MySQLMTSParser(new MySQLLexer(sql));
        savepoint = parser.savepoint();
        output = output2MySQL(savepoint, sql);
        Assert.assertEquals("SAVEPOINT `select`", output);
        Assert.assertEquals("`select`", savepoint.getSavepoint().getIdText());
    }

    public void testRelease() throws SQLSyntaxErrorException {
        String sql = "Release sAVEPOINT 1234e   ";
        MySQLMTSParser parser = new MySQLMTSParser(new MySQLLexer(sql));
        MTSReleaseStatement savepoint = parser.release();
        String output = output2MySQL(savepoint, sql);
        Assert.assertEquals("RELEASE SAVEPOINT 1234e", output);
        Assert.assertEquals("1234e", savepoint.getSavepoint().getIdText());

        sql = "Release SAVEPOINT sAVEPOINT";
        parser = new MySQLMTSParser(new MySQLLexer(sql));
        savepoint = parser.release();
        output = output2MySQL(savepoint, sql);
        Assert.assertEquals("RELEASE SAVEPOINT sAVEPOINT", output);
        Assert.assertEquals("sAVEPOINT", savepoint.getSavepoint().getIdText());
    }

    public void testRollback() throws SQLSyntaxErrorException {
        // ROLLBACK [WORK] TO [SAVEPOINT] identifier
        // ROLLBACK [WORK] [AND [NO] CHAIN | [NO] RELEASE]
        String sql = "rollBack work  ";
        MySQLMTSParser parser = new MySQLMTSParser(new MySQLLexer(sql));
        MTSRollbackStatement rollback = parser.rollback();
        String output = output2MySQL(rollback, sql);
        Assert.assertEquals("ROLLBACK", output);
        Assert.assertEquals(MTSRollbackStatement.CompleteType.UN_DEF, rollback.getCompleteType());
        Assert.assertNull(rollback.getSavepoint());

        sql = "rollBack  ";
        parser = new MySQLMTSParser(new MySQLLexer(sql));
        rollback = parser.rollback();
        output = output2MySQL(rollback, sql);
        Assert.assertEquals("ROLLBACK", output);
        Assert.assertEquals(MTSRollbackStatement.CompleteType.UN_DEF, rollback.getCompleteType());
        Assert.assertNull(rollback.getSavepoint());

        sql = "rollBack work TO savepoint 123e ";
        parser = new MySQLMTSParser(new MySQLLexer(sql));
        rollback = parser.rollback();
        output = output2MySQL(rollback, sql);
        Assert.assertEquals("ROLLBACK TO SAVEPOINT 123e", output);
        Assert.assertEquals("123e", rollback.getSavepoint().getIdText());
        Assert.assertNull(rollback.getCompleteType());

        sql = "rollBack to savePOINT savepoint ";
        parser = new MySQLMTSParser(new MySQLLexer(sql));
        rollback = parser.rollback();
        output = output2MySQL(rollback, sql);
        Assert.assertEquals("ROLLBACK TO SAVEPOINT savepoint", output);
        Assert.assertEquals("savepoint", rollback.getSavepoint().getIdText());
        Assert.assertNull(rollback.getCompleteType());

        sql = "rollBack to `select` ";
        parser = new MySQLMTSParser(new MySQLLexer(sql));
        rollback = parser.rollback();
        output = output2MySQL(rollback, sql);
        Assert.assertEquals("ROLLBACK TO SAVEPOINT `select`", output);
        Assert.assertEquals("`select`", rollback.getSavepoint().getIdText());
        Assert.assertNull(rollback.getCompleteType());

        sql = "rollBack work to  `select` ";
        parser = new MySQLMTSParser(new MySQLLexer(sql));
        rollback = parser.rollback();
        output = output2MySQL(rollback, sql);
        Assert.assertEquals("ROLLBACK TO SAVEPOINT `select`", output);
        Assert.assertEquals("`select`", rollback.getSavepoint().getIdText());
        Assert.assertNull(rollback.getCompleteType());

        sql = "rollBack work and no chaiN ";
        parser = new MySQLMTSParser(new MySQLLexer(sql));
        rollback = parser.rollback();
        output = output2MySQL(rollback, sql);
        Assert.assertEquals("ROLLBACK AND NO CHAIN", output);
        Assert.assertEquals(MTSRollbackStatement.CompleteType.NO_CHAIN, rollback.getCompleteType());
        Assert.assertNull(rollback.getSavepoint());

        sql = "rollBack work and  chaiN ";
        parser = new MySQLMTSParser(new MySQLLexer(sql));
        rollback = parser.rollback();
        output = output2MySQL(rollback, sql);
        Assert.assertEquals("ROLLBACK AND CHAIN", output);
        Assert.assertEquals(MTSRollbackStatement.CompleteType.CHAIN, rollback.getCompleteType());
        Assert.assertNull(rollback.getSavepoint());

        sql = "rollBack work NO release ";
        parser = new MySQLMTSParser(new MySQLLexer(sql));
        rollback = parser.rollback();
        output = output2MySQL(rollback, sql);
        Assert.assertEquals("ROLLBACK NO RELEASE", output);
        Assert.assertEquals(MTSRollbackStatement.CompleteType.NO_RELEASE, rollback.getCompleteType());
        Assert.assertNull(rollback.getSavepoint());

        sql = "rollBack work  release ";
        parser = new MySQLMTSParser(new MySQLLexer(sql));
        rollback = parser.rollback();
        output = output2MySQL(rollback, sql);
        Assert.assertEquals("ROLLBACK RELEASE", output);
        Assert.assertEquals(MTSRollbackStatement.CompleteType.RELEASE, rollback.getCompleteType());
        Assert.assertNull(rollback.getSavepoint());

        sql = "rollBack  and no chaiN ";
        parser = new MySQLMTSParser(new MySQLLexer(sql));
        rollback = parser.rollback();
        output = output2MySQL(rollback, sql);
        Assert.assertEquals("ROLLBACK AND NO CHAIN", output);
        Assert.assertEquals(MTSRollbackStatement.CompleteType.NO_CHAIN, rollback.getCompleteType());
        Assert.assertNull(rollback.getSavepoint());

        sql = "rollBack  and  chaiN ";
        parser = new MySQLMTSParser(new MySQLLexer(sql));
        rollback = parser.rollback();
        output = output2MySQL(rollback, sql);
        Assert.assertEquals("ROLLBACK AND CHAIN", output);
        Assert.assertEquals(MTSRollbackStatement.CompleteType.CHAIN, rollback.getCompleteType());
        Assert.assertNull(rollback.getSavepoint());

        sql = "rollBack  NO release ";
        parser = new MySQLMTSParser(new MySQLLexer(sql));
        rollback = parser.rollback();
        output = output2MySQL(rollback, sql);
        Assert.assertEquals("ROLLBACK NO RELEASE", output);
        Assert.assertEquals(MTSRollbackStatement.CompleteType.NO_RELEASE, rollback.getCompleteType());
        Assert.assertNull(rollback.getSavepoint());

        sql = "rollBack   release ";
        parser = new MySQLMTSParser(new MySQLLexer(sql));
        rollback = parser.rollback();
        output = output2MySQL(rollback, sql);
        Assert.assertEquals("ROLLBACK RELEASE", output);
        Assert.assertEquals(MTSRollbackStatement.CompleteType.RELEASE, rollback.getCompleteType());
        Assert.assertNull(rollback.getSavepoint());

    }
}
