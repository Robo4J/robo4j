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

package com.robo4j.spring;

import com.robo4j.AttributeDescriptor;
import com.robo4j.ConfigurationException;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;

import java.util.Collections;
import java.util.Map;

/**
 * RoboSpringRegisterUnit contains register of all registered spring components
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class RoboSpringRegisterUnit extends RoboUnit<Object> {
	public static final String NAME = "springRegisterUnit";

	public static final String PROPERTY_COMPONENTS = "components";

	private final RoboSpringRegister register = new RoboSpringRegisterImpl();

	public RoboSpringRegisterUnit(RoboContext context, String id) {
		super(Object.class, context, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		final Map<String, Object> beansMap = (Map<String, Object>) configuration.getValue(PROPERTY_COMPONENTS,
				Collections.emptyMap());
		if (beansMap.isEmpty()) {
			throw new IllegalArgumentException("no beans");
		}
		beansMap.forEach(register::register);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R> R onGetAttribute(AttributeDescriptor<R> attribute) {
		if (attribute.getAttributeType() == String.class) {
			return register.containsComponent(attribute.getAttributeName())
					? (R) register.getComponent(attribute.getAttributeName())
					: null;
		}
		return null;
	}
}
