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
package com.robo4j.units.rpi.led;

import com.robo4j.hw.rpi.i2c.adafruitbackpack.BiColor;

/**
 * Message for pixel based LEDs.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class DrawMessage {
	public static final DrawMessage MESSAGE_CLEAR = new DrawMessage(BackpackMessageCommand.CLEAR);

	private final short[] xs;
	private final short[] ys;
	private final BiColor[] colors;
	private final BackpackMessageCommand command;

	/**
	 * Constructor to only send a command.
	 * 
	 * @param command
	 *            the command to send.
	 */
	public DrawMessage(BackpackMessageCommand command) {
		this(command, new short[0], new short[0], new BiColor[0]);
	}

	/**
	 * Constructor.
	 */
	public DrawMessage(BackpackMessageCommand command, short[] xs, short[] ys, BiColor[] colors) {
		this.command = command;
		this.xs = xs;
		this.ys = ys;
		this.colors = colors;
	}

	public short[] getXs() {
		return xs;
	}

	public short[] getYs() {
		return ys;
	}

	public BiColor[] getColors() {
		return colors;
	}

	public BackpackMessageCommand getType() {
		return command;
	}
}
