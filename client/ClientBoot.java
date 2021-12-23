package org.ojim.client;

import org.ojim.core.client.AioClient;
import org.ojim.core.common.core.AioContext;
import org.ojim.core.common.core.AioSend;
import org.ojim.core.common.core.Packet;
import org.ojim.core.common.example.SignalPacket;

/**
 * Created by DELL(mxd) on 2021/12/22 20:17
 */
public class ClientBoot {

    public static void main(String[] args) throws InterruptedException {

        for (int i = 0; i < 3; i++) {

            int finalI = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    AioClient instance = new AioClient("127.0.0.1", 8888, new myClientHandler());
                    AioContext aioContext = instance.start();
                    String msg = "你好Aio " + finalI;
                    byte[] bytes = msg.getBytes();
                    Packet packet = new SignalPacket(bytes.length, bytes);
                    // 发个消息玩玩
                    AioSend.send(aioContext, packet);

                    AioSend.send(aioContext, packet);

                    AioSend.send(aioContext, packet);
                }
            }).start();

            Thread.sleep(2000);
        }


    }



}
