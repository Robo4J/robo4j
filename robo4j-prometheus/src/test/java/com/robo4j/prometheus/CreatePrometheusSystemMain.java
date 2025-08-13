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
import com.robo4j.configuration.ConfigurationBuilder;
import com.robo4j.util.SystemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreatePrometheusSystemMain {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreatePrometheusSystemMain.class);

    public static void main(String[] args) throws Exception {
        LOGGER.info("Hello Prometheus!");
        var systemBuilder = new RoboBuilder(SystemUtil.getInputStreamByResourceName("roboSystemOnly.xml"));

        var producerConfig = new ConfigurationBuilder().build();
        var consumerConfig = new ConfigurationBuilder()
                .addBoolean(PrometheusMetricsUnit.PROP_METRICS_JVM_ENABLE, false)
                .build();

        var systemContext = systemBuilder
                .add(PrometheusMetricsUnit.class, consumerConfig, PrometheusMetricsUnit.NAME)
                .add(MetricElementProducerUnit.class, producerConfig, MetricElementProducerUnit.NAME)
                .build();

        systemContext.start();

        LOGGER.info("Press any key to start PrometheusProducer");

        var producer = systemContext.getReference( MetricElementProducerUnit.NAME);
        producer.sendMessage("produce metrics");

        LOGGER.info("Press any key...");
        System.in.read();
        systemContext.stop();
        systemContext.shutdown();
    }
}