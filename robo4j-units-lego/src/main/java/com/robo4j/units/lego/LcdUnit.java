/*
 * Copyright (C) 2014, 2017. Miroslav Wengner, Marcus Hirt
 * This LcdUnit.java  is part of robo4j.
 * module: robo4j-units-lego
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

package com.robo4j.units.lego;

import com.robo4j.core.ConfigurationException;
import com.robo4j.core.LifecycleState;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboUnit;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.hw.lego.ILcd;
import com.robo4j.hw.lego.wrapper.LcdWrapper;

/**
 * Lego Mindstorm Brick LCD unit
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 * @since 01.02.2017
 */
public class LcdUnit extends RoboUnit<Object> {
	private static final String CONSTANT_CLEAR = "clear";
	protected ILcd lcd;

	public LcdUnit(RoboContext context, String id) {
		super(Object.class, context, id);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onMessage(Object message) {

		String lcdMessage = message.toString();
		switch (lcdMessage) {
		case CONSTANT_CLEAR:
			lcd.initiate();
			break;
		default:
			lcd.printText(lcdMessage);
			break;
		}
	}

	/**
	 *
	 * @param configuration
	 *            the {@link Configuration} provided.
	 * @throws ConfigurationException
     */
	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		setState(LifecycleState.UNINITIALIZED);
		lcd = new LcdWrapper<>();
		setState(LifecycleState.INITIALIZED);
	}

	@Override
	public void start() {
		setState(LifecycleState.STARTING);
		lcd.initiate();
		lcd.printText("Press Escape to quit!");
		setState(LifecycleState.STARTED);
	}

	@Override
	public void shutdown() {
		lcd.printText("System is going down...");
		super.shutdown();
	}
}
