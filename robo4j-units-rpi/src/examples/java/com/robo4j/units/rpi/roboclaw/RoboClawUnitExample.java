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
package com.robo4j.units.rpi.roboclaw;

import com.robo4j.RoboBuilder;
import com.robo4j.RoboBuilderException;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.units.rpi.pwm.ServoUnitExample;
import com.robo4j.util.SystemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Small example for driving around a roboclaw controlled robot.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class RoboClawUnitExample {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoboClawUnitExample.class);

    public static void main(String[] args) throws RoboBuilderException, FileNotFoundException {
        var settings = ServoUnitExample.class.getClassLoader().getResourceAsStream("roboclawexample.xml");
        if (args.length != 1) {
            LOGGER.warn("No file specified, using default roboclawexample.xml");
        } else {
            settings = new FileInputStream(args[0]);
        }

        var builder = new RoboBuilder();
        if (settings == null) {
            LOGGER.warn("Could not find the settings for  test!");
            System.exit(2);
        }
        builder.add(settings);
        RoboContext ctx = builder.build();
        LOGGER.info("State before start:");
        LOGGER.info(SystemUtil.printStateReport(ctx));
        ctx.start();

        LOGGER.info("State after start:");
        LOGGER.info(SystemUtil.printStateReport(ctx));

        String lastCommand = "";
        Scanner scanner = new Scanner(System.in);
        LOGGER.info(
                "Type the roboclaw unit to control and the speed [-1, 1] and angular direction[-180, 180]. For example:\ntank 1 0\nType q and enter to quit!\n");
        while (!"q".equals(lastCommand = scanner.nextLine())) {
            lastCommand = lastCommand.trim();
            String[] split = lastCommand.split(" ");
            if (split.length != 3) {
                System.out.println("Could not parse " + lastCommand + ". Please try again!");
                continue;
            }
            RoboReference<MotionEvent> servoRef = ctx.getReference(split[0]);
            if (servoRef == null) {
                System.out.println("Could not find any robo unit named " + split[0] + ". Please try again!");
                continue;
            }
            try {
                float speed = Float.parseFloat(split[1]);
                float direction = (float) Math.toRadians(Float.parseFloat(split[2]));
                servoRef.sendMessage(new MotionEvent(speed, direction));
            } catch (Exception e) {
                LOGGER.error("Could not parse {} as a float number. Error message was: {}. Please try again!", split[1], e.getMessage());
            }
        }
        ctx.shutdown();
        scanner.close();
    }

}
