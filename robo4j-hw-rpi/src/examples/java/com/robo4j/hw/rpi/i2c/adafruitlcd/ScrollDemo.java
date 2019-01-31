/*
 * Copyright (c) 2014-2019, Marcus Hirt, Miroslav Wengner
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

import com.robo4j.hw.rpi.i2c.adafruitlcd.AdafruitLcd;
import com.robo4j.hw.rpi.i2c.adafruitlcd.impl.AdafruitLcdImpl.Direction;

/**
 * Scrolls the view area back and forth a few times. Check out the documentation
 * for the HD44780 for more info on how the tiny (DDRAM) buffer is handled.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ScrollDemo implements LCDDemo {

	@Override
	public String getName() {
		return "Scroller";
	}

	@Override
	public void run(AdafruitLcd lcd) throws IOException {
		String message = "Running scroller. Be patient!\nBouncing this scroller once.";
		lcd.setText(message);
		for (int i = 0; i < 24; i++) {
			Util.sleep(100);
			lcd.scrollDisplay(Direction.LEFT);
		}
		for (int i = 0; i < 24; i++) {
			Util.sleep(100);
			lcd.scrollDisplay(Direction.RIGHT);
		}
		lcd.setText(1, "Done!             ");
	}
}
