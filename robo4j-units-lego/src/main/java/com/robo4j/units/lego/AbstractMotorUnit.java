/*
 * Copyright (c) 2014, 2018, Marcus Hirt, Miroslav Wengner
 *
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.units.lego;

import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.hw.lego.ILegoMotor;
import com.robo4j.units.lego.enums.MotorRotationEnum;

import java.util.concurrent.Future;

/**
 * AbstractMotorUnit provides functionality for the one Motor lego unit
 *
 * {@link SimpleTankUnit}
 * {@link SingleMotorUnit}
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
abstract class AbstractMotorUnit<T> extends RoboUnit<T> {
    AbstractMotorUnit(Class<T> messageType, RoboContext context, String id) {
        super(messageType, context, id);
    }

    Future<Boolean> runEngine(ILegoMotor motor, MotorRotationEnum rotation) {
        return getContext().getScheduler().submit(() -> {
            switch (rotation) {
                case FORWARD:
                    motor.forward();
                    return motor.isMoving();
                case STOP:
                    motor.stop();
                    return motor.isMoving();
                case BACKWARD:
                    motor.backward();
                    return motor.isMoving();
                default:
                    throw new LegoUnitException("no such rotation= " + rotation);
            }
        });
    }
}
