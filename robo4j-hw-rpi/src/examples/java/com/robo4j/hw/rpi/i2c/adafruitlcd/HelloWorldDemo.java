/*
 * Copyright (C) 2014-2017. Miroslav Wengner, Marcus Hirt
 * This HelloWorldTest.java  is part of robo4j.
 * module: robo4j-hw-rpi
 *
 * robo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * robo4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */
package com.robo4j.hw.rpi.i2c.adafruitlcd;

import java.io.IOException;

import com.robo4j.hw.rpi.i2c.adafruitlcd.AdafruitLcd;

/**
 * The mandatory Hello World!
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 * @since 04.01.2017
 */
public class HelloWorldDemo implements LCDDemo {

	@Override
	public String getName() {
		return "Hello World";
	}

	@Override
	public void run(AdafruitLcd lcd) throws IOException {
		lcd.clear();
		lcd.setText("Hello World!\nDone!");
	}

}
