/*
 * Copyright (c) 2014, 2025, Marcus Hirt, Miroslav Wengner
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

package com.robo4j.prometheus;

import com.robo4j.RoboBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.concurrent.Executors;

public class SimplePrometheusMetricsMain {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimplePrometheusMetricsMain.class);

    public static void main(String[] args) throws Exception {
        LOGGER.info("Starting Monitoring Example...");
        var classLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream systemIS = classLoader.getResourceAsStream("robo4jSystem.xml");
             InputStream contextIS = classLoader.getResourceAsStream("robo4j.xml");
             var executor = Executors.newSingleThreadScheduledExecutor()) {

            var ctx = new RoboBuilder(systemIS).add(contextIS).build();

            ctx.start();
            LOGGER.info("Press enter to quit");
            System.in.read();
            executor.shutdown();
            ctx.shutdown();
        }
    }
}
