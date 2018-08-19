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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.hw.lego.wrapper;

import com.robo4j.hw.lego.ILcd;
import com.robo4j.logging.SimpleLoggingUtil;

/**
 * Simple Lego Mindstorm LCD wrapper
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class LcdTestWrapper implements ILcd {

    @Override
    public void initRobo4j(String title, String robotName) {
        SimpleLoggingUtil.debug(getClass(), ":initRobo4j title: " + title + " name:" + robotName);
    }

    @Override
    public void printText(int line, int increment, String text) {
        SimpleLoggingUtil.debug(getClass(), ":printText line: " + line + ", text: " + text);
    }

    @Override
    public void clear() {
        SimpleLoggingUtil.debug(getClass(),":cleared");
    }

    @Override
    public void printText(String text) {
        SimpleLoggingUtil.debug(getClass(),":printText = " + text);
    }
}
