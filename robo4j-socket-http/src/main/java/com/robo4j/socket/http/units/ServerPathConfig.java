package com.robo4j.socket.http.units;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Configuration for http server unit {@link HttpServerUnit}
 *
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ServerPathConfig {

    private final List<ServerPathMethod> pathConfigs;

    public ServerPathConfig(List<ServerPathMethod> pathConfigs) {
        this.pathConfigs = pathConfigs;
    }

    public List<ServerPathMethod> asList() {
        return new LinkedList<>(pathConfigs);
    }

    public Stream<ServerPathMethod> asStream(){
        return asList().stream();
    }


    @Override
    public String toString() {
        return "ServerPathConfig{" +
                "pathConfigs=" + asList() +
                '}';
    }
}
