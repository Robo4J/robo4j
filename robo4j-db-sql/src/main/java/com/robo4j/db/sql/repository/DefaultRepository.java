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

package com.robo4j.db.sql.repository;

import com.robo4j.db.sql.model.ERoboUnit;
import com.robo4j.db.sql.support.DataSourceContext;
import com.robo4j.db.sql.support.SortType;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
@SuppressWarnings(value = { "unchecked", "rawtypes" })
public class DefaultRepository implements RoboRepository {

	private static final String FIELD_ID = "id";
	private static final String ROBO_UNIT_UID = "uid";
	private static final String PREFIX_LIKE = "like";
	private static final String SIGN_PERCENTAGE = "%";
	private static final CharSequence EMPTY_STRING = "";
	private final ReentrantLock lock = new ReentrantLock();
	
	private final DataSourceContext dataSourceContext;

	public DefaultRepository(DataSourceContext dataSourceContext) {
		this.dataSourceContext = dataSourceContext;
	}

	@Override
	public <T> List<T> findAllByClass(Class<T> clazz, SortType sort) {
		final TypedQuery<T> tq = getTypeQueryAllByClass(clazz, sort);
		return tq.getResultList();
	}

	@Override
	public <T> List<T> findByClassWithLimit(Class<T> clazz, int limit, SortType sort) {
		final TypedQuery<T> tq = getTypeQueryAllByClass(clazz, sort);
		return tq.setMaxResults(limit).getResultList();
	}

	@Override
	public <T, ID> T findById(Class<T> clazz, ID id) {
		return dataSourceContext.getEntityManager(clazz).getReference(clazz, id);
	}

	@Override
	public <T> List<T> findByFields(Class<T> clazz, Map<String, Object> map, int limit, SortType sort) {
		EntityManager em = dataSourceContext.getEntityManager(clazz);
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery cq = cb.createQuery(clazz);

		Root<T> rs = cq.from(clazz);
		//@formatter:on
		List<Predicate> predicates = map.entrySet().stream().map(e -> {

			// like check
			if (e.getKey().startsWith(PREFIX_LIKE)) {
				String rootKey = e.getKey().replace(PREFIX_LIKE, EMPTY_STRING).toLowerCase();
				return cb.like(rs.get(rootKey), likeString(e.getValue()));
			}

			Path p = rs.get(e.getKey());
			if (p.getJavaType().equals(ERoboUnit.class)) {
				return cb.equal(rs.get(e.getKey()).get(ROBO_UNIT_UID), e.getValue());
			}

			return cb.equal(rs.get(e.getKey()), e.getValue());

		}).collect(Collectors.toList());
		//@formatter:on
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
		lock.lock();
		try {
			EntityManager em = dataSourceContext.getEntityManager(entity.getClass());
			em.getTransaction().begin();
			if (em.contains(entity)) {
				em.merge(entity);
			} else {
				em.persist(entity);
			}
			em.getTransaction().commit();
			return entity;
		} finally {
			lock.unlock();
		}
	}

	// Private Methods
	private Order getOrderById(CriteriaBuilder cb, Root rs, SortType sortType) {
		return sortType.equals(SortType.ASC) ? cb.asc(rs.get(FIELD_ID)) : cb.desc(rs.get(FIELD_ID));
	}

	private <T> TypedQuery<T> getTypeQueryAllByClass(Class<T> clazz, SortType sort) {
		EntityManager em = dataSourceContext.getEntityManager(clazz);
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery cq = cb.createQuery(clazz);
		Root<T> rs = cq.from(clazz);
		CriteriaQuery<T> cq2 = cq.select(rs).orderBy(getOrderById(cb, rs, sort));
		return em.createQuery(cq2);
	}

	private String likeString(Object value) {
		return new StringBuilder(SIGN_PERCENTAGE).append(value).append(SIGN_PERCENTAGE).toString();
	}

}
