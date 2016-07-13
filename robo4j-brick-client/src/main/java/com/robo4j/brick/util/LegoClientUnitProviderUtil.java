/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This LegoClientUnitProviderUtil.java is part of robo4j.
 *
 *     robo4j is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     robo4j is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.robo4j.brick.util;

import com.robo4j.brick.client.io.ClientException;
import com.robo4j.lego.control.LegoEngine;
import com.robo4j.lego.control.LegoSensor;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.robotics.RegulatedMotor;

/**
 * Utility creates unit base on real lego hardware
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 04.07.2016
 */
public class LegoClientUnitProviderUtil {

    public static final int DEFAULT_SPEED = 300;

    public static RegulatedMotor createEngine(final LegoEngine engine) {

        final RegulatedMotor result;
        switch (engine.getEngine()){
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
                throw new ClientException("ENGINE WRONG");
        }
        result.setSpeed(DEFAULT_SPEED);
        return  result;
    }

    public static EV3TouchSensor createTouchSensor(final LegoSensor sensor){
        return new EV3TouchSensor(LocalEV3.get().getPort(sensor.getPort().getType()));
    }
}
