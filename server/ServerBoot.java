package org.ojim.server;

import org.ojim.core.common.monitor.Monitor;
import org.ojim.core.server.AioServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by DELL(mxd) on 2021/12/22 16:59
 */
public class ServerBoot {

    private static final Logger log = LoggerFactory.getLogger(ServerBoot.class);

    public static void main(String[] args) {

        AioServer instance = AioServer.getInstance(8888, new myServerHandler());
        instance.setMonitor(new Monitor());
        instance.start();
        log.info("On {} port, OpenJavaIM Server have been started successfully", 8888);
    }
}
