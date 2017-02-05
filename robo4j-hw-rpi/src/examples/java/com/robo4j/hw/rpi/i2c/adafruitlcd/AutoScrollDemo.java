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
package com.robo4j.hw.rpi.i2c.adafruitlcd;

import java.io.IOException;

/**
 * Tests autoscroll. Check out the documentation for the HD44780 for more info
 * on how the buffer is handled.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class AutoScrollDemo implements LCDDemo {

	@Override
	public String getName() {
		return "AutoScroll";
	}

	@Override
	public void run(AdafruitLcd lcd) throws IOException {
		lcd.clear();
		lcd.setText("AutoScroll off:\n");
		lcd.setCursorPosition(1, 0);
		lcd.setAutoScrollEnabled(false);
		for (int i = 0; i < 24; i++) {
			Util.sleep(200);
			lcd.setText(1, "" + (i % 10));
		}

		lcd.clear();
		lcd.setText("AutoScroll on:\n");
		lcd.setAutoScrollEnabled(true);
		lcd.setCursorPosition(1, 14);
		for (int i = 0; i < 24; i++) {
			Util.sleep(200);
			lcd.setText(1, "" + (i % 10));
		}
		lcd.setAutoScrollEnabled(false);
		lcd.setCursorPosition(0, 0);
		lcd.clear();
		lcd.setText("AutoScroll:\nDone!");
	}

}
