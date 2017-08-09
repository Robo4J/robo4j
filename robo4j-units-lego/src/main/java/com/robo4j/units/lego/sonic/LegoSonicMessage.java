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

package com.robo4j.units.lego.sonic;

import com.robo4j.core.RoboReference;
import com.robo4j.units.lego.enums.LegoSonicMessageTypeEnum;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class LegoSonicMessage {

    private final RoboReference<?> source;
    private final LegoSonicMessageTypeEnum type;

    public LegoSonicMessage(String text){
        this(null, LegoSonicMessageTypeEnum.getInternalByName(text));
    }

    public LegoSonicMessage(LegoSonicMessageTypeEnum type) {
        this(null, type);
    }

    public LegoSonicMessage(RoboReference<?> source, LegoSonicMessageTypeEnum type) {
        this.source = source;
        this.type = type;
    }

    public LegoSonicMessage(RoboReference<?> source, String text) {
        this.source = source;
        this.type = LegoSonicMessageTypeEnum.getInternalByName(text);
    }

    public RoboReference<?> getSource(){
        return source;
    }

    public LegoSonicMessageTypeEnum getType(){
        return type;
    }

    /*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
    @Override
    public String toString() {
        return String.format("Type: %s, Source: %s", type, String.valueOf(source));
    }
}
