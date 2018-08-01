/*
 * Copyright (C) 2014, 2017. Miroslav Wengner, Marcus Hirt
 * This MotorProvider.java  is part of robo4j.
 * module: robo4j-hw-lego
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

package com.robo4j.hw.lego.provider;

import com.robo4j.hw.lego.LegoException;
import com.robo4j.hw.lego.enums.AnalogPortEnum;
import com.robo4j.hw.lego.enums.MotorTypeEnum;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.robotics.RegulatedMotor;

/**
 * Provider is responsible for providing Motor
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public final class MotorProvider implements IProvider<RegulatedMotor, AnalogPortEnum, MotorTypeEnum> {

	@Override
	public RegulatedMotor create(final AnalogPortEnum port, final MotorTypeEnum type) {
		final RegulatedMotor result;
		switch (type) {
		case LARGE:
			result = new EV3LargeRegulatedMotor(LocalEV3.get().getPort(port.getType()));
			break;
		case MEDIUM:
			result = new EV3MediumRegulatedMotor(LocalEV3.get().getPort(port.getType()));
			break;
		case NXT:
			result = new NXTRegulatedMotor(LocalEV3.get().getPort(port.getType()));
			break;
		default:
			throw new LegoException("Lego engine not supported: " + port + " type: " + type);
		}
		return result;
	}

}
