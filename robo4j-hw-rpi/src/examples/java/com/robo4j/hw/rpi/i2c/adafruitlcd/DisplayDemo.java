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

/**
 * Simply turns off and on the display a few times.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class DisplayDemo implements LCDDemo {

	@Override
	public String getName() {
		return "Display";
	}

	@Override
	public void run(AdafruitLcd lcd) throws IOException {
		lcd.clear();
		lcd.setText("Turning off/on\ndisplay 10 times!");
		Util.sleep(1000);
		for (int i = 0; i < 10; i++) {
			lcd.setDisplayEnabled(false);
			Util.sleep(200);
			lcd.setDisplayEnabled(true);
			Util.sleep(400);
		}
		lcd.clear();
		lcd.setText("Display Test:\nDone!");
	}

}
