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

package com.robo4j.hw.rpi.imu.bno.shtp;

import java.util.HashMap;
import java.util.Map;

/**
 * The Shtp channels in the BNO 080.
 */
public enum ShtpChannel {
	//@formatter:off
    NONE            (-1),
    COMMAND         (0),
    EXECUTABLE      (1),
    CONTROL         (2),
    REPORTS         (3),
    WAKE_REPORTS    (4),
    GYRO            (5);
    //@formatter:on

	private static final Map<Byte, ShtpChannel> map = getMap();
	private byte channel;

	ShtpChannel(int channel) {
		this.channel = (byte) channel;
	}

	public byte getChannel() {
		return channel;
	}

	public static ShtpChannel getByChannel(byte channel) {
		ShtpChannel result = map.get(channel);
		return result == null ? NONE : result;
	}

	private static Map<Byte, ShtpChannel> getMap() {
		Map<Byte, ShtpChannel> map = new HashMap<>();
		for (ShtpChannel ch : values()) {
			map.put(ch.channel, ch);
		}
		return map;
	}
}
