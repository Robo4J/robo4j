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

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.robo4j.core.AttributeDescriptor;
import com.robo4j.core.DefaultAttributeDescriptor;
import com.robo4j.core.RoboSystem;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.configuration.ConfigurationFactory;
import com.robo4j.core.util.SystemUtil;
import com.robo4j.db.sql.model.Robo4JSystem;
import com.robo4j.db.sql.model.Robo4JUnit;
import com.robo4j.db.sql.model.RoboEntity;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class SQLDataSourceUnitTests {

	@SuppressWarnings("unchecked")
	@Test
	public void testH2Database() throws Exception {
		final RoboSystem system = new RoboSystem();
		Configuration config = ConfigurationFactory.createEmptyConfiguration();

		SQLDataSourceUnit sqlDataSourceUnit = new SQLDataSourceUnit(system, "dbSQLUnit");
		config.setString("sourceType", "h2");
		config.setString("packages", "com.robo4j.db.sql.model");
		sqlDataSourceUnit.initialize(config);
		system.addUnits(sqlDataSourceUnit);

		System.out.println("systemPong: State before start:");
		System.out.println(SystemUtil.printStateReport(system));

		system.start();

		System.out.println("systemPong: State after start:");
		System.out.println(SystemUtil.printStateReport(system));

		Robo4JUnit robo4JUnit1 = new Robo4JUnit();
		robo4JUnit1.setUid("system1");
		robo4JUnit1.setConfig("dbSQLUnit,httpClient");
		Robo4JUnit robo4JUnit2 = new Robo4JUnit();
		robo4JUnit2.setUid("system2");
		robo4JUnit2.setConfig("httpServer");

		Robo4JSystem robo4JSystem = new Robo4JSystem();
		robo4JSystem.setUid("mainSystem");

		sqlDataSourceUnit.onMessage(robo4JUnit1);
		sqlDataSourceUnit.onMessage(robo4JUnit2);
		sqlDataSourceUnit.onMessage(robo4JSystem);

		AttributeDescriptor<List> descriptor1 = DefaultAttributeDescriptor.create(List.class, "units");
		List<RoboEntity<Long>> list1 = (List<RoboEntity<Long>>) sqlDataSourceUnit.onGetAttribute(descriptor1);
		System.out.println("Stored entities = " + list1);

		AttributeDescriptor<List> descriptor2 = DefaultAttributeDescriptor.create(List.class, "system");
		List<RoboEntity<Long>> list2 = sqlDataSourceUnit.onGetAttribute(descriptor2);
		System.out.println("Stored system1 = " + list2);

		Assert.assertTrue(Arrays.asList(robo4JUnit1, robo4JUnit2, robo4JSystem).size() == list1.size());
		Assert.assertTrue(list1.contains(robo4JUnit1));
		Assert.assertTrue(list1.contains(robo4JUnit2));
		Assert.assertTrue(list1.contains(robo4JSystem));

		Assert.assertTrue(Arrays.asList(robo4JSystem).size() == list2.size());
		Assert.assertTrue(list2.contains(robo4JSystem));

		system.shutdown();
		System.out.println("systemPong: State after shutdown:");
		System.out.println(SystemUtil.printStateReport(system));

	}
}
