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
import com.robo4j.hw.lego.enums.DigitalPortEnum;
import com.robo4j.hw.lego.enums.SensorTypeEnum;
import com.robo4j.hw.lego.wrapper.SensorTestWrapper;
import com.robo4j.units.lego.sonic.LegoSonicServoMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
//  TODO : verify usage
public class BasicSonicSensorServoUnitMock extends BasicSonicServoUnit {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicSonicSensorServoUnitMock.class);

    public BasicSonicSensorServoUnitMock(RoboContext context, String id) {
        super(context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) {
        super.sensor = new SensorTestWrapper(DigitalPortEnum.S3, SensorTypeEnum.SONIC);
        LOGGER.info("on initialization");
    }

    @Override
    public void onMessage(LegoSonicServoMessage message) {
        LOGGER.info("onMessage : {}", message);
    }

    @Override
    public void shutdown() {
        super.shutdown();
        LOGGER.info("executor is down");
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
