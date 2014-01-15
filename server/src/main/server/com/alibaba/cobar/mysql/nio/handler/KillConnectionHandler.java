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
 * (created at 2012-5-12)
 */
package com.alibaba.cobar.mysql.nio.handler;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.alibaba.cobar.mysql.nio.MySQLConnection;
import com.alibaba.cobar.net.mysql.CommandPacket;
import com.alibaba.cobar.net.mysql.ErrorPacket;
import com.alibaba.cobar.net.mysql.MySQLPacket;
import com.alibaba.cobar.server.session.NonBlockingSession;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class KillConnectionHandler implements ResponseHandler {
    private static final Logger LOGGER = Logger.getLogger(KillConnectionHandler.class);

    private final MySQLConnection killee;
    private final NonBlockingSession session;
    private final Runnable finishHook;
    private final AtomicInteger counter;

    public KillConnectionHandler(MySQLConnection killee, NonBlockingSession session, Runnable finishHook,
                                 AtomicInteger counter) {
        this.killee = killee;
        this.session = session;
        this.finishHook = finishHook;
        this.counter = counter;
    }

    @Override
    public void connectionAcquired(MySQLConnection conn) {
        conn.setResponseHandler(this);
        CommandPacket packet = new CommandPacket();
        packet.packetId = 0;
        packet.command = MySQLPacket.COM_QUERY;
        packet.arg = new StringBuilder("KILL ").append(killee.getThreadId()).toString().getBytes();
        packet.write(conn);
    }

    private void finished() {
        if (counter.decrementAndGet() <= 0) {
            finishHook.run();
        }
    }

    @Override
    public void connectionError(Throwable e, MySQLConnection conn) {
        if (conn != null) {
            conn.close();
        }
        killee.close();
        finished();
    }

    @Override
    public void okResponse(byte[] ok, MySQLConnection conn) {
        conn.release();
        killee.close();
        finished();
    }

    @Override
    public void rowEofResponse(byte[] eof, MySQLConnection conn) {
        LOGGER.error(new StringBuilder().append("unexpected packet for ")
                                        .append(conn)
                                        .append(" bound by ")
                                        .append(session.getSource())
                                        .append(": field's eof")
                                        .toString());
        conn.quit();
        killee.close();
        finished();
    }

    @Override
    public void errorResponse(byte[] data, MySQLConnection conn) {
        ErrorPacket err = new ErrorPacket();
        err.read(data);
        String msg = null;
        try {
            msg = new String(err.message, conn.getCharset());
        } catch (UnsupportedEncodingException e) {
            msg = new String(err.message);
        }
        LOGGER.warn("kill backend connection " + killee + " failed: " + msg);
        conn.release();
        killee.close();
        finished();
    }

    @Override
    public void fieldEofResponse(byte[] header, List<byte[]> fields, byte[] eof, MySQLConnection conn) {
    }

    @Override
    public void rowResponse(byte[] row, MySQLConnection conn) {
    }

}
