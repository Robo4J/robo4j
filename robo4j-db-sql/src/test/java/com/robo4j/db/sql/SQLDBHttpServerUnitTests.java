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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.robo4j.core.AttributeDescriptor;
import com.robo4j.core.DefaultAttributeDescriptor;
import com.robo4j.core.RoboBuilder;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboReference;
import com.robo4j.core.client.util.RoboClassLoader;
import com.robo4j.core.util.SystemUtil;

/**
 * DB SQL unit attached to other units to safe entities
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class SQLDBHttpServerUnitTests {

	@Test
	public void sqlHttpServerImageSendTest() throws Exception {

		RoboBuilder builder = new RoboBuilder()
				.add(RoboClassLoader.getInstance().getResource("robo4j_db_sql_test.xml"));
		RoboContext roboSystemClient = builder.build();
		roboSystemClient.start();
		System.out.println("RoboSystem Client after start:");
		System.out.println(SystemUtil.printStateReport(roboSystemClient));

		RoboReference<SQLDataSourceUnit> sqlUnit = roboSystemClient.getReference("dbSqlUnit");

		AttributeDescriptor<List> descriptor1 = DefaultAttributeDescriptor.create(List.class, "all");

		Assert.assertNotNull(sqlUnit);
		Assert.assertTrue(sqlUnit.getAttribute(descriptor1).get().size() == 0);

	}
}
