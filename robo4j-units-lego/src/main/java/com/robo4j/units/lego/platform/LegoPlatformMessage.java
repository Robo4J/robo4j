/*
 * Copyright (C) 2014, 2017. Miroslav Wengner, Marcus Hirt
 * This LegoPlatformMessage.java  is part of robo4j.
 * module: robo4j-units-lego
 *
 * robo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * robo4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.units.lego.platform;

import com.robo4j.core.RoboReference;

/**
 * Lego Platform message
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 * @since 30.01.2017
 */
public class LegoPlatformMessage {

    private final RoboReference<?> source;
    private final LegoPlatformMessageType type;

    public LegoPlatformMessage(String text){
        this(null, LegoPlatformMessageType.getByName(text));
    }

    public LegoPlatformMessage(LegoPlatformMessageType type) {
        this(null, type);
    }

    public LegoPlatformMessage(RoboReference<?> source, LegoPlatformMessageType type) {
        this.source = source;
        this.type = type;
    }

    public LegoPlatformMessage(RoboReference<?> source, String text) {
        this.source = source;
        this.type = LegoPlatformMessageType.getByName(text);
    }

    public RoboReference<?> getSource(){
        return source;
    }

    public LegoPlatformMessageType getType(){
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
