package com.robo4j.socket.http.units;

import java.util.Collection;
import java.util.Map;

/**
 * configuring http client context
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class ClientContext implements HttpContext<ClientPathConfig> {

    private final Map<String, ClientPathConfig> pathConfigs;

    ClientContext(Map<String, ClientPathConfig> pathConfigs) {
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
    public Collection<ClientPathConfig> getPathConfigs(){
        return pathConfigs.values();
    }

    @Override
    public ClientPathConfig getPathConfig(String path){
        return pathConfigs.get(path);
    }

    @Override
    public String toString() {
        return "ClientContext{" +
                "pathConfigs=" + pathConfigs +
                '}';
    }
}
