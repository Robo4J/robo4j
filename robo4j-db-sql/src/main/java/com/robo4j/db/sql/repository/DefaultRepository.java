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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.robo4j.db.sql.support.DataSourceContext;
import com.robo4j.db.sql.support.SortType;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class DefaultRepository implements RoboRepository {

	private static final String FIELD_ID = "id";
	private final DataSourceContext dataSourceContext;

	public DefaultRepository(DataSourceContext dataSourceContext) {
		this.dataSourceContext = dataSourceContext;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> findAllByClass(Class<T> clazz, SortType sort) {
		EntityManager em = dataSourceContext.getEntityManager(clazz);
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery cq = cb.createQuery(clazz);
		Root<T> rs = cq.from(clazz);
		CriteriaQuery<T> cq2 = cq.select(rs).orderBy(getOrderById(cb, rs, sort));

		TypedQuery<T> tq = em.createQuery(cq2);

		return tq.getResultList();
	}

	@Override
	public <T, ID> T findById(Class<T> clazz, ID id) {
		return dataSourceContext.getEntityManager(clazz).getReference(clazz, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> findByFields(Class<T> clazz, Map<String, Object> map, int limit, SortType sort) {
		EntityManager em = dataSourceContext.getEntityManager(clazz);
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery cq = cb.createQuery(clazz);

		Root<T> rs = cq.from(clazz);
		List<Predicate> predicates = map.entrySet().stream().map(e -> cb.equal(rs.get(e.getKey()), e.getValue()))
				.collect(Collectors.toList());
		CriteriaQuery<T> cq2 = cq.where(predicates.toArray(new Predicate[predicates.size()]))
				.orderBy(getOrderById(cb, rs, sort)).select(rs);

		TypedQuery<T> tq = em.createQuery(cq2);
		//@formatter:off
		return tq.setMaxResults(limit)
				.getResultList();
		//@formatter:on
	}

	@Override
	public <T> T save(T entity) {
		EntityManager em = dataSourceContext.getEntityManager(entity.getClass());
		em.getTransaction().begin();
		em.persist(entity);
		em.getTransaction().commit();
		return entity;
	}

	// Private Methods
	private Order getOrderById(CriteriaBuilder cb, Root rs, SortType sortType) {
		return sortType.equals(SortType.ASC) ? cb.asc(rs.get(FIELD_ID)) : cb.desc(rs.get(FIELD_ID));
	}

}
