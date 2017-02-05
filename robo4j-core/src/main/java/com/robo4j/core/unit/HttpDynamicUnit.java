/*
 * Copyright (C) 2014, 2017. Miroslav Wengner, Marcus Hirt
 * This HttpDynamicUnit.java  is part of robo4j.
 * module: robo4j-core
 *
 * robo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * robo4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.core.unit;

import com.robo4j.core.ConfigurationException;
import com.robo4j.core.LifecycleState;
import com.robo4j.core.RoboContext;
import com.robo4j.core.configuration.Configuration;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Http Dynamic unit allows to configure format of the requests
 * currently is only GET method available
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 * @since 05.02.2017
 */
public class HttpDynamicUnit extends HttpUnit {

    private static final int _DEFAULT_PORT = 8042;
    private static final String _DEFAULT_COMMAND = "";
    private Integer port;
    private String target;
    private String path;
    private Set<String> command;

    public HttpDynamicUnit(RoboContext context, String id) {
        super(context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        setState(LifecycleState.UNINITIALIZED);
        target = configuration.getString("target", null);
        path = configuration.getString("path", null);
        port = configuration.getInteger("port", _DEFAULT_PORT);
        if (target == null && path == null) {
            throw ConfigurationException.createMissingConfigNameException("target");
        }
        //@formatter:off
        command = Stream.of(configuration.getString("command", _DEFAULT_COMMAND).split(","))
                .collect(Collectors.toSet());
        //@formatter:on

        setState(LifecycleState.INITIALIZED);
    }
}
