package org.ojim.core.common.core;

import java.nio.ByteBuffer;

/**
 * Created by DELL(mxd) on 2021/12/22 15:38
 */
public interface AioHandler {

    void handle(AioContext aioContext, Packet packet);

    ByteBuffer encode(AioContext aioContext, Packet packet);

    Packet decode(ByteBuffer byteBuffer, AioContext aioContext);
}
