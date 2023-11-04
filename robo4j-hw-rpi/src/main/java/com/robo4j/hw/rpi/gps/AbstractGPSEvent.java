/*
 * Copyright (c) 2014, 2023, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.hw.rpi.gps;

import com.robo4j.hw.rpi.serial.gps.MTK3339PositionEvent;
import com.robo4j.hw.rpi.serial.gps.MTK3339VelocityEvent;

/**
 * Abstract super class for GPS event classes.
 *
 * @see MTK3339PositionEvent
 * @see MTK3339VelocityEvent
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public abstract class AbstractGPSEvent {
	public static final float INVALID_VALUE = Float.NaN;

	private final GPS source;

	public AbstractGPSEvent(GPS source) {
		this.source = source;
	}

	public final GPS getSource() {
		return source;
	}

}
