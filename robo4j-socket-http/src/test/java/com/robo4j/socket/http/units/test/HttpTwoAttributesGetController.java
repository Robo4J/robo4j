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

package com.robo4j.socket.http.units.test;

import com.robo4j.AttributeDescriptor;
import com.robo4j.ConfigurationException;
import com.robo4j.DefaultAttributeDescriptor;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.socket.http.units.test.enums.TestCommandEnum;
import com.robo4j.util.AttributeUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * HttpTwoAttributesGetController
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class HttpTwoAttributesGetController extends RoboUnit<TestCommandEnum> {

    public static final String ATTR_NUMBER = "number";
    public static final String ATTR_TEXT = "text";
    public static final DefaultAttributeDescriptor<Integer> DESCRIPTOR_NUMBER = DefaultAttributeDescriptor
            .create(Integer.class, ATTR_NUMBER);
    public static final DefaultAttributeDescriptor<String> DESCRIPTOR_TEXT = DefaultAttributeDescriptor
            .create(String.class, ATTR_TEXT);

    public static final Collection<AttributeDescriptor<?>> KNOWN_ATTRIBUTES = Collections
            .unmodifiableCollection(Arrays.asList(DESCRIPTOR_NUMBER, DESCRIPTOR_TEXT));

    private int number = 42;
    private String text = "Some magic text";
    private String target;

    public HttpTwoAttributesGetController(RoboContext context, String id) {
        super(TestCommandEnum.class, context, id);
    }

    @Override
    public void onInitialization(Configuration configuration) throws ConfigurationException {
        target = configuration.getString("target", null);
        if (target == null) {
            throw ConfigurationException.createMissingConfigNameException("target");
        }
    }

    @Override
    public void onMessage(TestCommandEnum message) {
        super.onMessage(message);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <R> R onGetAttribute(AttributeDescriptor<R> descriptor) {
        if (AttributeUtils.validateAttributeByNameAndType(descriptor, ATTR_NUMBER, DESCRIPTOR_NUMBER.getAttributeType())) {
            return (R) Integer.valueOf(number);
        }
        if(AttributeUtils.validateAttributeByNameAndType(descriptor, ATTR_TEXT, DESCRIPTOR_TEXT.getAttributeType())){
            return (R) text;
        }
        return null;
    }

    @Override
    public Collection<AttributeDescriptor<?>> getKnownAttributes() {
        return KNOWN_ATTRIBUTES;
    }

}
