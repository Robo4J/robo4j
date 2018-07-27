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
import com.robo4j.RoboReference;
import com.robo4j.configuration.Configuration;
import com.robo4j.hw.lego.ILegoMotor;
import com.robo4j.hw.lego.enums.AnalogPortEnum;
import com.robo4j.hw.lego.enums.MotorTypeEnum;
import com.robo4j.hw.lego.provider.MotorProvider;
import com.robo4j.hw.lego.wrapper.MotorWrapper;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.units.lego.enums.MotorRotationEnum;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class SingleMotorUnit extends AbstractMotorUnit<MotorRotationEnum> implements RoboReference<MotorRotationEnum> {
	protected volatile ILegoMotor motor;

	public SingleMotorUnit(RoboContext context, String id) {
		super(MotorRotationEnum.class, context, id);
	}

	/**
	 *
	 * @param message
	 *            the message received by this unit.
	 *
	 */
	@Override
	public void onMessage(MotorRotationEnum message) {
		processPlatformMessage(message);
	}

	@Override
	public void shutdown() {
		setState(LifecycleState.SHUTTING_DOWN);
		motor.close();
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

		String motorPort = configuration.getString("motorPort", AnalogPortEnum.A.getType());
		Character motorType = configuration.getCharacter("motorType", MotorTypeEnum.NXT.getType());

		MotorProvider motorProvider = new MotorProvider();
		motor = new MotorWrapper<>(motorProvider, AnalogPortEnum.getByType(motorPort),
				MotorTypeEnum.getByType(motorType));
		setState(LifecycleState.INITIALIZED);
	}

	// Private Methods
	private void processPlatformMessage(MotorRotationEnum message) {
		switch (message) {
		case BACKWARD:
		case FORWARD:
		case STOP:
			runEngine(motor, message);
			break;
		default:
			SimpleLoggingUtil.error(getClass(), message + " not supported!");
			throw new LegoUnitException("single motor command: " + message);
		}

	}
}
