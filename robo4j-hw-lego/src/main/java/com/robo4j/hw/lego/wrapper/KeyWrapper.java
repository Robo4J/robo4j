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
package com.robo4j.hw.lego.wrapper;

import lejos.hardware.Key;

import java.util.function.Consumer;

/**
 * KeyWrapper wrapper to lego key
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class KeyWrapper {

	private final Key key;

	public KeyWrapper(Key key) {
		this.key = key;
	}

	public Key getKey() {
		return key;
	}

	public int getId() {
		return key.getId();
	}

	public boolean isDown() {
		return key.isDown();
	}

	public boolean isUp() {
		return key.isUp();
	}

	public void waitForPress() {
		key.waitForPress();
	}

	public void waitForPressAndRelease() {
		key.waitForPressAndRelease();
	}

	public void addKeyListener(Consumer<KeyWrapper> pressed, Consumer<KeyWrapper> released){
		KeyListenerWrapper keyListenerWrapper = new KeyListenerWrapper(this);
        keyListenerWrapper.setKeyPressed(pressed);
        keyListenerWrapper.setKeyReleased(released);
    }

	public void simulateEvent(int event) {
		key.simulateEvent(event);
	}

	public String getName() {
		return key.getName();
	}

}
