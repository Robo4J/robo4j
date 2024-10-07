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
package com.robo4j.units;

import com.robo4j.ConfigurationException;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.net.LookupServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * StringProducerRemote is the abstraction of the unit inside the RemoteContext
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class StringProducerRemote<T> extends RoboUnit<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(StringProducerRemote.class);
    private final AtomicInteger totalCounter = new AtomicInteger(0);
    private String target;
    private String targetContext;

    public StringProducerRemote(Class<T> clazz, RoboContext context, String id) {
        super(clazz, context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        target = configuration.getString("target", null);
        if (target == null) {
            throw ConfigurationException.createMissingConfigNameException("target");
        }
        targetContext = configuration.getString("targetContext", null);
        if (targetContext == null) {
            throw ConfigurationException.createMissingConfigNameException("targetContext");
        }
    }

    @Override
    public void onMessage(T message) {
        int totalMessages = totalCounter.incrementAndGet();
        RoboContext ctx = LookupServiceProvider.getDefaultLookupService().getContext(targetContext);
        ctx.getReference(target).sendMessage(message);
        LOGGER.info("class:{}, totalMessages:{}", getClass().getSimpleName(), totalMessages);
    }
}
