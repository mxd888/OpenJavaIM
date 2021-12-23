package org.ojim.core.common.core;

/**
 * Created by DELL(mxd) on 2021/12/22 15:48
 */
public abstract class AioConfig {

    public AioHandler aioHandler;

    public FastBlockingQueue writeCacheQueue = new FastBlockingQueue(20);

    public AioConfig(AioHandler aioHandler) {
        this.aioHandler = aioHandler;
    }

    public abstract boolean isServer();
}
