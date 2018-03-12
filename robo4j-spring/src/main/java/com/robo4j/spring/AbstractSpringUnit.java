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

import com.robo4j.ConfigurationException;
import com.robo4j.DefaultAttributeDescriptor;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;

import java.util.Objects;

/**
 * @param <T>
 *            robo unit type
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class AbstractSpringUnit<T> extends RoboUnit<T> {

	private RoboReference<?> registerUnit;

	public AbstractSpringUnit(Class<T> messageType, RoboContext context, String id) {
		super(messageType, context, id);
	}

	@Override
	public void initialize(Configuration configuration) throws ConfigurationException {
		super.initialize(configuration);
	}

	@Override
	public void start() {
		// TODO: 3/11/18 (miro) this should go to initiation phase: it means order of
		// initiation
		registerUnit = getContext().getReference(RoboSpringRegisterUnit.NAME);
	}

	/**
	 * get registered component
	 *
	 * @param name
	 *            spring component name
	 * @param clazz
	 *            component class
	 * @param <C>
	 *            component class type
	 * @return
	 */
	protected <C> C getComponent(String name, Class<C> clazz) {
		final DefaultAttributeDescriptor<String> descriptor = DefaultAttributeDescriptor.create(String.class, name);
		try {
			Object obj = registerUnit.getAttribute(descriptor).get();
			Objects.requireNonNull(obj, "spring component not available");
			return clazz.cast(obj);
		} catch (Exception e) {
			throw new RoboSpringException("problem", e);
		}
	}
}
