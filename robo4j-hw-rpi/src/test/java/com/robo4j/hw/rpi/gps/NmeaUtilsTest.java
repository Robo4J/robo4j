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
package com.robo4j.hw.rpi.gps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class NmeaUtilsTest {

	@Test
	public void testGoodCheckSum() {
		String nmeaString = "$GNGGA,223249.000,4704.833492,N,00826.465532,E,1,12,0.87,445.503,M,48.002,M,,*70";
		String cleaned = NmeaUtils.cleanLine(nmeaString);

		assertTrue(NmeaUtils.hasValidCheckSum(cleaned));
	}

	
	@Test
	public void testNoCheckSum() {
		String nmeaString = "$GNGGA,223249.000,4704.833492,N,00826.465532,E,1,12,0.87,445.503,M,48.002,M,,*";
		String cleaned = NmeaUtils.cleanLine(nmeaString);

		assertFalse(NmeaUtils.hasValidCheckSum(cleaned));
	}

	@Test
	public void testIncomplete() {
		String nmeaString = "$GNGGA,223249.000,4704.833492,N,00826.465532,E,1,12,0.87,445.503,";
		String cleaned = NmeaUtils.cleanLine(nmeaString);

		assertFalse(NmeaUtils.hasValidCheckSum(cleaned));
	}
	
	@Test
	public void testCleanLine() {
		String nmeaString = "$GNGGA,223249.000,4704.833492,N,00826.465532,E,1,12,0.87,445.503,M,48.002,M,,*70BLABLABLA";
		String cleaned = NmeaUtils.cleanLine(nmeaString);

		assertTrue(NmeaUtils.hasValidCheckSum(cleaned));
		assertEquals("$GNGGA,223249.000,4704.833492,N,00826.465532,E,1,12,0.87,445.503,M,48.002,M,,*70", cleaned);
	}

	@Test
	public void testBadCheckSum() {
		String nmeaString = "$GNGGA,223249.000,4704.833492,N,00826.465532,E,1,12,0.87,445.503,M,48.002,M,,*79";
		String cleaned = NmeaUtils.cleanLine(nmeaString);

		assertFalse(NmeaUtils.hasValidCheckSum(cleaned));
	}

}
