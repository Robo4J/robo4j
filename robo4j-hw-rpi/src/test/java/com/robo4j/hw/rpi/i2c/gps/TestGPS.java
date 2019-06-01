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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.robo4j.hw.rpi.i2c.gps.XA1110Device.NmeaSentenceType;
import com.robo4j.hw.rpi.i2c.gps.XA1110Device.NmeaSetting;

public class TestGPS {

	@Test
	public void testCreateMTKCommand() {
		// Also tests the checksum calculation - example from the documentation.
		String mtkPacket = XA1110Device.createMtkPacket(XA1110Device.PacketType.TEST, null);
		assertEquals("$PMTK000*32\r\n", mtkPacket);
	}

	@Test
	public void testCreateNmeaSetupPacket() {
		// Tests that it is possible to create a command packet for controlling
		// what Nmea packets to receive and at what frequency. Uses example from
		// the documentation, but corrected, since it was wrong...
		String mtkPacket = XA1110Device.createNmeaSentencesAndFrequenciesMtkPacket(new NmeaSetting(NmeaSentenceType.GEOPOS, 1),
				new NmeaSetting(NmeaSentenceType.RECOMMENDED_MIN_SPEC, 1), new NmeaSetting(NmeaSentenceType.COURSE_AND_SPEED, 1),
				new NmeaSetting(NmeaSentenceType.FIX_DATA, 1), new NmeaSetting(NmeaSentenceType.DOPS_SAT, 1),
				new NmeaSetting(NmeaSentenceType.SATS_IN_VIEW, 5), new NmeaSetting(NmeaSentenceType.MTK_DEBUG, 1),
				new NmeaSetting(NmeaSentenceType.TIME_DATE, 1));

		System.out.println(mtkPacket);
		assertEquals("$PMTK314,1,1,1,1,1,5,0,0,0,0,0,0,0,0,0,0,1,1,0*2c\r\n", mtkPacket);
	}
}
