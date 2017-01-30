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
import com.robo4j.core.RoboResult;
import com.robo4j.core.RoboUnit;
import com.robo4j.core.concurrency.RoboThreadFactory;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.hw.lego.LegoMotor;
import com.robo4j.hw.lego.enums.AnalogPortEnum;
import com.robo4j.hw.lego.enums.MotorTypeEnum;
import com.robo4j.hw.lego.provider.MotorProvider;
import com.robo4j.hw.lego.wrapper.MotorWrapper;
import com.robo4j.units.lego.platform.LegoPlatformMessage;
import com.robo4j.units.lego.platform.MotorRotationEnum;

/**
 * Lego Mindstorm tank platform consist from two engines
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 * @since 30.01.2017
 */
public class SimpleTankUnit extends RoboUnit<String> {

	private static final int DEFAULT_1 = 1;
	private static final int DEFAULT_0 = 0;
	private static final int DEFAULT_THREAD_POOL_SIZE = 2;
	private static final int TERMINATION_TIMEOUT = 2;
	private static final int KEEP_ALIVE_TIME = 10;
	private final LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
	private final ExecutorService executor = new ThreadPoolExecutor(DEFAULT_THREAD_POOL_SIZE, DEFAULT_THREAD_POOL_SIZE,
			KEEP_ALIVE_TIME, TimeUnit.SECONDS, workQueue, new RoboThreadFactory("Robo4J Lego Platform ", true));
	protected volatile LegoMotor rightMotor;
	protected volatile LegoMotor leftMotor;

	public SimpleTankUnit(RoboContext context, String id) {
		super(context, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public RoboResult<String, ?> onMessage(Object message) {

		if (message instanceof LegoPlatformMessage) {
			processPlatformMessage((LegoPlatformMessage) message);
		}

		return super.onMessage(message);
	}

	@Override
	public void shutdown() {
		setState(LifecycleState.SHUTTING_DOWN);
		rightMotor.close();
		leftMotor.close();
        try {
            executor.awaitTermination(TERMINATION_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            SimpleLoggingUtil.error(getClass(), "termination failed");
        }
        super.shutdown();
		System.out.println("LegoTankUnit shutdown");
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		MotorProvider motorProvider = new MotorProvider();
		rightMotor = new MotorWrapper(motorProvider, AnalogPortEnum.C,
				MotorTypeEnum.NXT);
		leftMotor = new MotorWrapper(motorProvider, AnalogPortEnum.B,
				MotorTypeEnum.NXT);

	}

	// Private Methods
	private boolean processPlatformMessage(LegoPlatformMessage message) {
		switch (message.getType()) {
		case STOP:
			return executeBothEnginesStop(rightMotor, leftMotor);
		case MOVE:
			return executeBothEngines(MotorRotationEnum.FORWARD, rightMotor, leftMotor);
		case BACK:
			return executeBothEngines(MotorRotationEnum.BACKWARD, rightMotor, leftMotor);
		case LEFT:
			return executeTurn(leftMotor, rightMotor);
		case RIGHT:
			return executeTurn(rightMotor, leftMotor);
		default:
			SimpleLoggingUtil.error(getClass(), message.getType() + " not supported!");
			throw new LegoUnitException("PLATFORM COMMAND: " + message);
		}
	}

	private boolean executeTurn(LegoMotor... motors) {
		LegoMotor rOne = motors[DEFAULT_0];
		LegoMotor rTwo = motors[DEFAULT_1];
		Future<Boolean> first = runEngine(rOne, MotorRotationEnum.BACKWARD);
		Future<Boolean> second = runEngine(rTwo, MotorRotationEnum.FORWARD);
		try {
			return first.get() && second.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new LegoUnitException("executeTurnByCycles error: ", e);
		}
	}

	protected boolean executeBothEngines(MotorRotationEnum rotation, LegoMotor... motors) {
		Future<Boolean> motorLeft = runEngine(motors[DEFAULT_0], rotation);
		Future<Boolean> motorRight = runEngine(motors[DEFAULT_1], rotation);

		try {
			return motorLeft.get() && motorRight.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new LegoUnitException("BothEnginesByCycles error: ", e);
		}
	}

	private Future<Boolean> runEngine(LegoMotor motor, MotorRotationEnum rotation) {
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

	private boolean executeBothEnginesStop(LegoMotor... motors) {
		Future<Boolean> motorLeft = executeEngineStop(motors[DEFAULT_0]);
		Future<Boolean> motorRight = executeEngineStop(motors[DEFAULT_1]);
		try {
			return motorLeft.get() && motorRight.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new LegoUnitException("executeBothEnginesStop error: ", e);
		}
	}

	private Future<Boolean> executeEngineStop(LegoMotor motor) {
		return executor.submit(() -> {
			motor.stop();
			return motor.isMoving();
		});
	}
}
