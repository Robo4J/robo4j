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
package com.robo4j.hw.rpi.i2c.adafruitlcd;

import com.pi4j.exception.InitializeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class Message {
    private static final Logger LOGGER = LoggerFactory.getLogger(Message.class);
    private static final String COLOR_PREFIX = "-c";

    public static void main(String[] params) throws IOException, InitializeException {
        if (params == null || params.length == 0) {
            LOGGER.info("Usage: [-c<COLORNAME>] <message>\nValid COLORNAME values are: OFF, RED, GREEN, BLUE, YELLOW, TEAL, VIOLET, WHITE, ON\n");
        }
        Color color = Color.GREEN;
        String message = "";
        for (String param : params) {
            if (param.startsWith(COLOR_PREFIX)) {
                color = Color.getByName(param.substring(COLOR_PREFIX.length()));
            } else {
                message = param;
            }
        }
        AdafruitLcd lcd = LcdFactory.createLCD();
        lcd.setBacklight(color);
        message = massage(message);
        lcd.setText(message);
        LOGGER.debug(message);
    }

    private static String massage(String message) {
        return message.replace("\\n", "\n");
    }
}
