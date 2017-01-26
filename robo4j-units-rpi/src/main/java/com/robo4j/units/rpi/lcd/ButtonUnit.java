/*
 * Copyright (C) 2016-2017. Miroslav Wengner, Marcus Hirt
 * This ButtonUnit.java  is part of robo4j.
 * module: robo4j-units-rpi
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
package com.robo4j.units.rpi.lcd;

import java.io.IOException;
import java.util.Map;

import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.core.LifecycleState;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboReference;
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
 * @author Miroslav Wengner (@miragemiko)
 * @since 17.12.2016
 */
public class ButtonUnit extends RoboUnit<Object> {

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

	@Override
	public void start() {
		final RoboReference<String> targetRef = getContext().getReference(target);
		setState(LifecycleState.STARTING);
		observer = new ButtonPressedObserver(lcd);
		buttonListener = (Button button) -> {
			if (getState() == LifecycleState.STARTED) {
				try {
					switch (button) {
					case UP:
						targetRef.sendMessage(AdafruitButtonPlateEnum.UP);
						break;
					case DOWN:
						targetRef.sendMessage(AdafruitButtonPlateEnum.DOWN);
						break;
					case RIGHT:
						targetRef.sendMessage(AdafruitButtonPlateEnum.LEFT);
						break;
					case LEFT:
						targetRef.sendMessage(AdafruitButtonPlateEnum.RIGHT);
						break;
					case SELECT:
						targetRef.sendMessage(AdafruitButtonPlateEnum.SELECT);
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
