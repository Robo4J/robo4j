/*
 * Copyright (c) 2014, 2019, Marcus Hirt, Miroslav Wengner
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

import java.util.LinkedHashMap;

/**
 * Raspistill request to take a picture
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public final class RaspistillRequest extends RaspiRequest<RaspistillRequest> {
	private static final long serialVersionUID = 1L;
	private static final String RASPISTILL_COMMAND = "raspistill";


    public RaspistillRequest(boolean active) {
        super(new LinkedHashMap<>(), active);
    }

    public RaspistillRequest put(RpiCameraProperty property, String value){
        parameters.put(property, value);
        return this;
    }

    public String create(){
        return create(RASPISTILL_COMMAND);
    }


    @Override
    public String toString() {
        return "RaspistillRequest{" +
                "active=" + isActive() +
                ", parameters=" + parameters +
                '}';
    }
}
