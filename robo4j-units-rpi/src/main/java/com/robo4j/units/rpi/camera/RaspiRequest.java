/*
 * Copyright (c) 2014, 2024, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.units.rpi.camera;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.robo4j.util.Utf8Constant.UTF8_SPACE;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public abstract class RaspiRequest<T> implements Serializable {
	@Serial
    private static final long serialVersionUID = 1L;
	protected final Map<RpiCameraProperty, String> parameters;
    private final boolean active;

    public RaspiRequest(Map<RpiCameraProperty, String> parameters, boolean active) {
        this.parameters = parameters;
        this.active = active;
    }

    public abstract T put(RpiCameraProperty property, String value);

    public boolean isActive() {
        return active;
    }

    public String getProperty(RpiCameraProperty property){
        return parameters.get(property);
    }

    protected  String create(String command){
        return command +
                UTF8_SPACE +
                parameters.entrySet().stream()
                        .filter(e -> Objects.nonNull(e.getValue()))
                        .map(e ->
                                e.getKey().getProperty() +
                                        UTF8_SPACE +
                                        e.getValue())
                        .collect(Collectors.joining(UTF8_SPACE));
    }
}


