package org.ojim.core.common.core;

/**
 * Created by DELL(mxd) on 2021/12/22 19:31
 */
public class AioSend {


    public static void send(AioContext aioContext, Packet packet){
        aioContext.write(packet);
    }
}
