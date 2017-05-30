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

package com.robo4j.db.sql.repository;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.ManagedType;

import com.robo4j.db.sql.Robo4DbException;
import com.robo4j.db.sql.support.DataSourceContext;
import com.robo4j.db.sql.util.DbEm;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class JpaDataSourceContext implements DataSourceContext {

	private final Map<Class<?>, EntityManager> entityManagerCache;

	public JpaDataSourceContext(Set<EntityManager> entityManagers) {
		//@formatter:off
		this.entityManagerCache = entityManagers.stream()
                .flatMap(e -> e.getMetamodel().getManagedTypes().stream()
                        .map(ManagedType::getJavaType)
                        .map(c -> new DbEm(c, e)))
				.collect(Collectors.toMap(DbEm::getClazz, DbEm::getEm));
		//@formatter:on
	}

	/**
	 *
	 * @param clazz
	 *            required class
	 * @return return class specific entity Manager
	 */
	@Override
	public EntityManager getEntityManager(Class<?> clazz) {
		if (clazz == null) {
			throw new Robo4DbException("not allowed state clazz: " + clazz);
		}
		return entityManagerCache.get(clazz);
	}

	/**
	 * close all entity managers
	 */
	@Override
	public void close() {
		//@formatter:off
		entityManagerCache.entrySet().stream()
                .map(Map.Entry::getValue)
                .map(EntityManager.class::cast)
				.forEach(EntityManager::close);
		//@formatter:on
	}
}
