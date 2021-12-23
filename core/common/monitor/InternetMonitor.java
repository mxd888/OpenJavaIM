package org.ojim.core.common.monitor;

/**
 * Created by DELL(mxd) on 2021/12/22 22:29
 */
public interface InternetMonitor {

    void acceptAfter();

    void massageTotal();

    default void print(){

    }
}
