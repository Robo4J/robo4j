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
import com.robo4j.units.lego.enums.MotorRotationEnum;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class TouchMotorUnit extends RoboUnit<Boolean> {

	private static final String PRESSED = "1.0";
	private static final String RELEASED = "0.0";
	private volatile AtomicBoolean sensorActive = new AtomicBoolean();
	private volatile MotorRotationEnum currentState;
	private volatile ILegoSensor sensor;
	private String target;

	public TouchMotorUnit(RoboContext context, String id) {
		super(Boolean.class, context, id);
	}

	@Override
	public void shutdown() {
		setState(LifecycleState.SHUTTING_DOWN);
		sensorActive.set(false);
		sensor.close();
		setState(LifecycleState.SHUTDOWN);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		setState(LifecycleState.UNINITIALIZED);
		target = configuration.getString("target", null);
		if (target == null) {
			throw ConfigurationException.createMissingConfigNameException("target");
		}

		String sensorPort = configuration.getString("touchSensorPort", DigitalPortEnum.S1.getType());
		SensorProvider provider = new SensorProvider();
		sensor = new SensorWrapper<>(provider, DigitalPortEnum.getByType(sensorPort), SensorTypeEnum.TOUCH);

		sensorActive.set(true);
		currentState = MotorRotationEnum.STOP;

		runTouchSensor();
		setState(LifecycleState.INITIALIZED);
	}

	// private methods
	private void runTouchSensor() {
		getContext().getScheduler().execute(() -> {
			while (sensorActive.get()) {
				if (sensor.getData().equals(PRESSED) && currentState == MotorRotationEnum.STOP) {
					currentState = MotorRotationEnum.FORWARD;
					getContext().getReference(target).sendMessage(currentState);
				}

				if (sensor.getData().equals(RELEASED) && currentState == MotorRotationEnum.FORWARD) {
					currentState = MotorRotationEnum.STOP;
					getContext().getReference(target).sendMessage(currentState);
				}
			}
		});
	}

}
