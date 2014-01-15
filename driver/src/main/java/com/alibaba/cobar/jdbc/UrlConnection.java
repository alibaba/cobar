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
package com.alibaba.cobar.jdbc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.alibaba.cobar.jdbc.packet.AuthPacket;
import com.alibaba.cobar.jdbc.packet.BinaryPacket;
import com.alibaba.cobar.jdbc.packet.CommandPacket;
import com.alibaba.cobar.jdbc.packet.EOFPacket;
import com.alibaba.cobar.jdbc.packet.ErrorPacket;
import com.alibaba.cobar.jdbc.packet.HandshakePacket;
import com.alibaba.cobar.jdbc.packet.OkPacket;
import com.alibaba.cobar.jdbc.packet.QuitPacket;
import com.alibaba.cobar.jdbc.packet.Reply323Packet;
import com.alibaba.cobar.jdbc.packet.ResultSetHeaderPacket;
import com.alibaba.cobar.jdbc.packet.RowDataPacket;
import com.alibaba.cobar.jdbc.util.Capabilities;
import com.alibaba.cobar.jdbc.util.ErrorPacketException;
import com.alibaba.cobar.jdbc.util.SecurityUtil;
import com.alibaba.cobar.jdbc.util.UnknownPacketException;

/**
 * @author xianmao.hexm 2012-4-27
 */
public class UrlConnection {

    private static final int RECV_BUFFER_SIZE = 1024;
    private static final int SEND_BUFFER_SIZE = 1024;
    private static final int INPUT_STREAM_BUFFER = 1024;
    private static final int OUTPUT_STREAM_BUFFER = 1024;
    private static final long MAX_PACKET_SIZE = 1024 * 1024 * 16;
    private static final long CLIENT_FLAGS = getClientFlags();
    private static final CommandPacket CLUSTER_CMD = new CommandPacket();
    static {
        CLUSTER_CMD.packetId = 0;
        CLUSTER_CMD.command = 3;
        CLUSTER_CMD.arg = "SHOW COBAR_CLUSTER".getBytes();
    }

    private String host;
    private int port;
    private String user;
    private String password;
    private String database;
    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private int charsetIndex;
    private AtomicBoolean isClosed;

    public UrlConnection(String host, int port, String user, String password, String database) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.database = database;
        this.isClosed = new AtomicBoolean(false);
    }

    public void connect(int timeout) throws Exception {
        // 网络IO参数设置
        socket = new Socket();
        socket.setTcpNoDelay(true);
        socket.setKeepAlive(true);
        socket.setReceiveBufferSize(RECV_BUFFER_SIZE);
        socket.setSendBufferSize(SEND_BUFFER_SIZE);
        socket.connect(new InetSocketAddress(host, port), timeout);
        in = new BufferedInputStream(socket.getInputStream(), INPUT_STREAM_BUFFER);
        out = new BufferedOutputStream(socket.getOutputStream(), OUTPUT_STREAM_BUFFER);

        // 读取握手数据包
        BinaryPacket initPacket = new BinaryPacket();
        initPacket.read(in);
        HandshakePacket hsp = new HandshakePacket();
        hsp.read(initPacket);

        // 设置连接参数
        this.charsetIndex = hsp.serverCharsetIndex & 0xff;

        // 发送认证数据包
        BinaryPacket bin = null;
        try {
            bin = auth411(hsp);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
        switch (bin.value[0]) {
        case OkPacket.FIELD_COUNT:
            break;
        case ErrorPacket.FIELD_COUNT:
            ErrorPacket err = new ErrorPacket();
            err.read(bin);
            throw new ErrorPacketException(new String(err.message));
        case EOFPacket.FIELD_COUNT:
            auth323(bin.packetId, hsp.seed);
            break;
        default:
            throw new UnknownPacketException(bin.toString());
        }
    }

    /**
     * 取得集群有效服务器列表
     */
    public List<CobarNode> getServerList() throws IOException {
        // 发送命令数据包
        CLUSTER_CMD.write(out);
        out.flush();

        // 处理响应结果集
        int fieldCount = 0;
        BinaryPacket bin = new BinaryPacket();
        bin.read(in);
        switch (bin.value[0]) {
        case ErrorPacket.FIELD_COUNT: {
            ErrorPacket err = new ErrorPacket();
            err.read(bin);
            throw new ErrorPacketException(new String(err.message));
        }
        default:
            ResultSetHeaderPacket rsh = new ResultSetHeaderPacket();
            rsh.read(bin);
            fieldCount = rsh.fieldCount;
        }
        for (;;) {
            bin = new BinaryPacket();
            bin.read(in);
            switch (bin.value[0]) {
            case ErrorPacket.FIELD_COUNT: {
                ErrorPacket err = new ErrorPacket();
                err.read(bin);
                throw new ErrorPacketException(new String(err.message));
            }
            case EOFPacket.FIELD_COUNT: {
                return getRowList(fieldCount);
            }
            default:
                continue;
            }
        }
    }

    /**
     * 关闭连接
     */
    public void close() {
        if (isClosed.compareAndSet(false, true)) {
            try {
                if (out != null) {
                    out.write(QuitPacket.QUIT);
                    out.flush();
                }
            } catch (IOException e) {
                //ignore log
            } finally {
                try {
                    socket.close();
                } catch (Throwable e) {
                    //ignore log
                }
            }
        }
    }

    private List<CobarNode> getRowList(int fieldCount) throws IOException {
        List<CobarNode> list = new LinkedList<CobarNode>();
        BinaryPacket bin = null;
        for (;;) {
            bin = new BinaryPacket();
            bin.read(in);
            switch (bin.value[0]) {
            case ErrorPacket.FIELD_COUNT: {
                ErrorPacket err = new ErrorPacket();
                err.read(bin);
                throw new ErrorPacketException(new String(err.message));
            }
            case EOFPacket.FIELD_COUNT: {
                return list;
            }
            default:
                RowDataPacket row = new RowDataPacket(fieldCount);
                row.read(bin);
                String host = new String(row.fieldValues.get(0));
                int weight = Integer.parseInt(new String(row.fieldValues.get(1)));
                list.add(new CobarNode(host, weight));
            }
        }
    }

    /**
     * 411协议认证
     */
    private BinaryPacket auth411(HandshakePacket hsp) throws IOException, NoSuchAlgorithmException {
        AuthPacket ap = new AuthPacket();
        ap.packetId = 1;
        ap.clientFlags = CLIENT_FLAGS;
        ap.maxPacketSize = MAX_PACKET_SIZE;
        ap.charsetIndex = charsetIndex;
        ap.user = user;
        String passwd = password;
        if (passwd != null && passwd.length() > 0) {
            byte[] password = passwd.getBytes();
            byte[] seed = hsp.seed;
            byte[] restOfScramble = hsp.restOfScrambleBuff;
            byte[] authSeed = new byte[seed.length + restOfScramble.length];
            System.arraycopy(seed, 0, authSeed, 0, seed.length);
            System.arraycopy(restOfScramble, 0, authSeed, seed.length, restOfScramble.length);
            ap.password = SecurityUtil.scramble411(password, authSeed);
        }
        ap.database = database;
        ap.write(out);
        out.flush();
        BinaryPacket bin = new BinaryPacket();
        bin.read(in);
        return bin;
    }

    /**
     * 323协议认证
     */
    private void auth323(byte packetId, byte[] seed) throws IOException {
        Reply323Packet r323 = new Reply323Packet();
        r323.packetId = ++packetId;
        String passwd = password;
        if (passwd != null && passwd.length() > 0) {
            r323.seed = SecurityUtil.scramble323(passwd, new String(seed)).getBytes();
        }
        r323.write(out);
        out.flush();
        BinaryPacket bin = new BinaryPacket();
        bin.read(in);
        switch (bin.value[0]) {
        case OkPacket.FIELD_COUNT:
            break;
        case ErrorPacket.FIELD_COUNT:
            ErrorPacket err = new ErrorPacket();
            err.read(bin);
            throw new ErrorPacketException(new String(err.message));
        default:
            throw new UnknownPacketException(bin.toString());
        }
    }

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
