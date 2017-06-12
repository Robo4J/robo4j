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

package com.robo4j.db.sql.jpa;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.robo4j.core.AttributeDescriptor;
import com.robo4j.core.ConfigurationException;
import com.robo4j.core.DefaultAttributeDescriptor;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboReference;
import com.robo4j.core.RoboUnit;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.db.sql.SQLDataSourceUnit;
import com.robo4j.db.sql.model.ERoboEntity;
import com.robo4j.db.sql.model.ERoboUnit;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public abstract class RoboPersitenceUnit<T> extends RoboUnit<T> {

	private static final String PERSISTENCE_UNIT_NAME = "persistenceUnit";
	private static final int DEFAULT_INDEX = 0;
	private static final String CONST_COLON = ":";
	private static final String CONST_COMMA = ",";
	private Map<String, Object> entityMap = new WeakHashMap<>();
	private SQLDataSourceUnit dataSourceUnit;

	/**
	 * Constructor
	 */
	public RoboPersitenceUnit(Class<T> messageType, RoboContext context, String id) {
		super(messageType, context, id);
	}

	@Override
	public void initialize(Configuration configuration) throws ConfigurationException {
		registerUnit(configuration);
		// super.initialize(configuration);
	}

	@SuppressWarnings("unchecked")
	protected ERoboUnit getEntity() {
		List<ERoboUnit> tmpList = dataSourceUnit.getByMap(ERoboUnit.class, entityMap);
		return tmpList.isEmpty() ? null : tmpList.get(DEFAULT_INDEX);
	}

	protected void save(ERoboUnit unit) {
		dataSourceUnit.onMessage(unit);
	}

	private void registerUnit(Configuration configuration) throws ConfigurationException {
		String tmpName = configuration.getString(PERSISTENCE_UNIT_NAME, null);
		if (tmpName == null) {
			throw ConfigurationException.createMissingConfigNameException(PERSISTENCE_UNIT_NAME);
		}
		RoboReference<ERoboEntity> sqlUnitReference = getContext().getReference(tmpName);
		if (sqlUnitReference == null) {
			throw ConfigurationException.createMissingConfigNameException(PERSISTENCE_UNIT_NAME);
		}
		AttributeDescriptor<SQLDataSourceUnit> descriptor = DefaultAttributeDescriptor.create(SQLDataSourceUnit.class,
				"robo_sql_unit");
		entityMap.put("uid", getId());
		try {
			dataSourceUnit = sqlUnitReference.getAttribute(descriptor).get();
		} catch (InterruptedException | ExecutionException e) {
			SimpleLoggingUtil.error(getClass(), "error", e);
		}

		ERoboUnit entity = new ERoboUnit();
		entity.setUid(getId());

		String configString = configuration.getValueNames().stream().map(n -> {
			StringBuilder sb = new StringBuilder();
			sb.append(n);
			sb.append(CONST_COLON);
			sb.append(configuration.getValue(n, null));
			return sb.toString();
		}).collect(Collectors.joining(CONST_COMMA));
		entity.setConfig(configString);
		sqlUnitReference.sendMessage(entity);

	}

}
