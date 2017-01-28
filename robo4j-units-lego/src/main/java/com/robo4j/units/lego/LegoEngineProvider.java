/*
 * Copyright (C) 2014, 2017. Miroslav Wengner, Marcus Hirt
 * This LegoEngineProvider.java  is part of robo4j.
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

import java.util.Map;
import java.util.stream.Collectors;

import com.robo4j.hw.lego.LegoException;
import com.robo4j.hw.lego.LegoMotor;
import com.robo4j.hw.lego.wrapper.LegoMotorWrapper;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.robotics.RegulatedMotor;

/**
 * Provider is responsible for providing Engine
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 * @since 26.11.2016
 */
public final class LegoEngineProvider<Type extends LegoMotor> implements RegistryProvider<RegulatedMotor, Type> {
	private static final int DEFAULT_SPEED = 300;

	@Override
	public RegulatedMotor create(final LegoMotor engine) {
		final RegulatedMotor result;
		switch (engine.getMotorType()) {
		case LARGE:
			result = new EV3LargeRegulatedMotor(LocalEV3.get().getPort(engine.getPort().getType()));
			break;
		case MEDIUM:
			result = new EV3MediumRegulatedMotor(LocalEV3.get().getPort(engine.getPort().getType()));
			break;
		case NXT:
			result = new NXTRegulatedMotor(LocalEV3.get().getPort(engine.getPort().getType()));
			break;
		default:
			throw new LegoException("Lego engine not supported: " + engine);
		}
		result.setSpeed(DEFAULT_SPEED);
		return result;
	}

	@SuppressWarnings(value = "unchecked")
	@Override
	public Map<String, Type> activate(Map<String, Type> engines) {
		return engines.entrySet().stream().peek(e -> {
			LegoMotor le = e.getValue();
			/* always instance of regulated motor */
			if (le instanceof LegoMotorWrapper) {
				RegulatedMotor rm = create((LegoMotorWrapper) le);
				((LegoMotorWrapper) le).setMotor(rm);
			}
		}).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

}
