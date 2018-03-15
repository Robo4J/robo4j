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

import com.robo4j.DefaultAttributeDescriptor;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.RoboUnit;

import java.util.Objects;

/**
 * AbstractSpringUnit provides spring framework support. When the unit is used
 * inside another RoboUnit, it is required to check spring support availability.
 * It means whether RoboSpringUnit is available inside the RoboContext
 *
 * @param <T>
 *            robo unit type
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
// TODO: 3/15/18 (miro) -> changed it to  proper spring integration
public class AbstractSpringUnit<T> extends RoboUnit<T> {

	private RoboReference<?> registerUnit;

	public AbstractSpringUnit(Class<T> messageType, RoboContext context, String id) {
		super(messageType, context, id);
	}

	/**
	 *
	 */
	@Override
	public void start() {
		RoboReference<?> reference = getContext().getReference(RoboSpringRegisterUnit.NAME);
		Objects.requireNonNull(reference, "register not available");
		registerUnit = reference;
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
		final DefaultAttributeDescriptor<Object> descriptor = DefaultAttributeDescriptor.create(Object.class, name);
		try {
			Object obj = registerUnit.getAttribute(descriptor).get();
			Objects.requireNonNull(obj, "spring component not available");
			return clazz.cast(obj);
		} catch (Exception e) {
			throw new RoboSpringException("problem", e);
		}
	}
}
