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

package com.robo4j.units.rpi.led;

import com.robo4j.RoboContext;
import com.robo4j.hw.rpi.i2c.adafruitbackpack.AbstractBackpack;
import com.robo4j.units.rpi.I2CRoboUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AbstractI2CBackpackUnit abstract I2C LedBackpack unit
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
abstract class AbstractI2CBackpackUnit extends I2CRoboUnit<DrawMessage> {
    static final String ATTRIBUTE_ADDRESS = "address";
    static final String ATTRIBUTE_BUS = "bus";
    static final String ATTRIBUTE_BRIGHTNESS = "brightness";
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractI2CBackpackUnit.class);

    AbstractI2CBackpackUnit(Class<DrawMessage> messageType, RoboContext context, String id) {
        super(messageType, context, id);
    }

    void processMessage(AbstractBackpack device, DrawMessage message) {
        switch (message.getType()) {
            case CLEAR:
                device.clear();
                break;
            case PAINT:
                paint(message);
                break;
            case DISPLAY:
                paint(message);
                device.display();
                break;
            default:
                LOGGER.warn("Illegal message: {}", message);
        }
    }

    abstract void paint(DrawMessage message);
}
