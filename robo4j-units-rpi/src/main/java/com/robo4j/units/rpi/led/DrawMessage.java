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
package com.robo4j.units.rpi.led;

import com.robo4j.hw.rpi.i2c.adafruitbackpack.BiColor;

public class DrawMessage {
	private final short [] xs;
	private final short [] ys;
	private final BiColor [] colors;
	private final MessageType type;

	public enum MessageType {

	    //@formatter:off
	    CLEAR,
	    PAINT,
	    DISPLAY
	    //@formatter:on

	}
	
	public DrawMessage(MessageType type, short[] xs, short[] ys, BiColor[] colors) {
		this.type = type;
		this.xs = xs;
		this.ys = ys;
		this.colors = colors;
	}

	public short [] getXs() {
		return xs;
	}

	public short [] getYs() {
		return ys;
	}

	public BiColor [] getColors() {
		return colors;
	}

	public MessageType getType() {
		return type;
	}
}
