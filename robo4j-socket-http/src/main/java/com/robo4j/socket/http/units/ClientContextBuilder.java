package com.robo4j.socket.http.units;

import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
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
public final class ClientContextBuilder {

    private List<ClientPathDTO> paths;

    private ClientContextBuilder() {
        paths = new LinkedList<>();
    }

    public static ClientContextBuilder Builder(){
        return new ClientContextBuilder();
    }

    public ClientContextBuilder addPaths(Collection<ClientPathDTO> paths){
        this.paths.addAll(paths);
        return this;
    }

    public ClientContext build(RoboContext context){
        final Map<String, ClientPathConfig> resultPaths = paths.stream().map(e -> {
            List<RoboReference<Object>> references = e.getCallbacks().stream()
                    .map(context::getReference)
                    .collect(Collectors.toList());
            return HttpPathUtils.toClientPathConfig(e, references);
        }).collect(Collectors.toMap(ClientPathConfig::getPath, e -> e));
        return new ClientContext(resultPaths);
    }

}
