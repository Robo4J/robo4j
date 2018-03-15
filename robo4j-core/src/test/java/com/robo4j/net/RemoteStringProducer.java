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
package com.robo4j.net;

import java.util.concurrent.atomic.AtomicInteger;

import com.robo4j.AttributeDescriptor;
import com.robo4j.ConfigurationException;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.StringToolkit;
import com.robo4j.configuration.Configuration;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class RemoteStringProducer extends RoboUnit<String> {
    /* default sent messages */
    private static final int DEFAULT = 0;
    private AtomicInteger counter;
    private String target;
	private String targetContext;

    /**
     * @param context
     * @param id
     */
    public RemoteStringProducer(RoboContext context, String id) {
        super(String.class, context, id);
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
        counter = new AtomicInteger(DEFAULT);
    }

    @Override
    public void onMessage(String message) {
        if (message == null) {
            System.out.println("No Message!");
        } else {
            counter.incrementAndGet();
            String[] input = message.split("::");
            String messageType = input[0];
            switch (messageType) {
                case "sendRandomMessage":
                    sendRandomMessage();
                    break;
                default:
                    System.out.println("don't understand message: " + message);

            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized <R> R onGetAttribute(AttributeDescriptor<R> attribute) {
        if (attribute.getAttributeName().equals("getNumberOfSentMessages") && attribute.getAttributeType() == Integer.class) {
            return (R) (Integer) counter.get();
        }
        return null;
    }

    public void sendRandomMessage() {
        final String message = StringToolkit.getRandomMessage(10);
        RoboContext ctx = LookupServiceProvider.getDefaultLookupService().getContext(targetContext);
        ctx.getReference(target).sendMessage(message);
    }

}
