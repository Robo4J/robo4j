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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.units.lego;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.robo4j.core.ConfigurationException;
import com.robo4j.core.LifecycleState;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboReference;
import com.robo4j.core.RoboResult;
import com.robo4j.core.RoboUnit;
import com.robo4j.core.concurrency.RoboThreadFactory;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.hw.lego.ILegoMotor;
import com.robo4j.hw.lego.enums.AnalogPortEnum;
import com.robo4j.hw.lego.enums.MotorTypeEnum;
import com.robo4j.hw.lego.provider.MotorProvider;
import com.robo4j.hw.lego.wrapper.MotorWrapper;
import com.robo4j.units.lego.platform.LegoPlatformMessage;
import com.robo4j.units.lego.platform.MotorRotationEnum;
import com.robo4j.units.lego.utils.LegoUtils;

/**
 * Lego Mindstorm tank platform consist from two engines
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class SimpleTankUnit extends RoboUnit<LegoPlatformMessage> implements RoboReference<LegoPlatformMessage> {

	/* test visible  */
	protected volatile ILegoMotor rightMotor;
	protected volatile ILegoMotor leftMotor;
	private ExecutorService executor = new ThreadPoolExecutor(LegoUtils.DEFAULT_THREAD_POOL_SIZE, LegoUtils.DEFAULT_THREAD_POOL_SIZE,
			LegoUtils.KEEP_ALIVE_TIME, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
			new RoboThreadFactory("Robo4J Lego Platform", true));

	public SimpleTankUnit(RoboContext context, String id) {
		super(LegoPlatformMessage.class, context, id);
	}

	/**
	 *
	 * @param message
	 *            the message received by this unit.
	 *
	 * @return result
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
		try {
			executor.awaitTermination(LegoUtils.TERMINATION_TIMEOUT, TimeUnit.SECONDS);
			executor.shutdown();
		} catch (InterruptedException e) {
			SimpleLoggingUtil.error(getClass(), "termination failed");
		}
		if (executor.isShutdown()) {
			SimpleLoggingUtil.debug(getClass(), "executor is down");
		}
		setState(LifecycleState.SHUTDOWN);
	}

	/**
	 *
	 * @param configuration
	 *            the {@link Configuration} provided.
	 * @throws ConfigurationException
	 */
	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		setState(LifecycleState.UNINITIALIZED);

		String leftMotorPort = configuration.getString("leftMotorPort", AnalogPortEnum.B.getType());
		Character leftMotorType = configuration.getCharacter("leftMotorType", MotorTypeEnum.NXT.getType());
		String rightMotorPort = configuration.getString("rightMotorPort", AnalogPortEnum.C.getType());
		Character rightMotorType = configuration.getCharacter("rightMotorType", MotorTypeEnum.NXT.getType());

		MotorProvider motorProvider = new MotorProvider();
		rightMotor = new MotorWrapper<>(motorProvider, AnalogPortEnum.getByType(rightMotorPort),
				MotorTypeEnum.getByType(rightMotorType));
		leftMotor = new MotorWrapper<>(motorProvider, AnalogPortEnum.getByType(leftMotorPort),
				MotorTypeEnum.getByType(leftMotorType));

		setState(LifecycleState.INITIALIZED);
	}

	// Private Methods
	private RoboResult<LegoPlatformMessage, Boolean> processPlatformMessage(LegoPlatformMessage message) {
		switch (message.getType()) {
		case STOP:
			return createResult(executeBothEnginesStop(rightMotor, leftMotor));
		case MOVE:
			return createResult(executeBothEngines(MotorRotationEnum.FORWARD, rightMotor, leftMotor));
		case BACK:
			return createResult(executeBothEngines(MotorRotationEnum.BACKWARD, rightMotor, leftMotor));
		case LEFT:
			return createResult(executeTurn(leftMotor, rightMotor));
		case RIGHT:
			return createResult(executeTurn(rightMotor, leftMotor));
		default:
			SimpleLoggingUtil.error(getClass(), message.getType() + " not supported!");
			throw new LegoUnitException("PLATFORM COMMAND: " + message);
		}
	}

	private RoboResult<LegoPlatformMessage, Boolean> createResult(boolean result) {
		return new RoboResult<>(this, result);
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

	private Future<Boolean> runEngine(ILegoMotor motor, MotorRotationEnum rotation) {
		return executor.submit(() -> {
			switch (rotation) {
			case FORWARD:
				motor.forward();
				SimpleLoggingUtil.debug(getClass(), "runEngine FORWARD rotation= " + motor.isMoving());
				return motor.isMoving();
			case STOP:
				motor.stop();
				return motor.isMoving();
			case BACKWARD:
				motor.backward();
				SimpleLoggingUtil.debug(getClass(), "runEngine BACKWARD rotation= " + motor.isMoving());
				return motor.isMoving();
			default:
				throw new LegoUnitException("no such rotation= " + rotation);
			}
		});
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
		return executor.submit(() -> {
			motor.stop();
			return motor.isMoving();
		});
	}

	public String getId() {
		return super.getId();
	}

	public LifecycleState getState() {
		return null;
	}
}
