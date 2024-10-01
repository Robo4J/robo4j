/*
 * Copyright (c) 2014, 2024, Marcus Hirt, Miroslav Wengner
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

import com.robo4j.ConfigurationException;
import com.robo4j.LifecycleState;
import com.robo4j.RoboContext;
import com.robo4j.configuration.Configuration;
import com.robo4j.hw.lego.enums.AnalogPortEnum;
import com.robo4j.hw.lego.enums.MotorTypeEnum;
import com.robo4j.hw.lego.wrapper.MotorTestWrapper;

/**
 * Simple Tank Test Platform Unit
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class SimpleTankTestUnit extends SimpleTankUnit {

    public SimpleTankTestUnit(RoboContext context, String id) {
        super(context, id);
        System.out.println(getClass().getSimpleName() + " constructor: id: " + id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        setState(LifecycleState.INITIALIZED);

        String leftMotorPort = configuration.getString("leftMotorPort", AnalogPortEnum.B.getType());
        Character leftMotorType = configuration.getCharacter("leftMotorType", MotorTypeEnum.NXT.getType());
        String rightMotorPort = configuration.getString("rightMotorPort", AnalogPortEnum.C.getType());
        Character rightMotorType = configuration.getCharacter("rightMotorType", MotorTypeEnum.NXT.getType());

        rightMotor = new MotorTestWrapper(AnalogPortEnum.getByType(rightMotorPort), MotorTypeEnum.getByType(rightMotorType));
        leftMotor = new MotorTestWrapper(AnalogPortEnum.getByType(leftMotorPort), MotorTypeEnum.getByType(leftMotorType));
        setState(LifecycleState.INITIALIZED);
    }



}
