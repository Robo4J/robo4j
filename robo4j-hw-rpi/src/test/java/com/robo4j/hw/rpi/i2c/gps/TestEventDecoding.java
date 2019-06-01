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
package com.robo4j.hw.rpi.i2c.gps;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.robo4j.hw.rpi.gps.GPS;
import com.robo4j.hw.rpi.gps.NmeaUtils;
import com.robo4j.hw.rpi.gps.PositionEvent;
import com.robo4j.hw.rpi.gps.VelocityEvent;

public class TestEventDecoding {
	@Test
	void testDecodePositionString() {
		GPS gps = new MockGPS();
		String line = "$GNGGA,223249.000,4704.833492,N,00826.465532,E,1,12,0.87,445.503,M,48.002,M,,*70";
		assertTrue(NmeaUtils.hasValidCheckSum(line), "Not a valid checksum!");

		PositionEvent event = XA1110PositionEvent.decode(gps, line);

		assertSame(gps, event.getSource());
		assertNotNull(event.getSource());
		assertNotNull(event);
		assertNotNull(event.getLocation());
		
		System.out.println(event);
	}

	@Test
	void testDecodeVelocityString() {
		GPS gps = new MockGPS();
		String line = "$GNVTG,343.70,T,,M,0.70,N,1.30,K,A*25";
		assertTrue(NmeaUtils.hasValidCheckSum(line), "Not a valid checksum!");

		VelocityEvent event = XA1110VelocityEvent.decode(gps, line);

		assertSame(gps, event.getSource());
		assertNotNull(event.getSource());
		assertNotNull(event);
		assertNotNull(event.getGroundSpeed());
		
		System.out.println(event);
	}
}
