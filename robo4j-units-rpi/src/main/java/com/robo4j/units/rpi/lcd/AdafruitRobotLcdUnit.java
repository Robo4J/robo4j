/*
 * Copyright (c) 2014, 2017, Marcus Hirt, Miroslav Wengner
 *
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.units.rpi.lcd;

import java.io.IOException;

import com.robo4j.core.ConfigurationException;
import com.robo4j.core.LifecycleState;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboResult;
import com.robo4j.core.RoboUnit;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.hw.rpi.i2c.adafruitlcd.AdafruitLcd;
import com.robo4j.hw.rpi.i2c.adafruitlcd.LcdFactory;
import com.robo4j.units.rpi.I2CEndPoint;
import com.robo4j.units.rpi.I2CRegistry;

/**
 * A {@link RoboUnit} for the Adafruit 16x2 character LCD shield.
 *
 * Lcd is used to display robot direction, last pressed button
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class AdafruitRobotLcdUnit extends I2CRoboUnit<LcdMessage> {

    private AdafruitLcd lcd;

    public AdafruitRobotLcdUnit(RoboContext context, String id) {
        super(context, id);
    }

    static AdafruitLcd getLCD(int bus, int address) throws IOException {
        Object lcd = I2CRegistry.getI2CDeviceByEndPoint(new I2CEndPoint(bus, address));
        if (lcd == null) {
            try {
                lcd = LcdFactory.createLCD(bus, address);
            } catch (Exception e) {
                throw new AdafruitException("error", e);
            }
            I2CRegistry.registerI2CDevice(lcd, new I2CEndPoint(bus, address));
        }
        return (AdafruitLcd) lcd;
    }

    /**
     *
     * @param configuration
     *            - unit configuration
     * @throws ConfigurationException
     */
    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        super.onInitialization(configuration);
        try {
            lcd = getLCD(getBus(), getAddress());
        } catch (IOException e) {
            throw new ConfigurationException("Could not initialize LCD", e);
        }
    }

    /**
     *
     * @param message
     *            the message received by this unit.
     *
     * @return
     */
    @Override
    public RoboResult<LcdMessage, Object> onMessage(LcdMessage message) {
        try {
            processControlMessage(message);
        } catch (Exception e) {
            SimpleLoggingUtil.debug(getClass(), "Could not accept message" + message.toString(), e);
        }
        return super.onMessage(message);
    }

    @Override
    public void stop() {
        setState(LifecycleState.STOPPING);
        try {
            lcd.clear();
            lcd.setDisplayEnabled(false);
            lcd.stop();
        } catch (IOException e) {
            throw new AdafruitException("error", e);
        }
        setState(LifecycleState.STOPPED);
    }


    //Private Methods
    /**
     * @param message
     *            accepted message type
     * @throws IOException
     */
    private void processControlMessage(LcdMessage message) throws IOException {

        switch (message.getType()) {
            case CLEAR:
                lcd.clear();
                break;
            case SET_TEXT:
            case LEFT:
            case RIGHT:
            case MOVE:
            case BACK:
            case HALT:
                lcd.setText(message.getText());
                break;
            case STOP:
                lcd.stop();
                break;
            default:
                SimpleLoggingUtil.error(getClass(), message.getType() + " control not supported!");
                break;
        }
    }




}
