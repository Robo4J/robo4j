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

package com.robo4j.db.sql.support;

import com.robo4j.db.sql.repository.JpaDataSourceContext;

import java.util.Collections;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class DataSourceProxy implements DataSourceContext {

	private final EntityManagerFactory emf;
	private Set<EntityManager> entityManagers;
	private DataSourceContext dataSourceContext;

	public DataSourceProxy( EntityManagerFactory emf) {
		this.emf = emf;
		this.entityManagers = Collections.singleton(emf.createEntityManager());
	}

	@Override
	public EntityManager getEntityManager(Class<?> clazz) {
		if (dataSourceContext == null) {
			dataSourceContext = new JpaDataSourceContext(entityManagers);
		}
		return dataSourceContext.getEntityManager(clazz);
	}

	@Override
	public void close() {
		dataSourceContext.close();
		emf.close();
	}


}
