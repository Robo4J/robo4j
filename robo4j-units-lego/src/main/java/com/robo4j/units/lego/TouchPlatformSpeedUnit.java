/*
 * Copyright (c) 2014, 2018, Marcus Hirt, Miroslav Wengner
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

package com.robo4j.units.lego;

import com.robo4j.ConfigurationException;
import com.robo4j.LifecycleState;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.hw.lego.ILegoSensor;
import com.robo4j.hw.lego.enums.DigitalPortEnum;
import com.robo4j.hw.lego.enums.SensorTypeEnum;
import com.robo4j.hw.lego.provider.SensorProvider;
import com.robo4j.hw.lego.wrapper.SensorWrapper;
import com.robo4j.units.lego.enums.LegoPlatformMessageTypeEnum;
import com.robo4j.units.lego.platform.LegoPlatformMessage;
import com.robo4j.units.lego.touch.TouchSensorMessage;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TouchPlatformSpeedUnit sends the touch event to the lcdTarget
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class TouchPlatformSpeedUnit extends RoboUnit<String> {

	public static final String PROPERTY_TARGET = "lcdTarget";
	public static final String PROPERTY_PLATFORM_TARGET = "platformTarget";
	public static final String PROPERTY_SENSOR_PORT = "sensorPort";
	public static final int PROPERTY_VALUE_DELAY = 1000;
	public static final int PROPERTY_VALUE_INTERVAL = 500;
	public static final int PROPERTY_VALUE_MAX_SPEED = 360;
	public static final String PROPERTY_SPEED_MIN = "speedMin";
	public static final String PROPERTY_SPEED_MAX = "speedMax";
	public static final String PROPERTY_SPEED_INCREMENT = "speedIncrement";
	private AtomicBoolean sensorActive = new AtomicBoolean();
	private ILegoSensor sensor;
	private String lcdTarget;
	private String platformTarget;
	private int speedMin;
	private int speedMax;
	private int speedIncrement;
	private int currentSpeed;

	public TouchPlatformSpeedUnit(RoboContext context, String id) {
		super(String.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		lcdTarget = configuration.getString(PROPERTY_TARGET, null);
		if (lcdTarget == null) {
			throw ConfigurationException.createMissingConfigNameException(PROPERTY_TARGET);
		}

		platformTarget = configuration.getString(PROPERTY_PLATFORM_TARGET, null);
		if (platformTarget == null) {
			throw ConfigurationException.createMissingConfigNameException(PROPERTY_PLATFORM_TARGET);
		}

		String sensorPort = configuration.getString(PROPERTY_SENSOR_PORT, null);
		if (sensorPort == null) {
			throw ConfigurationException.createMissingConfigNameException(PROPERTY_SENSOR_PORT);
		}
		SensorProvider provider = new SensorProvider();
		sensor = new SensorWrapper<>(provider, DigitalPortEnum.getByType(sensorPort), SensorTypeEnum.TOUCH);
		sensorActive.set(true);

		speedMin = configuration.getInteger(PROPERTY_SPEED_MIN, 150);
		speedMax = configuration.getInteger(PROPERTY_SPEED_MAX, 300);
		speedIncrement = configuration.getInteger(PROPERTY_SPEED_INCREMENT, 50);
		if ((speedMin % speedIncrement != 0) || (speedMax % speedIncrement != 0)) {
			throw new ConfigurationException(
					String.format("min: %d and max: %d speed needs to be divisible by increment: %d", speedMin,
							speedMax, speedIncrement));
		}
		currentSpeed = speedMin;

	}

	@Override
	public void onMessage(String message) {
		runTouchSensor();
	}

	@Override
	public void shutdown() {
		setState(LifecycleState.SHUTTING_DOWN);
		sensorActive.set(false);
		sensor.close();
		setState(LifecycleState.SHUTDOWN);
	}

	// private methods
	private void runTouchSensor() {
		getContext().getScheduler().scheduleAtFixedRate(() -> {
			if (sensorActive.get()) {
				TouchSensorMessage message = TouchSensorMessage.parseValue(sensor.getData());
				switch (message) {
				case PRESSED:
					currentSpeed = updateCurrentSpeed(currentSpeed);
					getContext().getReference(lcdTarget).sendMessage("SPEED:" + currentSpeed);
					LegoPlatformMessage platformMessage = new LegoPlatformMessage(null, LegoPlatformMessageTypeEnum.SPEED, currentSpeed);
					getContext().getReference(platformTarget).sendMessage(platformMessage);
					break;
				case RELEASED:
					break;
				default:
					break;
				}
			}
		}, PROPERTY_VALUE_DELAY, PROPERTY_VALUE_INTERVAL, TimeUnit.MILLISECONDS);
	}

	private int updateCurrentSpeed(int speed){
		if(speed == speedMax){
			return speedMin;
		} else {
			return speed + speedIncrement;
		}
	}
}
