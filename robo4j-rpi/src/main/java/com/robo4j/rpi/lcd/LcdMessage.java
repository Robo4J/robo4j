/*
 * Copyright (c) 2014, 2017, Miroslav Wengner, Marcus Hirt
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
package com.robo4j.rpi.lcd;

import com.robo4j.commons.unit.RoboUnit;
import com.robo4j.hw.rpi.i2c.adafruitlcd.Color;

/**
 * 
 * @author Marcus
 */
public class LcdMessage {
	private final RoboUnit<?> source;
	private final Color color;
	private final String text;
	private final LcdMessageType type;
	
	public LcdMessage(String text) {
		this(LcdMessageType.SET_TEXT, null, null, text);
	}

	public LcdMessage(String text, Color color) {
		this(LcdMessageType.SET_TEXT, null, color, text);
	}
	
	public LcdMessage(LcdMessageType type, RoboUnit<?> source, Color color, String text) {
		this.type = type;
		this.source = source;
		this.color = color;
		this.text = text;
	}

	public RoboUnit<?> getSource() {
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
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("Type: %s, Source: %s, Color: %s, Text: %s", type, source.toString(), color.toString(), text);
	}
}
