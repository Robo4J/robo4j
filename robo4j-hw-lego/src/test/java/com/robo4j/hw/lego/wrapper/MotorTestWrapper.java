/*
 * Copyright (C) 2014, 2017. Miroslav Wengner, Marcus Hirt
 * This MotorTestWrapper.java  is part of robo4j.
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

package com.robo4j.hw.lego.wrapper;

import com.robo4j.hw.lego.LegoMotor;
import com.robo4j.hw.lego.enums.AnalogPortEnum;
import com.robo4j.hw.lego.enums.MotorTypeEnum;

/**
 * Simple LegoMindstorm Mock Motor
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 * @since 29.01.2017
 */
public class MotorTestWrapper implements LegoMotor {

    private final AnalogPortEnum port;
    private final MotorTypeEnum type;
    private boolean moving;
    private int speed;

    public MotorTestWrapper(AnalogPortEnum port, MotorTypeEnum type) {
        this.port = port;
        this.type = type;
        this.moving = false;
    }

    @Override
    public AnalogPortEnum getPort() {
        return port;
    }

    @Override
    public MotorTypeEnum getType() {
        return type;
    }

    @Override
    public void forward() {
        moving = true;
        System.out.println(String.format("MotorTest.forward port:%s, type: %s, moving: %b ", port, type, moving));
    }

    @Override
    public void backward() {
        moving = true;
        System.out.println(String.format("MotorTest.backward port:%s, type: %s, moving: %b ", port, type, moving));
    }

    @Override
    public void stop() {
        moving = false;
        System.out.println(String.format("MotorTest.stop port:%s, type: %s, moving: %b ", port, type, moving));

    }

    @Override
    public void rotate(int val) {
        System.out.println("rotate: " + val);
    }

    @Override
    public boolean isMoving() {
        return moving;
    }

    @Override
    public void setSpeed(int speed) {
        System.out.println("speed: " + speed);
        this.speed = speed;
    }

    @Override
    public void close() {
        moving = false;
        System.out.println(String.format("MotorTest.close port:%s, type: %s, moving: %b ", port, type, moving));
    }

    @Override
    public String toString() {
        return "MotorTestWrapper{" +
                "port=" + port +
                ", type=" + type +
                ", moving=" + moving +
                ", speed=" + speed +
                '}';
    }
}
