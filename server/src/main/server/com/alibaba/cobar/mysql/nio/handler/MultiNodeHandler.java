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
 * (created at 2012-5-4)
 */
package com.alibaba.cobar.mysql.nio.handler;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import com.alibaba.cobar.config.ErrorCode;
import com.alibaba.cobar.mysql.nio.MySQLConnection;
import com.alibaba.cobar.net.mysql.ErrorPacket;
import com.alibaba.cobar.server.session.NonBlockingSession;
import com.alibaba.cobar.util.StringUtil;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public abstract class MultiNodeHandler implements ResponseHandler, Terminatable {
    protected final ReentrantLock lock = new ReentrantLock();
    protected final NonBlockingSession session;
    protected AtomicBoolean isFail = new AtomicBoolean(false);
    private ErrorPacket error;
    protected byte packetId;

    public MultiNodeHandler(NonBlockingSession session) {
        if (session == null) {
            throw new IllegalArgumentException("session is null!");
        }
        this.session = session;
    }

    private int nodeCount;
    private Runnable terminateCallBack;

    @Override
    public void terminate(Runnable terminateCallBack) {
        boolean zeroReached = false;
        lock.lock();
        try {
            if (nodeCount > 0) {
                this.terminateCallBack = terminateCallBack;
            } else {
                zeroReached = true;
            }
        } finally {
            lock.unlock();
        }
        if (zeroReached) {
            terminateCallBack.run();
        }
    }

    protected void decrementCountToZero() {
        Runnable callback;
        lock.lock();
        try {
            nodeCount = 0;
            callback = this.terminateCallBack;
            this.terminateCallBack = null;
        } finally {
            lock.unlock();
        }
        if (callback != null) {
            callback.run();
        }
    }

    protected boolean decrementCountBy(int finished) {
        boolean zeroReached = false;
        Runnable callback = null;
        lock.lock();
        try {
            if (zeroReached = --nodeCount == 0) {
                callback = this.terminateCallBack;
                this.terminateCallBack = null;
            }
        } finally {
            lock.unlock();
        }
        if (zeroReached && callback != null) {
            callback.run();
        }
        return zeroReached;
    }

    protected void reset(int initCount) {
        nodeCount = initCount;
        isFail.set(false);
        error = null;
        packetId = 0;
    }

    protected void backendConnError(MySQLConnection conn, String errMsg) {
        ErrorPacket err = new ErrorPacket();
        err.packetId = 1;// ERROR_PACKET
        err.errno = ErrorCode.ER_YES;
        err.message = StringUtil.encode(errMsg, session.getSource().getCharset());
        backendConnError(conn, err);
    }

    protected void backendConnError(MySQLConnection conn, ErrorPacket err) {
        conn.setRunning(false);
        lock.lock();
        try {
            if (error == null) {
                error = err;
            }
        } finally {
            lock.unlock();
        }
        isFail.set(true);
        if (decrementCountBy(1)) {
            notifyError();
        }
    }

    protected void notifyError() {
        recycleResources();
        byte errPacketId = ++packetId;
        ErrorPacket err;
        session.clearConnections();
        session.getSource().setTxInterrupt();
        err = error;
        if (err == null) {
            err = new ErrorPacket();
            err.packetId = errPacketId;
            err.errno = ErrorCode.ER_YES;
            err.message = StringUtil.encode("unknown error", session.getSource().getCharset());
        }
        err.packetId = errPacketId;
        err.write(session.getSource());
    }

    protected void notifyError(byte errPacketId) {
        recycleResources();
        ErrorPacket err;
        session.clearConnections();
        session.getSource().setTxInterrupt();
        err = error;
        if (err == null) {
            err = new ErrorPacket();
            err.packetId = errPacketId;
            err.errno = ErrorCode.ER_YES;
            err.message = StringUtil.encode("unknown error", session.getSource().getCharset());
        }
        err.packetId = errPacketId;
        err.write(session.getSource());
    }

    protected void recycleResources() {
    }
}
