/*
 * Copyright (c) 2014, 2017, Miroslav Wengner, Marcus Hirt
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
package com.robo4j.rpi.lcd;

import java.io.IOException;
import java.util.Map;

import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.core.LifecycleState;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboUnit;
import com.robo4j.hw.rpi.i2c.adafruitlcd.Button;
import com.robo4j.hw.rpi.i2c.adafruitlcd.ButtonListener;
import com.robo4j.hw.rpi.i2c.adafruitlcd.ButtonPressedObserver;
import com.robo4j.hw.rpi.i2c.adafruitlcd.AdafruitLcd;

/**
 * A {@link RoboUnit} for the button part of the Adafruit 16x2 character LCD
 * shield. Having a separate unit allows the buttons to be used independently of
 * the LCD.
 * 
 * @author Marcus Hirt (@hirt)
 */
public class ButtonUnit extends RoboUnit<Object> {
	public static enum Messages {
		UP("U"), DOWN("D"), LEFT("L"), RIGHT("R"), SELECT("S");

		private String command;

		private Messages(String command) {
			this.command = command;
		}

		public String getMessage() {
			return command;
		}

		public Messages fromCommand(String command) {
			switch (command) {
			case "U":
				return UP;
			case "D":
				return DOWN;
			case "L":
				return LEFT;
			case "R":
				return RIGHT;
			default:
				return SELECT;
			}
		}
	}

	private AdafruitLcd lcd;
	private ButtonPressedObserver observer;
	private String target;
	private ButtonListener buttonListener;

	public ButtonUnit(RoboContext context, String id) {
		super(context, id);
	}

	@Override
	public void initialize(Map<String, String> properties) throws Exception {
		int bus = Integer.parseInt(properties.get("bus"));
		int address = Integer.parseInt(properties.get("address"));
		target = properties.get("target");
		lcd = AdafruitLcdUnit.getLCD(bus, address);
		setState(LifecycleState.INITIALIZED);
	}

	public void start() {
		setState(LifecycleState.STARTING);
		observer = new ButtonPressedObserver(lcd);
		buttonListener = (Button button) -> {
			if (getState() == LifecycleState.STARTED) {
				try {
					switch (button) {
					case UP:
						sendMessage(target, Messages.UP);
						break;
					case DOWN:
						sendMessage(target, Messages.DOWN);
						break;
					case RIGHT:
						sendMessage(target, Messages.LEFT);
						break;
					case LEFT:
						sendMessage(target, Messages.RIGHT);
						break;
					case SELECT:
						sendMessage(target, Messages.SELECT);
						break;
					default:
						lcd.clear();
						lcd.setText(String.format("Button %s\nis not in use...", button.toString()));
					}
				} catch (IOException e) {
					handleException(e);
				}
			}
		};

		observer.addButtonListener(buttonListener);
		setState(LifecycleState.STARTED);
	}

	public void stop() {
		observer.removeButtonListener(buttonListener);
		observer = null;
		buttonListener = null;
	}

	public void shutdown() {
		try {
			lcd.stop();
		} catch (IOException e) {
			SimpleLoggingUtil.error(getClass(), "Failed to stop LCD", e);
		}
	}

	private void handleException(IOException e) {
		setState(LifecycleState.STOPPING);
		shutdown();
		setState(LifecycleState.FAILED);
	}
}
