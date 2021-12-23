package org.ojim.core.client;

import org.ojim.core.common.core.AioConfig;
import org.ojim.core.common.core.AioContext;
import org.ojim.core.common.core.ReadCompletionHandler;
import org.ojim.core.common.core.WriteCompletionHandler;

import java.nio.channels.AsynchronousSocketChannel;

/**
 * Created by DELL(mxd) on 2021/12/22 17:04
 */
public class ClientAioContext extends AioContext {

    public ClientAioContext(AsynchronousSocketChannel clientSocketChannel,
                            ReadCompletionHandler readCompletionHandler,
                            WriteCompletionHandler writeCompletionHandler,
                            AioConfig config) {
        super(clientSocketChannel, readCompletionHandler, writeCompletionHandler, config);
    }
}
