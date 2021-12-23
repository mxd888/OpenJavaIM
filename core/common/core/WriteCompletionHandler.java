package org.ojim.core.common.core;

import org.ojim.core.server.AioServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.CompletionHandler;

/**
 * Created by DELL(mxd) on 2021/12/22 15:43
 */
public class WriteCompletionHandler implements CompletionHandler<Integer, AioContext> {

    private static final Logger log = LoggerFactory.getLogger(WriteCompletionHandler.class);

    @Override
    public void completed(Integer result, AioContext attachment) {

        if (attachment.config instanceof AioServerConfig){
            ((AioServerConfig) attachment.config).monitor.massageTotal();
        }
        attachment.writeToChannel();
        if (result <= 0){
            log.info("本次写入数据量:{},OpenJavaIM怀疑你在滥用框架", result);
            failed(new Exception(), attachment);
        }

    }

    @Override
    public void failed(Throwable exc, AioContext attachment) {

        log.error("出错");
    }
}
