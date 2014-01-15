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
package com.alibaba.cobar.statistic;

/**
 * @author xianmao.hexm
 */
public class CommandCount {

    private long initDB;
    private long query;
    private long stmtPrepare;
    private long stmtExecute;
    private long stmtClose;
    private long ping;
    private long kill;
    private long quit;
    private long heartbeat;
    private long other;

    public void doInitDB() {
        ++initDB;
    }

    public long initDBCount() {
        return initDB;
    }

    public void doQuery() {
        ++query;
    }

    public long queryCount() {
        return query;
    }

    public void doStmtPrepare() {
        ++stmtPrepare;
    }

    public long stmtPrepareCount() {
        return stmtPrepare;
    }

    public void doStmtExecute() {
        ++stmtExecute;
    }

    public long stmtExecuteCount() {
        return stmtExecute;
    }

    public void doStmtClose() {
        ++stmtClose;
    }

    public long stmtCloseCount() {
        return stmtClose;
    }

    public void doPing() {
        ++ping;
    }

    public long pingCount() {
        return ping;
    }

    public void doKill() {
        ++kill;
    }

    public long killCount() {
        return kill;
    }

    public void doQuit() {
        ++quit;
    }

    public long quitCount() {
        return quit;
    }

    public void doOther() {
        ++other;
    }

    public long heartbeat() {
        return heartbeat;
    }

    public void doHeartbeat() {
        ++heartbeat;
    }

    public long otherCount() {
        return other;
    }

}
