/*
 * Copyright (C) 2014, 2017. Miroslav Wengner, Marcus Hirt
 * This SonicSensorUnitMock.java  is part of robo4j.
 * module: robo4j-units-lego
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

package com.robo4j.units.lego;

import com.robo4j.AttributeDescriptor;
import com.robo4j.ConfigurationException;
import com.robo4j.RoboContext;
import com.robo4j.configuration.Configuration;
import com.robo4j.hw.lego.enums.DigitalPortEnum;
import com.robo4j.hw.lego.enums.SensorTypeEnum;
import com.robo4j.hw.lego.wrapper.SensorTestWrapper;
import com.robo4j.units.lego.sonic.LegoSonicServoMessage;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class BasicSonicSensorServoUnitMock extends BasicSonicServoUnit {

    public BasicSonicSensorServoUnitMock(RoboContext context, String id) {
        super(context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        super.sensor = new SensorTestWrapper(DigitalPortEnum.S3, SensorTypeEnum.SONIC);
        System.out.println("on initialization");
    }

    @Override
    public void onMessage(LegoSonicServoMessage message) {
        System.out.println("onMessage : " + message);
    }

    @Override
    public void shutdown() {
        super.shutdown();
        System.out.println("executor is down");
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> R onGetAttribute(AttributeDescriptor<R> attribute) {
        if (attribute.getAttributeName().equals("getStatus") && attribute.getAttributeType() == Boolean.class) {
            return (R) Boolean.valueOf(true);
        }
        return null;
    }



}
