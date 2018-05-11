/*
 * Copyright (c) 2014, 2018, Marcus Hirt, Miroslav Wengner
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

import java.util.concurrent.TimeUnit;

import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.hw.rpi.i2c.adafruitlcd.Color;
import com.robo4j.scheduler.Scheduler;

/**
 * This demo should cycle through the background colors.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ColorDemo extends AbstractLcdDemo {

	public ColorDemo(Scheduler scheduler, RoboReference<LcdMessage> lcd) {
		super(scheduler, lcd);
	}

	@Override
	public String getName() {
		return "Colors";
	}

	@Override
	public void runDemo() {
		String prefix = "Color changes:\n";
		lcd.sendMessage(LcdMessage.MESSAGE_CLEAR);

		int delay = 0;
		int i = 0;
		for (; i < Color.values().length - 1; i++) {
			Color c = Color.values()[i];
			scheduler.schedule(lcd, getColorMessage(prefix, c), delay += 1, 1, TimeUnit.SECONDS, 1);
		}
		scheduler.schedule(lcd, getColorMessage(prefix, Color.values()[i]), delay += 1, 1, TimeUnit.SECONDS, 1,
				(RoboContext context) -> {
					lcd.sendMessage(new LcdMessage(getName() + " Demo:\nDone!", Color.ON));
					setDone();
				});
	}

	private LcdMessage getColorMessage(String prefix, Color c) {
		return new LcdMessage(prefix + "Color: " + c.toString(), c);
	}
}
