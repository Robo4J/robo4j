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

package com.robo4j.spring.unit;

import com.robo4j.AttributeDescriptor;
import com.robo4j.ConfigurationException;
import com.robo4j.DefaultAttributeDescriptor;
import com.robo4j.RoboContext;
import com.robo4j.configuration.Configuration;
import com.robo4j.spring.AbstractSpringUnit;
import com.robo4j.spring.service.SimpleService;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class SimpleRoboSpringUnit extends AbstractSpringUnit<String> {

    public static final String COMPONENT_SIMPLE_SERVICE = "simpleService";
    public static final String PROP_GET_RECEIVED_MESSAGES = "receivedMessages";
    @SuppressWarnings("rawtypes")
    public static final DefaultAttributeDescriptor<List> DESCRIPTOR_RECEIVED_MESSAGES = DefaultAttributeDescriptor
            .create(List.class, PROP_GET_RECEIVED_MESSAGES);

    private final List<String> receivedMessages = new LinkedList<>();
    private String component;
    private SimpleService simpleService;

	public SimpleRoboSpringUnit(RoboContext context, String id) {
		super(String.class, context, id);
	}

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        component = configuration.getString(COMPONENT_SIMPLE_SERVICE, null);
        if(component == null){
            throw new IllegalArgumentException("no component");
        }
    }

    @Override
    public void start() {
        super.start();
        simpleService = getComponent(component, SimpleService.class);
    }

    @Override
	public void onMessage(String message) {
        Integer number = simpleService.getRandom();
		System.out.println(getClass().getSimpleName() + ":message:" + message + ",number: " + number);
		receivedMessages.add(simpleService.updateMessage(message));
	}

    @SuppressWarnings("unchecked")
    @Override
    public synchronized <R> R onGetAttribute(AttributeDescriptor<R> attribute) {

        if (attribute.getAttributeName().equals(PROP_GET_RECEIVED_MESSAGES)
                && attribute.getAttributeType() == List.class) {
            return (R) new LinkedList<>(receivedMessages);
        }
        return null;
    }

}
