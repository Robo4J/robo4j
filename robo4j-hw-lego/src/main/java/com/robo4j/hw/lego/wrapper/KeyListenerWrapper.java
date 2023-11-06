/*
 * Copyright (c) 2014, 2023, Marcus Hirt, Miroslav Wengner
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
import lejos.hardware.KeyListener;

import java.util.function.Consumer;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class KeyListenerWrapper {

	private static class SimpleKeyListener implements KeyListener {

		private final KeyWrapper keyWrapper;
		private Consumer<KeyWrapper> pressedConsumer;
		private Consumer<KeyWrapper> releasedConsumer;

		private SimpleKeyListener(KeyWrapper keyWrapper) {
			this.keyWrapper = keyWrapper;
		}

		@Override
		public void keyPressed(Key key) {
			pressedConsumer.accept(keyWrapper);
		}

		@Override
		public void keyReleased(Key key) {
			releasedConsumer.accept(keyWrapper);
		}

		public void setPressedConsumer(Consumer<KeyWrapper> pressedConsumer) {
			this.pressedConsumer = pressedConsumer;
		}

		public void setReleasedConsumer(Consumer<KeyWrapper> releasedConsumer) {
			this.releasedConsumer = releasedConsumer;
		}
	}

	private final SimpleKeyListener simpleKeyListener;

	public KeyListenerWrapper(KeyWrapper keyWrapper) {
		simpleKeyListener = new SimpleKeyListener(keyWrapper);
	}

	public void setKeyPressed(Consumer<KeyWrapper> consumer) {
		simpleKeyListener.pressedConsumer = consumer;
	}

	public void setKeyReleased(Consumer<KeyWrapper> consumer) {
		simpleKeyListener.releasedConsumer = consumer;
	}
}
