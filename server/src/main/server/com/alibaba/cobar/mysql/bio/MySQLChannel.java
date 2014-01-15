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
package com.alibaba.cobar.mysql.bio;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.alibaba.cobar.CobarServer;
import com.alibaba.cobar.config.Capabilities;
import com.alibaba.cobar.config.Isolations;
import com.alibaba.cobar.config.model.DataSourceConfig;
import com.alibaba.cobar.exception.ErrorPacketException;
import com.alibaba.cobar.exception.UnknownCharsetException;
import com.alibaba.cobar.exception.UnknownPacketException;
import com.alibaba.cobar.exception.UnknownTxIsolationException;
import com.alibaba.cobar.mysql.CharsetUtil;
import com.alibaba.cobar.mysql.MySQLDataSource;
import com.alibaba.cobar.mysql.SecurityUtil;
import com.alibaba.cobar.net.mysql.AuthPacket;
import com.alibaba.cobar.net.mysql.BinaryPacket;
import com.alibaba.cobar.net.mysql.CommandPacket;
import com.alibaba.cobar.net.mysql.EOFPacket;
import com.alibaba.cobar.net.mysql.ErrorPacket;
import com.alibaba.cobar.net.mysql.HandshakePacket;
import com.alibaba.cobar.net.mysql.MySQLPacket;
import com.alibaba.cobar.net.mysql.OkPacket;
import com.alibaba.cobar.net.mysql.QuitPacket;
import com.alibaba.cobar.net.mysql.Reply323Packet;
import com.alibaba.cobar.route.RouteResultsetNode;
import com.alibaba.cobar.server.ServerConnection;
import com.alibaba.cobar.statistic.SQLRecord;
import com.alibaba.cobar.statistic.SQLRecorder;
import com.alibaba.cobar.util.StringUtil;
import com.alibaba.cobar.util.TimeUtil;

/**
 * @author xianmao.hexm 2011-5-5 下午01:01:20
 */
public final class MySQLChannel implements Channel {
    private static final Logger LOGGER = Logger.getLogger(MySQLChannel.class);
    private static final int RECV_BUFFER_SIZE = 16 * 1024;
    private static final int SEND_BUFFER_SIZE = 8 * 1024;
    private static final int INPUT_STREAM_BUFFER = 16 * 1024;
    private static final int OUTPUT_STREAM_BUFFER = 8 * 1024;
    private static final int SOCKET_CONNECT_TIMEOUT = 10 * 1000;
    private static final long CLIENT_FLAGS = getClientFlags();
    private static final long MAX_PACKET_SIZE = 1024 * 1024 * 16;
    private static final CommandPacket _READ_UNCOMMITTED = new CommandPacket();
    private static final CommandPacket _READ_COMMITTED = new CommandPacket();
    private static final CommandPacket _REPEATED_READ = new CommandPacket();
    private static final CommandPacket _SERIALIZABLE = new CommandPacket();
    private static final CommandPacket _AUTOCOMMIT_ON = new CommandPacket();
    private static final CommandPacket _AUTOCOMMIT_OFF = new CommandPacket();
    private static final CommandPacket _COMMIT = new CommandPacket();
    private static final CommandPacket _ROLLBACK = new CommandPacket();
    static {
        _READ_UNCOMMITTED.packetId = 0;
        _READ_UNCOMMITTED.command = MySQLPacket.COM_QUERY;
        _READ_UNCOMMITTED.arg = "SET SESSION TRANSACTION ISOLATION LEVEL READ UNCOMMITTED".getBytes();
        _READ_COMMITTED.packetId = 0;
        _READ_COMMITTED.command = MySQLPacket.COM_QUERY;
        _READ_COMMITTED.arg = "SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED".getBytes();
        _REPEATED_READ.packetId = 0;
        _REPEATED_READ.command = MySQLPacket.COM_QUERY;
        _REPEATED_READ.arg = "SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ".getBytes();
        _SERIALIZABLE.packetId = 0;
        _SERIALIZABLE.command = MySQLPacket.COM_QUERY;
        _SERIALIZABLE.arg = "SET SESSION TRANSACTION ISOLATION LEVEL SERIALIZABLE".getBytes();
        _AUTOCOMMIT_ON.packetId = 0;
        _AUTOCOMMIT_ON.command = MySQLPacket.COM_QUERY;
        _AUTOCOMMIT_ON.arg = "SET autocommit=1".getBytes();
        _AUTOCOMMIT_OFF.packetId = 0;
        _AUTOCOMMIT_OFF.command = MySQLPacket.COM_QUERY;
        _AUTOCOMMIT_OFF.arg = "SET autocommit=0".getBytes();
        _COMMIT.packetId = 0;
        _COMMIT.command = MySQLPacket.COM_QUERY;
        _COMMIT.arg = "commit".getBytes();
        _ROLLBACK.packetId = 0;
        _ROLLBACK.command = MySQLPacket.COM_QUERY;
        _ROLLBACK.arg = "rollback".getBytes();
    }

    private final MySQLDataSource dataSource;
    private final DataSourceConfig dsc;
    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private long threadId;
    private int charsetIndex;
    private String charset;
    private volatile int txIsolation;
    private volatile boolean autocommit;
    private volatile boolean isRunning;
    private final AtomicBoolean isClosed;
    private long lastActiveTime;

    public MySQLChannel(MySQLDataSource dataSource) {
        this.dataSource = dataSource;
        this.dsc = dataSource.getConfig();
        this.autocommit = true;
        this.isRunning = false;
        this.isClosed = new AtomicBoolean(false);
        this.lastActiveTime = TimeUtil.currentTimeMillis();
    }

    public String getCharset() {
        return charset;
    }

    @Override
    public long getLastAcitveTime() {
        return lastActiveTime;
    }

    @Override
    public void setLastActiveTime(long time) {
        this.lastActiveTime = time;
    }

    @Override
    public boolean isAutocommit() {
        return autocommit;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void setRunning(boolean running) {
        this.isRunning = running;
    }

    @Override
    public void connect(long timeout) throws Exception {
        // 网络IO参数设置
        socket = new Socket();
        socket.setTcpNoDelay(true);
        socket.setTrafficClass(0x04 | 0x10);
        socket.setPerformancePreferences(0, 2, 1);
        socket.setReceiveBufferSize(RECV_BUFFER_SIZE);
        socket.setSendBufferSize(SEND_BUFFER_SIZE);
        socket.connect(new InetSocketAddress(dsc.getHost(), dsc.getPort()), SOCKET_CONNECT_TIMEOUT);
        in = new BufferedInputStream(socket.getInputStream(), INPUT_STREAM_BUFFER);
        out = new BufferedOutputStream(socket.getOutputStream(), OUTPUT_STREAM_BUFFER);

        // 完成连接和初始化
        FutureTask<Channel> ft = new FutureTask<Channel>(new Callable<Channel>() {

            @Override
            public Channel call() throws Exception {
                return handshake();
            }
        });
        CobarServer.getInstance().getInitExecutor().execute(ft);
        try {
            ft.get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            ft.cancel(true);
            throw e;
        } catch (ExecutionException e) {
            ft.cancel(true);
            throw e;
        } catch (TimeoutException e) {
            ft.cancel(true);
            throw e;
        }
    }

    public BinaryPacket execute(RouteResultsetNode rrn, ServerConnection sc, boolean autocommit) throws IOException {
        // 状态一致性检查
        if (this.charsetIndex != sc.getCharsetIndex()) {
            sendCharset(sc.getCharsetIndex());
        }
        if (this.txIsolation != sc.getTxIsolation()) {
            sendTxIsolation(sc.getTxIsolation());
        }
        if (this.autocommit != autocommit) {
            sendAutocommit(autocommit);
        }

        // 生成执行数据包
        CommandPacket packet = new CommandPacket();
        packet.packetId = 0;
        packet.command = MySQLPacket.COM_QUERY;
        packet.arg = rrn.getStatement().getBytes(charset);

        // 记录执行开始时间
        lastActiveTime = TimeUtil.currentTimeMillis();

        // 递交执行数据包并等待执行返回
        packet.write(out);
        out.flush();
        BinaryPacket bin = receive();

        // SQL执行时间统计
        long now = TimeUtil.currentTimeMillis();
        if (now > lastActiveTime) {
            recordSql(sc.getHost(), sc.getSchema(), rrn.getStatement());
        }

        // 记录执行结束时间
        lastActiveTime = now;
        return bin;
    }

    public BinaryPacket receive() throws IOException {
        BinaryPacket bin = new BinaryPacket();
        bin.read(in);
        return bin;
    }

    public BinaryPacket commit() throws IOException {
        _COMMIT.write(out);
        out.flush();
        return receive();
    }

    public BinaryPacket rollback() throws IOException {
        _ROLLBACK.write(out);
        out.flush();
        return receive();
    }

    @Override
    public boolean isClosed() {
        return isClosed.get();
    }

    @Override
    public void kill() {
        if (isClosed.compareAndSet(false, true)) {
            killChannel();
            try {
                mysqlClose();
            } finally {
                dataSource.deActive();
            }
        }
    }

    @Override
    public void close() {
        if (isClosed.compareAndSet(false, true)) {
            try {
                mysqlClose();
            } finally {
                dataSource.deActive();
            }
        }
    }

    @Override
    public void closeNoActive() {
        if (isClosed.compareAndSet(false, true)) {
            mysqlClose();
        }
    }

    @Override
    public void release() {
        dataSource.releaseChannel(this);
    }

    public String getErrMessage(BinaryPacket bin) {
        String message = null;
        ErrorPacket err = new ErrorPacket();
        err.read(bin);
        if (err.message != null) {
            message = StringUtil.decode(err.message, charset);
        }
        return message;
    }

    public String getErrLog(String stmt, String info, ServerConnection source) {
        StringBuilder s = new StringBuilder();
        s.append("\n MSG:").append(info);
        s.append("\n ROUTE:").append(source).append(" -> ").append(this);
        s.append("\n SQL:").append(stmt);
        return s.toString();
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("[host=").append(dsc.getHost()).append(",port=").append(dsc.getPort());
        s.append(",schema=").append(dsc.getDatabase()).append(']');
        return s.toString();
    }

    private MySQLChannel handshake() throws IOException {
        // 读取握手数据包
        BinaryPacket initPacket = new BinaryPacket();
        initPacket.read(in);
        HandshakePacket hsp = new HandshakePacket();
        hsp.read(initPacket);

        // 设置通道参数
        this.threadId = hsp.threadId;
        int ci = hsp.serverCharsetIndex & 0xff;
        if ((charset = CharsetUtil.getCharset(ci)) != null) {
            this.charsetIndex = ci;
        } else {
            throw new UnknownCharsetException("charset:" + ci);
        }

        // 发送认证数据包
        BinaryPacket bin = null;
        try {
            bin = sendAuth411(hsp);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        switch (bin.data[0]) {
        case OkPacket.FIELD_COUNT:
            afterSuccess();
            break;
        case ErrorPacket.FIELD_COUNT:
            ErrorPacket err = new ErrorPacket();
            err.read(bin);
            throw new ErrorPacketException(new String(err.message, charset));
        case EOFPacket.FIELD_COUNT:
            auth323(bin.packetId, hsp.seed);
            break;
        default:
            throw new UnknownPacketException(bin.toString());
        }

        return this;
    }

    /**
     * 发送411协议的认证数据包
     */
    private BinaryPacket sendAuth411(HandshakePacket hsp) throws IOException, NoSuchAlgorithmException {
        AuthPacket ap = new AuthPacket();
        ap.packetId = 1;
        ap.clientFlags = CLIENT_FLAGS;
        ap.maxPacketSize = MAX_PACKET_SIZE;
        ap.charsetIndex = charsetIndex;
        ap.user = dsc.getUser();
        String passwd = dsc.getPassword();
        if (passwd != null && passwd.length() > 0) {
            byte[] password = passwd.getBytes(charset);
            byte[] seed = hsp.seed;
            byte[] restOfScramble = hsp.restOfScrambleBuff;
            byte[] authSeed = new byte[seed.length + restOfScramble.length];
            System.arraycopy(seed, 0, authSeed, 0, seed.length);
            System.arraycopy(restOfScramble, 0, authSeed, seed.length, restOfScramble.length);
            ap.password = SecurityUtil.scramble411(password, authSeed);
        }
        ap.database = dsc.getDatabase();
        ap.write(out);
        out.flush();
        return receive();
    }

    /**
     * 323协议认证
     */
    private void auth323(byte packetId, byte[] seed) throws IOException {
        Reply323Packet r323 = new Reply323Packet();
        r323.packetId = ++packetId;
        String passwd = dsc.getPassword();
        if (passwd != null && passwd.length() > 0) {
            r323.seed = SecurityUtil.scramble323(passwd, new String(seed)).getBytes();
        }
        r323.write(out);
        out.flush();
        BinaryPacket bin = receive();
        switch (bin.data[0]) {
        case OkPacket.FIELD_COUNT:
            afterSuccess();
            break;
        case ErrorPacket.FIELD_COUNT:
            ErrorPacket err = new ErrorPacket();
            err.read(bin);
            throw new ErrorPacketException(new String(err.message, charset));
        default:
            throw new UnknownPacketException(bin.toString());
        }
    }

    /**
     * 连接和验证成功以后
     */
    private void afterSuccess() throws IOException {
        if (dsc.getSqlMode() != null) {
            sendSqlMode();
        }
        // 为防止握手阶段字符集编码交互无效，连接成功之后做一次字符集编码同步。
        sendCharset(charsetIndex);
    }

    /**
     * 发送SQL_MODE设置
     */
    private void sendSqlMode() throws IOException {
        CommandPacket cmd = getSqlModeCommand();
        cmd.write(out);
        out.flush();
        BinaryPacket bin = receive();
        switch (bin.data[0]) {
        case OkPacket.FIELD_COUNT:
            break;
        case ErrorPacket.FIELD_COUNT:
            ErrorPacket err = new ErrorPacket();
            err.read(bin);
            throw new ErrorPacketException(new String(err.message, charset));
        default:
            throw new UnknownPacketException(bin.toString());
        }
    }

    /**
     * 发送字符集设置
     */
    private void sendCharset(int ci) throws IOException {
        CommandPacket cmd = getCharsetCommand(ci);
        cmd.write(out);
        out.flush();
        BinaryPacket bin = receive();
        switch (bin.data[0]) {
        case OkPacket.FIELD_COUNT:
            this.charsetIndex = ci;
            this.charset = CharsetUtil.getCharset(ci);
            break;
        case ErrorPacket.FIELD_COUNT:
            ErrorPacket err = new ErrorPacket();
            err.read(bin);
            throw new ErrorPacketException(new String(err.message, charset));
        default:
            throw new UnknownPacketException(bin.toString());
        }
    }

    /**
     * 发送事务级别设置
     */
    private void sendTxIsolation(int txIsolation) throws IOException {
        CommandPacket cmd = getTxIsolationCommand(txIsolation);
        cmd.write(out);
        out.flush();
        BinaryPacket bin = receive();
        switch (bin.data[0]) {
        case OkPacket.FIELD_COUNT:
            this.txIsolation = txIsolation;
            break;
        case ErrorPacket.FIELD_COUNT:
            ErrorPacket err = new ErrorPacket();
            err.read(bin);
            throw new ErrorPacketException(new String(err.message, charset));
        default:
            throw new UnknownPacketException(bin.toString());
        }
    }

    /**
     * 发送事务递交模式设置
     */
    private void sendAutocommit(boolean autocommit) throws IOException {
        CommandPacket cmd = getAutocommitCommand(autocommit);
        cmd.write(out);
        out.flush();
        BinaryPacket bin = receive();
        switch (bin.data[0]) {
        case OkPacket.FIELD_COUNT:
            this.autocommit = autocommit;
            break;
        case ErrorPacket.FIELD_COUNT:
            ErrorPacket err = new ErrorPacket();
            err.read(bin);
            throw new ErrorPacketException(new String(err.message, charset));
        default:
            throw new UnknownPacketException(bin.toString());
        }
    }

    private CommandPacket getSqlModeCommand() {
        StringBuilder s = new StringBuilder();
        s.append("SET sql_mode=\"").append(dsc.getSqlMode()).append('"');
        CommandPacket cmd = new CommandPacket();
        cmd.packetId = 0;
        cmd.command = MySQLPacket.COM_QUERY;
        cmd.arg = s.toString().getBytes();
        return cmd;
    }

    private CommandPacket getCharsetCommand(int ci) {
        String charset = CharsetUtil.getCharset(ci);
        StringBuilder s = new StringBuilder();
        s.append("SET names ").append(charset);
        CommandPacket cmd = new CommandPacket();
        cmd.packetId = 0;
        cmd.command = MySQLPacket.COM_QUERY;
        cmd.arg = s.toString().getBytes();
        return cmd;
    }

    private CommandPacket getTxIsolationCommand(int txIsolation) {
        switch (txIsolation) {
        case Isolations.READ_UNCOMMITTED:
            return _READ_UNCOMMITTED;
        case Isolations.READ_COMMITTED:
            return _READ_COMMITTED;
        case Isolations.REPEATED_READ:
            return _REPEATED_READ;
        case Isolations.SERIALIZABLE:
            return _SERIALIZABLE;
        default:
            throw new UnknownTxIsolationException("txIsolation:" + txIsolation);
        }
    }

    private CommandPacket getAutocommitCommand(boolean autocommit) {
        return autocommit ? _AUTOCOMMIT_ON : _AUTOCOMMIT_OFF;
    }

    private void killChannel() {
        MySQLChannel killChannel = null;
        try {
            killChannel = (MySQLChannel) dataSource.getChannel();
        } catch (Exception e) {
            LOGGER.error("killProcess failure for getting channel", e);
            return;
        }
        CommandPacket killPacket = new CommandPacket();
        killPacket.packetId = 0;
        killPacket.command = MySQLPacket.COM_QUERY;
        killPacket.arg = new StringBuilder("KILL ").append(threadId).toString().getBytes();
        try {
            killPacket.write(killChannel.out);
            killChannel.out.flush();
            BinaryPacket bin = new BinaryPacket();
            bin.read(killChannel.in);
            switch (bin.data[0]) {
            case OkPacket.FIELD_COUNT:
                killChannel.release();
                break;
            case ErrorPacket.FIELD_COUNT:
                LOGGER.error("kill error! id:" + threadId + ", err=" + bin);
                killChannel.release();
                break;
            default:
                LOGGER.error("kill unknown response, id:" + threadId + "packet=" + bin);
                killChannel.close();
            }
        } catch (IOException e) {
            killChannel.close();
            LOGGER.error("kill IOException, id:" + threadId, e);
        }
    }

    /**
     * 关闭连接之前先尝试发送quit数据包
     */
    private void mysqlClose() {
        try {
            if (out != null) {
                out.write(QuitPacket.QUIT);
                out.flush();
            }
        } catch (IOException e) {
            LOGGER.error(toString(), e);
        } finally {
            try {
                socket.close();
            } catch (Throwable e) {
                LOGGER.error(toString(), e);
            }
        }
    }

    /**
     * 记录sql执行信息
     */
    private void recordSql(String host, String schema, String stmt) {
        long time = TimeUtil.currentTimeMillis() - lastActiveTime;
        SQLRecorder sqlRecorder = dataSource.getSqlRecorder();
        if (sqlRecorder.check(time)) {
            SQLRecord recorder = new SQLRecord();
            recorder.host = host;
            recorder.schema = schema;
            recorder.statement = stmt;
            recorder.startTime = lastActiveTime;
            recorder.executeTime = time;
            recorder.dataNode = dataSource.getName();
            recorder.dataNodeIndex = dataSource.getIndex();
            sqlRecorder.add(recorder);
        }
    }

    /**
     * 与MySQL连接时的一些特性指定
     */
    private static long getClientFlags() {
        int flag = 0;
        flag |= Capabilities.CLIENT_LONG_PASSWORD;
        flag |= Capabilities.CLIENT_FOUND_ROWS;
        flag |= Capabilities.CLIENT_LONG_FLAG;
        flag |= Capabilities.CLIENT_CONNECT_WITH_DB;
        // flag |= Capabilities.CLIENT_NO_SCHEMA;
        // flag |= Capabilities.CLIENT_COMPRESS;
        flag |= Capabilities.CLIENT_ODBC;
        // flag |= Capabilities.CLIENT_LOCAL_FILES;
        flag |= Capabilities.CLIENT_IGNORE_SPACE;
        flag |= Capabilities.CLIENT_PROTOCOL_41;
        flag |= Capabilities.CLIENT_INTERACTIVE;
        // flag |= Capabilities.CLIENT_SSL;
        flag |= Capabilities.CLIENT_IGNORE_SIGPIPE;
        flag |= Capabilities.CLIENT_TRANSACTIONS;
        // flag |= Capabilities.CLIENT_RESERVED;
        flag |= Capabilities.CLIENT_SECURE_CONNECTION;
        // client extension
        // flag |= Capabilities.CLIENT_MULTI_STATEMENTS;
        // flag |= Capabilities.CLIENT_MULTI_RESULTS;
        return flag;
    }

}
