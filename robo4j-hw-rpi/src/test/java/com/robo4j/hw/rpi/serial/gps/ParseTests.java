/*
 *  Copyright (C) 2015 Marcus Hirt
 *                     www.hirt.se
 *
 * This software is free:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESSED OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright (C) Marcus Hirt, 2015
 */
package com.robo4j.hw.rpi.serial.gps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.robo4j.hw.rpi.serial.gps.PositionEvent;
import com.robo4j.hw.rpi.serial.gps.VelocityEvent;
import com.robo4j.hw.rpi.serial.gps.PositionEvent.FixQuality;

/**
 * Unit tests.
 * 
 * @author Marcus Hirt
 */
public class ParseTests {

	@Test
	public void testVelocityEvent() {
		VelocityEvent ve = new VelocityEvent(null,
				"$GPVTG,054.7,T,034.4,M,005.5,N,010.2,K*48");
		assertEquals(54.7, ve.getTrueTrackMadeGood(), 0.01);
		assertEquals(34.4, ve.getMagneticTrackMadeGood(), 0.01);
		assertEquals(10.2, ve.getGroundSpeed(), 0.01);
	}

	@Test
	public void testPositionEvent() {
		PositionEvent pe = new PositionEvent(null,
				"$GPGGA,123519,4807.038,N,01131.000,E,1,08,0.9,545.4,M,46.9,M,,*47");
		assertNotNull(pe.getTime());
		assertEquals("Could not determine fix quality", pe.getFixQuality(),
				FixQuality.GPS);
		assertEquals(545.4, pe.getAltitude(), 0.01);
		assertEquals(545.4 + 46.9, pe.getElipsoidAltitude(), 0.01);
		assertEquals(8, pe.getNumberOfSatellites());
	}
}
