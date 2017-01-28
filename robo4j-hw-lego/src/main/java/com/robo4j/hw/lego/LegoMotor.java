/*
 * Copyright (C) 2014, 2017. Miroslav Wengner, Marcus Hirt
 * This LegoMotor.java  is part of robo4j.
 * module: robo4j-hw-lego
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

package com.robo4j.hw.lego;

import com.robo4j.hw.lego.enums.LegoAnalogPortEnum;
import com.robo4j.hw.lego.enums.MotorTypeEnum;

/**
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 * @since 05.05.2016
 */
public interface LegoMotor {

	LegoAnalogPortEnum getPort();

	MotorTypeEnum getMotorType();

	void forward();

	void backward();

	void stop();

	void rotate(int val);

	boolean isMoving();

	void setSpeed(int speed);

	void close();

}
