/*
 * Copyright (C) 2014, 2017. Miroslav Wengner, Marcus Hirt
 * This RoboHttpDynamicTests.java  is part of robo4j.
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

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.configuration.ConfigurationFactory;
import com.robo4j.core.unit.HttpDynamicUnit;

/**
 *
 * Dynamic HttpUnit request/method configuration
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 * @since 05.02.2017
 */
public class RoboHttpDynamicTests {

    private static final int PORT = 8025;

    @Test
    public void simpleHttpNonUnitTest() throws ConfigurationException, IOException {
        RoboSystem system = new RoboSystem();
        Configuration config = ConfigurationFactory.createEmptyConfiguration();

        HttpDynamicUnit httpDynamic = new HttpDynamicUnit(system, "http_dynamic");
        config.setString("target", "request_consumer");
        config.setInteger("port", PORT);

        /* specific configuration */
        config.setInteger("pathsNumber", 1);
        config.setString("path_0", "test");
        //TODO: we need to work on request design
        config.setInteger("pathCommands_0", 1);
        config.setString("commandName_0_0", "command");
        config.setString("commandValues_0_0", "right,left,up,down,enter");
        httpDynamic.initialize(config);

        StringConsumer consumer = new StringConsumer(system, "request_consumer");

        Assert.assertNotNull(system.getUnits());
        Assert.assertEquals(system.getUnits().size(), 0);
        Assert.assertEquals(httpDynamic.getState(), LifecycleState.INITIALIZED);
        Assert.assertEquals(system.getState(), LifecycleState.UNINITIALIZED);
    }
}
