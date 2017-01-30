/*
 * Copyright (C) 2014, 2017. Miroslav Wengner, Marcus Hirt
 * This LegoFactory.java  is part of robo4j.
 * module: robo4j-hw-lego
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

package com.robo4j.hw.lego.wrapper;

import com.robo4j.hw.lego.LegoMotor;
import com.robo4j.hw.lego.LegoSensor;
import com.robo4j.hw.lego.provider.MotorProvider;
import com.robo4j.hw.lego.provider.SensorProvider;

/**
 *
 * Creates
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 * @since 29.01.2017
 */
public final class LegoHwFactory {
	private static final String SYSTEM_PROPERTY_LEGO_MOTOR_MOCK = "lego.motor.mock";
	private static final String SYSTEM_PROPERTY_LEGO_SENOR_MOCK = "lego.sensor.mock";

	private static MotorProvider motorProvider;
	private static SensorProvider sensorProvider;

    @SuppressWarnings("unchecked")
    public static LegoMotor createMotor(LegoMotor motor){
        if(Boolean.getBoolean(SYSTEM_PROPERTY_LEGO_MOTOR_MOCK)){
            return new MotorTestWrapper(motor.getPort(), motor.getType());
        }
        if(motorProvider == null){
            motorProvider = new MotorProvider();
        }
        final MotorWrapper result = new MotorWrapper(motor.getPort(), motor.getType());
        result.setMotor(motorProvider.create(motor));
        return result;
    }

    @SuppressWarnings("unchecked")
    public static LegoSensor createSensor(LegoSensor sensor){
        if(Boolean.getBoolean(SYSTEM_PROPERTY_LEGO_SENOR_MOCK)){
            return new SensorTestWrapper(sensor.getPort(), sensor.getType());
        }
        if(sensorProvider == null){
            sensorProvider = new SensorProvider();
        }
        final SensorWrapper result = new SensorWrapper(sensor.getPort(), sensor.getType());
        result.setSensor(sensorProvider.create(sensor));
        return result;
    }


}
