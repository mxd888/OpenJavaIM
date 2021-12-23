package org.ojim.core.common.example;

import org.ojim.core.common.core.Packet;

/**
 * Created by DELL(mxd) on 2021/12/23 14:00
 */
public class SignalPacket extends Packet {


    private static final long serialVersionUID = 949915356810988023L;

    private int count;

    private byte[] body;

    public SignalPacket(int count, byte[] body) {
        this.count = count;
        this.body = body;
    }

    public SignalPacket() {
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
