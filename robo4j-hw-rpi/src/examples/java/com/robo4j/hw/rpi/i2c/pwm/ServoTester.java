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

import com.robo4j.hw.rpi.Servo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Scanner;

/**
 * This is a simple example allowing you to try out the servos connected to a
 * PCA9685.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ServoTester {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServoTester.class);
    // The internetz says 50Hz is the standard PWM frequency for operating RC
    // servos.
    private static final int SERVO_FREQUENCY = 50;
    private static final Servo[] SERVOS = new Servo[16];

    public static void main(String[] args) throws IOException, InterruptedException {
        LOGGER.info("Creating device...");
        PWMPCA9685Device device = new PWMPCA9685Device();
        device.setPWMFrequency(SERVO_FREQUENCY);
        LOGGER.info("done!");
        LOGGER.info(
                "Type the id of the channel of the servo to control and how much to move the servo,\nbetween -1 and 1. For example:\nknown servos=0>15 -1.0\nType q and enter to quit!\n");
        System.out.flush();
        Scanner scanner = new Scanner(System.in);
        String lastCommand;
        printPrompt();
        while (!"q".equals(lastCommand = scanner.nextLine())) {
            lastCommand = lastCommand.trim();
            String[] split = lastCommand.split(" ");
            if (split.length != 2) {
                LOGGER.debug("Could not parse {}. Please try again!", lastCommand);
                continue;
            }
            int channel = Integer.parseInt(split[0]);
            float position = Float.parseFloat(split[1]);

            if (channel < 0 || channel > 15) {
                LOGGER.debug("Channel number {} is not allowed! Try again...", channel);
                continue;
            }
            Servo servo = SERVOS[channel];

            if (servo == null) {
                servo = new PCA9685Servo(device.getChannel(channel));
                SERVOS[channel] = servo;
            }

            if (position < -1 || position > 1) {
                LOGGER.debug("Input {} is not allowed! Try again...", position);
                continue;
            }
            servo.setInput(position);
            printPrompt();
        }
        scanner.close();
        LOGGER.info("Bye!");
    }

    private static void printPrompt() {
        LOGGER.info("known servos={}", getNumberOfKnownServos());
    }

    private static int getNumberOfKnownServos() {
        int count = 0;
        for (Servo servo : SERVOS) {
            if (servo != null) {
                count++;
            }
        }
        return count;
    }
}
