/*
 * Copyright (C) 2013, 2016, Marcus Hirt
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
package com.robo4j.hw.rpi.i2c.adafruitlcd;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides an example on how you can listen for buttons being
 * pressed on the LCD shield. Notice that this implementation will busy poll the
 * buttons. If you can spare a GPIO port (not one on the MCP27017 on the LCD
 * shield) the buttons can be checked a little bit less often by hooking up INTA
 * (pin 20) to the GPIO port and check for interrupts before reading the state
 * of the input pins. Note that such changes require an update to the LCD class
 * as well.
 * 
 * @author Marcus Hirt
 */
public class ButtonPressedObserver {
	private volatile boolean isRunning = false;
	private final List<ButtonListener> buttonListeners = new LinkedList<ButtonListener>();
	private final ILCD lcd;
	private final long [] buttonDownTimes = new long[Button.values().length];
	private volatile Thread t;
	
	private class ButtonChecker implements Runnable {
		@Override
		public void run() {
			while (isRunning) {
				try {
					for (Button button : Button.values()) {
						if (button.isButtonPressed(lcd.buttonsPressedBitmask())) {
							if (buttonDownTimes[button.getPin()] != 0) {
								continue;
							}
							buttonDownTimes[button.getPin()] = System.currentTimeMillis();
						} else if (buttonDownTimes[button.getPin()] != 0) {
							if ((System.currentTimeMillis() - buttonDownTimes[button.getPin()]) > 15) {
								fireNotification(button);
							}
							buttonDownTimes[button.getPin()] = 0;
						}
					}
				} catch (IOException e) {
					Logger.getLogger("se.hirt.pi.adafruit").log(Level.SEVERE,
							"Could not get buttons bitmask!", e);
				}
				sleep(15);
				Thread.yield();
			}
		}

		private void sleep(int time) {
			try {
				Thread.sleep(time);
			} catch (InterruptedException e) {
				Logger.getLogger("se.hirt.pi.adafruit").log(Level.SEVERE,
						"Could not get buttons bitmask!", e);
			}			
		}

		private void fireNotification(Button trackedButton) {
			ButtonListener[] listeners;
			synchronized (buttonListeners) {
				listeners = buttonListeners
						.toArray(new ButtonListener[buttonListeners.size()]);
			}
			for (ButtonListener l : listeners) {
				l.onButtonPressed(trackedButton);
			}
		}

	}

	public ButtonPressedObserver(ILCD lcd) {
		this.lcd = lcd;
	}

	public void removeButtonListener(ButtonListener l) {
		synchronized (buttonListeners) {
			buttonListeners.remove(l);
			if (buttonListeners.isEmpty()) {
				stopButtonMonitor();
			}
		}
	}

	public void stopButtonMonitor() {
		isRunning = false;
		try {
			t.join(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void addButtonListener(ButtonListener l) {
		synchronized (buttonListeners) {
			if (buttonListeners.isEmpty()) {
				startButtonMonitor();
			}
			buttonListeners.add(l);
		}
	}

	public void startButtonMonitor() {
		isRunning = true;
		t = new Thread(new ButtonChecker(), "Button Checker");
		t.setDaemon(true);
		t.start();
	}
}
