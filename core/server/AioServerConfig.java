package org.ojim.core.server;

import org.ojim.core.common.core.AioConfig;
import org.ojim.core.common.core.AioHandler;
import org.ojim.core.common.monitor.InternetMonitor;

/**
 * Created by DELL(mxd) on 2021/12/22 19:17
 */
public class AioServerConfig extends AioConfig {

    public InternetMonitor monitor;

    public AioServerConfig(AioHandler aioHandler) {
        super(aioHandler);
    }

    public AioServerConfig(AioHandler aioHandler, InternetMonitor monitor) {
        super(aioHandler);
        this.monitor = monitor;
    }

    @Override
    public boolean isServer() {
        return true;
    }
}
