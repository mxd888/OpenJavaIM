package org.ojim.client;

import org.ojim.core.common.core.AioContext;
import org.ojim.core.common.core.AioHandler;
import org.ojim.core.common.core.Packet;
import org.ojim.core.common.example.SignalPacket;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Created by DELL(mxd) on 2021/12/22 20:17
 */
public class myClientHandler implements AioHandler {
    @Override
    public void handle(AioContext aioContext, Packet packet) {

        SignalPacket signalPacket = (SignalPacket) packet;
        byte[] body = signalPacket.getBody();
        if (body != null) {
            String str = new String(body, UTF_8);
            System.out.println("收到消息：" + str);

        }
    }

    @Override
    public ByteBuffer encode(AioContext aioContext, Packet packet) {
        SignalPacket signalPacket = (SignalPacket) packet;
        byte[] body = signalPacket.getBody();
        int bodyLen = 0;
        if (body != null) {
            bodyLen = body.length;
        }
        //bytebuffer的总长度是 = 消息头的长度 + 消息体的长度
        int allLen = 4 + bodyLen;
        //创建一个新的bytebuffer
        ByteBuffer buffer = ByteBuffer.allocate(allLen);
        //设置字节序
        buffer.order(ByteOrder.BIG_ENDIAN);
//        buffer.order(tioConfig.getByteOrder());
        //写入消息头----消息头的内容就是消息体的长度
        buffer.putInt(bodyLen);
        //写入消息体
        if (body != null) {
            buffer.put(body);
        }
        buffer.flip();
        return buffer;
    }

    @Override
    public Packet decode(ByteBuffer byteBuffer, AioContext aioContext) {
        //提醒：buffer的开始位置并不一定是0，应用需要从buffer.position()开始读取数据
        //收到的数据组不了业务包，则返回null以告诉框架数据不够
//        if (readableLength < HelloPacket.HEADER_LENGHT) {
//            return null;
//        }
        //读取消息体的长度
        int bodyLength = byteBuffer.getInt();
        //数据不正确，则抛出AioDecodeException异常
        if (bodyLength < 0) {
            System.out.println("无数据");
//            throw new TioDecodeException("bodyLength [" + bodyLength + "] is not right, remote:" + channelContext.getClientNode());
        }
        //计算本次需要的数据长度
        int neededLength = 4 + bodyLength;
        //收到的数据是否足够组包
//        int isDataEnough = readableLength - neededLength;
        // 不够消息体长度(剩下的buffe组不了消息体)

        SignalPacket signalPacket = new SignalPacket();
        if (bodyLength > 0) {
            byte[] dst = new byte[bodyLength];
            byteBuffer.get(dst);
            signalPacket.setBody(dst);
        }
        return signalPacket;
    }
}
