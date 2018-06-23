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

package com.robo4j.units.rpi.pwm;

import com.robo4j.ConfigurationException;
import com.robo4j.hw.rpi.i2c.pwm.PWMPCA9685Device;
import com.robo4j.units.rpi.I2CEndPoint;
import com.robo4j.units.rpi.I2CRegistry;

import java.io.IOException;

/**
 * @see MC33926HBridgeUnit
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class PCA9685Utils {

	public static final int DEFAULT_FREQUENCY = 50;

	public static PWMPCA9685Device initPwmDevice(Object pwmDevice, int bus, int address) throws ConfigurationException {
		return initPwmDevice(pwmDevice, bus, address, DEFAULT_FREQUENCY);
	}

	public static PWMPCA9685Device initPwmDevice(Object pwmDevice, int bus, int address, int frequency)
			throws ConfigurationException {
		try {
			if (pwmDevice == null) {
				PWMPCA9685Device result = new PWMPCA9685Device(bus, address);
				I2CRegistry.registerI2CDevice(result, new I2CEndPoint(bus, address));
				result.setPWMFrequency(frequency);
				return result;
			} else {
				return (PWMPCA9685Device) pwmDevice;
			}
		} catch (IOException e) {
			throw new ConfigurationException("Could not initialize hardware", e);
		}
	}

}
