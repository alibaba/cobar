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

package com.alibaba.cobar.manager.mock;

import java.sql.Connection;

import static com.alibaba.cobar.manager.mock.SQLEquals.sqlEquals;
import static com.alibaba.cobar.manager.util.SQLDefine.*;

import java.sql.ResultSet;
import java.sql.Statement;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.expect;


public class JDBCMock {
    public IMocksControl control;
    public DBUtility mockDBUtility;
    public Connection mockConnection;

    public Connection getMockConnection() {
        return mockConnection;
    }

    public Statement mockStatement;

    public ResultSet rsDataNodes;
    public ResultSet rsTimeCurrent;
    public ResultSet rsTimeStartUp;
    public ResultSet rsVersion;
    public ResultSet rsThreadPool;
    public ResultSet rsDataSources;
    public ResultSet rsProcessor;
    public ResultSet rsDataBases;
    public ResultSet rsCommand;
    public ResultSet rsConnection;
    public ResultSet rsConnectionSql;
    public ResultSet rsSqlExecute;
    public ResultSet rsSqlSlow;
    public ResultSet rsParser;
    public ResultSet rsRoute;
    public ResultSet rsServer;
    public ResultSet rsSqlDetail;
    public ResultSet rsSql;

    public JDBCMock() {
        control = EasyMock.createControl();
        mockDBUtility = control.createMock(DBUtility.class);
        mockConnection = control.createMock(Connection.class);
        mockStatement = control.createMock(Statement.class);
        //rsDataNodes = EasyMock.createMock(ResultSet.class);
        rsDataNodes = showDataNodes();
        rsTimeCurrent = EasyMock.createMock(ResultSet.class);
        rsCommand = EasyMock.createMock(ResultSet.class);
        rsConnection = EasyMock.createMock(ResultSet.class);
        rsDataBases = EasyMock.createMock(ResultSet.class);
        rsDataSources = EasyMock.createMock(ResultSet.class);
        rsParser = EasyMock.createMock(ResultSet.class);
        rsProcessor = EasyMock.createMock(ResultSet.class);
        rsRoute = EasyMock.createMock(ResultSet.class);
        rsSqlExecute = EasyMock.createMock(ResultSet.class);
        rsSqlSlow = EasyMock.createMock(ResultSet.class);
        rsThreadPool = EasyMock.createMock(ResultSet.class);
        rsTimeStartUp = EasyMock.createMock(ResultSet.class);
        rsVersion = showVersion();
        rsConnectionSql = EasyMock.createMock(ResultSet.class);
        rsServer = EasyMock.createMock(ResultSet.class);
        rsSqlDetail = EasyMock.createMock(ResultSet.class);
        rsSql = EasyMock.createMock(ResultSet.class);
        //showDataNodes();
        showTimeCurrent();
        showCommand();
        showConnection();
        showConnectionSql();
        showDataBases();
        showDataSources();
        showParser();
        showProcessor();
        showRouter();
        showSqlExecute();
        showSqlSlow();
        showThreadPool();
        showTimeStartup();
        //showVersion();
        showServer();
        showSqlDetail();
        showSql();

        try {
            mockDBUtility.getConnection();
            expectLastCall().andStubReturn(mockConnection);
            mockConnection.createStatement();
            expectLastCall().andStubReturn(mockStatement);
            mockStatement.executeQuery(sqlEquals("show @@datanodes"));
            expectLastCall().andStubReturn(rsDataNodes);
            mockStatement.executeQuery(sqlEquals("show @@datanode"));
            expectLastCall().andStubReturn(rsDataNodes);
            mockStatement.executeUpdate(sqlEquals("switch @@datasource"));
            expectLastCall().andStubReturn(1);
            mockStatement.executeQuery(sqlEquals("show @@time.current"));
            expectLastCall().andStubReturn(rsTimeCurrent);
            mockStatement.executeQuery(sqlEquals("show @@time.startup"));
            expectLastCall().andStubReturn(rsTimeStartUp);
            mockStatement.executeQuery(sqlEquals("show @@version"));
            expectLastCall().andStubReturn(rsVersion);
            mockStatement.executeQuery(sqlEquals("show @@threadpool"));
            expectLastCall().andStubReturn(rsThreadPool);
            mockStatement.executeQuery(sqlEquals("show @@databases"));
            expectLastCall().andStubReturn(rsDataBases);
            mockStatement.executeQuery(sqlEquals("show @@datasources"));
            expectLastCall().andStubReturn(rsDataSources);
            mockStatement.executeQuery(sqlEquals("show @@processor"));
            expectLastCall().andStubReturn(rsProcessor);
            mockStatement.executeQuery(sqlEquals("show @@command"));
            expectLastCall().andStubReturn(rsCommand);
            mockStatement.executeQuery(sqlEquals("show @@connection"));
            expectLastCall().andStubReturn(rsConnection);
            mockStatement.executeQuery(sqlEquals("show @@connection.sql"));
            expectLastCall().andStubReturn(rsConnectionSql);
            mockStatement.executeQuery(sqlEquals("show @@sql.execute"));
            expectLastCall().andStubReturn(rsSqlExecute);
            mockStatement.executeQuery(sqlEquals("show @@sql.slow"));
            expectLastCall().andStubReturn(rsSqlSlow);
            mockStatement.executeQuery(sqlEquals("show @@parser"));
            expectLastCall().andStubReturn(rsParser);
            mockStatement.executeQuery(sqlEquals("show @@router"));
            expectLastCall().andStubReturn(rsRoute);
            mockStatement.executeQuery(sqlEquals("show @@server"));
            expectLastCall().andStubReturn(rsServer);
            mockStatement.executeQuery(sqlEquals("show @@sql.detail where id=1000"));
            expectLastCall().andStubReturn(rsSqlDetail);
            mockStatement.executeQuery(sqlEquals("show @@sql where id=1000"));
            expectLastCall().andStubReturn(rsSql);
            mockStatement.executeUpdate(sqlEquals("kill @@connection 1909417519"));
            expectLastCall().andStubReturn(1);
            mockStatement.executeUpdate(sqlEquals("stop @@heartbeat dubbo:100"));
            expectLastCall().andStubReturn(1);
            mockStatement.executeUpdate(sqlEquals("reload @@config"));
            expectLastCall().andStubReturn(1);
            mockStatement.executeUpdate(sqlEquals("reload @@route"));
            expectLastCall().andStubReturn(1);
            mockStatement.executeUpdate(sqlEquals("rollback @@config"));
            expectLastCall().andStubReturn(1);
            mockStatement.executeUpdate(sqlEquals("rollback @@route"));
            expectLastCall().andStubReturn(1);

            control.replay();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ResultSet showDataNodes() {
        ResultSet tmp = EasyMock.createMock(ResultSet.class);
        //ResultSet tmp = rsDataNodes;
        try {
            tmp.next();
            expectLastCall().andReturn(true).times(6);
            expectLastCall().andReturn(false).times(100);

            tmp.getString(POOL_NAME);
            expectLastCall().andReturn("circe").times(100);
            expectLastCall().andReturn("cndb").times(100);
            expectLastCall().andReturn("dubbo").times(100);
            expectLastCall().andReturn("napoli").times(100);
            expectLastCall().andReturn("offer[0]").times(100);
            expectLastCall().andReturn("offer[1]").times(100);

            tmp.getString(DS);
            expectLastCall().andReturn("circe").times(100);
            expectLastCall().andReturn("cndb_master,cndn_slave").times(100);
            expectLastCall().andReturn("dubbo_master,dubbo_slave").times(100);
            expectLastCall().andReturn("napoli_master,napoli_slave").times(100);
            expectLastCall().andReturn("offer[0]_master,offer[0]_slave").times(100);
            expectLastCall().andReturn("offer[1]_master,offer[1]_slave").times(100);

            tmp.getInt(INDEX);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(1).times(100);
            expectLastCall().andReturn(1).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);

            tmp.getString(TYPE);
            expectLastCall().andReturn("mysql").times(100);
            expectLastCall().andReturn("mysql").times(100);
            expectLastCall().andReturn("mysql").times(100);
            expectLastCall().andReturn("mysql").times(100);
            expectLastCall().andReturn("mysql").times(100);
            expectLastCall().andReturn("mysql").times(100);

            tmp.getInt(ACTIVE);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);

            tmp.getInt(IDLE);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);

            tmp.getInt(SIZE);
            expectLastCall().andReturn(16).times(100);
            expectLastCall().andReturn(16).times(100);
            expectLastCall().andReturn(16).times(100);
            expectLastCall().andReturn(16).times(100);
            expectLastCall().andReturn(16).times(100);
            expectLastCall().andReturn(16).times(100);

            tmp.getLong(EXECUTE);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(2).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);

            tmp.getLong(TOTAL_TIME);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);

            tmp.getLong(MAX_TIME);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);

            tmp.getLong(MAX_SQL);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);

            tmp.getLong(RECOVERY_TIME);
            expectLastCall().andReturn(-1).times(100);
            expectLastCall().andReturn(-1).times(100);
            expectLastCall().andReturn(-1).times(100);
            expectLastCall().andReturn(-1).times(100);
            expectLastCall().andReturn(-1).times(100);
            expectLastCall().andReturn(-1).times(100);

            EasyMock.replay(tmp);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return tmp;
    }

    public ResultSet showTimeCurrent() {
        ResultSet tmp = rsTimeCurrent;
        try {
            tmp.next();
            expectLastCall().andReturn(true);
            expectLastCall().andReturn(false);

            tmp.getString(TIMESTAMP);
            expectLastCall().andReturn("1314668515434").times(2);

            EasyMock.replay(tmp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tmp;
    }

    //   public ResultSet showTimeStartup(){
    public void showTimeStartup() {
        ResultSet tmp = rsTimeStartUp;
        try {
            expect(tmp.next()).andReturn(true).times(100);
            expect(tmp.next()).andReturn(false).times(100);

            expect(tmp.getString(TIMESTAMP)).andReturn("1314238929709").times(100);

            EasyMock.replay(tmp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //       return tmp;
    }

    public ResultSet showVersion() {
        ResultSet tmp = EasyMock.createMock(ResultSet.class);;
        try {
            tmp.next();
            expectLastCall().andReturn(true).times(1);
            expectLastCall().andReturn(false).times(1);

            tmp.getString(VERSION);
            expectLastCall().andReturn("5.1.48-cobar-1.2.3").times(100);

            EasyMock.replay(tmp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tmp;
    }

    public ResultSet showThreadPool() {
        ResultSet tmp = rsThreadPool;
        try {
            tmp.next();
            expectLastCall().andReturn(true).times(19);
            expectLastCall().andReturn(false).times(100);

            tmp.getString(TP_NAME);
            expectLastCall().andReturn("Assistant").times(100);
            expectLastCall().andReturn("Checker").times(100);
            expectLastCall().andReturn("Manager").times(100);
            expectLastCall().andReturn("Processor0-C").times(100);
            expectLastCall().andReturn("Processor0-S").times(100);
            expectLastCall().andReturn("Processor1-C").times(100);
            expectLastCall().andReturn("Processor1-S").times(100);
            expectLastCall().andReturn("Processor2-C").times(100);
            expectLastCall().andReturn("Processor2-S").times(100);
            expectLastCall().andReturn("Processor3-C").times(100);
            expectLastCall().andReturn("Processor3-S").times(100);
            expectLastCall().andReturn("Processor4-C").times(100);
            expectLastCall().andReturn("Processor4-S").times(100);
            expectLastCall().andReturn("Processor5-C").times(100);
            expectLastCall().andReturn("Processor5-S").times(100);
            expectLastCall().andReturn("Processor6-C").times(100);
            expectLastCall().andReturn("Processor6-S").times(100);
            expectLastCall().andReturn("Processor7-C").times(100);
            expectLastCall().andReturn("Processor7-S").times(100);

            tmp.getInt(POOL_SIZE);
            expectLastCall().andReturn(8).times(100);
            expectLastCall().andReturn(8).times(100);
            expectLastCall().andReturn(16).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);

            tmp.getInt(ACTIVE_COUNT);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(1).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);

            tmp.getInt(TASK_QUEUE_SIZE);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);

            tmp.getLong(COMPLETED_TASK);
            expectLastCall().andReturn(44254).times(100);
            expectLastCall().andReturn(28529).times(100);
            expectLastCall().andReturn(6437).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);

            tmp.getLong(TOTAL_TASK);
            expectLastCall().andReturn(44254).times(100);
            expectLastCall().andReturn(28529).times(100);
            expectLastCall().andReturn(6438).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);

            EasyMock.replay(tmp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tmp;
    }

    public ResultSet showDataBases() {
        ResultSet tmp = rsDataBases;
        try {
            tmp.next();
            expectLastCall().andReturn(true).times(4);
            expectLastCall().andReturn(false).times(100);

            tmp.getString(DATABASE);
            expectLastCall().andReturn("dubbo").times(100);
            expectLastCall().andReturn("napoli").times(100);
            expectLastCall().andReturn("cndb").times(100);
            expectLastCall().andReturn("circe").times(100);

            EasyMock.replay(tmp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tmp;
    }

    public ResultSet showDataSources() {
        ResultSet tmp = rsDataSources;
        try {
            tmp.next();
            expectLastCall().andReturn(true).times(11);
            expectLastCall().andReturn(false).times(100);

            tmp.getString(DS_NAME);
            expectLastCall().andReturn("circe").times(100);
            expectLastCall().andReturn("cndb_master").times(100);
            expectLastCall().andReturn("cndb_slave").times(100);
            expectLastCall().andReturn("dubbo_master").times(100);
            expectLastCall().andReturn("dubbo_slave").times(100);
            expectLastCall().andReturn("napoli_master").times(100);
            expectLastCall().andReturn("napoli_slave").times(100);
            expectLastCall().andReturn("offer_master[0]").times(100);
            expectLastCall().andReturn("offer_master[1]").times(100);
            expectLastCall().andReturn("offer_slave[0]").times(100);
            expectLastCall().andReturn("offer_slave[1]").times(100);

            tmp.getString(DS_TYPE);
            expectLastCall().andReturn("mysql").times(100);
            expectLastCall().andReturn("mysql").times(100);
            expectLastCall().andReturn("mysql").times(100);
            expectLastCall().andReturn("mysql").times(100);
            expectLastCall().andReturn("mysql").times(100);
            expectLastCall().andReturn("mysql").times(100);
            expectLastCall().andReturn("mysql").times(100);
            expectLastCall().andReturn("mysql").times(100);
            expectLastCall().andReturn("mysql").times(100);
            expectLastCall().andReturn("mysql").times(100);
            expectLastCall().andReturn("mysql").times(100);

            tmp.getString(DS_HOST);
            expectLastCall().andReturn("10.249.192.239").times(100);
            expectLastCall().andReturn("10.20.153.177").times(100);
            expectLastCall().andReturn("10.20.153.177").times(100);
            expectLastCall().andReturn("10.20.153.177").times(100);
            expectLastCall().andReturn("10.20.153.177").times(100);
            expectLastCall().andReturn("10.20.153.177").times(100);
            expectLastCall().andReturn("10.20.153.177").times(100);
            expectLastCall().andReturn("10.20.153.177").times(100);
            expectLastCall().andReturn("10.20.153.177").times(100);
            expectLastCall().andReturn("10.20.153.177").times(100);
            expectLastCall().andReturn("10.20.153.177").times(100);

            tmp.getInt(DS_PORT);
            expectLastCall().andReturn(3306).times(100);
            expectLastCall().andReturn(3306).times(100);
            expectLastCall().andReturn(3306).times(100);
            expectLastCall().andReturn(3306).times(100);
            expectLastCall().andReturn(3306).times(100);
            expectLastCall().andReturn(3306).times(100);
            expectLastCall().andReturn(3306).times(100);
            expectLastCall().andReturn(3306).times(100);
            expectLastCall().andReturn(3306).times(100);
            expectLastCall().andReturn(3306).times(100);
            expectLastCall().andReturn(3306).times(100);

            tmp.getString(DS_SCHEMA);
            expectLastCall().andReturn("circe0").times(100);
            expectLastCall().andReturn("offer1").times(100);
            expectLastCall().andReturn("offer1").times(100);
            expectLastCall().andReturn("offer2").times(100);
            expectLastCall().andReturn("offer2").times(100);
            expectLastCall().andReturn("offer1").times(100);
            expectLastCall().andReturn("offer1").times(100);
            expectLastCall().andReturn("offer1").times(100);
            expectLastCall().andReturn("offer2").times(100);
            expectLastCall().andReturn("offer1").times(100);
            expectLastCall().andReturn("offer2").times(100);

            tmp.getString(DS_CHARSET);
            expectLastCall().andReturn("UTF-8").times(100);
            expectLastCall().andReturn("UTF-8").times(100);
            expectLastCall().andReturn("UTF-8").times(100);
            expectLastCall().andReturn("UTF-8").times(100);
            expectLastCall().andReturn("UTF-8").times(100);
            expectLastCall().andReturn("UTF-8").times(100);
            expectLastCall().andReturn("UTF-8").times(100);
            expectLastCall().andReturn("UTF-8").times(100);
            expectLastCall().andReturn("UTF-8").times(100);
            expectLastCall().andReturn("UTF-8").times(100);
            expectLastCall().andReturn("UTF-8").times(100);

            EasyMock.replay(tmp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tmp;
    }

    public ResultSet showProcessor() {
        ResultSet tmp = rsProcessor;
        try {
            tmp.next();
            expectLastCall().andReturn(true).times(8);
            expectLastCall().andReturn(false).times(100);

            tmp.getString(P_NAME);
            expectLastCall().andReturn("Processor0").times(100);
            expectLastCall().andReturn("Processor1").times(100);
            expectLastCall().andReturn("Processor2").times(100);
            expectLastCall().andReturn("Processor3").times(100);
            expectLastCall().andReturn("Processor4").times(100);
            expectLastCall().andReturn("Processor5").times(100);
            expectLastCall().andReturn("Processor6").times(100);
            expectLastCall().andReturn("Processor7").times(100);

            tmp.getLong(P_NET_IN);
            expectLastCall().andReturn(10617).times(100);
            expectLastCall().andReturn(91631).times(100);
            expectLastCall().andReturn(24969).times(100);
            expectLastCall().andReturn(10870).times(100);
            expectLastCall().andReturn(10496).times(100);
            expectLastCall().andReturn(13208).times(100);
            expectLastCall().andReturn(42190).times(100);
            expectLastCall().andReturn(11818).times(100);

            tmp.getLong(P_NET_OUT);
            expectLastCall().andReturn(62371).times(100);
            expectLastCall().andReturn(2135856).times(100);
            expectLastCall().andReturn(443916).times(100);
            expectLastCall().andReturn(105434).times(100);
            expectLastCall().andReturn(52389).times(100);
            expectLastCall().andReturn(338870).times(100);
            expectLastCall().andReturn(1063047).times(100);
            expectLastCall().andReturn(144475).times(100);

            tmp.getLong(REQUEST_COUNT);
            expectLastCall().andReturn(89).times(100);
            expectLastCall().andReturn(4319).times(100);
            expectLastCall().andReturn(733).times(100);
            expectLastCall().andReturn(98).times(100);
            expectLastCall().andReturn(86).times(100);
            expectLastCall().andReturn(164).times(100);
            expectLastCall().andReturn(1594).times(100);
            expectLastCall().andReturn(146).times(100);

            tmp.getInt(R_QUEUE);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);

            tmp.getInt(W_QUEUE);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);

            tmp.getLong(FREE_BUFFER);
            expectLastCall().andReturn(4096).times(100);
            expectLastCall().andReturn(4096).times(100);
            expectLastCall().andReturn(4096).times(100);
            expectLastCall().andReturn(4096).times(100);
            expectLastCall().andReturn(4096).times(100);
            expectLastCall().andReturn(4096).times(100);
            expectLastCall().andReturn(4096).times(100);
            expectLastCall().andReturn(4096).times(100);

            tmp.getLong(TOTAL_BUFFER);
            expectLastCall().andReturn(4096).times(100);
            expectLastCall().andReturn(4096).times(100);
            expectLastCall().andReturn(4096).times(100);
            expectLastCall().andReturn(4096).times(100);
            expectLastCall().andReturn(4096).times(100);
            expectLastCall().andReturn(4096).times(100);
            expectLastCall().andReturn(4096).times(100);
            expectLastCall().andReturn(4096).times(100);

            tmp.getLong(FC_COUNT);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(1).times(100);
            expectLastCall().andReturn(1).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(1).times(100);

            EasyMock.replay(tmp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tmp;
    }

    public ResultSet showCommand() {
        ResultSet tmp = rsCommand;
        try {
            tmp.next();
            expectLastCall().andReturn(true).times(8);
            expectLastCall().andReturn(false).times(100);

            tmp.getString(CMD_PROCESSOR);
            expectLastCall().andReturn("Processor0").times(100);
            expectLastCall().andReturn("Processor1").times(100);
            expectLastCall().andReturn("Processor2").times(100);
            expectLastCall().andReturn("Processor3").times(100);
            expectLastCall().andReturn("Processor4").times(100);
            expectLastCall().andReturn("Processor5").times(100);
            expectLastCall().andReturn("Processor6").times(100);
            expectLastCall().andReturn("Processor7").times(100);

            tmp.getLong(INIT_DB);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(1).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);

            tmp.getLong(QUERY);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(5).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);

            tmp.getLong(STMT_PREPARED);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);

            tmp.getLong(STMT_EXECUTE);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);

            tmp.getLong(STMT_CLOSE);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);

            tmp.getLong(PING);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(1).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);

            tmp.getLong(KILL);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(1).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);

            tmp.getLong(QUIT);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(1).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);

            tmp.getLong(OTHER);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(1).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);

            EasyMock.replay(tmp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tmp;
    }

    public ResultSet showConnection() {
        ResultSet tmp = rsConnection;
        try {
            tmp.next();
            expectLastCall().andReturn(true).times(2);
            expectLastCall().andReturn(false).times(100);

            tmp.getString(C_PROCESSOR);
            expectLastCall().andReturn("processor7").times(100);
            expectLastCall().andReturn("processor2").times(100);

            tmp.getLong(ID);
            expectLastCall().andReturn(50503845).times(100);
            expectLastCall().andReturn(1909417519).times(100);

            tmp.getString(HOST);
            expectLastCall().andReturn("10.16.28.20").times(100);
            expectLastCall().andReturn("10.16.200.82").times(100);

            tmp.getInt(PORT);
            expectLastCall().andReturn(1606).times(100);
            expectLastCall().andReturn(50173).times(100);

            tmp.getInt(LOCAL_PORT);
            expectLastCall().andReturn(9066).times(100);
            expectLastCall().andReturn(9066).times(100);

            tmp.getString(SCHEMA);
            expectLastCall().andReturn(null).times(100);
            expectLastCall().andReturn(null).times(100);

            tmp.getString(CHARSET);
            expectLastCall().andReturn("latin1").times(100);
            expectLastCall().andReturn("utf8").times(100);

            tmp.getLong(C_NET_IN);
            expectLastCall().andReturn(465).times(100);
            expectLastCall().andReturn(1150).times(100);

            tmp.getLong(C_NET_OUT);
            expectLastCall().andReturn(51483).times(100);
            expectLastCall().andReturn(1150).times(100);

            tmp.getLong(ALIVE_TIME);
            expectLastCall().andReturn(24984).times(100);
            expectLastCall().andReturn(52).times(100);

            tmp.getInt(ATTEMPS_COUNT);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);

            tmp.getLong(RECV_BUFFER);
            expectLastCall().andReturn(2048).times(100);
            expectLastCall().andReturn(2048).times(100);

            tmp.getLong(SEND_QUEUE);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);

            EasyMock.replay(tmp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tmp;
    }

    public ResultSet showConnectionSql() {
        ResultSet tmp = rsConnectionSql;
        try {
            tmp.next();
            expectLastCall().andReturn(true).times(100);
            expectLastCall().andReturn(false).times(100);

            tmp.getLong(CS_ID);
            expectLastCall().andReturn(50503845).times(100);

            tmp.getString(CS_HOST);
            expectLastCall().andReturn("10.16.28.20").times(100);

            tmp.getString(EXECUTE_START);
            expectLastCall().andReturn("1314693533501").times(100);

            tmp.getString(EXECUTE_TIME);
            expectLastCall().andReturn("0").times(100);

            tmp.getString(4);
            expectLastCall().andReturn("show @@connection.sql").times(100);

            EasyMock.replay(tmp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tmp;
    }

    public ResultSet showSqlExecute() {
        ResultSet tmp = rsSqlExecute;
        try {
            tmp.next();
            expectLastCall().andReturn(true).times(3);
            expectLastCall().andReturn(false).times(100);

            tmp.getLong(E_SQL);
            expectLastCall().andReturn(1000).times(100);
            expectLastCall().andReturn(2000).times(100);
            expectLastCall().andReturn(3000).times(100);

            tmp.getLong(E_EXECUTE);
            expectLastCall().andReturn(100).times(100);
            expectLastCall().andReturn(200).times(100);
            expectLastCall().andReturn(300).times(100);

            tmp.getDouble(E_TIME);
            expectLastCall().andReturn(989.9).times(100);
            expectLastCall().andReturn(989.9).times(100);
            expectLastCall().andReturn(989.9).times(100);

            tmp.getDouble(E_MAX_TIME);
            expectLastCall().andReturn(8.8).times(100);
            expectLastCall().andReturn(8.8).times(100);
            expectLastCall().andReturn(8.8).times(100);

            tmp.getDouble(E_MIN_TIME);
            expectLastCall().andReturn(1).times(100);
            expectLastCall().andReturn(1).times(100);
            expectLastCall().andReturn(1).times(100);

            EasyMock.replay(tmp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tmp;
    }

    public ResultSet showSqlSlow() {
        ResultSet tmp = rsSqlSlow;
        try {
            tmp.next();
            expectLastCall().andReturn(true).times(3);
            expectLastCall().andReturn(false).times(100);

            tmp.getDouble(S_TIME);
            expectLastCall().andReturn(213.2).times(100);
            expectLastCall().andReturn(213.2).times(100);
            expectLastCall().andReturn(213.2).times(100);

            tmp.getString(S_DATA_SOURCE);
            expectLastCall().andReturn("mysql_1").times(100);
            expectLastCall().andReturn("mysql_1").times(100);
            expectLastCall().andReturn("mysql_1").times(100);

            tmp.getString(S_EXECUTE_TIMESTAMP);
            expectLastCall().andReturn("1279188420682").times(100);
            expectLastCall().andReturn("1279188420682").times(100);
            expectLastCall().andReturn("1279188420682").times(100);

            tmp.getString(S_SQL);
            expectLastCall().andReturn("select * from offer limit 1").times(100);
            expectLastCall().andReturn("select * from offer limit 1").times(100);
            expectLastCall().andReturn("select * from offer limit 1").times(100);

            EasyMock.replay(tmp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tmp;
    }

    public ResultSet showParser() {
        ResultSet tmp = rsParser;
        try {
            tmp.next();
            expectLastCall().andReturn(true).times(100);
            expectLastCall().andReturn(false).times(100);

            tmp.getString(PROCESSOR_NAME);
            expectLastCall().andReturn(null).times(100);

            tmp.getLong(PARSE_COUNT);
            expectLastCall().andReturn(11).times(100);

            tmp.getDouble(TIME_COUNT);
            expectLastCall().andReturn(12).times(100);

            tmp.getFloat(MAX_PARSE_TIME);
            expectLastCall().andReturn(13).times(100);

            tmp.getLong(MAX_PARSE_SQL_ID);
            expectLastCall().andReturn(14).times(100);

            tmp.getLong(CACHED_COUNT);
            expectLastCall().andReturn(15).times(100);

            tmp.getInt(CACHE_SIZE);
            expectLastCall().andReturn(16).times(100);

            EasyMock.replay(tmp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tmp;
    }

    public ResultSet showRouter() {
        ResultSet tmp = rsRoute;
        try {
            tmp.next();
            expectLastCall().andReturn(true).times(8);
            expectLastCall().andReturn(false).times(100);

            tmp.getString(R_P_NAME);
            expectLastCall().andReturn("Processor0").times(100);
            expectLastCall().andReturn("Processor1").times(100);
            expectLastCall().andReturn("Processor2").times(100);
            expectLastCall().andReturn("Processor3").times(100);
            expectLastCall().andReturn("Processor4").times(100);
            expectLastCall().andReturn("Processor5").times(100);
            expectLastCall().andReturn("Processor6").times(100);
            expectLastCall().andReturn("Processor7").times(100);

            tmp.getLong(ROUTE_COUNT);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);

            tmp.getDouble(R_TIME_COUNT);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);

            tmp.getFloat(MAX_ROUTE_TIME);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);

            tmp.getLong(MAX_ROUTE_SQL_ID);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);
            expectLastCall().andReturn(0).times(100);

            EasyMock.replay(tmp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tmp;
    }

    public ResultSet showServer() {
        ResultSet tmp = rsServer;
        try {
            tmp.next();
            expectLastCall().andReturn(true).times(100);
            expectLastCall().andReturn(false).times(100);

            tmp.getString(UPTIME);
            expectLastCall().andReturn("1d 5h 42m").times(100);

            tmp.getString(USED_MEMORY);
            expectLastCall().andReturn("367532544").times(100);

            tmp.getString(TOTAL_MEMORY);
            expectLastCall().andReturn("2093809664").times(100);

            tmp.getString(MAX_MEMORY);
            expectLastCall().andReturn("2093809644").times(100);

            tmp.getString(RELOAD_TIME);
            expectLastCall().andReturn("1315202090992").times(100);

            tmp.getString(ROLLBACK_TIME);
            expectLastCall().andReturn("1315202100028").times(100);

            tmp.getString(STATUS);
            expectLastCall().andReturn("RUNNING").times(100);

            EasyMock.replay(tmp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tmp;
    }

    public ResultSet showSqlDetail() {
        ResultSet tmp = rsSqlDetail;
        try {
            EasyMock.reset(tmp);
            tmp.next();
            expectLastCall().andReturn(true).times(3);
            expectLastCall().andReturn(false).times(100);

            tmp.getString(D_DATA_SOURCE);
            expectLastCall().andReturn("mysql_1").times(100);
            expectLastCall().andReturn("mysql_1").times(100);
            expectLastCall().andReturn("mysql_1").times(100);

            tmp.getLong(D_EXECUTE);
            expectLastCall().andReturn(123).times(100);
            expectLastCall().andReturn(123).times(100);
            expectLastCall().andReturn(123).times(100);

            tmp.getDouble(D_TIME);
            expectLastCall().andReturn(2.3).times(100);
            expectLastCall().andReturn(2.3).times(100);
            expectLastCall().andReturn(2.3).times(100);

            tmp.getString(D_LAST_EXECUTE_TIMESTAMP);
            expectLastCall().andReturn("1279188420602").times(100);
            expectLastCall().andReturn("1279188420602").times(100);
            expectLastCall().andReturn("1279188420602").times(100);

            tmp.getDouble(D_LAST_TIME);
            expectLastCall().andReturn(3.42).times(100);
            expectLastCall().andReturn(3.42).times(100);
            expectLastCall().andReturn(3.42).times(100);

            EasyMock.replay(tmp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tmp;
    }

    public ResultSet showSql() {
        ResultSet tmp = rsSql;
        try {
            tmp.next();
            expectLastCall().andReturn(true).times(100);
            expectLastCall().andReturn(false).times(100);

            tmp.getLong(SQL_ID);
            expectLastCall().andReturn(1000).times(100);

            tmp.getString(SQL_DETAIL);
            expectLastCall().andReturn("insert into T...").times(100);

            EasyMock.replay(tmp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tmp;
    }

    public void resetDataNodes() {
        try {
            EasyMock.reset(rsDataNodes);
            showDataNodes();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetDataBases() {
        try {
            EasyMock.reset(rsDataBases);
            showDataBases();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetDataSources() {
        try {
            EasyMock.reset(rsDataSources);
            showDataSources();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetTimeCurrent() {
        try {
            EasyMock.reset(rsTimeCurrent);
            showTimeCurrent();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetCommand() {
        try {
            EasyMock.reset(rsCommand);
            showCommand();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetConnection() {
        try {
            EasyMock.reset(rsConnection);
            showConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetConnectionSql() {
        try {
            EasyMock.reset(rsConnectionSql);
            showConnectionSql();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetParser() {
        try {
            EasyMock.reset(rsParser);
            showParser();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetProcessor() {
        try {
            EasyMock.reset(rsProcessor);
            showProcessor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetRouter() {
        try {
            EasyMock.reset(rsRoute);
            showRouter();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetSqlExecute() {
        try {
            EasyMock.reset(rsSqlExecute);
            showSqlExecute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetSqlSlow() {
        try {
            EasyMock.reset(rsSqlSlow);
            showSqlSlow();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetThreadPool() {
        try {
            EasyMock.reset(rsThreadPool);
            showThreadPool();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetTimeStartUp() {
        try {
            EasyMock.reset(rsTimeStartUp);
            showTimeStartup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetVersion() {
        try {
            EasyMock.reset(rsVersion);
            showVersion();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetServer() {
        try {
            EasyMock.reset(rsServer);
            showServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetSqlDetail() {
        try {
            EasyMock.reset(rsSqlDetail);
            showSqlDetail();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetSql() {
        try {
            EasyMock.reset(rsSql);
            showSql();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reset() {
        try {
            EasyMock.reset(rsServer);
            showServer();
            EasyMock.reset(rsSqlDetail);
            showSqlDetail();
            EasyMock.reset(rsSql);
            showSql();
            EasyMock.reset(rsDataNodes);
            showDataNodes();
            EasyMock.reset(rsDataBases);
            showDataBases();
            EasyMock.reset(rsDataSources);
            showDataSources();
            EasyMock.reset(rsTimeCurrent);
            showTimeCurrent();
            EasyMock.reset(rsCommand);
            showCommand();
            EasyMock.reset(rsConnection);
            showConnection();
            EasyMock.reset(rsConnectionSql);
            showConnectionSql();
            EasyMock.reset(rsParser);
            showParser();
            EasyMock.reset(rsProcessor);
            showProcessor();
            EasyMock.reset(rsRoute);
            showRouter();
            EasyMock.reset(rsSqlExecute);
            showSqlExecute();
            EasyMock.reset(rsSqlSlow);
            showSqlSlow();
            EasyMock.reset(rsThreadPool);
            showThreadPool();
            EasyMock.reset(rsTimeStartUp);
            showTimeStartup();
            EasyMock.reset(rsVersion);
            showVersion();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
