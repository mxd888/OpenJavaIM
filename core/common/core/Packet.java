package org.ojim.core.common.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * Created by DELL(mxd) on 2021/12/22 16:57
 */
public class Packet implements Serializable, Cloneable {

    private static final Logger log = LoggerFactory.getLogger(Packet.class);

    private static final long serialVersionUID = 7843541680713697996L;

    /**
     * 同步发送时，需要的同步序列号
     */
    private Integer	synSeq = 0;

    public Packet() {
    }

    public Integer getSynSeq() {
        return synSeq;
    }

    public void setSynSeq(Integer synSeq) {
        this.synSeq = synSeq;
    }

}
