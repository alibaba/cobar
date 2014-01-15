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
package com.alibaba.cobar.heartbeat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.alibaba.cobar.mysql.MySQLDataNode;
import com.alibaba.cobar.mysql.MySQLDataSource;
import com.alibaba.cobar.statistic.HeartbeatRecorder;

/**
 * @author xianmao.hexm
 */
public class MySQLHeartbeat {
    public static final int OK_STATUS = 1;
    public static final int ERROR_STATUS = -1;
    private static final int TIMEOUT_STATUS = -2;
    private static final int INIT_STATUS = 0;
    private static final int MAX_RETRY_COUNT = 5;
    private static final Logger LOGGER = Logger.getLogger(MySQLHeartbeat.class);

    private final MySQLDataSource source;
    private final AtomicBoolean isStop;
    private final AtomicBoolean isChecking;
    private final MySQLDetectorFactory factory;
    private final HeartbeatRecorder recorder;
    private final ReentrantLock lock;
    private final int maxRetryCount;
    private int errorCount;
    private volatile int status;
    private MySQLDetector detector;

    public MySQLHeartbeat(MySQLDataSource source) {
        this.source = source;
        this.isStop = new AtomicBoolean(false);
        this.isChecking = new AtomicBoolean(false);
        this.factory = new MySQLDetectorFactory();
        this.recorder = new HeartbeatRecorder();
        this.lock = new ReentrantLock(false);
        this.maxRetryCount = MAX_RETRY_COUNT;
        this.status = INIT_STATUS;
    }

    public MySQLDataSource getSource() {
        return source;
    }

    public MySQLDetector getDetector() {
        return detector;
    }

    public int getStatus() {
        return status;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public long getTimeout() {
        MySQLDetector detector = this.detector;
        if (detector == null) {
            return -1L;
        }
        return detector.getHeartbeatTimeout();
    }

    public HeartbeatRecorder getRecorder() {
        return recorder;
    }

    public String getLastActiveTime() {
        MySQLDetector detector = this.detector;
        if (detector == null) {
            return null;
        }
        long t = Math.max(detector.lastReadTime(), detector.lastWriteTime());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(t));
    }

    public boolean isStop() {
        return isStop.get();
    }

    public boolean isChecking() {
        return isChecking.get();
    }

    public void start() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            isStop.compareAndSet(true, false);
        } finally {
            lock.unlock();
        }
    }

    public void stop() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (isStop.compareAndSet(false, true)) {
                if (isChecking.get()) {
                    // nothing
                } else {
                    MySQLDetector detector = this.detector;
                    if (detector != null) {
                        detector.quit();
                        isChecking.set(false);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * execute heart beat
     */
    public void heartbeat() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (isChecking.compareAndSet(false, true)) {
                MySQLDetector detector = this.detector;
                if (detector == null || detector.isQuit() || detector.isClosed()) {
                    try {
                        detector = factory.make(this);
                    } catch (Throwable e) {
                        LOGGER.warn(source.getConfig().toString(), e);
                        setError(null);
                        return;
                    }
                    this.detector = detector;
                } else {
                    detector.heartbeat();
                }
            } else {
                MySQLDetector detector = this.detector;
                if (detector != null) {
                    if (detector.isQuit() || detector.isClosed()) {
                        isChecking.compareAndSet(true, false);
                    } else if (detector.isHeartbeatTimeout()) {
                        setTimeout(detector);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void setResult(int result, MySQLDetector detector, boolean isTransferError) {
        switch (result) {
        case OK_STATUS:
            setOk(detector);
            break;
        case ERROR_STATUS:
            if (detector.isQuit()) {
                isChecking.set(false);
            } else {
                if (isTransferError) {
                    detector.close();
                }
                setError(detector);
            }
            break;
        }
    }

    private void setOk(MySQLDetector detector) {
        recorder.set(detector.lastReadTime() - detector.lastWriteTime());
        switch (status) {
        case TIMEOUT_STATUS:
            this.status = INIT_STATUS;
            this.errorCount = 0;
            this.isChecking.set(false);
            if (isStop.get()) {
                detector.quit();
            } else {
                heartbeat();// timeout, heart beat again
            }
            break;
        default:
            this.status = OK_STATUS;
            this.errorCount = 0;
            this.isChecking.set(false);
            if (isStop.get()) {
                detector.quit();
            }
        }
    }

    private void setError(MySQLDetector detector) {
        if (++errorCount < maxRetryCount) {
            isChecking.set(false);
            if (detector != null && isStop.get()) {
                detector.quit();
            } else {
                heartbeat(); // error count not enough, heart beat again
            }
        } else {
            this.status = ERROR_STATUS;
            this.errorCount = 0;
            this.isChecking.set(false);
            try {
                switchSource("ERROR");
            } finally {
                if (detector != null && isStop.get()) {
                    detector.quit();
                }
            }
        }
    }

    private void setTimeout(MySQLDetector detector) {
        status = TIMEOUT_STATUS;
        try {
            switchSource("TIMEOUT");
        } finally {
            detector.quit();
            isChecking.set(false);
        }
    }

    /**
     * switch data source
     */
    private void switchSource(String reason) {
        if (!isStop.get()) {
            MySQLDataNode node = source.getNode();
            int i = node.next(source.getIndex());
            node.switchSource(i, true, reason);
        }
    }

}
