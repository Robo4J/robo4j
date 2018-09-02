/*
 * Copyright (c) 2014, 2018, Marcus Hirt, Miroslav Wengner
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

package com.robo4j.socket.http.units;

import com.robo4j.RoboBuilder;
import com.robo4j.RoboContext;
import com.robo4j.configuration.Configuration;
import com.robo4j.configuration.ConfigurationFactory;
import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.units.test.HttpOneAttributeGetController;
import com.robo4j.socket.http.units.test.HttpTwoAttributesGetController;
import com.robo4j.socket.http.units.test.StringConsumer;
import com.robo4j.socket.http.util.HttpPathConfigJsonBuilder;
import com.robo4j.util.SystemUtil;
import org.junit.Ignore;
import org.junit.Test;

import static com.robo4j.socket.http.units.RoboHttpPingPongTest.PACKAGE_CODECS;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_SOCKET_PORT;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_UNIT_PATHS_CONFIG;

/**
 * RoboHttpUnitGetTest should test Http get requests
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class RoboHttpUnitGetTest {



    @Test
    @Ignore
    public void oneKnownAttributeTest() throws Exception{

        Configuration systemConfiguration = ConfigurationFactory.createEmptyConfiguration();
        systemConfiguration.setInteger("poolSizeScheduler", 4);
        systemConfiguration.setInteger("poolSizeWorker", 2);
        systemConfiguration.setInteger("poolSizeBlocking", 3);
        RoboBuilder builder = new RoboBuilder(systemConfiguration);

        Configuration config = ConfigurationFactory.createEmptyConfiguration();
        config.setInteger(PROPERTY_SOCKET_PORT, 8061);
        config.setString("packages", PACKAGE_CODECS);

        final HttpPathConfigJsonBuilder pathBuilder = HttpPathConfigJsonBuilder.Builder()
                .addPath("controller",
                HttpMethod.GET);
        config.setString(PROPERTY_UNIT_PATHS_CONFIG, pathBuilder.build());
        builder.add(HttpServerUnit.class, config, "http_server");

        config = ConfigurationFactory.createEmptyConfiguration();
        config.setInteger(StringConsumer.PROP_TOTAL_NUMBER_MESSAGES, 1);
        builder.add(StringConsumer.class, config, "request_consumer");

        config = ConfigurationFactory.createEmptyConfiguration();
        config.setString("target", "request_consumer");
        builder.add(HttpOneAttributeGetController.class, config, "controller");

        RoboContext system = builder.build();

        system.start();
        System.out.println("systemPong: State after start:");
        System.out.println(SystemUtil.printStateReport(system));
        System.out.println("Press Key...");
        System.in.read();
        system.shutdown();

    }

    @Test
    @Ignore
    public void twoKnownAttributesTest() throws Exception {
        Configuration systemConfiguration = ConfigurationFactory.createEmptyConfiguration();
        systemConfiguration.setInteger("poolSizeScheduler", 4);
        systemConfiguration.setInteger("poolSizeWorker", 2);
        systemConfiguration.setInteger("poolSizeBlocking", 3);
        RoboBuilder builder = new RoboBuilder(systemConfiguration);

        Configuration config = ConfigurationFactory.createEmptyConfiguration();
        config.setInteger(PROPERTY_SOCKET_PORT, 8062);
        config.setString("packages", PACKAGE_CODECS);

        final HttpPathConfigJsonBuilder pathBuilder = HttpPathConfigJsonBuilder.Builder()
                .addPath("controller",
                        HttpMethod.GET);
        config.setString(PROPERTY_UNIT_PATHS_CONFIG, pathBuilder.build());
        builder.add(HttpServerUnit.class, config, "http_server");

        config = ConfigurationFactory.createEmptyConfiguration();
        config.setInteger(StringConsumer.PROP_TOTAL_NUMBER_MESSAGES, 1);
        builder.add(StringConsumer.class, config, "request_consumer");

        config = ConfigurationFactory.createEmptyConfiguration();
        config.setString("target", "request_consumer");
        builder.add(HttpTwoAttributesGetController.class, config, "controller");

        RoboContext system = builder.build();

        system.start();
        System.out.println("systemPong: State after start:");
        System.out.println(SystemUtil.printStateReport(system));
        System.out.println("Press Key...");
        System.in.read();
        system.shutdown();
    }
}
