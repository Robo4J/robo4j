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
package com.robo4j.hw.lego;

import com.robo4j.hw.lego.enums.AnalogPortEnum;
import com.robo4j.hw.lego.enums.MotorTypeEnum;

/**
 * Basic functionality of the LegoMindstorm Motor
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public interface ILegoMotor {

	AnalogPortEnum getPort();

	MotorTypeEnum getType();

	void forward();

	void backward();

	void stop();

	void rotate(int val);

	boolean isMoving();

	void setSpeed(int speed);

	void close();

}
