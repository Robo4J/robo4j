/*
 * Copyright (c) 2014, 2019, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.units.lego.brick;

import com.robo4j.hw.lego.enums.ButtonTypeEnum;
import com.robo4j.units.lego.enums.PlateButtonEnum;

/**
 * Plate button works as the interface of binding LegoMindstorm Hardware button with Robo4J abstraction
 * to allow dynamic configuration
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public interface PlateButtonI {

    PlateButtonEnum getKey();
    ButtonTypeEnum getValue();

}
