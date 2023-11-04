/*
 * Copyright (c) 2014, 2023, Marcus Hirt, Miroslav Wengner
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

package com.robo4j.hw.rpi.i2c.adafruitbackpack;

import java.util.concurrent.TimeUnit;

/**
 * Simple example of displaying characters on {@link AlphanumericDevice}
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class AlphanumericDeviceExample {
	public static void main(String[] args) throws Exception {
		System.out.println("=== Alphanumeric Backpack Example ===");

		AlphanumericDevice device = new AlphanumericDevice();
		device.clear();
		device.display();
		device.addCharacter('A', false);
		device.addCharacter('B', true);
		device.addCharacter('C', false);
		device.addCharacter('D', true);
		device.display();
        TimeUnit.SECONDS.sleep(3);
        device.clear();
        device.display();
        device.addCharacter('R', false);
        device.addCharacter('O', true);
        device.addCharacter('B', false);
        device.addValue((short) 0x3FFF, true);
        device.display();


        System.out.println("Press <Enter> to quit!");
        System.in.read();
        device.clear();
        device.display();

	}
}
