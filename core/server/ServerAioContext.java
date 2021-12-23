package org.ojim.core.server;

import org.ojim.core.client.ClientAioContext;
import org.ojim.core.common.core.AioContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by DELL(mxd) on 2021/12/22 16:20
 */
public class ServerAioContext extends AioContext {

    private static final Logger log = LoggerFactory.getLogger(ServerAioContext.class);

    private final Map<String, ClientAioContext> group = new HashMap<>();

    private static ServerAioContext serverAioContext = null;

    public static ServerAioContext getInstance() {
        log.info("获取ServerAioContext单例对象...");
        if (serverAioContext != null) {
            return serverAioContext;
        }
        serverAioContext = new ServerAioContext();
        return serverAioContext;
    }

    private ServerAioContext() {
        super();
    }

    public Map<String, ClientAioContext> getGroup() {
        return group;
    }

}
