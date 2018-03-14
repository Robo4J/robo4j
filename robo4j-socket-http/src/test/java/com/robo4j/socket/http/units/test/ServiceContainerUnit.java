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

package com.robo4j.socket.http.units.test;

import com.robo4j.RoboContext;
import com.robo4j.socket.http.units.ExtendedRoboUnit;
import com.robo4j.socket.http.units.test.service.NumberService;

import java.util.Objects;

/**
 * Unit holds references to another service
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ServiceContainerUnit extends ExtendedRoboUnit<Object, NumberService> {

    public static final String NAME = "containerUnit";
    public static final String NUMBER_SERVICE = "numberService";

    public ServiceContainerUnit(RoboContext context, String id) {
		super(Object.class, context, id);
	}

    @Override
    public void start() {
        super.start();
        Objects.requireNonNull(getService(), "service is not available");

    }

    @Override
    public void onMessage(Object message) {
        System.out.println(getClass().getSimpleName() + ":message:" + message);
        System.out.println(getClass().getSimpleName() + ":number:" + getService().getNumber());
    }


}
