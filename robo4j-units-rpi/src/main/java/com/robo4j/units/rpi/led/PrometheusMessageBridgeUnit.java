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

package com.robo4j.units.rpi.led;

import com.robo4j.ConfigurationException;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.prometheus.model.MetricsElement;
import com.robo4j.prometheus.model.MetricsType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrometheusMessageBridgeUnit extends RoboUnit<AlphaNumericMessage> {
    public static final String KEY_METRICS_UNIT = "metricsUnit";
    private static final String DEFAULT_METRICS_UNIT = "metrics";

    private static final Logger LOGGER = LoggerFactory.getLogger(PrometheusMessageBridgeUnit.class);

    private String metricsUnit;

    public PrometheusMessageBridgeUnit(RoboContext context, String id) {
        super(AlphaNumericMessage.class, context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        metricsUnit = configuration.getString(KEY_METRICS_UNIT, DEFAULT_METRICS_UNIT);
    }

    @Override
    public void onMessage(AlphaNumericMessage message) {

        double value = message.getCharacters().length == 0 ? 0 : Byte.toUnsignedInt(message.getCharacters()[0]);
        LOGGER.info("Received message:{}, value:{}", message, value);
        var metricsElement = new MetricsElement("robo4j_alpha_numeric", MetricsType.GAUGE, this.id(), value);
        if(value != 0){
            getContext().getReference(metricsUnit).sendMessage(metricsElement);
        }
    }
}
