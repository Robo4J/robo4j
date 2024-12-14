/*
 * Copyright (c) 2014, 2024, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.hw.rpi.i2c.pwm;

import com.pi4j.Pi4J;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;
import com.robo4j.hw.rpi.Motor;
import com.robo4j.hw.rpi.i2c.pwm.PWMPCA9685Device.PWMChannel;
import com.robo4j.hw.rpi.utils.GpioPin;

import java.io.IOException;

/**
 * Motor controller for the Pololu H-bridge motor controller based on
 * Freescale's MC33926.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class HBridgeMC33926Device implements Motor {
    private final String name;
    private final PWMChannel channel;
    private final boolean invert;
    private final DigitalOutput gpioOut1;
    private final DigitalOutput gpioOut2;

    private Direction direction = Direction.FORWARD;
    private float speed = 0;

    public enum Direction {
        FORWARD, REVERSE
    }

    public HBridgeMC33926Device(String name, PWMChannel channel, GpioPin pin1, GpioPin pin2, boolean invert) {
        this.name = name;
        this.channel = channel;
        this.invert = invert;

        var pi4jRpiContext = Pi4J.newAutoContext();
        var digitalOutputBuilder = DigitalOutput.newConfigBuilder(pi4jRpiContext);
        var gpioConfig1 = digitalOutputBuilder.address(pin1.address()).onState(DigitalState.LOW).build();
        var gpioConfig2 = digitalOutputBuilder.address(pin2.address()).onState(DigitalState.HIGH).build();

        gpioOut1 = pi4jRpiContext.dout().create(gpioConfig1);
        gpioOut2 = pi4jRpiContext.dout().create(gpioConfig2);
        setDirection(Direction.FORWARD);
    }

    public String getName() {
        return name;
    }

    @Override
    public float getSpeed() throws IOException {
        return (this.getDirection() == Direction.FORWARD ? 1 : -1) * internalGetSpeed();
    }

    @Override
    public void setSpeed(float speed) throws IOException {
        if (speed < 0) {
            if (this.direction != Direction.REVERSE) {
                setDirection(Direction.REVERSE);
            }
        } else {
            if (this.direction != Direction.FORWARD) {
                setDirection(Direction.FORWARD);
            }
        }
        internalSetSpeed(speed);
    }

    private void internalSetSpeed(float speed) throws IOException {
        int width = Math.round(speed * 4095);
        channel.setPWM(0, width);
        this.speed = speed;
    }

    private float internalGetSpeed() {
        return speed;
    }

    private Direction getDirection() {
        return direction;
    }

    private void setDirection(Direction direction) {
        boolean forward = direction == Direction.FORWARD;
        if (invert) {
            forward = !forward;
        }

        if (forward) {
            gpioOut1.setState(digitalStateToByte(DigitalState.HIGH));
            gpioOut2.setState(digitalStateToByte(DigitalState.LOW));
        } else {
            gpioOut1.setState(digitalStateToByte(DigitalState.LOW));
            gpioOut2.setState(digitalStateToByte(DigitalState.HIGH));
        }
        this.direction = direction;
    }

    private int digitalStateToByte(DigitalState state) {
        return state.value().intValue();
    }
}
