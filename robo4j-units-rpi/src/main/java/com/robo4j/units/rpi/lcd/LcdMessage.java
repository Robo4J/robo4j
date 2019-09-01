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
package com.robo4j.units.rpi.lcd;

import com.robo4j.RoboReference;
import com.robo4j.hw.rpi.i2c.adafruitlcd.Color;

import java.io.Serializable;

/**
 * Message class that can be used to send messages to {@link AdafruitLcdUnit}s.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class LcdMessage implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static LcdMessage MESSAGE_CLEAR = new LcdMessage(LcdMessageType.CLEAR, null, null, null);
	public static LcdMessage MESSAGE_STOP = new LcdMessage(LcdMessageType.STOP, null, null, null);
	public static LcdMessage MESSAGE_TURN_ON = new LcdMessage(LcdMessageType.DISPLAY_ENABLE, null, null, "true");
	public static LcdMessage MESSAGE_TURN_OFF = new LcdMessage(LcdMessageType.DISPLAY_ENABLE, null, null, "false");

	private final RoboReference<?> source;
	private final Color color;
	private final String text;
	private final LcdMessageType type;

	public LcdMessage(String text) {
		this(LcdMessageType.SET_TEXT, null, null, text);
	}

	public LcdMessage(String text, Color color) {
		this(LcdMessageType.SET_TEXT, null, color, text);
	}

	// TODO : we should probably start with source
	public LcdMessage(LcdMessageType type, RoboReference<?> source, Color color, String text) {
		this.type = type;
		this.source = source;
		this.color = color;
		this.text = text;
	}

	public RoboReference<?> getSource() {
		return source;
	}

	public Color getColor() {
		return color;
	}

	public String getText() {
		return text;
	}

	public LcdMessageType getType() {
		return type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("Type: %s, Source: %s, Color: %s, Text: %s", type, String.valueOf(source),
				String.valueOf(color), text);
	}
}
