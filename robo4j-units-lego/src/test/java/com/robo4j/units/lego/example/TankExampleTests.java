/*
 * Copyright (c) 2014, 2017, Marcus Hirt, Miroslav Wengner
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

package com.robo4j.units.lego.example;

import org.junit.Test;

import com.robo4j.core.RoboSystem;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.configuration.ConfigurationFactory;
import com.robo4j.socket.http.util.RoboHttpUtils;
import com.robo4j.socket.http.units.HttpServerUnit;
import com.robo4j.units.lego.LcdTestUnit;
import com.robo4j.units.lego.SimpleTankTestUnit;

/**
 * Simple Tanks examples tests
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class TankExampleTests {

    private static final String UNIT_CONTROLLER_NAME = "controller";
    private static final int PORT = 8025;

    @Test
    public void legoTankExampleTest() throws Exception {
        RoboSystem system = new RoboSystem();
        Configuration config = ConfigurationFactory.createEmptyConfiguration();

        HttpServerUnit http = new HttpServerUnit(system, "http");
        config.setString("target", UNIT_CONTROLLER_NAME);
        config.setInteger("port", PORT);
        config.setString("packages", "com.robo4j.units.lego.example.codec");
        /* specific configuration */
        Configuration targetUnits = config.createChildConfiguration(RoboHttpUtils.HTTP_TARGET_UNITS);
        targetUnits.setString(UNIT_CONTROLLER_NAME, "GET");
        http.initialize(config);

        TankExampleController ctrl = new TankExampleController(system, UNIT_CONTROLLER_NAME);
        config = ConfigurationFactory.createEmptyConfiguration();
        config.setString("target", "platform");
        ctrl.initialize(config);

		/* platform is listening to the bus */
        SimpleTankTestUnit platform = new SimpleTankTestUnit(system, "platform");
        config = ConfigurationFactory.createEmptyConfiguration();
        config.setString("leftMotorPort", "B");
        config.setCharacter("leftMotorType", 'N');
        config.setString("rightMotorPort", "C");
        config.setCharacter("rightMotorType", 'N');
        platform.initialize(config);

		/* lcd is listening to the bus */
        LcdTestUnit lcd = new LcdTestUnit(system, "lcd");
        config = ConfigurationFactory.createEmptyConfiguration();
        lcd.initialize(config);

        // BasicSonicUnit sonic = new BasicSonicUnit(system, "sonic");
        // config = ConfigurationFactory.createEmptyConfiguration();
        // config.setString("target", "controller");
        // sonic.initialize(config);

        // system.addUnits(http, ctrl, platform, lcd, sonic);
        system.addUnits(http, ctrl, platform, lcd);
        system.start();



        lcd.onMessage("Press Key to end...");
//		System.in.read();
        system.stop();
        system.shutdown();

    }
}
