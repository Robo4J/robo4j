/*
 * Copyright (c) 2014, 2017, Marcus Hirt, Miroslav Wengner
 *
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.units.rpi.camera;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.robo4j.util.Utf8Constant.UTF8_SPACE;

/**
 * Raspistill request to take a picture
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public final class RaspistillRequest {

    private static final String RASPISTILL_COMMAND = "raspistill";
    private final boolean active;
    private final Map<RpiCameraProperty, String> parameters;

    public RaspistillRequest(boolean active) {
        this.active = active;
        this.parameters = new LinkedHashMap<>();
    }

    public RaspistillRequest put(RpiCameraProperty property, String value){
        parameters.put(property, value);
        return this;
    }

    public boolean isActive() {
        return active;
    }

    public String getProperty(RpiCameraProperty property){
        return parameters.get(property);
    }

    public String create(){
        return new StringBuilder()
                .append(RASPISTILL_COMMAND)
                .append(UTF8_SPACE)
                .append(parameters.entrySet().stream()
                        .filter(e -> Objects.nonNull(e.getValue()))
                        .map(e ->
                             new StringBuilder().append(e.getKey().getProperty())
                                    .append(UTF8_SPACE)
                                    .append(e.getValue()).toString())
                        .collect(Collectors.joining(UTF8_SPACE)))
                .toString();
    }


    @Override
    public String toString() {
        return "RaspistillRequest{" +
                "active=" + active +
                ", parameters=" + parameters +
                '}';
    }
}
