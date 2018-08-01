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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.units.lego.platform;

import com.robo4j.RoboReference;
import com.robo4j.units.lego.enums.LegoPlatformMessageTypeEnum;

/**
 * Lego Platform message
 * message is immutable
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class LegoPlatformMessage {
    public static final int DEFAULT_SPEED = 300;

    private final RoboReference<?> source;
    private final LegoPlatformMessageTypeEnum type;
    private final int speed;

    public LegoPlatformMessage(String text){
		this(null, LegoPlatformMessageTypeEnum.getByName(text), DEFAULT_SPEED);
    }

    public LegoPlatformMessage(LegoPlatformMessageTypeEnum type) {
        this(null, type, DEFAULT_SPEED);
    }

    public LegoPlatformMessage(RoboReference<?> source, LegoPlatformMessageTypeEnum type, int speed) {
        this.source = source;
        this.type = type;
        this.speed = speed;
    }

    public LegoPlatformMessage(RoboReference<?> source, String text, int speed) {
        this.source = source;
		this.type = LegoPlatformMessageTypeEnum.getByName(text);
		this.speed = speed;
    }

    public RoboReference<?> getSource(){
        return source;
    }

    public LegoPlatformMessageTypeEnum getType(){
        return type;
    }

    public int getSpeed() {
        return speed;
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
