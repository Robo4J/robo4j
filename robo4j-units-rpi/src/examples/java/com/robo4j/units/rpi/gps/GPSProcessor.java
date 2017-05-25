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
package com.robo4j.units.rpi.gps;

import com.robo4j.core.ConfigurationException;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboUnit;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.hw.rpi.serial.gps.GPSEvent;

/**
 * Example recipient for GPS events.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class GPSProcessor extends RoboUnit<GPSEvent> {
	public GPSProcessor(RoboContext context, String id) {
		super(GPSEvent.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {

	}

	@Override
	public void onMessage(GPSEvent result) {
		System.out.println("GPSEvent: " + result.toString());
	}
}
