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

import com.robo4j.AttributeDescriptor;
import com.robo4j.RoboContext;
import com.robo4j.configuration.Configuration;
import com.robo4j.hw.lego.enums.AnalogPortEnum;
import com.robo4j.hw.lego.enums.MotorTypeEnum;
import com.robo4j.hw.lego.wrapper.MotorTestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class SimpleTankUnitMock extends SimpleTankUnit {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleTankUnitMock.class);

    public SimpleTankUnitMock(RoboContext context, String id) {
        super(context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) {
        super.rightMotor = new MotorTestWrapper(AnalogPortEnum.C,
                MotorTypeEnum.NXT);
        super.leftMotor = new MotorTestWrapper(AnalogPortEnum.B,
                MotorTypeEnum.NXT);
    }

    @Override
    public void shutdown() {
        super.shutdown();
        LOGGER.info("executor is down");
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> R onGetAttribute(AttributeDescriptor<R> attribute) {
        if (attribute.attributeName().equals("getStatus") && attribute.attributeType() == Boolean.class) {
            return (R) Boolean.valueOf(true);
        }
        return null;
    }

}
