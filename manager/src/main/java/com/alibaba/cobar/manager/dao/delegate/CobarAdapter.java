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

package com.alibaba.cobar.manager.dao.delegate;

import static com.alibaba.cobar.manager.util.SQLDefine.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import com.alibaba.cobar.manager.dao.CobarAdapterDAO;
import com.alibaba.cobar.manager.dataobject.cobarnode.CommandStatus;
import com.alibaba.cobar.manager.dataobject.cobarnode.ConnectionStatus;
import com.alibaba.cobar.manager.dataobject.cobarnode.DataNodesStatus;
import com.alibaba.cobar.manager.dataobject.cobarnode.DataSources;
import com.alibaba.cobar.manager.dataobject.cobarnode.ProcessorStatus;
import com.alibaba.cobar.manager.dataobject.cobarnode.ServerStatus;
import com.alibaba.cobar.manager.dataobject.cobarnode.ThreadPoolStatus;
import com.alibaba.cobar.manager.dataobject.cobarnode.TimeStamp;
import com.alibaba.cobar.manager.util.Pair;

/**
 * @author haiqing.zhuhq 2011-6-20
 */
/**
 * ????cobar???????????
 */

@SuppressWarnings("unchecked")
public class CobarAdapter extends JdbcDaoSupport implements DisposableBean, CobarAdapterDAO {

    @Override
    protected void checkDaoConfig() {
        super.checkDaoConfig();
        DataSource ds = getDataSource();
        if (!(ds instanceof DataSource)) {
            throw new IllegalArgumentException("property 'dataSource' is not type of " + DataSource.class.getName());
        }
    }

    @Override
    public void destroy() throws Exception {
        ((BasicDataSource) getDataSource()).close();
    }

    private static class TimeStampRowMapper implements RowMapper {

        @Override
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            TimeStamp ts = new TimeStamp();
            ts.setTimestamp(rs.getLong(TIMESTAMP));
            return ts;
        }

    }

    private static class ServerStatusRowMapper implements RowMapper {

        @Override
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            ServerStatus s = new ServerStatus();
            s.setUptime(rs.getString(UPTIME));
            s.setStatus(rs.getString(STATUS));
            s.setReloadTime(rs.getLong(RELOAD_TIME));
            s.setRollbackTime(rs.getLong(ROLLBACK_TIME));
            s.setUsedMemory(rs.getLong(USED_MEMORY));
            s.setTotalMemory(rs.getLong(TOTAL_MEMORY));
            s.setMaxMemory(rs.getLong(MAX_MEMORY));
            s.setCharSet(rs.getString(S_CHARSET));
            return s;
        }
    }

    private static class ProccessorStatusRowMapper implements RowMapper {

        @Override
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            ProcessorStatus p = new ProcessorStatus();
            p.setProcessorId(rs.getString(P_NAME));
            p.setNetIn(rs.getLong(P_NET_IN));
            p.setNetOut(rs.getLong(P_NET_OUT));
            p.setrQueue(rs.getInt(R_QUEUE));
            p.setwQueue(rs.getInt(W_QUEUE));
            p.setConnections(rs.getInt(FC_COUNT));
            p.setFreeBuffer(rs.getLong(FREE_BUFFER));
            p.setTotalBuffer(rs.getLong(TOTAL_BUFFER));
            p.setRequestCount(rs.getLong(REACT_COUNT));
            p.setBc_count(rs.getLong(BC_COUNT));
            p.setSampleTimeStamp(System.currentTimeMillis());
            return p;
        }

    }

    private static class ThreadPoolStatusRowMapper implements RowMapper {

        @Override
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            ThreadPoolStatus tp = new ThreadPoolStatus();
            tp.setThreadPoolName(rs.getString(TP_NAME));
            tp.setActiveSize(rs.getInt(ACTIVE_COUNT));
            tp.setCompletedTask(rs.getLong(COMPLETED_TASK));
            tp.setPoolSize(rs.getInt(POOL_SIZE));
            tp.setTaskQueue(rs.getInt(TASK_QUEUE_SIZE));
            tp.setTotalTask(rs.getLong(TOTAL_TASK));
            return tp;
        }

    }

    private static class DataNodesStatusRowMapper implements RowMapper {

        @Override
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            DataNodesStatus ds = new DataNodesStatus();
            ds.setPoolName(rs.getString(POOL_NAME));
            ds.setDataSource(rs.getString(DS));
            if (null == rs.getString(TYPE)) {
                ds.setType("NULL");
                ds.setActive(-1);
                ds.setIdle(-1);
                ds.setIndex(-1);
                ds.setSize(-1);
            } else {
                ds.setType(rs.getString(TYPE));
                ds.setActive(rs.getInt(ACTIVE));
                ds.setIdle(rs.getInt(IDLE));
                ds.setSize(rs.getInt(SIZE));
                ds.setIndex(rs.getInt(INDEX));
            }
            ds.setExecuteCount(rs.getLong(EXECUTE));
            ds.setMaxTime(rs.getLong(MAX_TIME));
            ds.setMaxSQL(rs.getLong(MAX_SQL));
            ds.setRecoveryTime(rs.getLong(RECOVERY_TIME));
            ds.setTotalTime(rs.getLong(TOTAL_TIME));
            ds.setSampleTimeStamp(System.currentTimeMillis());
            return ds;
        }
    }

    private static class ConnectionRowMapper implements RowMapper {

        @Override
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            ConnectionStatus c = new ConnectionStatus();
            c.setProcessor(rs.getString(C_PROCESSOR));

            c.setHost(rs.getString(HOST));
            c.setPort(rs.getInt(PORT));
            c.setLocal_port(rs.getInt(LOCAL_PORT));
            if (null == rs.getString(SCHEMA)) {
                c.setSchema("NULL");
            } else {
                c.setSchema(rs.getString(SCHEMA));
            }
            c.setCharset(rs.getString(CHARSET));
            c.setNetIn(rs.getLong(C_NET_IN));
            c.setNetOut(rs.getLong(C_NET_OUT));
            c.setAliveTime(rs.getLong(ALIVE_TIME));
            c.setAttempsCount(rs.getInt(ATTEMPS_COUNT));
            c.setRecvBuffer(rs.getInt(RECV_BUFFER));
            c.setSendQueue(rs.getInt(SEND_QUEUE));
            c.setId(rs.getLong(ID));
            c.setChannel(rs.getInt(CHENNEL));
            c.setSampleTimeStamp(System.currentTimeMillis());
            return c;
        }
    }

    private static class CommandStatusRowMapper implements RowMapper {

        @Override
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            CommandStatus cmd = new CommandStatus();
            cmd.setProcessorId(rs.getString(CMD_PROCESSOR));
            cmd.setQuery(rs.getLong(QUERY));
            cmd.setStmtExecute(rs.getLong(STMT_EXECUTE));
            cmd.setStmtPrepared(rs.getLong(STMT_PREPARED));
            cmd.setStmtClose(rs.getLong(STMT_CLOSE));
            cmd.setQuit(rs.getLong(QUIT));
            cmd.setPing(rs.getLong(PING));
            cmd.setOther(rs.getLong(OTHER));
            cmd.setKill(rs.getLong(KILL));
            cmd.setInitDB(rs.getLong(INIT_DB));
            cmd.setSampleTimeStamp(System.currentTimeMillis());
            return cmd;
        }
    }

    private static class DataSourceRowMapper implements RowMapper {

        @Override
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            DataSources ds = new DataSources();
            ds.setName(rs.getString(DS_NAME));
            ds.setType(rs.getString(DS_TYPE));
            ds.setHost(rs.getString(DS_HOST));
            ds.setPort(rs.getInt(DS_PORT));
            ds.setSchema(rs.getString(DS_SCHEMA));
            return ds;
        }
    }

    private TimeStampRowMapper timeStampRowMapper = new TimeStampRowMapper();
    private ServerStatusRowMapper serverStatusRowMapper = new ServerStatusRowMapper();
    private ProccessorStatusRowMapper proccessorStatusRowMapper = new ProccessorStatusRowMapper();
    private ThreadPoolStatusRowMapper threadPoolStatusRowMapper = new ThreadPoolStatusRowMapper();
    private DataNodesStatusRowMapper dataNodesStatusRowMapper = new DataNodesStatusRowMapper();
    private ConnectionRowMapper connectionRowMapper = new ConnectionRowMapper();
    private CommandStatusRowMapper commandStatusRowMapper = new CommandStatusRowMapper();
    private DataSourceRowMapper dataSourcesRowMapper = new DataSourceRowMapper();

    @Override
    public TimeStamp getCurrentTime() {
        try {
            return (TimeStamp) getJdbcTemplate().queryForObject(SHOW_TIME_CURRENT, timeStampRowMapper);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public TimeStamp getStartUpTime() {
        try {
            return (TimeStamp) getJdbcTemplate().queryForObject(SHOW_TIME_STARTUP, timeStampRowMapper);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public String getVersion() {
        try {
            return (String) getJdbcTemplate().queryForObject(SHOW_VERSION, new RowMapper() {

                @Override
                public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                    String version = rs.getString(VERSION);
                    return version;
                }

            });
        } catch (EmptyResultDataAccessException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public ServerStatus getServerStatus() {
        try {
            return (ServerStatus) getJdbcTemplate().queryForObject(SHOW_SERVER, serverStatusRowMapper);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<ProcessorStatus> listProccessorStatus() {
        return getJdbcTemplate().query(SHOW_PROCESSOR, proccessorStatusRowMapper);
    }

    @Override
    public List<ThreadPoolStatus> listThreadPoolStatus() {
        return getJdbcTemplate().query(SHOW_THREADPOOL, threadPoolStatusRowMapper);
    }

    @Override
    public List<String> listDataBases() {
        return getJdbcTemplate().query(SHOW_DATABASE, new RowMapper() {

            @Override
            public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                String db = rs.getString(DATABASE);
                return db;
            }

        });
    }

    @Override
    public List<DataNodesStatus> listDataNodes() {
        return getJdbcTemplate().query(SHOW_DATANODE, dataNodesStatusRowMapper);
    }

    @Override
    public List<DataSources> listDataSources() {
        return getJdbcTemplate().query(SHOW_DATASOURCE, dataSourcesRowMapper);
    }

    @Override
    public List<ConnectionStatus> listConnectionStatus() {
        return getJdbcTemplate().query(SHOW_CONNECTION, connectionRowMapper);
    }

    @Override
    public List<CommandStatus> listCommandStatus() {
        return getJdbcTemplate().query(SHOW_COMMAND, commandStatusRowMapper);
    }

    @Override
    public Pair<Long, Long> getCurrentTimeMillis() {
        return (Pair<Long, Long>) getJdbcTemplate().execute(new StatementCallback() {
            @Override
            public Object doInStatement(Statement stmt) throws SQLException, DataAccessException {
                ResultSet rs = null;
                try {
                    long time1 = System.currentTimeMillis();
                    rs = stmt.executeQuery("show @@status.time");
                    long time2 = System.currentTimeMillis();
                    if (rs.next()) {
                        return new Pair<Long, Long>(time1 + (time2 - time1) / 2, rs.getLong(1));
                    } else {
                        throw new IncorrectResultSizeDataAccessException(1, 0);
                    }
                } finally {
                    if (rs != null) {
                        rs.close();
                    }
                }
            }
        });
    }

    @Override
    public int switchDataNode(String datanodes, int index) {
        StringBuilder sb = new StringBuilder(SWITCH_DATASOURCE);
        sb.append(datanodes);
        sb.append(":").append(index);
        String sql = sb.toString();
        return getJdbcTemplate().update(sql);
    }

    @Override
    public int killConnection(long id) {
        StringBuilder sb = new StringBuilder(KILL_CONNECTION);
        sb.append(id);
        String sql = sb.toString();
        return getJdbcTemplate().update(sql);
    }

    @Override
    public boolean reloadConfig() {
        try {
            getJdbcTemplate().update(RELOAD_CONFIG);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean rollbackConfig() {
        try {
            getJdbcTemplate().update(ROLLBACK_CONFIG);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public int stopHeartbeat(String datanodes, int time) {
        int t = time * 3600;
        StringBuilder sb = new StringBuilder(STOP_HEARTBEAT);
        sb.append(datanodes);
        sb.append(":").append(t);
        String sql = sb.toString();
        return getJdbcTemplate().update(sql);
    }

    @Override
    public boolean checkConnection() {
        try {
            return null != getVersion();
        } catch (CannotGetJdbcConnectionException ex) {
            logger.error(new StringBuilder("checkConnection error for Url:").append(((BasicDataSource) this.getDataSource()).getUrl())
                                                                            .toString());
            return false;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean setCobarStatus(boolean status) {
        try {
            if (status) {
                getJdbcTemplate().update(ONLINE);
            } else {
                getJdbcTemplate().update(OFFLINE);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
