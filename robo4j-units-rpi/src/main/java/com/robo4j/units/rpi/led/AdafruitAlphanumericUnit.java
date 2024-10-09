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

import com.robo4j.ConfigurationException;
import com.robo4j.RoboContext;
import com.robo4j.configuration.Configuration;
import com.robo4j.hw.rpi.i2c.adafruitbackpack.AbstractBackpack;
import com.robo4j.hw.rpi.i2c.adafruitbackpack.AlphanumericDevice;
import com.robo4j.units.rpi.I2CRoboUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * AdafruitAlphanumericUnit
 * <p>
 * This version of the LED backpack is designed for two dual 14-segment
 * "Alphanumeric" displays. These 14-segment displays normally require 18 pins
 * (4 'characters' and 14 total segments each) This backpack solves the
 * annoyance of using 18 pins or a bunch of chips by having an I2C
 * constant-current matrix controller sit neatly on the back of the PCB. The
 * controller chip takes care of everything, drawing all the LEDs in the
 * background. All you have to do is write data to it using the 2-pin I2C
 * interface
 * <p>
 * see https://learn.adafruit.com/adafruit-led-backpack/0-54-alphanumeric
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class AdafruitAlphanumericUnit extends I2CRoboUnit<AlphaNumericMessage> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdafruitAlphanumericUnit.class);
    private AlphanumericDevice device;

    public AdafruitAlphanumericUnit(RoboContext context, String id) {
        super(AlphaNumericMessage.class, context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        super.onInitialization(configuration);
        int brightness = configuration.getInteger(AbstractI2CBackpackUnit.ATTRIBUTE_BRIGHTNESS, AbstractBackpack.DEFAULT_BRIGHTNESS);

        try {
            device = new AlphanumericDevice(getBus(), getAddress(), brightness);
        } catch (IOException e) {
            throw new ConfigurationException("Failed to instantiate device", e);
        }
    }

    @Override
    public void onMessage(AlphaNumericMessage message) {
        switch (message.getCommand()) {
            case CLEAR:
                device.clear();
                break;
            case PAINT:
                render(message);
                break;
            case DISPLAY:
                render(message);
                device.display();
                break;
            default:
                LOGGER.error("Illegal message: {}", message);
        }
    }

    private void render(AlphaNumericMessage message) {
        if (message.getStartPosition() == -1) {
            for (int i = 0; i < message.getCharacters().length; i++) {
                device.addCharacter((char) message.getCharacters()[i], message.getDots()[i]);
            }
        } else {
            for (int i = 0; i < message.getCharacters().length; i++) {
                device.setCharacter((message.getStartPosition() + i) % device.getNumberOfCharacters(), (char) message.getCharacters()[i],
                        message.getDots()[i]);
            }
        }

    }
}
