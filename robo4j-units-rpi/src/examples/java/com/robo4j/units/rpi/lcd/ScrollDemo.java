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
 * Scrolls the view area back and forth a few times. Check out the documentation
 * for the HD44780 for more info on how the tiny (DDRAM) buffer is handled.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ScrollDemo extends AbstractLcdDemo {

	public ScrollDemo(Scheduler scheduler, RoboReference<LcdMessage> lcd) {
		super(scheduler, lcd);
	}

	@Override
	public String getName() {
		return "Scroller";
	}

	@Override
	public void runDemo() throws IOException {
		final LcdMessage left = new LcdMessage(LcdMessageType.SCROLL, null, null, "left");
		final LcdMessage right = new LcdMessage(LcdMessageType.SCROLL, null, null, "right");
		lcd.sendMessage(new LcdMessage("Running scroller. Be patient!\nBouncing this scroller once."));

		scheduler.schedule(lcd, left, 100, 100, TimeUnit.MILLISECONDS, 24, (RoboContext context1) -> scheduler
				.schedule(lcd, right, 100, 100, TimeUnit.MILLISECONDS, 24, (RoboContext context2) -> {
					lcd.sendMessage(new LcdMessage("Scroller Demo:\nDone!"));
					setDone();
				}));
	}
}
