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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

	private static final int DEFAULT_LIMIT = 2;
	private static final String DEFAULT_SORTED = "asc";
	private static final String UNIT_SYSTEM_1_NAME = "system1";
	private static final String UNIT_SYSTEM_2_NAME = "system2";
	private static final String UNIT_SYSTEM_3_NAME = "system3";

	@SuppressWarnings("unchecked")
	@Test
	public void testAllRoboReferencesInDatabase() throws Exception {
		final SQLDataSourceUnit sqlDataSourceUnit = prepareSystemWithSQLUnit(DEFAULT_SORTED, DEFAULT_LIMIT);

		AttributeDescriptor<List> descriptor1 = DefaultAttributeDescriptor.create(List.class, "all");
		List<ERoboEntity<Long>> list1 = (List<ERoboEntity<Long>>) sqlDataSourceUnit.onGetAttribute(descriptor1);
		System.out.println("Stored entities = " + list1);

		//@formatter:off
		List<String> nameList = list1.stream()
				.map(ERoboUnit.class::cast)
				.map(ERoboUnit::getUid)
				.collect(Collectors.toList());
		//@formatter:on

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
		final SQLDataSourceUnit sqlDataSourceUnit = prepareSystemWithSQLUnit(DEFAULT_SORTED, DEFAULT_LIMIT);
		Map<String, Object> map = new HashMap<>();
		map.put("likeUid", "system");
		List<ERoboUnit> units = sqlDataSourceUnit.getByMap(ERoboUnit.class, map);

		AttributeDescriptor<List> descriptor1 = DefaultAttributeDescriptor.create(List.class, "units_all_desc");
		List<ERoboEntity<Long>> allUnitsList = (List<ERoboEntity<Long>>) sqlDataSourceUnit.onGetAttribute(descriptor1);

		ERoboUnit eRoboUnit1 = units.get(0);
		eRoboUnit1.addPoints(getRoboPoint(eRoboUnit1, 2));
		sqlDataSourceUnit.onMessage(eRoboUnit1);

		ERoboUnit eRoboUnit2 = units.get(1);
		eRoboUnit2.addPoints(getRoboPoint(eRoboUnit2, 1));
		sqlDataSourceUnit.onMessage(eRoboUnit2);

		AttributeDescriptor<List> descriptorLimitPoints = DefaultAttributeDescriptor.create(List.class, "unit_points");
		List<ERoboPoint> pointsList = sqlDataSourceUnit.onGetAttribute(descriptorLimitPoints);

		Assert.assertTrue(!units.isEmpty());
		Assert.assertTrue(!allUnitsList.isEmpty());
		Assert.assertTrue(allUnitsList.size() == units.size());
		Assert.assertTrue(eRoboUnit1.getUid().equals(UNIT_SYSTEM_1_NAME));
		Assert.assertTrue(eRoboUnit2.getUid().equals(UNIT_SYSTEM_2_NAME));
		Assert.assertTrue(pointsList.size() == 1);

		sqlDataSourceUnit.getContext().shutdown();
		System.out.println("System: State after shutdown:");
		System.out.println(SystemUtil.printStateReport(sqlDataSourceUnit.getContext()));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testOnlyRoboUnitDESCWithPointsInDB() throws Exception {
		int maxPoints = 2;
		final SQLDataSourceUnit sqlDataSourceUnit = prepareSystemWithSQLUnit("desc", DEFAULT_LIMIT);
		Map<String, Object> map = new HashMap<>();
		map.put("likeUid", "system");
		List<ERoboUnit> units = sqlDataSourceUnit.getByMap(ERoboUnit.class, map);

		AttributeDescriptor<List> descriptor1 = DefaultAttributeDescriptor.create(List.class, "units_all_desc");
		List<ERoboEntity<Long>> allUnitsList = (List<ERoboEntity<Long>>) sqlDataSourceUnit.onGetAttribute(descriptor1);

		ERoboUnit eRoboUnit1 = units.get(0);
		eRoboUnit1.addPoints(getRoboPoint(eRoboUnit1, maxPoints));
		sqlDataSourceUnit.onMessage(eRoboUnit1);

		ERoboUnit eRoboUnit2 = units.get(1);
		eRoboUnit2.addPoints(getRoboPoint(eRoboUnit2, 1));
		sqlDataSourceUnit.onMessage(eRoboUnit2);

		AttributeDescriptor<List> descriptorLimitPoints = DefaultAttributeDescriptor.create(List.class, "unit_points");
		List<ERoboPoint> pointsList = sqlDataSourceUnit.onGetAttribute(descriptorLimitPoints);

		Assert.assertTrue(!units.isEmpty());
		Assert.assertTrue(!allUnitsList.isEmpty());
		Assert.assertTrue(allUnitsList.size() == units.size());
		Assert.assertTrue(eRoboUnit1.getUid().equals(UNIT_SYSTEM_2_NAME));
		Assert.assertTrue(eRoboUnit2.getUid().equals(UNIT_SYSTEM_1_NAME));
		Assert.assertTrue(pointsList.size() == maxPoints);
		pointsList.forEach(p -> {
			Assert.assertNotNull(p.getCreatedOn());
			Assert.assertNotNull(p.getUpdatedOn());
			Assert.assertTrue(p.getCreatedOn().equals(p.getUpdatedOn()));
		});

		System.out.println("LIST : " + pointsList);
		sqlDataSourceUnit.getContext().shutdown();
		System.out.println("System: State after shutdown:");
		System.out.println(SystemUtil.printStateReport(sqlDataSourceUnit.getContext()));
	}

	/**
	 * Test updates target entity by adding points limit is set
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testEntityOrderWithLimitLower() throws Exception {
		int maxPoints = 1;
		final SQLDataSourceUnit sqlDataSourceUnit = prepareSystemWithSQLUnit(DEFAULT_SORTED, DEFAULT_LIMIT);

		Map<String, Object> map = new HashMap<>();
		map.put("uid", UNIT_SYSTEM_2_NAME);
		List<ERoboUnit> units = sqlDataSourceUnit.getByMap(ERoboUnit.class, map);
		ERoboUnit system2Enity = units.get(0);
		system2Enity.addPoints(getRoboPoint(system2Enity, maxPoints));
		sqlDataSourceUnit.onMessage(system2Enity);

		AttributeDescriptor<List> descriptorLimitPoints = DefaultAttributeDescriptor.create(List.class, "unit_points");
		List<ERoboPoint> pointsList = sqlDataSourceUnit.onGetAttribute(descriptorLimitPoints);

		Assert.assertTrue(!units.isEmpty());
		Assert.assertTrue(units.size() == 1);
		Assert.assertTrue(system2Enity.getUid().equals(UNIT_SYSTEM_2_NAME));
		Assert.assertTrue(system2Enity.getPoints().size() == maxPoints);
		Assert.assertTrue(pointsList.size() == maxPoints);
		pointsList.forEach(p -> {
			Assert.assertNotNull(p.getCreatedOn());
			Assert.assertNotNull(p.getUpdatedOn());
			Assert.assertTrue(p.getCreatedOn().equals(p.getUpdatedOn()));
		});

		sqlDataSourceUnit.getContext().shutdown();
		System.out.println("System: State after shutdown:");
		System.out.println(SystemUtil.printStateReport(sqlDataSourceUnit.getContext()));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testEntityOrderWithLimitHigher() throws Exception {
		int maxPoints = 3;
		final SQLDataSourceUnit sqlDataSourceUnit = prepareSystemWithSQLUnit(DEFAULT_SORTED, DEFAULT_LIMIT);

		Map<String, Object> map = new HashMap<>();
		map.put("uid", UNIT_SYSTEM_2_NAME);
		List<ERoboUnit> units = sqlDataSourceUnit.getByMap(ERoboUnit.class, map);
		ERoboUnit system2Enity = units.get(0);
		system2Enity.addPoints(getRoboPoint(system2Enity, maxPoints));
		sqlDataSourceUnit.onMessage(system2Enity);

		AttributeDescriptor<List> descriptorLimitPoints = DefaultAttributeDescriptor.create(List.class, "unit_points");
		List<ERoboPoint> pointsList = sqlDataSourceUnit.onGetAttribute(descriptorLimitPoints);

		Assert.assertTrue(!units.isEmpty());
		Assert.assertTrue(units.size() == 1);
		Assert.assertTrue(system2Enity.getUid().equals(UNIT_SYSTEM_2_NAME));
		Assert.assertTrue(system2Enity.getPoints().size() == maxPoints);
		Assert.assertTrue(pointsList.size() == DEFAULT_LIMIT);

		sqlDataSourceUnit.getContext().shutdown();
		System.out.println("System: State after shutdown:");
		System.out.println(SystemUtil.printStateReport(sqlDataSourceUnit.getContext()));
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

	private SQLDataSourceUnit prepareSystemWithSQLUnit(String sorted, int limit) throws Exception {
		final RoboSystem system = new RoboSystem();
		Configuration config = ConfigurationFactory.createEmptyConfiguration();

		SQLDataSourceUnit result = new SQLDataSourceUnit(system, "dbSQLUnit");
		config.setString("sourceType", "h2");
		config.setString("packages", "com.robo4j.db.sql.model");
		config.setInteger("limit", limit);
		config.setString("sorted", sorted);
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
