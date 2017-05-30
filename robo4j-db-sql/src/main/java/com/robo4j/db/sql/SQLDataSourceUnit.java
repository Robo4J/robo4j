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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.robo4j.core.AttributeDescriptor;
import com.robo4j.core.ConfigurationException;
import com.robo4j.core.DefaultAttributeDescriptor;
import com.robo4j.core.LifecycleState;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboUnit;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.db.sql.model.Robo4JUnit;
import com.robo4j.db.sql.support.DataSourceContext;
import com.robo4j.db.sql.support.DataSourceProxy;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class SQLDataSourceUnit extends RoboUnit<Robo4JUnit> {

	private static final String ATTRIBUTE_ROBO_UNIT_NAME = "units";
	private static final Collection<AttributeDescriptor<?>> KNOWN_ATTRIBUTES = Collections
			.singleton(DefaultAttributeDescriptor.create(List.class, ATTRIBUTE_ROBO_UNIT_NAME));

	private static final String PERSISTENCE_UNIT = "persistenceUnit";
	private String persistenceUnit;
	private DataSourceContext dataSourceContext;
	private EntityManagerFactory emf;

	public SQLDataSourceUnit(RoboContext context, String id) {
		super(Robo4JUnit.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		persistenceUnit = configuration.getString(PERSISTENCE_UNIT, null);
		if (persistenceUnit == null) {
			throw ConfigurationException.createMissingConfigNameException(PERSISTENCE_UNIT);
		}
	}

	@Override
	public void onMessage(Robo4JUnit message) {
		EntityManager em = dataSourceContext.getEntityManager(message.getClass());
		em.getTransaction().begin();
		em.persist(message);
		em.getTransaction().commit();
	}

	@Override
	public void start() {
		setState(LifecycleState.STARTING);
		emf = Persistence.createEntityManagerFactory(persistenceUnit);
		dataSourceContext = new DataSourceProxy(Collections.singleton(emf.createEntityManager()));
		setState(LifecycleState.STARTED);
	}

	@Override
	public void stop() {
		setState(LifecycleState.STOPPING);
		dataSourceContext.close();
		setState(LifecycleState.STOPPED);
	}

	@Override
	public void shutdown() {
		setState(LifecycleState.SHUTTING_DOWN);
		;
		emf.close();
		setState(LifecycleState.SHUTDOWN);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <R> R onGetAttribute(AttributeDescriptor<R> descriptor) {
		if (descriptor.getAttributeName().equals(ATTRIBUTE_ROBO_UNIT_NAME)
				&& descriptor.getAttributeType() == List.class) {
			EntityManager em = dataSourceContext.getEntityManager(Robo4JUnit.class);
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Robo4JUnit> q = cb.createQuery(Robo4JUnit.class);
			Root<Robo4JUnit> rs = q.from(Robo4JUnit.class);
			q.select(rs);
			TypedQuery<Robo4JUnit> query = em.createQuery(q);
			return (R) query.getResultList();

		}
		return super.onGetAttribute(descriptor);
	}

	@Override
	public Collection<AttributeDescriptor<?>> getKnownAttributes() {
		return KNOWN_ATTRIBUTES;
	}
}
