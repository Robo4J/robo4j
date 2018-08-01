/*
 * Copyright (C) 2014, 2017. Miroslav Wengner, Marcus Hirt
 * This LegoTankUnit.java  is part of robo4j.
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
import com.robo4j.configuration.Configuration;
import com.robo4j.hw.lego.ILegoMotor;
import com.robo4j.hw.lego.enums.AnalogPortEnum;
import com.robo4j.hw.lego.enums.MotorTypeEnum;
import com.robo4j.hw.lego.provider.MotorProvider;
import com.robo4j.hw.lego.wrapper.MotorWrapper;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.units.lego.enums.MotorRotationEnum;
import com.robo4j.units.lego.platform.LegoPlatformMessage;
import com.robo4j.units.lego.utils.LegoUtils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Lego Mindstorm tank platform consist from two engines
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class SimpleTankUnit extends AbstractMotorUnit<LegoPlatformMessage> implements RoboReference<LegoPlatformMessage> {

	public static final String PROPERTY_SPEED = "speed";
	public static final String PROPERTY_LEFT_MOTOR_PORT = "leftMotorPort";
	public static final String PROPERTY_LEFT_MOTOR_TYPE = "leftMotorType";
	public static final String PROPERTY_RIGHT_MOTOR_PORT = "rightMotorPort";
	public static final String PROPERTY_RIGHT_MOTOR_TYPE = "rightMotorType";
	/* test visible */
	protected volatile ILegoMotor rightMotor;
	protected volatile ILegoMotor leftMotor;
	private volatile int speed;


	public SimpleTankUnit(RoboContext context, String id) {
		super(LegoPlatformMessage.class, context, id);
	}

	/**
	 *
	 * @param message
	 *            the message received by this unit.
	 */
	@Override
	public void onMessage(LegoPlatformMessage message) {
		processPlatformMessage(message);
	}

	@Override
	public void shutdown() {
		setState(LifecycleState.SHUTTING_DOWN);
		rightMotor.close();
		leftMotor.close();
		setState(LifecycleState.SHUTDOWN);
	}

	/**
	 *
	 * @param configuration
	 *            the {@link Configuration} provided.
	 * @throws ConfigurationException
	 *             exception
	 */
	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		setState(LifecycleState.UNINITIALIZED);
		speed = configuration.getInteger(PROPERTY_SPEED, LegoPlatformMessage.DEFAULT_SPEED);
		String leftMotorPort = configuration.getString(PROPERTY_LEFT_MOTOR_PORT, AnalogPortEnum.B.getType());
		Character leftMotorType = configuration.getCharacter(PROPERTY_LEFT_MOTOR_TYPE, MotorTypeEnum.NXT.getType());
		String rightMotorPort = configuration.getString(PROPERTY_RIGHT_MOTOR_PORT, AnalogPortEnum.C.getType());
		Character rightMotorType = configuration.getCharacter(PROPERTY_RIGHT_MOTOR_TYPE, MotorTypeEnum.NXT.getType());

		MotorProvider motorProvider = new MotorProvider();
		rightMotor = new MotorWrapper<>(motorProvider, AnalogPortEnum.getByType(rightMotorPort),
				MotorTypeEnum.getByType(rightMotorType));
		rightMotor.setSpeed(speed);
		leftMotor = new MotorWrapper<>(motorProvider, AnalogPortEnum.getByType(leftMotorPort),
				MotorTypeEnum.getByType(leftMotorType));
		leftMotor.setSpeed(speed);

		setState(LifecycleState.INITIALIZED);
	}



	// Private Methods
	private void processPlatformMessage(LegoPlatformMessage message) {
		switch (message.getType()) {
		case STOP:
			executeBothEnginesStop(rightMotor, leftMotor);
			break;
		case MOVE:
			executeBothEngines(MotorRotationEnum.FORWARD, rightMotor, leftMotor);
			break;
		case BACK:
			executeBothEngines(MotorRotationEnum.BACKWARD, rightMotor, leftMotor);
			break;
		case LEFT:
			executeTurn(leftMotor, rightMotor);
			break;
		case RIGHT:
			executeTurn(rightMotor, leftMotor);
			break;
		case SPEED:
			checkUpdateSpeed(message);
		default:
			SimpleLoggingUtil.error(getClass(), message.getType() + " not supported!");
			throw new LegoUnitException("PLATFORM COMMAND: " + message);
		}
	}

	private void checkUpdateSpeed(LegoPlatformMessage message) {
		if(message.getSpeed() != speed){
			speed = message.getSpeed();
			rightMotor.setSpeed(speed);
			leftMotor.setSpeed(speed);
		}
	}

	private boolean executeTurn(ILegoMotor... motors) {
		ILegoMotor rOne = motors[LegoUtils.DEFAULT_0];
		ILegoMotor rTwo = motors[LegoUtils.DEFAULT_1];
		Future<Boolean> first = runEngine(rOne, MotorRotationEnum.BACKWARD);
		Future<Boolean> second = runEngine(rTwo, MotorRotationEnum.FORWARD);
		try {
			return first.get() && second.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new LegoUnitException("executeTurnByCycles error: ", e);
		}
	}

	private boolean executeBothEngines(MotorRotationEnum rotation, ILegoMotor... motors) {
		Future<Boolean> motorLeft = runEngine(motors[LegoUtils.DEFAULT_0], rotation);
		Future<Boolean> motorRight = runEngine(motors[LegoUtils.DEFAULT_1], rotation);

		try {
			return motorLeft.get() && motorRight.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new LegoUnitException("BothEnginesByCycles error: ", e);
		}
	}

	private boolean executeBothEnginesStop(ILegoMotor... motors) {
		Future<Boolean> motorLeft = executeEngineStop(motors[LegoUtils.DEFAULT_0]);
		Future<Boolean> motorRight = executeEngineStop(motors[LegoUtils.DEFAULT_1]);
		try {
			return motorLeft.get() && motorRight.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new LegoUnitException("executeBothEnginesStop error: ", e);
		}
	}

	private Future<Boolean> executeEngineStop(ILegoMotor motor) {
		return getContext().getScheduler().submit(() -> {
			motor.stop();
			return motor.isMoving();
		});
	}
}
