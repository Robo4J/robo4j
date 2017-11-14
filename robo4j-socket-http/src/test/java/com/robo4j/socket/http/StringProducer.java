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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */
package com.robo4j.socket.http;

import com.robo4j.AttributeDescriptor;
import com.robo4j.ConfigurationException;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.StringToolkit;
import com.robo4j.configuration.Configuration;
import com.robo4j.socket.http.units.Constants;
import com.robo4j.socket.http.util.RoboHttpUtils;
import com.robo4j.util.StringConstants;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class StringProducer extends RoboUnit<String> {
    /* default sent messages */
    private static final int DEFAULT = 0;
    private AtomicInteger counter;
    private String target;
    private String method;
    private String uri;
    private String targetAddress;

    /**
     * @param context
     * @param id
     */
    public StringProducer(RoboContext context, String id) {
        super(String.class, context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        target = configuration.getString("target", null);
        if (target == null) {
            throw ConfigurationException.createMissingConfigNameException("target");
        }

        method = configuration.getString("method", null);
        uri = configuration.getString("uri", null);
        targetAddress = configuration.getString("targetAddress", "0.0.0.0");

        counter = new AtomicInteger(DEFAULT);

    }

    @Override
    public void onMessage(String message) {
        if (message == null) {
            System.out.println("No Message!");
        } else {
            counter.incrementAndGet();
            String[] input = message.split("::");
            switch (input[0]) {
                case "sendRandomMessage":
                    sendRandomMessage();
                    break;
                case "sendGetMessage":
                    sendGetSimpleMessage(targetAddress, input[1].trim());
                    break;
                case "sendPostMessage":
                    sendPostSimpleMessage(targetAddress, uri, input[1].trim());
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
        getContext().getReference(target).sendMessage(message);
    }

    public void sendGetSimpleMessage(String host, String path) {
        final String request = RoboHttpUtils.createRequest(HttpMethod.GET, host, path, StringConstants.EMPTY);
        getContext().getReference(target).sendMessage(request);
    }

    public void sendPostSimpleMessage(String host, String uri, String message) {
        if(uri == null){
            throw new IllegalStateException("uri not available");
        }
        final String request = RoboHttpUtils.createRequest(HttpMethod.POST, host, uri, message);
        getContext().getReference(target).sendMessage(request);
    }


}
