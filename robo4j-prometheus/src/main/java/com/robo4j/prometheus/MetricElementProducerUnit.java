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
package com.robo4j.prometheus;

import com.robo4j.ConfigurationException;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.prometheus.model.MetricsElement;
import com.robo4j.prometheus.model.MetricsType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class MetricElementProducerUnit extends RoboUnit<String> {
    public static final String NAME = "producer";
    public static final String PROP_TARGET = "target";
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricElementProducerUnit.class);
    private final Random random = new Random();
    private AtomicInteger counter;
    private String target;
    private RoboReference<?> roboReference;


    /**
     * @param context context
     * @param id      identifier
     */
    public MetricElementProducerUnit(RoboContext context, String id) {
        super(String.class, context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        target = PrometheusMetricsUnit.NAME;
        counter = new AtomicInteger(0);
    }

    @Override
    public void onMessage(String message) {
        RoboReference<MetricsElement> roboReference = getContext().getReference(target);
        LOGGER.info("START, message:'{}', roboReferenceId:'{}', count:'{}'", message, roboReference.id(), counter.incrementAndGet());
        getContext().getScheduler().scheduleAtFixedRate(() -> sendCounter(roboReference, "robo4j_counter", this.id()), 2, 5, TimeUnit.SECONDS);
        getContext().getScheduler().scheduleAtFixedRate(() -> sendRandomGaugeDouble(roboReference, "robo4j_gauge", this.id()), 2, 5, TimeUnit.SECONDS);
        getContext().getScheduler().scheduleAtFixedRate(() -> sendHistogram(roboReference, "robo4j_historgram", this.id()), 2, 5, TimeUnit.SECONDS);
        getContext().getScheduler().scheduleAtFixedRate(() -> sendSummary(roboReference, "robo4j_summary", this.id()), 2, 5, TimeUnit.SECONDS);
    }


    private void sendCounter(RoboReference<MetricsElement> roboReference, String measurementName, String label) {
        roboReference.sendMessage(new MetricsElement(measurementName, MetricsType.COUNTER, label, 0));
    }

    private void sendRandomGaugeDouble(RoboReference<MetricsElement> roboReference, String measurementName, String label) {
        var doubleValue = random.nextDouble(100);
        LOGGER.info("GAUGE, next value: {}", doubleValue);
        roboReference.sendMessage(new MetricsElement(measurementName, MetricsType.GAUGE, label, doubleValue));
    }

    private void sendHistogram(RoboReference<MetricsElement> roboReference, String measurementName, String label) {
        var doubleValue = random.nextGaussian(10, 5);
        LOGGER.info("HISTOGRAM, next value: {}", doubleValue);
        roboReference.sendMessage(new MetricsElement(measurementName, MetricsType.HISTOGRAM, label, doubleValue));
    }

    private void sendSummary(RoboReference<MetricsElement> roboReference, String measurementName, String label) {
        var doubleValue = random.nextDouble(10, 30);
        LOGGER.info("SUMMARY, next value: {}", doubleValue);
        roboReference.sendMessage(new MetricsElement(measurementName, MetricsType.SUMMARY, label, doubleValue));
    }


}
