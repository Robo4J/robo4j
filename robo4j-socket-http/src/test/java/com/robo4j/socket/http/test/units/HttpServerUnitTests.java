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
package com.robo4j.socket.http.test.units;

import com.robo4j.LifecycleState;
import com.robo4j.RoboBuilder;
import com.robo4j.RoboBuilderException;
import com.robo4j.RoboReference;
import com.robo4j.configuration.ConfigurationBuilder;
import com.robo4j.socket.http.units.HttpServerUnit;
import com.robo4j.util.SystemUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.robo4j.socket.http.test.units.HttpUnitTests.CODECS_UNITS_TEST_PACKAGE;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_CODEC_PACKAGES;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_SOCKET_PORT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class HttpServerUnitTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerUnitTests.class);
    private static final int PORT = 9000;
    private static final String ID_HTTP_SERVER = "empty_server";

    @Test
    void httpServerUnitNoCodecsPackageTest() throws Exception {

        Throwable exception = assertThrows(RoboBuilderException.class, () -> {
            var builder = new RoboBuilder();
            var config = new ConfigurationBuilder().addInteger(PROPERTY_SOCKET_PORT, PORT).build();
            builder.add(HttpServerUnit.class, config, ID_HTTP_SERVER);
            var system = builder.build();

            system.start();
            LOGGER.info(SystemUtil.printStateReport(system));
            var systemReference = system.getReference(ID_HTTP_SERVER);
            system.shutdown();

            LOGGER.info(SystemUtil.printStateReport(system));
            assertEquals(LifecycleState.SHUTDOWN, systemReference.getState());
        });

        assertEquals("Error initializing RoboUnit", exception.getMessage());

    }

    @Test
    void httpServerUnitNoPathTest() throws Exception {
        var builder = new RoboBuilder();
        var config = new ConfigurationBuilder().addInteger(PROPERTY_SOCKET_PORT, PORT)
                .addString(PROPERTY_CODEC_PACKAGES, CODECS_UNITS_TEST_PACKAGE).build();
        builder.add(HttpServerUnit.class, config, ID_HTTP_SERVER);
        var system = builder.build();

        system.start();
        LOGGER.info(SystemUtil.printStateReport(system));
        RoboReference<HttpServerUnit> systemReference = system.getReference(ID_HTTP_SERVER);
        system.shutdown();

        LOGGER.info(SystemUtil.printStateReport(system));
        assertEquals(LifecycleState.SHUTDOWN, systemReference.getState());
    }

}
