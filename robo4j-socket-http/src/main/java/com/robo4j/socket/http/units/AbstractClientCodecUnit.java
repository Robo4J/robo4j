/*
 * Copyright (c) 2014, 2018, Marcus Hirt, Miroslav Wengner
 *
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.socket.http.units;

import com.robo4j.ConfigurationException;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.socket.http.dto.ClientPathDTO;
import com.robo4j.socket.http.util.HttpPathUtils;
import com.robo4j.socket.http.util.JsonUtil;
import com.robo4j.socket.http.util.RoboHttpUtils;

import java.util.List;
import java.util.Objects;

import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_TARGET;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_CODEC_PACKAGES;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_CODEC_REGISTRY;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_UNIT_PATHS_CONFIG;
import static com.robo4j.util.Utf8Constant.UTF8_COMMA;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public abstract  class AbstractClientCodecUnit extends RoboUnit<ClientMessageWrapper> {

    final ClientContext clientContext = new ClientContext();
    protected String target;

    AbstractClientCodecUnit(Class<ClientMessageWrapper> messageType, RoboContext context, String id) {
        super(messageType, context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        target = configuration.getString(PROPERTY_TARGET, null);
        Objects.requireNonNull(target, "empty target");

        final List<ClientPathDTO> paths = JsonUtil.readPathConfig(ClientPathDTO.class,
                configuration.getString(PROPERTY_UNIT_PATHS_CONFIG, null));
        if (paths.isEmpty()) {
            throw ConfigurationException.createMissingConfigNameException(PROPERTY_UNIT_PATHS_CONFIG);
        }
        HttpPathUtils.updateHttpClientContextPaths(clientContext, paths);

        String packages = configuration.getString(PROPERTY_CODEC_PACKAGES, null);
        if (RoboHttpUtils.validatePackages(packages)) {
            final CodecRegistry codecRegistry = new CodecRegistry(Thread.currentThread().getContextClassLoader(), packages.split(UTF8_COMMA));
            clientContext.putProperty(PROPERTY_CODEC_REGISTRY, codecRegistry);
        } else {
            throw new IllegalStateException("not available codec packages");
        }
    }
}
