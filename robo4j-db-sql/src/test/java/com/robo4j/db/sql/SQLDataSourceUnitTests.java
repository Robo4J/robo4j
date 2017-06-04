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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
import com.robo4j.db.sql.model.Robo4JUnitPoint;
import com.robo4j.db.sql.model.RoboEntity;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class SQLDataSourceUnitTests {

	@SuppressWarnings("unchecked")
	@Test
	public void testAllRoboReferencesInDatabase() throws Exception {
		final RoboSystem system = new RoboSystem();
		Configuration config = ConfigurationFactory.createEmptyConfiguration();

		SQLDataSourceUnit sqlDataSourceUnit = new SQLDataSourceUnit(system, "dbSQLUnit");
		config.setString("sourceType", "h2");
		config.setString("packages", "com.robo4j.db.sql.model");
		config.setInteger("limit", 2);
		config.setString("sorted", "asc");
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

		AttributeDescriptor<List> descriptor1 = DefaultAttributeDescriptor.create(List.class, "all");
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

	@SuppressWarnings("unchecked")
	@Test
	public void testOnlyRoboUnitASCWithPointsInDB() throws Exception {
		final RoboSystem system = new RoboSystem();
		Configuration config = ConfigurationFactory.createEmptyConfiguration();

		SQLDataSourceUnit sqlDataSourceUnit = new SQLDataSourceUnit(system, "dbSQLUnit");
		config.setString("sourceType", "h2");
		config.setString("packages", "com.robo4j.db.sql.model");
		config.setInteger("limit", 2);
		config.setString("sorted", "asc");
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
		robo4JUnit1.setPoints(getRoboPoint(robo4JUnit1, 2));

		Robo4JUnit robo4JUnit2 = new Robo4JUnit();
		robo4JUnit2.setUid("system2");
		robo4JUnit2.setConfig("httpServer");
		robo4JUnit2.setPoints(getRoboPoint(robo4JUnit2, 1));

		Robo4JSystem robo4JSystem = new Robo4JSystem();
		robo4JSystem.setUid("mainSystem");

		sqlDataSourceUnit.onMessage(robo4JUnit1);
		sqlDataSourceUnit.onMessage(robo4JUnit2);
		sqlDataSourceUnit.onMessage(robo4JSystem);

		AttributeDescriptor<List> descriptor1 = DefaultAttributeDescriptor.create(List.class, "units_asc");
		List<RoboEntity<Long>> list1 = (List<RoboEntity<Long>>) sqlDataSourceUnit.onGetAttribute(descriptor1);
		System.out.println("Stored entities = " + list1.size());
		System.out.println("Stored entities = " + list1);

		Assert.assertTrue(Arrays.asList(robo4JUnit1, robo4JUnit2).size() == list1.size());
		Assert.assertTrue(list1.contains(robo4JUnit1));
		Assert.assertTrue(list1.contains(robo4JUnit2));

		Robo4JUnit unit1 = (Robo4JUnit)list1.get(0);
		Robo4JUnit unit2 = (Robo4JUnit)list1.get(1);
		Assert.assertTrue(!unit1.getPoints().isEmpty());
		Assert.assertTrue(unit1.getPoints().size() == 2);
		Assert.assertTrue(!unit2.getPoints().isEmpty());
		Assert.assertTrue(unit2.getPoints().size() == 1);


		system.shutdown();
		System.out.println("systemPong: State after shutdown:");
		System.out.println(SystemUtil.printStateReport(system));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testOnlyRoboUnitDESCWithPointsInDB() throws Exception {
		final RoboSystem system = new RoboSystem();
		Configuration config = ConfigurationFactory.createEmptyConfiguration();

		SQLDataSourceUnit sqlDataSourceUnit = new SQLDataSourceUnit(system, "dbSQLUnit");
		config.setString("sourceType", "h2");
		config.setString("packages", "com.robo4j.db.sql.model");
		config.setInteger("limit", 2);
		config.setString("sorted", "asc");
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
		robo4JUnit1.setPoints(getRoboPoint(robo4JUnit1, 2));

		Robo4JUnit robo4JUnit2 = new Robo4JUnit();
		robo4JUnit2.setUid("system2");
		robo4JUnit2.setConfig("httpServer");
		robo4JUnit2.setPoints(getRoboPoint(robo4JUnit2, 1));

		Robo4JSystem robo4JSystem = new Robo4JSystem();
		robo4JSystem.setUid("mainSystem");

		sqlDataSourceUnit.onMessage(robo4JUnit1);
		sqlDataSourceUnit.onMessage(robo4JUnit2);
		sqlDataSourceUnit.onMessage(robo4JSystem);

		AttributeDescriptor<List> descriptor1 = DefaultAttributeDescriptor.create(List.class, "units_desc");
		List<RoboEntity<Long>> list1 = (List<RoboEntity<Long>>) sqlDataSourceUnit.onGetAttribute(descriptor1);
		System.out.println("Stored entities = " + list1.size());
		System.out.println("Stored entities = " + list1);

		Assert.assertTrue(Arrays.asList(robo4JUnit1, robo4JUnit2).size() == list1.size());
		Assert.assertTrue(list1.contains(robo4JUnit1));
		Assert.assertTrue(list1.contains(robo4JUnit2));

		Robo4JUnit unit1 = (Robo4JUnit)list1.get(0);
		Robo4JUnit unit2 = (Robo4JUnit)list1.get(1);
		Assert.assertTrue(!unit1.getPoints().isEmpty());
		Assert.assertTrue(unit1.getPoints().size() == 1);
		Assert.assertTrue(!unit2.getPoints().isEmpty());
		Assert.assertTrue(unit2.getPoints().size() == 2);


		system.shutdown();
		System.out.println("systemPong: State after shutdown:");
		System.out.println(SystemUtil.printStateReport(system));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testEntityOrderWithLimit() throws Exception {
		final int max = 5;
		final int limit = 2;
		final RoboSystem system = new RoboSystem();
		Configuration config = ConfigurationFactory.createEmptyConfiguration();

		SQLDataSourceUnit sqlDataSourceUnit = new SQLDataSourceUnit(system, "dbSQLUnit");
		config.setString("sourceType", "h2");
		config.setString("packages", "com.robo4j.db.sql.model");
		config.setInteger("limit", limit);
		config.setString("sorted", "desc");
		sqlDataSourceUnit.initialize(config);
		system.addUnits(sqlDataSourceUnit);

		System.out.println("systemPong: State before start:");
		System.out.println(SystemUtil.printStateReport(system));

		system.start();

		System.out.println("systemPong: State after start:");
		System.out.println(SystemUtil.printStateReport(system));

		Robo4JSystem robo4JSystem = null;
		for (int i = 0; i < max; i++) {
			robo4JSystem = new Robo4JSystem();
			robo4JSystem.setUid("mainSystem");
			sqlDataSourceUnit.onMessage(robo4JSystem);
		}

		AttributeDescriptor<List> descriptor1 = DefaultAttributeDescriptor.create(List.class, "system");
		List<RoboEntity<Long>> list1 = sqlDataSourceUnit.onGetAttribute(descriptor1);
		System.out.println("Stored system1 = " + list1);

		Assert.assertNotNull(robo4JSystem);
		Assert.assertTrue(list1.size() == limit);
		Assert.assertTrue(list1.get(0).getId() == max);

		system.shutdown();
		System.out.println("systemPong: State after shutdown:");
		System.out.println(SystemUtil.printStateReport(system));
	}

	// Private Methods
	private List<Robo4JUnitPoint> getRoboPoint(Robo4JUnit unit, int number) {
		//@formatter:off
		return IntStream.range(0, number).mapToObj(i -> {
							Robo4JUnitPoint point = new Robo4JUnitPoint();
							point.setUnit(unit);
							point.setValues("value: " + i);
							point.setValueType("magicType: " + i);
							return point;})
						.collect(Collectors.toList());
		//@formatter:on
	}

}
