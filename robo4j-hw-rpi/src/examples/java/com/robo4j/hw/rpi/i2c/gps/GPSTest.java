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
package com.robo4j.hw.rpi.i2c.gps;

import java.io.IOException;

import com.robo4j.hw.rpi.gps.GPS;
import com.robo4j.hw.rpi.gps.GPSListener;
import com.robo4j.hw.rpi.gps.PositionEvent;
import com.robo4j.hw.rpi.gps.VelocityEvent;

/**
 * Listens for GPS event and prints them to stdout as they come.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class GPSTest {
	public static void main(String[] args) throws InterruptedException, IOException {
		GPS sparkFunGPS = new TitanX1GPS();
		sparkFunGPS.addListener(new GPSListener() {
			@Override
			public void onPosition(PositionEvent event) {
				System.out.println(event);
			}

			@Override
			public void onVelocity(VelocityEvent event) {
				System.out.println(event);
			}
		});
		sparkFunGPS.start();
		System.out.println("Press <Enter> to quit!");
		System.in.read();
		sparkFunGPS.shutdown();
	}
}
