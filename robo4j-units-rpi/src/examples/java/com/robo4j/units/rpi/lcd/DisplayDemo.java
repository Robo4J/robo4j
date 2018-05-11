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

import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.scheduler.Scheduler;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Simply turns off and on the display a few times.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class DisplayDemo extends AbstractLcdDemo implements LcdDemo {

	public DisplayDemo(Scheduler scheduler, RoboReference<LcdMessage> lcd) {
		super(scheduler, lcd);
	}

	@Override
	public String getName() {
		return "Display";
	}

	@Override
	public void runDemo() throws IOException {
		lcd.sendMessage(LcdMessage.MESSAGE_CLEAR);
		lcd.sendMessage(new LcdMessage(LcdMessageType.SET_TEXT, null, null, "Turning off/on\ndisplay 10 times!"));

		scheduler.schedule(lcd, LcdMessage.MESSAGE_TURN_OFF, 300, 600, TimeUnit.MILLISECONDS, 10);
		scheduler.schedule(lcd, LcdMessage.MESSAGE_TURN_ON, 600, 600, TimeUnit.MILLISECONDS, 10,
				(RoboContext context ) ->  {
						lcd.sendMessage(LcdMessage.MESSAGE_CLEAR);
						lcd.sendMessage(
								new LcdMessage(LcdMessageType.SET_TEXT, null, null, "Display Demo:\nDone!"));
						setDone();
				});
	}
}
