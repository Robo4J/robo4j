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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */
package com.robo4j.units.rpi;

import com.robo4j.ConfigurationException;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;

/**
 * Helpful base class for {@link RoboUnit} units using the I2C protocol.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public abstract class I2CRoboUnit<T> extends RoboUnit<T> {
	// For all Raspberry Pi's except the first model, this is always the case.
	private static final int DEFAULT_BUS = 1;
	public static final String PROPERTY_KEY_BUS = "bus";
	public static final String PROPERTY_KEY_ADDRESS = "address";

	private Integer bus;
	private Integer address;

	/**
	 * @param context
	 * @param id
	 */
	public I2CRoboUnit(Class<T> messageType, RoboContext context, String id) {
		super(messageType, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		bus = configuration.getInteger(PROPERTY_KEY_BUS, DEFAULT_BUS);
		address = configuration.getInteger(PROPERTY_KEY_ADDRESS, null);
		if (address == null) {
			throw ConfigurationException.createMissingConfigNameException(PROPERTY_KEY_ADDRESS);
		}
	}

	public int getAddress() {
		return address;
	}

	public int getBus() {
		return bus;
	}

}
