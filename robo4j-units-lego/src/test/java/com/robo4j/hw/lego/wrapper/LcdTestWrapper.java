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
package com.robo4j.hw.lego.wrapper;

import com.robo4j.hw.lego.ILcd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple Lego Mindstorm LCD wrapper
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class LcdTestWrapper implements ILcd {
    private static final Logger LOGGER = LoggerFactory.getLogger(LcdTestWrapper.class);

    @Override
    public void initRobo4j(String title, String robotName) {
        LOGGER.info(":initRobo4j title: {} name:{}", title, robotName);
    }

    @Override
    public void printText(int line, int increment, String text) {
        LOGGER.info(":printText line: {}, text: {}", line, text);
    }

    @Override
    public void clear() {
        LOGGER.info(":cleared");
    }

    @Override
    public void printText(String text) {
        LOGGER.info(":printText = " + text);
    }
}
