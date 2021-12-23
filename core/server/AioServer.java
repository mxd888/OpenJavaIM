package org.ojim.core.server;

import org.ojim.core.client.ClientAioContext;
import org.ojim.core.common.core.AioHandler;
import org.ojim.core.common.core.ReadCompletionHandler;
import org.ojim.core.common.core.SystemConst;
import org.ojim.core.common.core.WriteCompletionHandler;
import org.ojim.core.common.monitor.InternetMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Created by DELL(mxd) on 2021/12/22 15:40
 */
public class AioServer implements SystemConst {

    private static final Logger log = LoggerFactory.getLogger(AioServer.class);

    private static AsynchronousServerSocketChannel serverSocketChannel = null;

    private static AsynchronousChannelGroup asynchronousChannelGroup = null;

    private final int port;

    private static AioServer server = null;

    private static AioHandler aioHandler;

    private static AioServerConfig serverConfig;

    private static InternetMonitor monitor;

    /**
     * 服务器处理线程数
     */
    private final int threadNum = Runtime.getRuntime().availableProcessors() + 1;

    public static AioServer getInstance(int port, AioHandler handler) {
        aioHandler = handler;
        if (server != null) {
            return server;
        }
        server = new AioServer(port);
        return server;
    }

    private AioServer(int port) {
        this.port = port;
    }

    public void start() {
        log.info("OpenJavaIM Version: {}", version);
        try {
            AsynchronousChannelProvider provider = AsynchronousChannelProvider.provider();
            asynchronousChannelGroup = provider.openAsynchronousChannelGroup(threadNum, new ThreadFactory() {
                byte index = 0;

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "AIO:Server-" + (++index));
                }
            });
            serverSocketChannel = AsynchronousServerSocketChannel.open(asynchronousChannelGroup);
            serverSocketChannel.bind(new InetSocketAddress(port));
            serverConfig = new AioServerConfig(aioHandler, monitor);
        } catch (IOException e) {
            shutdown();
            String message = e.getMessage();
            log.error(message);
        }
        if (monitor != null){
            startMonitor();
        }
        serverSocketChannel.accept(serverSocketChannel, new CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel>() {
            int times = 0;


            @Override
            public void completed(final AsynchronousSocketChannel channel, AsynchronousServerSocketChannel serverSocketChannel) {
                serverSocketChannel.accept(serverSocketChannel, this);
                createContext(channel);
            }

            @Override
            public void failed(Throwable exc, AsynchronousServerSocketChannel serverSocketChannel) {
                log.error("server accept fail", exc);
                if (times++ < 10) {
                    serverSocketChannel.accept(serverSocketChannel, this);
                }
            }
        });


    }


    /**
     * 为每个新建立的连接创建AIOSession对象
     *
     * @param channel .
     */
    private void createContext(AsynchronousSocketChannel channel) {
        //连接成功则构造AIOContext对象
        ClientAioContext context = new ClientAioContext(channel,new ReadCompletionHandler(), new WriteCompletionHandler(),
                serverConfig);
        try {
//            log.info("用户LocalAddressIP:{},RemoteAddress:{}", channel.getLocalAddress(), channel.getRemoteAddress());
            Map<String, ClientAioContext> group = ServerAioContext.getInstance().getGroup();
            group.put("100",context);
            context.initContext();
            if (context.config instanceof AioServerConfig){
                // 监控连接
                ((AioServerConfig) context.config).monitor.acceptAfter();
            }
        } catch (Exception e1) {
            log.debug(e1.getMessage(), e1);
            closeChannel(channel);
        }
    }



    private void closeChannel(AsynchronousSocketChannel channel) {
        try {
            channel.shutdownInput();
        } catch (IOException e) {
            log.debug(e.getMessage(), e);
        }
        try {
            channel.shutdownOutput();
        } catch (IOException e) {
            log.debug(e.getMessage(), e);
        }
        try {
            channel.close();
        } catch (IOException e) {
            log.debug("close channel exception", e);
        }
    }

    private final void startMonitor(){
        new Thread(() -> {
            for (int i = 0; i < 20; i++) {
                try {
                    Thread.sleep(10000);
                }catch (Exception e){
                    e.printStackTrace();
                }
                monitor.print();
            }

        }).start();
    }


    /**
     * 停止服务端
     */
    public final void shutdown() {
        try {
            if (serverSocketChannel != null) {
                serverSocketChannel.close();
                serverSocketChannel = null;
            }
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        }
        if (!asynchronousChannelGroup.isTerminated()) {
            try {
                asynchronousChannelGroup.shutdownNow();
            } catch (IOException e) {
                log.error("shutdown exception", e);
            }
        }
        try {
            asynchronousChannelGroup.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("shutdown exception", e);
        }
    }

    public void setMonitor(InternetMonitor monitor) {
        AioServer.monitor = monitor;
    }
}
