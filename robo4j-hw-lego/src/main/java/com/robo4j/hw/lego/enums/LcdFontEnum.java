/*
 * Copyright (c) 2014, 2018, Marcus Hirt, Miroslav Wengner
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

package com.robo4j.hw.lego.enums;

import lejos.hardware.lcd.Font;

/**
 * LcdFontEnum provides mapping between Robo4j framework and lego fonts
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public enum  LcdFontEnum {

    //@formatter:off
    LARGE   (1, Font.getLargeFont()),
    MEDIUM  (2, Font.getDefaultFont()),
    SMALL   (3, Font.getSmallFont()),;
    //@formatter:on

    private final Font font;

    LcdFontEnum(int id, Font font) {
        this.font = font;
    }

    public Font getFont() {
        return font;
    }
}
