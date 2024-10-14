/*
 * Copyright (c) 2014, 2024, Marcus Hirt, Miroslav Wengner
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

import com.robo4j.hw.rpi.gps.*;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

public class TestEventDecoding {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestEventDecoding.class);

    @Test
    void testDecodePositionString() {
        GPS gps = new MockGPS();
        String line = "$GNGGA,223249.000,4704.833492,N,00826.465532,E,1,12,0.87,445.503,M,48.002,M,,*70";

        PositionEvent event = XA1110PositionEvent.decode(gps, line);

        LOGGER.info(event.toString());

        assertTrue(NmeaUtils.hasValidCheckSum(line), "Not a valid checksum!");
        assertSame(gps, event.getSource());
        assertNotNull(event.getSource());
        assertNotNull(event);
        assertNotNull(event.getLocation());
    }

    @Test
    void testDecodeVelocityString() {
        GPS gps = new MockGPS();
        String line = "$GNVTG,343.70,T,,M,0.70,N,1.30,K,A*25";
        assertTrue(NmeaUtils.hasValidCheckSum(line), "Not a valid checksum!");

        VelocityEvent event = XA1110VelocityEvent.decode(gps, line);

        LOGGER.info("event: {}", event);

        assertSame(gps, event.getSource());
        assertNotNull(event.getSource());
        assertNotNull(event);
        event.getGroundSpeed();
    }
}
