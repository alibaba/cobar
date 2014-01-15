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

/**
 * @author xianmao.hexm
 */
public class HeartbeatContext {

    // private final static long TIMER_PERIOD = 1000L;
    //
    // private String name;
    // private Timer timer;
    // private NIOProcessor[] processors;
    // private NIOConnector connector;
    //
    // public HeartbeatContext(String name) throws IOException {
    // this.name = name;
    // this.init();
    // }
    //
    // public void startup() {
    // // startup timer
    // timer.schedule(new TimerTask() {
    // @Override
    // public void run() {
    // TimeUtil.update();
    // }
    // }, 0L, TimeUtil.UPDATE_PERIOD);
    //
    // // startup processors
    // for (int i = 0; i < processors.length; i++) {
    // processors[i].startup();
    // }
    //
    // // startup connector
    // connector.start();
    // }
    //
    // public void doHeartbeat(HeartbeatConfig heartbeat) {
    // timer.schedule(new MySQLHeartbeatTask(connector, heartbeat), 0L,
    // TIMER_PERIOD);
    // }
    //
    // private void init() throws IOException {
    // // init timer
    // this.timer = new Timer(name + "Timer", false);
    //
    // // init processors
    // processors = new
    // NIOProcessor[Runtime.getRuntime().availableProcessors()];
    // for (int i = 0; i < processors.length; i++) {
    // processors[i] = new NIOProcessor(name + "Processor" + i);
    // }
    //
    // // init connector
    // connector = new NIOConnector(name + "Connector");
    // connector.setProcessors(processors);
    // }

}
