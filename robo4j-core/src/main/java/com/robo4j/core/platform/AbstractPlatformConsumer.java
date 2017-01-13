/*
 * Copyright (C) 2016. Miroslav Wengner, Marcus Hirt
 * This AbstractPlatformConsumer.java  is part of robo4j.
 * module: robo4j-core
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

package com.robo4j.core.platform;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.robo4j.commons.motor.GenericMotor;
import com.robo4j.commons.motor.MotorRotationEnum;

/**
 * @author Miroslav Wengner (@miragemiko)
 * @since 20.12.2016
 */
public abstract class AbstractPlatformConsumer {

	private static final int DEFAULT_1 = 1;
	private static final int DEFAULT_0 = 0;

	abstract public Future<Boolean> runEngine(GenericMotor engine, MotorRotationEnum rotation);

	protected boolean executeTurn(GenericMotor... engines) {
		GenericMotor rOne = engines[DEFAULT_0];
		GenericMotor rTwo = engines[DEFAULT_1];
		Future<Boolean> first = runEngine(rOne, MotorRotationEnum.BACKWARD);
		Future<Boolean> second = runEngine(rTwo, MotorRotationEnum.FORWARD);
		try {
			return first.get() && second.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new ClientPlatformException("executeTurnByCycles error: ", e);
		}
	}

	protected boolean executeBothEngines(MotorRotationEnum rotation, GenericMotor... engines) {
		Future<Boolean> engineLeft = runEngine(engines[DEFAULT_0], rotation);
		Future<Boolean> engineRight = runEngine(engines[DEFAULT_1], rotation);

		try {
			return engineLeft.get() && engineRight.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new ClientPlatformException("BothEnginesByCycles error: ", e);
		}
	}
}
