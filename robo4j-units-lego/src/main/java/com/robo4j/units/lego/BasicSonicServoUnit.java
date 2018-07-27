/*
 * Copyright (C) 2014, 2017. Miroslav Wengner, Marcus Hirt
 * This BasicSonicServoUnit.java  is part of robo4j.
 * module: robo4j-units-lego
 *
 * robo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * robo4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.units.lego;

import com.robo4j.ConfigurationException;
import com.robo4j.LifecycleState;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.hw.lego.ILegoMotor;
import com.robo4j.hw.lego.ILegoSensor;
import com.robo4j.hw.lego.enums.AnalogPortEnum;
import com.robo4j.hw.lego.enums.DigitalPortEnum;
import com.robo4j.hw.lego.enums.MotorTypeEnum;
import com.robo4j.hw.lego.enums.SensorTypeEnum;
import com.robo4j.hw.lego.provider.MotorProvider;
import com.robo4j.hw.lego.provider.SensorProvider;
import com.robo4j.hw.lego.wrapper.MotorWrapper;
import com.robo4j.hw.lego.wrapper.SensorWrapper;
import com.robo4j.units.lego.sonic.LegoServoRotationEnum;
import com.robo4j.units.lego.sonic.LegoSonicBrainMessage;
import com.robo4j.units.lego.sonic.LegoSonicServoMessage;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * BasicSonicServoUnit unit with servo
 *
 * unit is capable to communicate with it self by passing {@link LegoSonicServoMessage}
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class BasicSonicServoUnit extends RoboUnit<LegoSonicServoMessage> implements RoboReference<LegoSonicServoMessage> {
	private static final int POSITION_START = 0;
	private static final int POSITION_STEP = 30;
	private static final int POSITION_MAX = 30; // should be degrees
	private String target;
	private volatile AtomicBoolean servoRight = new AtomicBoolean(false);
	private volatile AtomicBoolean unitActive = new AtomicBoolean(false);
	private volatile AtomicInteger servoPosition = new AtomicInteger(POSITION_START);
	/* used by wrapper */
	volatile ILegoSensor sensor;
	volatile ILegoMotor servo;

	public BasicSonicServoUnit(RoboContext context, String id) {
		super(LegoSonicServoMessage.class, context, id);
	}

	@Override
	public void onMessage(LegoSonicServoMessage message) {
		processSonicMessage(message);
	}


	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		setState(LifecycleState.UNINITIALIZED);
		target = configuration.getString("target", null);
		if (target == null) {
			throw ConfigurationException.createMissingConfigNameException("target");
		}

		String sensorType = configuration.getString("sonicSensorPort", DigitalPortEnum.S3.getType());
		SensorProvider provider = new SensorProvider();
		sensor = new SensorWrapper<>(provider, DigitalPortEnum.getByType(sensorType), SensorTypeEnum.SONIC);


		String servoPort = configuration.getString("sonicServoPort", AnalogPortEnum.A.getType());
		Character servoType = configuration.getCharacter("sonicServoType", MotorTypeEnum.MEDIUM.getType());
		MotorProvider motorProvider = new MotorProvider();
		servo = new MotorWrapper<>(motorProvider, AnalogPortEnum.getByType(servoPort), MotorTypeEnum.getByType(servoType));

		unitActive.set(true);
		servoRight.set(true);

		setState(LifecycleState.INITIALIZED);
	}

	@Override
	public void shutdown() {
		setState(LifecycleState.SHUTTING_DOWN);
		sensor.close();
		servo.close();
		setState(LifecycleState.SHUTDOWN);
	}


	//Private Methods
	private void sendMessage(LegoSonicBrainMessage message) {
		System.err.println(getClass().getSimpleName() + " SEND target: " + target + " message: " + message);
		getContext().getReference(target).sendMessage(message);
	}

	private void processSonicMessage(LegoSonicServoMessage message){
		try {
			switch (message.getType()){
				case FINISH:
					finish(sensor, servo).get();
					break;
				case SCAN:
					scan(sensor, servo).get();
					break;
				case CENTER:
					center(sensor, servo).get();
					break;
				case STOP:
					stop(sensor, servo).get();
					break;
				default:
					break;
			}
		} catch (InterruptedException | ExecutionException e){
			throw new LegoUnitException("sonic unit processSonicMessage: ", e);
		}
	}

	private Future<Boolean> center(ILegoSensor sensor, ILegoMotor motor){
		return getContext().getScheduler().submit(() -> {
			if(!motor.isMoving()){
				sensor.activate(true);
				String data = sensor.getData();
				sensor.activate(false);
				int currentPosition = servoPosition.get();
				int rotation = -currentPosition;
				final LegoServoRotationEnum servoRotation = !servoRight.get() ? LegoServoRotationEnum.RIGHT : LegoServoRotationEnum.LEFT;
				sendMessage(new LegoSonicBrainMessage(servoRotation, currentPosition,  data));
				rotateToPosition(motor, rotation, currentPosition);
			}
			return true;
		});
	}

	private Future<Boolean> scan(ILegoSensor sensor, ILegoMotor motor){
		unitActive.set(true);
		return getContext().getScheduler().submit(() -> {
			while(unitActive.get()){
				if(!motor.isMoving()){
					sensor.activate(true);
					String data = sensor.getData();
					sensor.activate(false);
					int sign = servoRight.get() ? 1 : -1;
					int currentPosition = servoPosition.get();
					int rotation = sign * (POSITION_STEP);
					if (currentPosition == POSITION_MAX) {
						servoRight.set(false);
					} else if (currentPosition == -POSITION_MAX) {
						servoRight.set(true);
					}
					final LegoServoRotationEnum servoRotation = servoRight.get() ? LegoServoRotationEnum.RIGHT : LegoServoRotationEnum.LEFT;
					sendMessage(new LegoSonicBrainMessage(servoRotation, currentPosition,  data));
					rotateToPosition(motor, rotation, currentPosition);
				}
			}
			return true;
		});
	}

	private void rotateToPosition(ILegoMotor motor, int rotation, int currentPostion) {
		final int calcPosition = currentPostion + rotation;
		servoPosition.set(calcPosition);
		motor.rotate(rotation);
	}

	private Future<Boolean> finish(ILegoSensor sensor, ILegoMotor motor){
		return getContext().getScheduler().submit(() -> {
			sensor.close();
			motor.close();
			unitActive.set(false);
			return motor.isMoving();
		});
	}

	private Future<Boolean> stop(ILegoSensor sensor, ILegoMotor motor){
		return getContext().getScheduler().submit(() -> {
			if (unitActive.get()) {
				motor.stop();
				unitActive.set(false);
				sensor.activate(false);
			}
			return motor.isMoving();
		});
	}

}
