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

import com.robo4j.db.sql.support.SortType;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public interface RoboRepository {

	<T> List<T> findAllByClass(Class<T> clazz, SortType sort);

	<T> List<T> findByClassWithLimit(Class<T> clazz, int limit, SortType sort);

	<T, ID> T findById(Class<T> clazz, ID id);

	<T> List<T> findByFields(Class<T> clazz, Map<String, Object> map, int limit, SortType sort);

	<T> T save(T entity);

}
