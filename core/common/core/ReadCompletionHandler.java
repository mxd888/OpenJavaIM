package org.ojim.core.common.core;

import org.ojim.core.server.AioServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.CompletionHandler;

/**
 * Created by DELL(mxd) on 2021/12/22 15:40
 */
public class ReadCompletionHandler implements CompletionHandler<Integer, AioContext> {

    private static final Logger log = LoggerFactory.getLogger(ReadCompletionHandler.class);


    @Override
    public void completed(Integer result, AioContext attachment) {
        try {

            if (result <= 0){
                log.error("收到消息为:{},OpenJavaIM怀疑有人在攻击服务器", result);
            }
            attachment.readFromChannel();

        } catch (Exception e) {
            failed(e, attachment);
        }
    }

    @Override
    public void failed(Throwable exc, AioContext attachment) {

        try {
            attachment.close();
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
    }
}
