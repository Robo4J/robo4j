package com.robo4j.socket.http.units;

import com.robo4j.RoboContext;
import com.robo4j.socket.http.dto.ClientPathDTO;
import com.robo4j.socket.http.util.HttpPathUtils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class ClientContextBuilder implements ContextBuilder<ClientPathDTO, ClientContext>{

    private List<ClientPathDTO> paths;

    private ClientContextBuilder() {
        paths = new LinkedList<>();
    }

    public static ClientContextBuilder Builder(){
        return new ClientContextBuilder();
    }

    @Override
    public ClientContextBuilder addPaths(Collection<ClientPathDTO> paths){
        this.paths.addAll(paths);
        return this;
    }

    @Override
    public ClientContext build(RoboContext context){
        //@formatter:off
        final Map<String, ClientPathConfig> resultPaths = paths.stream()
                .map(HttpPathUtils::toClientPathConfig)
                .collect(Collectors.toMap(ClientPathConfig::getPath, e -> e));
        //@formatter:on
        return new ClientContext(resultPaths);
    }

}
