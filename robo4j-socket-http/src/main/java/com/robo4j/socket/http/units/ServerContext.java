package com.robo4j.socket.http.units;

import java.util.Map;

/**
 * Configuration for http server unit {@link HttpServerUnit}
 * Server context contains available registered paths
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ServerContext {

    /**
     *  map of registered paths and related configuration
     */
    private final Map<String, ServerPathConfig> pathConfigs;

    public ServerContext(Map<String, ServerPathConfig> pathConfigs) {
        this.pathConfigs = pathConfigs;
    }

    public boolean containsPath(String path){
        return pathConfigs.containsKey(path);
    }

    public ServerPathConfig getPathConfig(String path){
        return pathConfigs.get(path);
    }

    @Override
    public String toString() {
        return "ServerContext{" +
                "pathConfigs=" + pathConfigs +
                '}';
    }
}
