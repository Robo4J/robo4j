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
package com.robo4j.units.lego;

import com.robo4j.RoboBuilder;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.hw.lego.util.EscapeButtonUtil;
import com.robo4j.util.SystemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;

/**
 * InfraPushTankPlatformExample lego platform example with push and infra red sensor
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class InfraPushTankPlatformExample {
    private static final Logger LOGGER = LoggerFactory.getLogger(InfraPushTankPlatformExample.class);

    public static void main(String[] args) throws Exception {
        final var robo4jSystem = "robo4jSystem.xml";
        final var robo4jConfig = "robo4jInfraPushTankPlatformExample.xml";
        final var systemSystem = InfraSensorExample.class.getClassLoader().getResourceAsStream(robo4jSystem);
        var settings = GripperSonicTankPlatformExample.class.getClassLoader().getResourceAsStream(robo4jConfig);
        if (args.length != 1) {
            LOGGER.info("No file specified, using default {}", robo4jConfig);
        } else {
            settings = new FileInputStream(args[0]);
        }

        final var builder = new RoboBuilder(systemSystem);
        if (settings == null) {
            LOGGER.warn("Could not find the settings for test!");
            System.exit(2);
        }

        builder.add(settings);
        RoboContext system = builder.build();
        LOGGER.info(SystemUtil.printStateReport(system));

        system.start();

        RoboReference<String> lcd = system.getReference("lcd");
        lcd.sendMessage("Robo4J.io");

        RoboReference<String> infraSensor = system.getReference("infraSensor");
        infraSensor.sendMessage("start");

        RoboReference<String> touchUnit = system.getReference("touchUnit");
        touchUnit.sendMessage("START TOUCH");

        shutdown(system);
    }

    private static void shutdown(RoboContext system) {
        EscapeButtonUtil.waitForPressAndRelease();
        system.shutdown();
    }
}
