package com.robo4j.socket.http.request;

/**
 * NIO Channel result of request
 *
 *
 * @see HttpResponseProcess
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public interface ChannelResponseProcess<T> {

    String getPath();

    T getTarget();

    Object getResult();

}
