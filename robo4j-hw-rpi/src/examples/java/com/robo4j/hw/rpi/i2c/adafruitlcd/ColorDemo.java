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
package com.robo4j.hw.rpi.i2c.adafruitlcd;

import java.io.IOException;

import com.robo4j.hw.rpi.i2c.adafruitlcd.Color;
import com.robo4j.hw.rpi.i2c.adafruitlcd.AdafruitLcd;

/**
 * This demo should cycle through the background colors. I only have the monochrome one,
 * so I really can't tell if this works. :)
 * 
 * @author Marcus Hirt
 * @author Miroslav Wengner (@miragemiko)
 */
public class ColorDemo implements LCDDemo {

	@Override
	public String getName() {
		return "Backlight";
	}

	@Override
	public void run(AdafruitLcd lcd) throws IOException {
		lcd.clear();
		lcd.setText("Color changes:");
		Util.sleep(1000);
		for (Color c : Color.values()) {
			lcd.setText(1, "Color: " + c.toString() + "      ");
			lcd.setBacklight(c);
			Util.sleep(1000);
		}
		lcd.setBacklight(Color.ON);
		lcd.clear();
		lcd.setText("Backlight Test:\nDone!");	
	}

}
