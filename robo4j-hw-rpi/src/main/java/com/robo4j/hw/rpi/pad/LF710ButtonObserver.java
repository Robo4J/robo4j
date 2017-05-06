/*
 * Copyright (c) 2014, 2017, Marcus Hirt, Miroslav Wengner
 *
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.hw.rpi.pad;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * Observing Logitech F710 Pad buttons and joystick behaviour and notifying
 * attached listeners
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class LF710ButtonObserver {

	private static final int SLEEP_MILLIS = 2000;
	private volatile boolean isRunning = false;
	private volatile Thread observerThread;
	private final List<PadInputResponseListener> buttonListeners = new LinkedList<>();
	private final RoboControlPad pad;

	private class ButtonChecker implements Runnable {
		private static final int ANDING_LEFT = 0x00ff;
		private static final int ANDING_LONG_LEFT = 0x00000000000000ff;
		private static final int BUFFER_SIZE = 8;
		private static final int INDEX_START = 0;
		private static final int INDEX_TIME_1 = 1;
		private static final int INDEX_TIME_2 = 2;
		private static final int INDEX_TIME_3 = 3;
		private static final int INDEX_AMOUNT_4 = 4;
		private static final int INDEX_AMOUNT_5 = 5;
		private static final int INDEX_PART = 6;
		private static final int INDEX_ELEMENT = 7;
		private final byte[] buffer = new byte[BUFFER_SIZE];
		private final InputStream source;

		ButtonChecker(InputStream source) {
			this.source = source;
		}

		@Override
		public void run() {
			while (isRunning) {
				try {
					int bytes = source.read(buffer);
					if (bytes == BUFFER_SIZE) {
						final long time = ((((((buffer[INDEX_TIME_3] & ANDING_LONG_LEFT) << BUFFER_SIZE)
								| (buffer[INDEX_TIME_2] & ANDING_LEFT)) << BUFFER_SIZE)
								| (buffer[INDEX_TIME_1] & ANDING_LEFT)) << BUFFER_SIZE)
								| (buffer[INDEX_START] & ANDING_LEFT);
						final short amount = (short) (((buffer[INDEX_AMOUNT_5] & ANDING_LEFT) << BUFFER_SIZE)
								| (buffer[INDEX_AMOUNT_4] & ANDING_LEFT));
						final short part = buffer[INDEX_PART];
						final short element = buffer[INDEX_ELEMENT];
						if (part > 0) {
							final LF710Part lf710Part = LF710Part.getByMask(part);
							switch (lf710Part) {
							case BUTTON:
								fireNotification(new LF710Response(time, amount, lf710Part,
										LF710Button.getByMask(element), getInputState(amount)));
								break;
							case JOYSTICK:
								fireNotification(new LF710Response(time, amount, lf710Part,
										LF710JoystickButton.getByMask(element), getInputState(amount)));
								break;
							default:
								throw new LF710Exception("uknonw pad part:" + lf710Part);
							}
						}
					}
				} catch (IOException e) {
					throw new LF710Exception("gamepad reading problem", e);
				}
			}
		}

		// Private Methods
		private void fireNotification(LF710Response response) {
			synchronized (buttonListeners) {
				buttonListeners.forEach(l -> l.onInputPressed(response));
			}
		}

		private LF710State getInputState(short amount) {
			return (amount == 0) ? LF710State.RELEASED : LF710State.PRESSED;
		}
	}

	public LF710ButtonObserver(RoboControlPad pad) {
		this.pad = pad;
	}

	public void stopButtonMonitor() {
		isRunning = false;
		try {
			pad.disconnect();
			observerThread.join(SLEEP_MILLIS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void addButtonListener(PadInputResponseListener listener) {
		synchronized (listener) {
			if (buttonListeners.isEmpty()) {
				observerThread = startMonitor();
			}
		}
		buttonListeners.add(listener);
	}

	public void removeButtonListener(PadInputResponseListener listener) {
		synchronized (buttonListeners) {
			buttonListeners.remove(listener);
			if (buttonListeners.isEmpty()) {
				stopButtonMonitor();
			}
		}
	}

	// Private Methods
	//TODO, FIXME: do not randomly threads use executor also @see ButtonPressedObserver Adafruit
	private Thread startMonitor() {
		isRunning = true;
		Thread result = new Thread(new ButtonChecker(pad.source()), "LF710Pad button checker");
		result.setDaemon(true);
		result.start();
		return result;
	}
}
