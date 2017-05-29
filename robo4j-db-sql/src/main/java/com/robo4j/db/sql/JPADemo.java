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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceUnit;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.robo4j.db.sql.model.Robo4JSystem;

import java.util.List;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class JPADemo {
	public static void main(String[] args) {



		EntityManagerFactory emf = Persistence.createEntityManagerFactory("h2");




		EntityManager em = emf.createEntityManager();

		em.getTransaction().begin();
		Robo4JSystem robo4JSystem = new Robo4JSystem();
		robo4JSystem.setUid("robo4j_unit");
		robo4JSystem.setConfig("httpClient,httpServer");
		em.persist(robo4JSystem);
		em.getTransaction().commit();
		System.out.println("COMMIT: " + robo4JSystem);

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Robo4JSystem> q = cb.createQuery(Robo4JSystem.class);
		Root<Robo4JSystem> rs = q.from(Robo4JSystem.class);
		q.select(rs);

		TypedQuery<Robo4JSystem> query = em.createQuery(q);
		List<Robo4JSystem> result = query.getResultList();

		System.out.println("Result: " + result);

	}
}
