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
import java.util.LinkedHashMap;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class RaspividRequest extends RaspiRequest<RaspividRequest>{
	@Serial
    private static final long serialVersionUID = 1L;
	private static final String RASPIVID_COMMAND = "raspivid";
    private final RaspividRequestType type;

    public RaspividRequest(boolean active, RaspividRequestType type) {
        super(new LinkedHashMap<>(), active);
        this.type = type;
    }


    public RaspividRequestType getType() {
        return type;
    }


    public RaspividRequest put(RpiCameraProperty property, String value){
        parameters.put(property, value);
        return this;
    }

    public String create(){
        return create(RASPIVID_COMMAND);
    }

    @Override
    public String toString() {
        return "RaspistillRequest{" +
                "active=" + isActive() +
                ", parameters=" + parameters +
                '}';
    }

}
