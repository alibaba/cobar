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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.alibaba.cobar.manager.dao.CobarAdapterDAO;

/**
 * (created at 2010-7-26)
 * 
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 * @author haiqing.zhuhq 2011-7-21
 */
public class AdapterDelegate implements InitializingBean, DisposableBean {

    private Logger logger = Logger.getLogger(getClass());

    private DataSourceFactory dsFactory;

    private EvictThread evictThread;
    private long cobarNodeEvictThreadSweepInverval = 10 * 60 * 1000; //10min
    private long cobarNodeAdapterIdleTime = 40 * 60 * 1000; //40min

    public void setCobarNodeEvictThreadSweepInverval(long cobarNodeEvictThreadSweepInverval) {
        this.cobarNodeEvictThreadSweepInverval = cobarNodeEvictThreadSweepInverval;
    }

    public void setCobarNodeAdapterIdleTime(long cobarNodeAdapterIdleTime) {
        this.cobarNodeAdapterIdleTime = cobarNodeAdapterIdleTime;
    }

    public void setDsFactory(DataSourceFactory dsFactory) {
        this.dsFactory = dsFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        evictThread = new EvictThread();
        evictThread.setDaemon(true);
        evictThread.start();
    }

    @Override
    public void destroy() throws Exception {
        evictThread.shutdown();
    }

    public CobarAdapterDAO getCobarNodeAccesser(String ip, int port, String user, String password) {
        return getCobarNodeAdapterInternal(ip, port, user, password);
    }

    private ReadWriteLock adapterMapLock = new ReentrantReadWriteLock(true);
    private Map<CobarAdapterKey, TimestampPair> adapterMap = new HashMap<CobarAdapterKey, TimestampPair>();

    protected CobarAdapter getCobarNodeAdapterInternal(String ip, int port, String user, String password) {
        final CobarAdapterKey key = new CobarAdapterKey(ip, port, user, password);
        TimestampPair rst = null;

        try {
            adapterMapLock.readLock().lock();
            rst = adapterMap.get(key);
            if (rst != null) {
                return rst.refreshTime().getValue();
            }
        } finally {
            adapterMapLock.readLock().unlock();
        }
        try {
            adapterMapLock.writeLock().lock();
            rst = adapterMap.get(key);
            if (rst != null) {
                return rst.refreshTime().getValue();
            }
            rst = createAdapter(ip, port, user, password);
            adapterMap.put(key, rst);
            return rst.refreshTime().getValue();
        } finally {
            adapterMapLock.writeLock().unlock();
        }
    }

    private TimestampPair createAdapter(String ip, int port, String user, String password) {
        CobarAdapter adapter = null;
        try {
            DataSource ds = dsFactory.createDataSource(ip, port, user, password);
            adapter = new CobarAdapter();
            adapter.setDataSource(ds);
            ((InitializingBean) adapter).afterPropertiesSet();
            return new TimestampPair(adapter);
        } catch (Exception exception) {
            logger.error("ip=" + ip + ", port=" + port, exception);
            try {
                adapter.destroy();
            } catch (Exception e) {
            }
            throw new RuntimeException(exception);
        }
    }

    private void sweepCobarNodeAdapter() {
        Map<CobarAdapterKey, TimestampPair> toClose = new HashMap<CobarAdapterKey, TimestampPair>();
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("evictThread sweep adapter begin, wating for write lock.");
            }
            adapterMapLock.writeLock().lock();
            if (logger.isDebugEnabled()) {
                logger.debug("evictThread sweep adapter begin, acquired write lock.");
            }
            Iterator<Entry<CobarAdapterKey, TimestampPair>> iter = adapterMap.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<CobarAdapterKey, TimestampPair> entry = iter.next();
                CobarAdapterKey key = entry.getKey();
                TimestampPair pair = entry.getValue();
                long time = pair.getCreatedTime();
                if (System.currentTimeMillis() - time >= cobarNodeAdapterIdleTime) {
                    toClose.put(key, pair);
                    iter.remove();
                }
            }
        } finally {
            adapterMapLock.writeLock().unlock();
        }

        if (logger.isDebugEnabled()) {
            logger.debug("evictThread sweep adapter finished, toClose.size()="
                         + toClose.size()
                         + ", toCloseSet="
                         + toClose.keySet());
        }
        for (CobarAdapterKey key : toClose.keySet()) {
            try {
                toClose.get(key).getValue().destroy();
            } catch (Exception e) {
                logger.warn(e);
            }
        }
    }

    private class EvictThread extends Thread {

        public volatile boolean keepRunning = true;

        public void shutdown() {
            keepRunning = false;
            interrupt();
        }

        @Override
        public void run() {
            while (keepRunning) {
                try {
                    sleep(cobarNodeEvictThreadSweepInverval);
                    sweepCobarNodeAdapter();
                } catch (InterruptedException e) {
                }
            }
        }
    }

    private class TimestampPair {

        private volatile long time;
        private final CobarAdapter value;

        public TimestampPair(CobarAdapter v) {
            time = System.currentTimeMillis();
            value = v;
        }

        public TimestampPair refreshTime() {
            time = System.currentTimeMillis();
            return this;
        }

        public long getCreatedTime() {
            return time;
        }

        public CobarAdapter getValue() {
            return value;
        }
    }
}
