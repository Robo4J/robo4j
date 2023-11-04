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
package com.robo4j.units.rpi.led;

import java.io.Serializable;
import java.nio.charset.Charset;

/**
 * Message for the alphanumeric display with dots after the characters.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class AlphaNumericMessage implements Serializable {
	public static final AlphaNumericMessage MESSAGE_CLEAR = new AlphaNumericMessage(BackpackMessageCommand.CLEAR);
	public static final AlphaNumericMessage MESSAGE_DISPLAY = new AlphaNumericMessage(BackpackMessageCommand.DISPLAY);

	private static final long serialVersionUID = 1L;

	private final BackpackMessageCommand command;

	// String in 7-bit ascii
	private final byte[] characters;

	// Start position, if specified
	private final int startPosition;

	// Turn dot on/off after the character at the position
	private final boolean[] dots;

	/**
	 * Constructor.
	 * 
	 * @param command
	 *            the command to send.
	 */
	public AlphaNumericMessage(BackpackMessageCommand command) {
		this(command, new byte[0], new boolean[0], -1);
	}

	/**
	 * Constructor. Will not care about dots...
	 * 
	 * @param command
	 *            the message command.
	 * @param message
	 *            the alphanumeric message to show.
	 * @param startPosition
	 *            -1 if do not care, otherwise the starting position.
	 */
	public AlphaNumericMessage(BackpackMessageCommand command, String message, int startPosition) {
		this(command, message.getBytes(Charset.forName("ISO646-US")), new boolean[message.length()], startPosition);
	}

	/**
	 * Constructor.
	 * 
	 * @param command
	 *            the message command.
	 * @param characters
	 *            the characters encoded as 7 bit ascii.
	 * @param dots
	 *            the dots on/off after each character
	 * @param startPosition
	 */
	public AlphaNumericMessage(BackpackMessageCommand command, byte[] characters, boolean[] dots, int startPosition) {
		this.command = command;
		this.characters = characters;
		this.dots = dots;
		this.startPosition = startPosition;
	}

	public byte[] getCharacters() {
		return characters;
	}

	public int getStartPosition() {
		return startPosition;
	}

	public boolean[] getDots() {
		return dots;
	}

	public BackpackMessageCommand getCommand() {
		return command;
	}
}
