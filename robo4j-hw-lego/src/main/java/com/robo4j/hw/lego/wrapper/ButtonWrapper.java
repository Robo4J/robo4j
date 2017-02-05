/*
 * Copyright (C) 2014, 2017. Miroslav Wengner, Marcus Hirt
 * This ButtonWrapper.java  is part of robo4j.
 * module: robo4j-hw-lego
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

package com.robo4j.hw.lego.wrapper;

import com.robo4j.hw.lego.IButton;
import lejos.hardware.Key;
import lejos.hardware.KeyListener;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class ButtonWrapper<ButtonKey extends Key> implements IButton {

	private ButtonKey buttonKey;

	public ButtonWrapper(ButtonKey buttonKey) {
		this.buttonKey = buttonKey;
	}

	@Override
	public void waitForPressAndRelease() {
		buttonKey.waitForPressAndRelease();
	}

	@Override
	public void addKeyListener(KeyListener listener) {
		buttonKey.addKeyListener(listener);
	}

	@Override
	public String getName() {
		return buttonKey.getName();
	}
}
