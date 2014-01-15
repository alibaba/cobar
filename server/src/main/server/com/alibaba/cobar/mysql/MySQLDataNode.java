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
package com.alibaba.cobar.mysql;

import java.sql.SQLSyntaxErrorException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.alibaba.cobar.config.Alarms;
import com.alibaba.cobar.config.model.DataNodeConfig;
import com.alibaba.cobar.config.util.ConfigException;
import com.alibaba.cobar.heartbeat.MySQLHeartbeat;
import com.alibaba.cobar.mysql.bio.Channel;
import com.alibaba.cobar.mysql.nio.MySQLConnectionPool;
import com.alibaba.cobar.mysql.nio.handler.ResponseHandler;
import com.alibaba.cobar.parser.ast.expression.primary.PlaceHolder;
import com.alibaba.cobar.parser.ast.stmt.SQLStatement;
import com.alibaba.cobar.parser.recognizer.SQLParserDelegate;
import com.alibaba.cobar.parser.visitor.EmptySQLASTVisitor;
import com.alibaba.cobar.parser.visitor.MySQLOutputASTVisitor;
import com.alibaba.cobar.util.TimeUtil;

/**
 * @author xianmao.hexm 2011-4-26 上午11:12:24
 */
public final class MySQLDataNode {
    private static final Logger LOGGER = Logger.getLogger(MySQLDataNode.class);
    private static final Logger ALARM = Logger.getLogger("alarm");

    private final String name;
    private final DataNodeConfig config;
    private MySQLDataSource[] sources;
    private MySQLConnectionPool[] dataSources;
    private int activedIndex;
    private long executeCount;
    private long heartbeatRecoveryTime;
    private volatile boolean initSuccess;
    private final ReentrantLock switchLock;

    private SQLStatement heartbeatAST;// 动态心跳语句AST
    private Map<PlaceHolder, Object> placeHolderToStringer;

    public MySQLDataNode(DataNodeConfig config) {
        this.name = config.getName();
        this.config = config;
        this.setHeartbeat(config.getHeartbeatSQL());
        this.activedIndex = 0;
        this.heartbeatRecoveryTime = -1L;
        this.switchLock = new ReentrantLock();
    }

    public void init(int size, int index) {
        if (!checkIndex(index)) {
            index = 0;
        }
        int active = -1;
        for (int i = 0; i < sources.length; i++) {
            int j = loop(i + index);
            if (initSource(sources[j], size)) {
                active = j;
                break;
            }
        }
        if (checkIndex(active)) {
            activedIndex = active;
            initSuccess = true;
            LOGGER.info(getMessage(active, " init success"));
        } else {
            initSuccess = false;
            StringBuilder s = new StringBuilder();
            s.append(Alarms.DEFAULT).append(name).append(" init failure");
            ALARM.error(s.toString());
        }
    }

    private void setHeartbeat(String heartbeat) {
        if (heartbeat == null) {
            heartbeatAST = null;
            placeHolderToStringer = null;
            return;
        }
        try {
            final Set<PlaceHolder> plist = new HashSet<PlaceHolder>(1, 1);
            SQLStatement ast = SQLParserDelegate.parse(heartbeat);
            ast.accept(new EmptySQLASTVisitor() {
                @Override
                public void visit(PlaceHolder node) {
                    plist.add(node);
                }
            });
            if (plist.isEmpty()) {
                heartbeatAST = null;
                placeHolderToStringer = null;
                return;
            }
            Map<PlaceHolder, Object> phm = new HashMap<PlaceHolder, Object>(plist.size(), 1);
            for (PlaceHolder ph : plist) {
                final String content = ph.getName();
                final int low = Integer.parseInt(content.substring(content.indexOf('(') + 1, content.indexOf(','))
                                                        .trim());
                final int high = Integer.parseInt(content.substring(content.indexOf(',') + 1, content.indexOf(')'))
                                                         .trim());
                phm.put(ph, new Object() {
                    private Random rnd = new Random();

                    @Override
                    public String toString() {
                        return String.valueOf(rnd.nextInt(high - low + 1) + low);
                    }
                });
            }
            heartbeatAST = ast;
            placeHolderToStringer = phm;
        } catch (SQLSyntaxErrorException e) {
            throw new ConfigException("heartbeat syntax err: " + heartbeat, e);
        }
    }

    public String getHeartbeatSQL() {
        if (heartbeatAST == null) {
            return config.getHeartbeatSQL();
        }
        MySQLOutputASTVisitor sqlGen = new MySQLOutputASTVisitor(new StringBuilder());
        sqlGen.setPlaceHolderToString(placeHolderToStringer);
        heartbeatAST.accept(sqlGen);
        return sqlGen.getSql();
    }

    public String getName() {
        return name;
    }

    public DataNodeConfig getConfig() {
        return config;
    }

    public long getExecuteCount() {
        return executeCount;
    }

    public int getActivedIndex() {
        return activedIndex;
    }

    public boolean isInitSuccess() {
        return initSuccess;
    }

    public long getHeartbeatRecoveryTime() {
        return heartbeatRecoveryTime;
    }

    public void setHeartbeatRecoveryTime(long time) {
        this.heartbeatRecoveryTime = time;
    }

    public Channel getChannel() throws Exception {
        return getChannel(activedIndex);
    }

    /**
     * 取得数据源通道
     */
    public Channel getChannel(int i) throws Exception {
        if (initSuccess) {
            Channel c = sources[i].getChannel();
            ++executeCount;
            return c;
        } else {
            throw new IllegalArgumentException("Invalid DataSource:" + i);
        }
    }

    public void getConnection(ResponseHandler handler, Object attachment) throws Exception {
        getConnection(handler, attachment, activedIndex);
    }

    public void getConnection(ResponseHandler handler, Object attachment, int i) throws Exception {
        if (initSuccess) {
            MySQLConnectionPool pool = dataSources[i];
            pool.getConnection(handler, attachment);
        } else {
            throw new IllegalArgumentException("Invalid DataSource:" + activedIndex);
        }
    }

    public MySQLConnectionPool[] getDataSources() {
        return dataSources;
    }

    public void setDataSources(MySQLConnectionPool[] dataSources) {
        this.dataSources = dataSources;
    }

    public MySQLDataSource[] getSources() {
        return sources;
    }

    public void setSources(MySQLDataSource[] sources) {
        this.sources = sources;
    }

    public MySQLDataSource getSource() {
        return sources[activedIndex];
    }

    /**
     * 切换数据源
     */
    public boolean switchSource(int newIndex, boolean isAlarm, String reason) {
        if (!checkIndex(newIndex)) {
            return false;
        }
        final ReentrantLock lock = this.switchLock;
        lock.lock();
        try {
            int current = activedIndex;
            if (current != newIndex) {
                // 清理即将使用的数据源并开启心跳
                sources[newIndex].clear();
                sources[newIndex].startHeartbeat();

                // 执行切换赋值
                activedIndex = newIndex;

                // 清理切换前的数据源
                sources[current].clear();
                sources[current].stopHeartbeat();

                // 记录切换日志
                if (isAlarm) {
                    ALARM.error(switchMessage(current, newIndex, true, reason));
                } else {
                    LOGGER.warn(switchMessage(current, newIndex, false, reason));
                }

                return true;
            }
        } finally {
            lock.unlock();
        }
        return false;
    }

    /**
     * 空闲检查
     */
    public void idleCheck() {
        for (MySQLDataSource ds : sources) {
            if (ds != null) {
                ds.idleCheck(config.getIdleTimeout());
            }
        }
    }

    public MySQLHeartbeat getHeartbeat() {
        MySQLDataSource source = this.getSource();
        if (source != null) {
            return source.getHeartbeat();
        } else {
            StringBuilder s = new StringBuilder();
            s.append(Alarms.DEFAULT).append(name).append(" current dataSource is null!");
            ALARM.error(s.toString());
            return null;
        }
    }

    public void startHeartbeat() {
        MySQLDataSource source = this.getSource();
        if (source != null) {
            source.startHeartbeat();
        } else {
            StringBuilder s = new StringBuilder();
            s.append(Alarms.DEFAULT).append(name).append(" current dataSource is null!");
            ALARM.error(s.toString());
        }
    }

    public void stopHeartbeat() {
        MySQLDataSource source = this.getSource();
        if (source != null) {
            source.stopHeartbeat();
        } else {
            StringBuilder s = new StringBuilder();
            s.append(Alarms.DEFAULT).append(name).append(" current dataSource is null!");
            ALARM.error(s.toString());
        }
    }

    public void doHeartbeat() {
        // 判断是否需要执行心跳检查
        if (!config.isNeedHeartbeat()) {
            return;
        }

        // 检查内部是否有连接池配置信息
        if (sources == null || sources.length == 0) {
            return;
        }

        // 未到预定恢复时间，不执行心跳检测。
        if (TimeUtil.currentTimeMillis() < heartbeatRecoveryTime) {
            return;
        }

        // 准备执行心跳检测
        MySQLDataSource source = this.getSource();
        if (source != null) {
            source.doHeartbeat();
        } else {
            StringBuilder s = new StringBuilder();
            s.append(Alarms.DEFAULT).append(name).append(" current dataSource is null!");
            ALARM.error(s.toString());
        }
    }

    public int next(int i) {
        if (checkIndex(i)) {
            return (++i == sources.length) ? 0 : i;
        } else {
            return 0;
        }
    }

    private int loop(int i) {
        return i < sources.length ? i : (i - sources.length);
    }

    private boolean checkIndex(int i) {
        return i >= 0 && i < sources.length;
    }

    private boolean initSource(MySQLDataSource ds, int size) {
        boolean success = true;
        Channel[] list = new Channel[size < ds.size() ? size : ds.size()];
        for (int i = 0; i < list.length; i++) {
            try {
                list[i] = ds.getChannel();
            } catch (Exception e) {
                success = false;
                LOGGER.warn(getMessage(ds.getIndex(), " init error."), e);
                break;
            }
        }
        for (Channel c : list) {
            if (c == null) {
                continue;
            }
            if (success) {
                c.release();
            } else {
                c.close();
            }
        }
        return success;
    }

    private String getMessage(int index, String info) {
        return new StringBuilder().append(name).append(':').append(index).append(info).toString();
    }

    private String switchMessage(int current, int newIndex, boolean alarm, String reason) {
        StringBuilder s = new StringBuilder();
        if (alarm) {
            s.append(Alarms.DATANODE_SWITCH);
        }
        s.append("[name=").append(name).append(",result=[").append(current).append("->");
        s.append(newIndex).append("],reason=").append(reason).append(']');
        return s.toString();
    }

}
