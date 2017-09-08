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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.db.sql;

import com.robo4j.core.AttributeDescriptor;
import com.robo4j.core.BlockingTrait;
import com.robo4j.core.ConfigurationException;
import com.robo4j.core.DefaultAttributeDescriptor;
import com.robo4j.core.LifecycleState;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboReference;
import com.robo4j.core.RoboUnit;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.db.sql.dto.ERoboDbContract;
import com.robo4j.db.sql.model.ERoboPoint;
import com.robo4j.db.sql.model.ERoboUnit;
import com.robo4j.db.sql.repository.DefaultRepository;
import com.robo4j.db.sql.repository.RoboRepository;
import com.robo4j.db.sql.support.DataSourceContext;
import com.robo4j.db.sql.support.DataSourceType;
import com.robo4j.db.sql.support.PersistenceContextBuilder;
import com.robo4j.db.sql.support.SortType;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
@BlockingTrait
@SuppressWarnings(value = { "rawtypes" })
public class SQLDataSourceUnit extends RoboUnit<ERoboDbContract> {
	private static final String UTF8_COMMA = "\u002C";
	private static final String ATTRIBUTE_ROBO_ALL_NAME = "all";
	private static final Collection<AttributeDescriptor<?>> KNOWN_ATTRIBUTES = Collections
			.singletonList(DefaultAttributeDescriptor.create(List.class, ATTRIBUTE_ROBO_ALL_NAME));
	private Map<String, Object> targetUnitSearchMap = new HashMap<>();

	private static final String PERSISTENCE_UNIT = "sourceType";
	private static final String TARGET_UNIT = "targetUnit";
	private static final String PACKAGES = "packages";
	private static final String UNIT_RECEIVER = "receiver";
	private static final String HIBERNATE_HBM2DDL = "hibernate.hbm2ddl.auto";
	private static final String HIBERNATE_CONNECTION_URL = "hibernate.connection.url";

	private List<Class<?>> registeredClasses;
	private DataSourceType sourceType;
	private int limit;
	private SortType sorted;
	private String[] packages;
	private DataSourceContext dataSourceContext;
	private RoboRepository repository;
	private Map<String, Object> persistenceMap = new WeakHashMap<>();
	private String receiverUnitName;

	public SQLDataSourceUnit(RoboContext context, String id) {
		super(ERoboDbContract.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		String tmpSourceType = configuration.getString(PERSISTENCE_UNIT, null);
		if (tmpSourceType == null) {
			throw ConfigurationException.createMissingConfigNameException(PERSISTENCE_UNIT);
		}
		sourceType = DataSourceType.getByName(tmpSourceType);

		String tmpPackages = configuration.getString(PACKAGES, null);
		if (tmpPackages == null) {
			throw ConfigurationException.createMissingConfigNameException(PACKAGES);
		}

		String targetUnit = configuration.getString(TARGET_UNIT, null);
		if (targetUnit == null) {
			throw ConfigurationException.createMissingConfigNameException(TARGET_UNIT);
		}
		targetUnitSearchMap.put("unit", targetUnit);

		packages = tmpPackages.split(UTF8_COMMA);
		limit = configuration.getInteger("limit", 2);
		sorted = SortType.getByName(configuration.getString("sorted", "desc"));
		receiverUnitName = configuration.getString(UNIT_RECEIVER, null);

		String hibernateHbm2ddlAuto = configuration.getString(HIBERNATE_HBM2DDL, null);
		if (hibernateHbm2ddlAuto != null) {
			persistenceMap.put(HIBERNATE_HBM2DDL, hibernateHbm2ddlAuto);
		}

		String hibernateConnectionUrl = configuration.getString(HIBERNATE_CONNECTION_URL, null);
		if (hibernateConnectionUrl != null) {
			persistenceMap.put(HIBERNATE_CONNECTION_URL, hibernateConnectionUrl);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onMessage(ERoboDbContract message) {
		switch (message.getType()) {
		case SAVE:
			message.getData().forEach((key, value) -> {
				if (value instanceof List) {
					List values = (List) value;
					values.forEach(this::storeEntity);
				} else {
					storeEntity(value);
				}
			});
			break;
		case READ:
			if (receiverUnitName != null) {
				final ERoboDbContract response = new ERoboDbContract(message.getType());
				final RoboReference<ERoboDbContract> receiverUnit = getContext().getReference(receiverUnitName);
				message.getData().forEach((key, value) -> {
					if (registeredClasses.contains(key)) {
						Map<String, Object> searchParametersMap = (Map<String, Object>) value;
						if (!searchParametersMap.isEmpty()) {
							List<?> tmpList = repository.findByFields(key, searchParametersMap, limit, sorted);
							response.addData(key, tmpList);
						} else {
							List<?> tmpList = repository.findAllByClass(key, sorted);
							response.addData(key, tmpList);
						}
					}
				});

				receiverUnit.sendMessage(response);
			}
			break;
		default:
			SimpleLoggingUtil.error(getClass(), "not implemented: " + message);
		}
	}

	@Override
	public void initialize(Configuration configuration) throws ConfigurationException {
		super.initialize(configuration);

		PersistenceContextBuilder builder = persistenceMap.isEmpty()
				? new PersistenceContextBuilder(sourceType, packages).build()
				: new PersistenceContextBuilder(sourceType, packages, persistenceMap).build();
		registeredClasses = builder.getRegisteredClasses();
		dataSourceContext = builder.getDataSourceContext();
		repository = new DefaultRepository(dataSourceContext);
	}

	@Override
	public void shutdown() {
		setState(LifecycleState.SHUTTING_DOWN);
		dataSourceContext.close();
		setState(LifecycleState.SHUTDOWN);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <R> R onGetAttribute(AttributeDescriptor<R> descriptor) {
		if (descriptor.getAttributeName().equals(ATTRIBUTE_ROBO_ALL_NAME)
				&& descriptor.getAttributeType() == List.class) {
			//@formatter:off
			return (R) registeredClasses.stream().map(rc -> repository.findAllByClass(rc, sorted))
					.flatMap(List::stream)
					.collect(Collectors.toList());
			//@formatter:on
		}
		return super.onGetAttribute(descriptor);
	}

	@Override
	public Collection<AttributeDescriptor<?>> getKnownAttributes() {
		return KNOWN_ATTRIBUTES;
	}

	@SuppressWarnings("unchecked")
	public <R> R getByMap(Class<?> c, Map<String, Object> map) {
		return (R) repository.findByFields(c, map, limit, sorted);
	}

	// Private Methods
	private void storeEntity(Object entity) {
		Object id;
		if (entity instanceof ERoboPoint) {
			ERoboPoint eRoboPoint = (ERoboPoint) entity;
			ERoboUnit eRoboUnit = eRoboPoint.getUnit();
			eRoboUnit.addPoint(eRoboPoint);
			id = repository.save(eRoboUnit);
		} else {
			id = repository.save(entity);
		}
		if (id == null) {
			SimpleLoggingUtil.error(getClass(), "entity not stored: " + entity);
		}
	}

}
