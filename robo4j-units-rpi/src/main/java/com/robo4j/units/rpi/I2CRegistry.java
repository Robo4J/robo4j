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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Registry for I2C devices. Useful for sharing hardware between units.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class I2CRegistry {
	private static Map<I2CEndPoint, Object> devices = new HashMap<>();

	public static Object getI2CDeviceByEndPoint(I2CEndPoint endPoint) {
		return devices.get(endPoint);
	}

	public static void registerI2CDevice(Object device, I2CEndPoint endPoint) {
		devices.put(endPoint, device);
	}

	public static <T> T createAndRegisterIfAbsent(int bus, int address, Supplier<T> supplier) {
		I2CEndPoint endPoint = new I2CEndPoint(bus, address);
		@SuppressWarnings("unchecked")
		T pwmDevice = (T) I2CRegistry.getI2CDeviceByEndPoint(endPoint);
		if (pwmDevice == null) {
			pwmDevice = supplier.get();
			registerI2CDevice(pwmDevice, endPoint);
		}
		return pwmDevice;
	}

}
