/*
 * Copyright (c) 2014, 2017, Miroslav Wengner, Marcus Hirt
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
package com.robo4j.rpi.lcd;

import java.util.Map;

import com.robo4j.core.LifecycleState;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboUnit;

/**
 * Helpful base class for {@link RoboUnit} units using the I2C protocol.
 * 
 * @author Marcus Hirt (@hirt)
 */
public abstract class I2CRoboUnit<T> extends RoboUnit<T> {
	public final static String PROPERTY_KEY_BUS = "bus";
	public final static String PROPERTY_KEY_ADDRESS = "address";

	private int bus;
	private int address;

	/**
	 * @param context
	 * @param id
	 */
	public I2CRoboUnit(RoboContext context, String id) {
		super(context, id);
	}

	@Override
	public void initialize(Map<String, String> properties) throws Exception {
		bus = Integer.parseInt(properties.get("bus"));
		address = Integer.parseInt(properties.get("address"));
		setState(LifecycleState.INITIALIZED);
	}

	public int getAddress() {
		return address;
	}

	public int getBus() {
		return bus;
	}

}
