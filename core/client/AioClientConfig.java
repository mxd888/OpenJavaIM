package org.ojim.core.client;

import org.ojim.core.common.core.AioConfig;
import org.ojim.core.common.core.AioHandler;

/**
 * Created by DELL(mxd) on 2021/12/22 19:50
 */
public class AioClientConfig extends AioConfig {

    public AioClientConfig(AioHandler aioHandler) {
        super(aioHandler);
    }

    @Override
    public boolean isServer() {
        return false;
    }
}
