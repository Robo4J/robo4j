package com.robo4j.socket.http.request;

import com.robo4j.RoboReference;

/**
 * Datagram read channel process wrapper
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class DatagramResponseProcess implements ChannelResponseProcess<RoboReference<Object>> {

    private final String path;
    private final RoboReference<Object> target;
    private final Object result;

    public DatagramResponseProcess(String path, RoboReference<Object> target, Object result) {
        this.path = path;
        this.target = target;
        this.result = result;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public RoboReference<Object> getTarget() {
        return target;
    }

    @Override
    public Object getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "DatagramResponseProcess{" +
                "path='" + path + '\'' +
                ", target='" + target + '\'' +
                ", result=" + result +
                '}';
    }
}
