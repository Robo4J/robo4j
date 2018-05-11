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

import java.io.IOException;

/**
 * This one really doesn't anything but clean up and exit.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ExitDemo extends AbstractLcdDemo {

	private RoboContext ctx;
	public ExitDemo(RoboContext ctx, RoboReference<LcdMessage> lcd) {
		super(null, lcd);
		this.ctx = ctx;
	}

	@Override
	public String getName() {
		return "<Exit>";
	}

	@Override
	public void runDemo() throws IOException {
		ctx.shutdown();
	}

}
