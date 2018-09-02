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

package com.robo4j.util;

import com.robo4j.AttributeDescriptor;

/**
 * AttributeUtils useful methods to work with unit Attributes
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class AttributeUtils {

	private AttributeUtils() {
	}

	/**
	 *
	 * @param descriptor
	 *            attribute descriptor
	 * @param name
	 *            attribute name
	 * @param clazz
	 *            expected attribute class
	 * @param <R>
	 *            Attribute type
	 * @return descriptor is valid
	 */
	public static <R> boolean validateAttributeByNameAndType(AttributeDescriptor<R> descriptor, String name,
			Class<?> clazz) {
		return (descriptor.getAttributeName().equals(name) && descriptor.getAttributeType() == clazz);
	}
}
