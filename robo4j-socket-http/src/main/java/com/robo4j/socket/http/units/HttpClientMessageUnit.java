package com.robo4j.socket.http.units;

import com.robo4j.ConfigurationException;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.socket.http.dto.ClientPathDTO;
import com.robo4j.socket.http.util.HttpPathUtils;

import java.util.List;
import java.util.Objects;

import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_PATHS_CONFIG;

/**
 *
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class HttpClientMessageUnit extends RoboUnit<String> {

    private ClientContext clientContext;
    private List<ClientPathDTO> paths;
    private String target;

    public HttpClientMessageUnit(RoboContext context, String id) {
        super(String.class, context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        this.target = configuration.getString("target", null);
        Objects.requireNonNull(target, "necessary to specify target");

        paths = HttpPathUtils.readPathConfig(ClientPathDTO.class, configuration.getString(HTTP_PATHS_CONFIG, null));

    }
}
