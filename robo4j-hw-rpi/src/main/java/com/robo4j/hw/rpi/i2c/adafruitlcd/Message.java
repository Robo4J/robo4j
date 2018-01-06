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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */
package com.robo4j.hw.rpi.i2c.adafruitlcd;

import java.io.IOException;

import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class Message {
	private static final String COLOR_PREFIX = "-c";
	
	public static void main(String [] params) throws IOException, UnsupportedBusNumberException {
		if (params == null || params.length == 0) {
			System.out.println("Usage: [-c<COLORNAME>] <message>\nValid COLORNAME values are: OFF, RED, GREEN, BLUE, YELLOW, TEAL, VIOLET, WHITE, ON\n");
		}
		Color color = Color.GREEN;
		String message = "";
		for(int i = 0; i < params.length; i++) {
			if (params[i].startsWith(COLOR_PREFIX)) {
				color = Color.getByName(params[i].substring(COLOR_PREFIX.length()));
			} else {
				message = params[i];
			}
		}
		AdafruitLcd lcd = LcdFactory.createLCD();
		lcd.setBacklight(color);
		message = massage(message);
		lcd.setText(message);
		System.out.println(message);
	}

	private static String massage(String message) {
		return message.replace("\\n", "\n");
	}
}
