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

package com.robo4j.db.sql.support;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.persistence.spi.PersistenceUnitInfo;

import org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl;
import org.hibernate.jpa.boot.internal.PersistenceUnitInfoDescriptor;
import org.hibernate.jpa.boot.spi.EntityManagerFactoryBuilder;
import org.hibernate.jpa.boot.spi.PersistenceUnitDescriptor;

import com.robo4j.core.client.util.RoboClassLoader;
import com.robo4j.db.sql.jpa.PersistenceDescriptorFactory;

/**
 *
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public final class PersistenceContextBuilder {

	private final DataSourceType sourceType;
	private final String[] packages;
	private DataSourceContext dataSourceContext;
	private List<Class<?>> registeredClasses;
	/**
	 * additional parameters
	 */
	private Map<String, Object> params;

	public PersistenceContextBuilder(DataSourceType sourceType, String[] packages) {
		this(sourceType, packages, new WeakHashMap<>());
	}

	public PersistenceContextBuilder(DataSourceType sourceType, String[] packages, Map<String, Object> params) {
		this.sourceType = sourceType;
		this.packages = packages;
		this.params = params;
	}

	public void addParamater(String key, Object value){
		params.put(key, value);
	}

	public PersistenceContextBuilder build() {
		PersistenceDescriptorFactory persistenceDescriptorFactory = new PersistenceDescriptorFactory(params);
		PersistenceUnitInfo persistenceUnitInfo = persistenceDescriptorFactory
				.get(RoboClassLoader.getInstance().getClassLoader(), sourceType, packages);
		PersistenceUnitDescriptor persistenceUnitDescriptor = new PersistenceUnitInfoDescriptor(persistenceUnitInfo);
		EntityManagerFactoryBuilder builder = new EntityManagerFactoryBuilderImpl(persistenceUnitDescriptor,
				new HashMap<>());
		registeredClasses = persistenceDescriptorFactory.registeredClasses();
		dataSourceContext = new DataSourceProxy(builder.build());
		return this;
	}

	public List<Class<?>> getRegisteredClasses() {
		return registeredClasses;
	}

	public DataSourceContext getDataSourceContext() {
		return dataSourceContext;
	}
}
