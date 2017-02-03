/*
 * Copyright (C) 2014, 2017. Miroslav Wengner, Marcus Hirt
 * This RoboHttpNonTests.java  is part of robo4j.
 * module: robo4j-core
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

package com.robo4j.core;

import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.configuration.ConfigurationFactory;
import com.robo4j.core.unit.HttpNonUnit;
import com.robo4j.core.util.SystemUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 * @since 03.02.2017
 */
public class RoboHttpNonTests {
    private static final int PORT = 8025;

    @Test
    public void simpleHttpNonUnitTest() throws ConfigurationException, IOException {
        RoboSystem system = new RoboSystem();
        Configuration config = ConfigurationFactory.createEmptyConfiguration();

        HttpNonUnit http = new HttpNonUnit(system, "http");
        config.setString("target", "request_consumer");
        config.setInteger("port", PORT);
        http.initialize(config);

        StringConsumer consumer = new StringConsumer(system, "request_consumer");

        system.addUnits(http, consumer);

        System.out.println("State before start:");
        System.out.println(SystemUtil.generateStateReport(system));
        system.start();

        System.out.println("State after start:");
        System.out.println(SystemUtil.generateStateReport(system));

        System.out.println("RoboSystem http server\n\tPort:" + PORT + "\n");
        System.out.println("Usage:\n\tRequest GET: http://<IP_ADDRESS>:" + PORT + "?type=tank&command=stop");
        System.out.println("\tRequest command types: stop, move, back, left, right\n");


        System.out.println("Going Down!");
//        System.in.read();
        system.shutdown();
        System.out.println("System is Down!");
        Assert.assertNotNull(system.getUnits());
        Assert.assertEquals(system.getUnits().size(), 2);
    }

}
