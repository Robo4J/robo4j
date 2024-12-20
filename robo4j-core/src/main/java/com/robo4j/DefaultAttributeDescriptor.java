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
package com.robo4j;

import java.io.Serial;
import java.io.Serializable;

/**
 * Default implementation of an attribute descriptor.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public record DefaultAttributeDescriptor<T>(Class<T> attributeType,
											String attributeName) implements AttributeDescriptor<T>, Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 *
	 * @param attributeType the type of the attribute. Needed since the generic type is
	 *                      erased.
	 * @param attributeName the name of the attribute.
	 */
	public DefaultAttributeDescriptor {
	}


	/**
	 * Factory method for creating attribute descriptors.
	 *
	 * @param attributeType the type of the attribute.
	 * @param attributeName the name of the attribute.
	 * @param <T>           attribute type class
	 * @return created attribute
	 */
	public static <T> DefaultAttributeDescriptor<T> create(Class<T> attributeType, String attributeName) {
		return new DefaultAttributeDescriptor<>(attributeType, attributeName);
	}

	@Override
	public String toString() {
		return "DefaultAttributeDescriptor{" + "attributeType=" + attributeType + ", attributeName='" + attributeName + '\'' + '}';
	}
}
