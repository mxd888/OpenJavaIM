package org.ojim.core.client;

import org.ojim.core.common.core.AioContext;
import org.ojim.core.common.core.AioHandler;
import org.ojim.core.common.core.ReadCompletionHandler;
import org.ojim.core.common.core.WriteCompletionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadFactory;

/**
 * Created by DELL(mxd) on 2021/12/22 17:03
 */
public class AioClient {

    private static final Logger log = LoggerFactory.getLogger(AioClient.class);

    private static AsynchronousSocketChannel channel = null;

    private static AsynchronousChannelGroup asynchronousChannelGroup = null;

    private final int port;

    private final String host;

    private static AioHandler aioHandler;

    private static AioClient aioClient;

    private static ClientAioContext aioContext;


    public static AioClient getInstance(String host, int port, AioHandler handler) {
        aioHandler = handler;
        if (aioClient != null) {
            return aioClient;
        }
        aioClient = new AioClient(host, port);
        return aioClient;
    }

    private AioClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    // 测试使用
    public AioClient(String host, int port, AioHandler handler) {
        this.host = host;
        this.port = port;
        aioHandler = handler;
    }


    public AioContext start(){
        log.info("Client start...");
        AsynchronousChannelProvider provider = AsynchronousChannelProvider.provider();
        try {
            asynchronousChannelGroup = provider.openAsynchronousChannelGroup(2, new ThreadFactory() {
                byte index = 0;

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "AIO:Client-" + (++index));
                }
            });

            channel = AsynchronousSocketChannel.open(asynchronousChannelGroup);
            channel.connect(new InetSocketAddress(host, port)).get();
            aioContext = new ClientAioContext(channel, new ReadCompletionHandler(),
                    new WriteCompletionHandler(), new AioClientConfig(aioHandler));
            aioContext.initContext();
        }catch (InterruptedException | ExecutionException |IOException e){
            log.error(e.getMessage());
            shutdown();
            e.printStackTrace();
        }
        return aioContext;
    }

    public final void shutdown() {
        try {
            if (channel != null) {
                channel.close();
                channel = null;
            }
            if (aioContext != null) {
                aioContext.close();
                aioContext = null;
            }
        }catch (IOException e){
            log.error(e.getMessage());
            e.printStackTrace();
        }
        //仅Client内部创建的ChannelGroup需要shutdown
        if (asynchronousChannelGroup != null) {
            asynchronousChannelGroup.shutdown();
        }
    }
}
