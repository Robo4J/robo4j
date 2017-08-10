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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */
package com.robo4j.units.rpi.lcd;

import java.io.IOException;

import com.robo4j.core.ConfigurationException;
import com.robo4j.core.LifecycleState;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboReference;
import com.robo4j.core.RoboUnit;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.hw.rpi.i2c.adafruitlcd.AdafruitLcd;
import com.robo4j.hw.rpi.i2c.adafruitlcd.Button;
import com.robo4j.hw.rpi.i2c.adafruitlcd.ButtonListener;
import com.robo4j.hw.rpi.i2c.adafruitlcd.ButtonPressedObserver;
import com.robo4j.units.rpi.I2CRoboUnit;

/**
 * A {@link RoboUnit} for the button part of the Adafruit 16x2 character LCD
 * shield. Having a separate unit allows the buttons to be used independently of
 * the LCD.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class AdafruitButtonUnit extends I2CRoboUnit<Object> {
	private AdafruitLcd lcd;
	private ButtonPressedObserver observer;
	private String target;
	private ButtonListener buttonListener;

	public AdafruitButtonUnit(RoboContext context, String id) {
		super(Object.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		super.onInitialization(configuration);
		target = configuration.getString("target", null);
		if (target == null) {
			throw ConfigurationException.createMissingConfigNameException("target");
		}
		try {
			lcd = AdafruitLcdUnit.getLCD(getBus(), getAddress());
		} catch (IOException e) {
			throw new ConfigurationException("Could not initialize LCD shield", e);
		}
		setState(LifecycleState.INITIALIZED);
	}

	@Override
	public void start() {
		final RoboReference<AdafruitButtonEnum> targetRef = getContext().getReference(target);
		setState(LifecycleState.STARTING);
		observer = new ButtonPressedObserver(lcd);
		buttonListener = (Button button) -> {
			if (getState() == LifecycleState.STARTED) {
				try {
					switch (button) {
					case UP:
						targetRef.sendMessage(AdafruitButtonEnum.UP);
						break;
					case DOWN:
						targetRef.sendMessage(AdafruitButtonEnum.DOWN);
						break;
					case RIGHT:
						targetRef.sendMessage(AdafruitButtonEnum.LEFT);
						break;
					case LEFT:
						targetRef.sendMessage(AdafruitButtonEnum.RIGHT);
						break;
					case SELECT:
						targetRef.sendMessage(AdafruitButtonEnum.SELECT);
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

	@Override
	public void stop() {
		observer.removeButtonListener(buttonListener);
		observer = null;
		buttonListener = null;
	}

	@Override
	public void shutdown() {
		try {
			lcd.stop();
		} catch (IOException e) {
			SimpleLoggingUtil.error(getClass(), "Failed to disconnect LCD", e);
		}
	}


	//Private Methods
	private void handleException(IOException e) {
		setState(LifecycleState.STOPPING);
		shutdown();
		setState(LifecycleState.FAILED);
	}
}