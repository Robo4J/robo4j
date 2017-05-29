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

package com.robo4j.db.sql.util;

import javax.persistence.EntityManager;

/**
 * Helper class to register EntityManager
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class DbEm {

	private Class<?> clazz;
	private EntityManager em;

	public DbEm(Class<?> clazz, EntityManager em) {
		this.clazz = clazz;
		this.em = em;
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public EntityManager getEm() {
		return em;
	}

	@Override
	public String toString() {
		return "DbEm{" + "clazz=" + clazz + ", em=" + em + '}';
	}
}
