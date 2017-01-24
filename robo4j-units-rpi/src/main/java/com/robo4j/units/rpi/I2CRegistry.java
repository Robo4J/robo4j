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
package com.robo4j.units.rpi;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for I2C devices. Useful for sharing hardware between units.
 * 
 * @author Marcus Hirt (@hirt)
 */
public class I2CRegistry {
	private static Map<I2CEndPoint, Object> devices = new HashMap<>();

	public static Object getI2CDeviceByEndPoint(I2CEndPoint endPoint) {
		return devices.get(endPoint);
	}

	public static void registerI2CDevice(Object device, I2CEndPoint endPoint) {
		devices.put(endPoint, device);
	}

}
