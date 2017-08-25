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

package com.robo4j.socket.http.util;

import com.robo4j.core.AttributeDescriptor;
import com.robo4j.core.ConfigurationException;
import com.robo4j.core.RoboContext;
import com.robo4j.core.StringProducer;
import com.robo4j.core.StringToolkit;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.socket.http.client.util.RoboHttpUtils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class HttpStringProducer extends StringProducer{

    /* default sent messages */
    private static final int DEFAULT = 0;
    private static final String SEND_GET_MESSAGE = "sendGetMessage";
    public static final String SEND_POST_MESSAGE = "sendPostMessage";
    private AtomicInteger counter;
    private String target;
    private String method;
    private String uri;
    private String targetAddress;

    /**
     * @param context
     * @param id
     */
    public HttpStringProducer(RoboContext context, String id) {
        super(context, id);
    }


    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        super.onInitialization(configuration);

        target = configuration.getString("target", null);
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
            String inMessageType = input[0];
            String inMessage = input[1].trim();
            switch (inMessageType) {
                case SEND_GET_MESSAGE:
                    sendGetSimpleMessage(targetAddress, inMessage);
                    break;
                case SEND_POST_MESSAGE:
                    sendPostSimpleMessage(targetAddress, uri, inMessage);
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

    private void sendGetSimpleMessage(String host, String message) {
        final String request = method.equals("GET") ? RoboHttpUtils.createGetRequest(host, message) : null;
        getContext().getReference(target).sendMessage(request);
    }

    private void sendPostSimpleMessage(String host, String uri, String message) {
        if(uri == null){
            throw new IllegalStateException("uri not available");
        }
        String postMessage = RoboHttpUtils.createPostRequest(host, uri, message);
        final String request = method.equals("POST") ? postMessage : null;
        getContext().getReference(target).sendMessage(request);
    }
}
