/*
 * Copyright (C)  2016. Miroslav Kopecky
 * This LegoSensorEngineProviderUtil.java  is part of robo4j.
 *
 *  robo4j is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  robo4j is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.lego.util;

import com.robo4j.lego.control.LegoSensor;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;

/**
 * Utility creates unit base on real lego hardware
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 04.07.2016
 */
public final class LegoSensorEngineProviderUtil {

    public static EV3TouchSensor createTouchSensor(final LegoSensor sensor){
        return new EV3TouchSensor(LocalEV3.get().getPort(sensor.getPort().getType()));
    }

    public static EV3GyroSensor createGyroSensor(final LegoSensor sensor){
        return new EV3GyroSensor(LocalEV3.get().getPort(sensor.getPort().getType()));
    }

    public static EV3UltrasonicSensor createSonicSensor(final LegoSensor sensor){
        return new EV3UltrasonicSensor(LocalEV3.get().getPort(sensor.getPort().getType()));
    }

    public static EV3ColorSensor createColorSensor(final LegoSensor sensor){
        return new EV3ColorSensor(LocalEV3.get().getPort(sensor.getPort().getType()));
    }
}
