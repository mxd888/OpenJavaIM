package org.ojim.core.common.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by DELL(mxd) on 2021/12/22 22:45
 */
public class Monitor implements InternetMonitor{

    private static final Logger log = LoggerFactory.getLogger(Monitor.class);

    private static final long initialValue = 0;

    private static final AtomicLong	acceptNum = new AtomicLong(initialValue);

    private static final AtomicLong	massageCount = new AtomicLong(initialValue);


    private int i = 0;
    public Monitor() {
        log.info("框架装配流量监控器");
    }

    @Override
    public void acceptAfter() {
        acceptNum.incrementAndGet();
    }

    @Override
    public void massageTotal() {
        i++;
        massageCount.incrementAndGet();
    }

    @Override
    public void print() {
        log.info("网络监控：连接数量：{} ,消息总量：{},{}", acceptNum.get(), massageCount.get(),i);
    }
}
