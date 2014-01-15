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

import com.alibaba.cobar.config.ErrorCode;
import com.alibaba.cobar.exception.HeartbeatException;
import com.alibaba.cobar.net.handler.BackendAsyncHandler;
import com.alibaba.cobar.net.mysql.EOFPacket;
import com.alibaba.cobar.net.mysql.ErrorPacket;
import com.alibaba.cobar.net.mysql.OkPacket;

/**
 * @author xianmao.hexm
 */
public class CobarDetectorHandler extends BackendAsyncHandler {
    private static final int RESULT_STATUS_INIT = 0;
    private static final int RESULT_STATUS_HEADER = 1;
    private static final int RESULT_STATUS_FIELD_EOF = 2;

    private final CobarDetector source;
    private volatile int resultStatus;

    public CobarDetectorHandler(CobarDetector source) {
        this.source = source;
        this.resultStatus = RESULT_STATUS_INIT;
    }

    @Override
    public void handle(byte[] data) {
        offerData(data, source.getProcessor().getExecutor());
    }

    @Override
    protected void offerDataError() {
        dataQueue.clear();
        resultStatus = RESULT_STATUS_INIT;
        throw new HeartbeatException("offer data error!");
    }

    @Override
    protected void handleData(byte[] data) {
        switch (resultStatus) {
        case RESULT_STATUS_INIT:
            switch (data[4]) {
            case OkPacket.FIELD_COUNT:
                handleOkPacket(data);
                break;
            case ErrorPacket.FIELD_COUNT:
                handleErrorPacket(data);
                break;
            default:
                resultStatus = RESULT_STATUS_HEADER;
            }
            break;
        case RESULT_STATUS_HEADER:
            switch (data[4]) {
            case ErrorPacket.FIELD_COUNT:
                resultStatus = RESULT_STATUS_INIT;
                handleErrorPacket(data);
                break;
            case EOFPacket.FIELD_COUNT:
                resultStatus = RESULT_STATUS_FIELD_EOF;
                break;
            }
            break;
        case RESULT_STATUS_FIELD_EOF:
            switch (data[4]) {
            case ErrorPacket.FIELD_COUNT:
                resultStatus = RESULT_STATUS_INIT;
                handleErrorPacket(data);
                break;
            case EOFPacket.FIELD_COUNT:
                resultStatus = RESULT_STATUS_INIT;
                handleRowEofPacket();
                break;
            }
            break;
        default:
            throw new HeartbeatException("unknown status!");
        }
    }

    @Override
    protected void handleDataError(Throwable t) {
        dataQueue.clear();
        resultStatus = RESULT_STATUS_INIT;
        source.error(ErrorCode.ERR_HANDLE_DATA, t);
    }

    /**
     * OK数据包处理
     */
    private void handleOkPacket(byte[] data) {
        source.getHeartbeat().setResult(CobarHeartbeat.OK_STATUS, source, false, data);
    }

    /**
     * ERROR数据包处理
     */
    private void handleErrorPacket(byte[] data) {
        ErrorPacket err = new ErrorPacket();
        err.read(data);
        switch (err.errno) {
        case ErrorCode.ER_SERVER_SHUTDOWN:
            source.getHeartbeat().setResult(CobarHeartbeat.OFF_STATUS, source, false, err.message);
            break;
        default:
            throw new HeartbeatException(new String(err.message));
        }
    }

    /**
     * 行数据包结束处理
     */
    private void handleRowEofPacket() {
        source.getHeartbeat().setResult(CobarHeartbeat.OK_STATUS, source, false, null);
    }

}
