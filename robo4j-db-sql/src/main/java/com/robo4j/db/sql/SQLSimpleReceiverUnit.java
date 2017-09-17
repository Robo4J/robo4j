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

package com.robo4j.db.sql;

import com.robo4j.AttributeDescriptor;
import com.robo4j.DefaultAttributeDescriptor;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.db.sql.dto.ERoboDbContract;

import java.util.Collection;
import java.util.Collections;

/**
 * Simple receiver for ERobo Entities
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class SQLSimpleReceiverUnit extends RoboUnit<ERoboDbContract> {

	public static final String ATTRIBUTE_SQL_RESPONSE = "response";
	private static final Collection<AttributeDescriptor<?>> KNOWN_ATTRIBUTES = Collections
			.singleton(DefaultAttributeDescriptor.create(SQLDataSourceUnit.class, ATTRIBUTE_SQL_RESPONSE));

	public SQLSimpleReceiverUnit(RoboContext context, String id) {
		super(ERoboDbContract.class, context, id);
	}

	private volatile ERoboDbContract response;

	@Override
	public void onMessage(ERoboDbContract message) {
		System.out.println(getClass().getSimpleName() + " response message: " + message);
		response = message;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <R> R onGetAttribute(AttributeDescriptor<R> descriptor) {
		if (descriptor.getAttributeName().equals(ATTRIBUTE_SQL_RESPONSE)
				&& descriptor.getAttributeType() == ERoboDbContract.class) {
			return (R) response;
		}
		return super.onGetAttribute(descriptor);
	}

	@Override
	public Collection<AttributeDescriptor<?>> getKnownAttributes() {
		return KNOWN_ATTRIBUTES;
	}
}
