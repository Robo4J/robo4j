/*
 * Copyright (C) 2014, 2017. Miroslav Wengner, Marcus Hirt
 * This ButtonListener.java  is part of robo4j.
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

package com.robo4j.units.lego.brick;

import com.robo4j.core.RoboReference;
import com.robo4j.units.lego.enums.LegoPlatformMessageTypeEnum;

import lejos.hardware.Button;
import lejos.hardware.Key;
import lejos.hardware.KeyListener;

/**
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 * @since 31.01.2017
 */
public class ButtonListener implements KeyListener {

	private static final int OFF = 0;
	private final RoboReference<LegoPlatformMessageTypeEnum> target;
	private final PlateButtonEnum button;
	private final int color;

	public ButtonListener(final RoboReference<LegoPlatformMessageTypeEnum> target, PlateButtonEnum button, int color) {
		this.target = target;
		this.button = button;
		this.color = color;
	}

	@Override
	public void keyPressed(Key key) {
		Button.LEDPattern(color);
		target.sendMessage(button.getMessage());
	}

	@Override
	public void keyReleased(Key key) {
		Button.LEDPattern(OFF);
	}
}
