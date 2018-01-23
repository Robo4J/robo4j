package com.robo4j.socket.http.units;

import java.util.Map;
import java.util.Objects;

/**
 * Configuration for http server unit {@link HttpServerUnit}
 * Server context contains available registered paths
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class ServerContext implements HttpContext<ServerPathConfig>{

    /**
     *  map of registered paths and related configuration
     */
    private final Map<String, ServerPathConfig> pathConfigs;

    ServerContext(Map<String, ServerPathConfig> pathConfigs) {
        this.pathConfigs = pathConfigs;
    }

    @Override
    public boolean isEmpty() {
        return pathConfigs.isEmpty();
    }

    @Override
    public boolean containsPath(String path){
        return pathConfigs.containsKey(path);
    }

    @Override
    public ServerPathConfig getPathConfig(String path){
        return pathConfigs.get(path);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerContext that = (ServerContext) o;
        return Objects.equals(pathConfigs, that.pathConfigs);
    }

    @Override
    public int hashCode() {

        return Objects.hash(pathConfigs);
    }

    @Override
    public String toString() {
        return "ServerContext{" +
                "pathConfigs=" + pathConfigs +
                '}';
    }
}
