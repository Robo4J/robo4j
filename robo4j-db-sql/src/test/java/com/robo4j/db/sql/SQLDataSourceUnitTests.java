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
import com.robo4j.db.sql.model.ERoboEntity;
import com.robo4j.db.sql.model.ERoboPoint;
import com.robo4j.db.sql.model.ERoboUnit;

/**
 * SQL database focused tests
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class SQLDataSourceUnitTests {

	private static String UNIT_SYSTEM_1_NAME = "system1";
	private static String UNIT_SYSTEM_2_NAME = "system2";
	private static String UNIT_SYSTEM_3_NAME = "system3";

	@SuppressWarnings("unchecked")
	@Test
	public void testAllRoboReferencesInDatabase() throws Exception {
		final SQLDataSourceUnit sqlDataSourceUnit = prepareSystemWithSQLUnit();

		AttributeDescriptor<List> descriptor1 = DefaultAttributeDescriptor.create(List.class, "all");
		List<ERoboEntity<Long>> list1 = (List<ERoboEntity<Long>>) sqlDataSourceUnit.onGetAttribute(descriptor1);
		System.out.println("Stored entities = " + list1);

		List<String> nameList = list1.stream().map(ERoboUnit.class::cast).map(ERoboUnit::getUid)
				.collect(Collectors.toList());
		Assert.assertTrue(list1.size() == 2);
		Assert.assertTrue(nameList.contains(UNIT_SYSTEM_1_NAME));
		Assert.assertTrue(nameList.contains(UNIT_SYSTEM_2_NAME));

		sqlDataSourceUnit.getContext().shutdown();
		System.out.println("systemPong: State after shutdown:");
		System.out.println(SystemUtil.printStateReport(sqlDataSourceUnit.getContext()));

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
		config.setString("targetUnit", UNIT_SYSTEM_2_NAME);
		sqlDataSourceUnit.initialize(config);
		system.addUnits(sqlDataSourceUnit);

		System.out.println("systemPong: State before start:");
		System.out.println(SystemUtil.printStateReport(system));

		system.start();

		System.out.println("systemPong: State after start:");
		System.out.println(SystemUtil.printStateReport(system));

		ERoboUnit ERoboUnit1 = new ERoboUnit();
		ERoboUnit1.setUid("system1");
		ERoboUnit1.setConfig("dbSQLUnit,httpClient");
		ERoboUnit1.addPoints(getRoboPoint(ERoboUnit1, 2));

		ERoboUnit ERoboUnit2 = new ERoboUnit();
		ERoboUnit2.setUid("system2");
		ERoboUnit2.setConfig("httpServer");
		ERoboUnit2.addPoints(getRoboPoint(ERoboUnit2, 1));

		sqlDataSourceUnit.onMessage(ERoboUnit1);
		sqlDataSourceUnit.onMessage(ERoboUnit2);

		AttributeDescriptor<List> descriptor1 = DefaultAttributeDescriptor.create(List.class, "units_all_asc");
		List<ERoboEntity<Long>> list1 = (List<ERoboEntity<Long>>) sqlDataSourceUnit.onGetAttribute(descriptor1);
		System.out.println("Stored entities = " + list1.size());
		System.out.println("Stored entities = " + list1);

		Assert.assertTrue(Arrays.asList(ERoboUnit1, ERoboUnit2).size() == list1.size());
		Assert.assertTrue(list1.contains(ERoboUnit1));
		Assert.assertTrue(list1.contains(ERoboUnit2));

		ERoboUnit unit1 = (ERoboUnit) list1.get(0);
		ERoboUnit unit2 = (ERoboUnit) list1.get(1);
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
		config.setString("targetUnit", UNIT_SYSTEM_2_NAME);
		sqlDataSourceUnit.initialize(config);
		system.addUnits(sqlDataSourceUnit);

		System.out.println("systemPong: State before start:");
		System.out.println(SystemUtil.printStateReport(system));

		system.start();

		System.out.println("systemPong: State after start:");
		System.out.println(SystemUtil.printStateReport(system));

		ERoboUnit ERoboUnit1 = new ERoboUnit();
		ERoboUnit1.setUid("system1");
		ERoboUnit1.setConfig("dbSQLUnit,httpClient");
		ERoboUnit1.addPoints(getRoboPoint(ERoboUnit1, 2));

		ERoboUnit ERoboUnit2 = new ERoboUnit();
		ERoboUnit2.setUid("system2");
		ERoboUnit2.setConfig("httpServer");
		ERoboUnit2.addPoints(getRoboPoint(ERoboUnit2, 1));

		sqlDataSourceUnit.onMessage(ERoboUnit1);
		sqlDataSourceUnit.onMessage(ERoboUnit2);

		AttributeDescriptor<List> descriptor1 = DefaultAttributeDescriptor.create(List.class, "units_all_desc");
		List<ERoboEntity<Long>> list1 = (List<ERoboEntity<Long>>) sqlDataSourceUnit.onGetAttribute(descriptor1);
		System.out.println("Stored entities = " + list1.size());
		System.out.println("Stored entities = " + list1);

		Assert.assertTrue(Arrays.asList(ERoboUnit1, ERoboUnit2).size() == list1.size());
		Assert.assertTrue(list1.contains(ERoboUnit1));
		Assert.assertTrue(list1.contains(ERoboUnit2));

		ERoboUnit unit1 = (ERoboUnit) list1.get(0);
		ERoboUnit unit2 = (ERoboUnit) list1.get(1);
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
		final int max = 3;
		final int limit = 2;
		final int roboUnit3Points = 5;
		final RoboSystem system = new RoboSystem();
		Configuration config = ConfigurationFactory.createEmptyConfiguration();

		SQLDataSourceUnit sqlDataSourceUnit = new SQLDataSourceUnit(system, "dbSQLUnit");
		config.setString("sourceType", "h2");
		config.setString("packages", "com.robo4j.db.sql.model");
		config.setInteger("limit", limit);
		config.setString("sorted", "desc");
		config.setString("targetUnit", UNIT_SYSTEM_2_NAME);
		sqlDataSourceUnit.initialize(config);
		system.addUnits(sqlDataSourceUnit);

		System.out.println("systemPong: State before start:");
		System.out.println(SystemUtil.printStateReport(system));

		system.start();

		System.out.println("systemPong: State after start:");
		System.out.println(SystemUtil.printStateReport(system));

		ERoboUnit eRoboUnit1 = new ERoboUnit();
		eRoboUnit1.setUid(UNIT_SYSTEM_1_NAME);
		eRoboUnit1.setConfig("dbSQLUnit,httpClient");
		eRoboUnit1.addPoints(getRoboPoint(eRoboUnit1, max));

		ERoboUnit eRoboUnit2 = new ERoboUnit();
		eRoboUnit2.setUid(UNIT_SYSTEM_2_NAME);
		eRoboUnit2.setConfig("httpServer");
		eRoboUnit2.addPoints(getRoboPoint(eRoboUnit2, max));
		eRoboUnit2.addPart(eRoboUnit1);
		eRoboUnit2.setParent(eRoboUnit1);

		ERoboUnit eRoboUnit3 = new ERoboUnit();
		eRoboUnit3.setUid(UNIT_SYSTEM_3_NAME);
		eRoboUnit3.setConfig("httpServer,httpClient");
		eRoboUnit3.addPoints(getRoboPoint(eRoboUnit3, roboUnit3Points));
		eRoboUnit3.addParts(Arrays.asList(eRoboUnit1, eRoboUnit2));
		eRoboUnit3.setParent(eRoboUnit1);

		sqlDataSourceUnit.onMessage(eRoboUnit1);
		sqlDataSourceUnit.onMessage(eRoboUnit2);
		sqlDataSourceUnit.onMessage(eRoboUnit3);

		AttributeDescriptor<List> descriptor1 = DefaultAttributeDescriptor.create(List.class, "units_desc");
		AttributeDescriptor<List> descriptor2 = DefaultAttributeDescriptor.create(List.class, "unit_points");
		List<ERoboEntity<Long>> list1 = sqlDataSourceUnit.onGetAttribute(descriptor1);
		List<ERoboPoint> list2 = sqlDataSourceUnit.onGetAttribute(descriptor2);

		Assert.assertTrue(list1.size() == limit);
		Assert.assertTrue(list1.get(0).getId() == max);

		ERoboUnit roboUnit1 = (ERoboUnit) list1.get(0);
		Assert.assertTrue(roboUnit1.getUid().equals(UNIT_SYSTEM_3_NAME));
		Assert.assertTrue(roboUnit1.getPoints().size() == roboUnit3Points);
		Assert.assertTrue(roboUnit1.getParts().size() == 2);
		Assert.assertTrue(roboUnit1.getParent().getUid().equals(UNIT_SYSTEM_1_NAME));
		Assert.assertTrue(list2.size() == limit);
		System.out.println("LIST2: " + list2);

		system.shutdown();
		System.out.println("systemPong: State after shutdown:");
		System.out.println(SystemUtil.printStateReport(system));
	}

	// Private Methods
	private List<ERoboPoint> getRoboPoint(ERoboUnit unit, int number) {
		//@formatter:off
		return IntStream.range(0, number).mapToObj(i -> {
							ERoboPoint point = new ERoboPoint();
							point.setUnit(unit);
							point.setValues("value: " + i);
							point.setValueType("magicType: " + i);
							return point;})
						.collect(Collectors.toList());
		//@formatter:on
	}

	private SQLDataSourceUnit prepareSystemWithSQLUnit() throws Exception {
		final RoboSystem system = new RoboSystem();
		Configuration config = ConfigurationFactory.createEmptyConfiguration();

		SQLDataSourceUnit result = new SQLDataSourceUnit(system, "dbSQLUnit");
		config.setString("sourceType", "h2");
		config.setString("packages", "com.robo4j.db.sql.model");
		config.setInteger("limit", 2);
		config.setString("sorted", "asc");
		config.setString("targetUnit", UNIT_SYSTEM_2_NAME);
		result.initialize(config);
		system.addUnits(result);

		System.out.println("systemPong: State before start:");
		System.out.println(SystemUtil.printStateReport(system));

		system.start();

		System.out.println("systemPong: State after start:");
		System.out.println(SystemUtil.printStateReport(system));

		ERoboUnit ERoboUnit1 = new ERoboUnit();
		ERoboUnit1.setUid("system1");
		ERoboUnit1.setConfig("dbSQLUnit,httpClient");

		ERoboUnit ERoboUnit2 = new ERoboUnit();
		ERoboUnit2.setUid("system2");
		ERoboUnit2.setConfig("httpServer");

		result.onMessage(ERoboUnit1);
		result.onMessage(ERoboUnit2);
		return result;
	}
}
