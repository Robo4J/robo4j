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
import java.util.stream.IntStream;

import com.robo4j.core.RoboUnit;
import com.robo4j.db.sql.model.ERoboEntity;
import com.robo4j.db.sql.model.ERoboPoint;
import com.robo4j.db.sql.model.ERoboUnit;
import com.robo4j.db.sql.unit.TestPersistPointDTO;
import org.junit.Assert;
import org.junit.Test;

import com.robo4j.core.AttributeDescriptor;
import com.robo4j.core.DefaultAttributeDescriptor;
import com.robo4j.core.RoboBuilder;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboReference;
import com.robo4j.core.RoboSystem;
import com.robo4j.core.client.util.RoboClassLoader;
import com.robo4j.core.client.util.RoboHttpUtils;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.configuration.ConfigurationFactory;
import com.robo4j.core.util.SystemUtil;
import com.robo4j.db.sql.unit.TestPersistUnit;

/**
 * DB SQL unit attached to other units to safe entities
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class SQLDBHttpServerUnitTests {

	private static final int DEFAULT_INDEX = 0;

	// @Test
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

	@SuppressWarnings("unchecked")
	@Test
	public void createSystem() throws Exception {

		final int maxPoints = 3;
		final String targetUnit = "testSystem";
		final String dataSourceName = "dbSQLUnit";
		final RoboSystem system = new RoboSystem();
		Configuration sqlConfig = ConfigurationFactory.createEmptyConfiguration();

		SQLDataSourceUnit sqlUnit = new SQLDataSourceUnit(system, dataSourceName);
		sqlConfig.setString("sourceType", "h2");
		sqlConfig.setString("packages", "com.robo4j.db.sql.model");
		sqlConfig.setInteger("limit", 3);
		sqlConfig.setString("sorted", "asc");
		sqlConfig.setString("targetUnit", targetUnit);

		TestPersistUnit testRoboUnitOne = new TestPersistUnit(system, targetUnit);
		Configuration testConfig = ConfigurationFactory.createEmptyConfiguration();
		testConfig.setString("persistenceUnit", dataSourceName);
		testConfig.setString("config", "magic config");

		/* specific configuration */
		system.addUnits(sqlUnit, testRoboUnitOne);
		sqlUnit.initialize(sqlConfig);
		testRoboUnitOne.initialize(testConfig);

		system.start();

		System.out.println("System: State after start:");
		System.out.println(SystemUtil.printStateReport(system));


		IntStream.range(DEFAULT_INDEX, maxPoints)
				.forEach(i -> testRoboUnitOne.onMessage(new TestPersistPointDTO("testType" + i, "testValue" + i)));
		
		AttributeDescriptor<List> descriptorAllPoints = DefaultAttributeDescriptor.create(List.class, "unit_points");
		List<ERoboPoint> allPointsList = (List<ERoboPoint>) sqlUnit.onGetAttribute(descriptorAllPoints);

		Map<String, Object> tmpMap= new HashMap<>();
		tmpMap.put("unit", targetUnit);
		List<ERoboPoint> directBySQLUnit = sqlUnit.getByMap(ERoboPoint.class, tmpMap);

		AttributeDescriptor<List> descriptorAllUnits = DefaultAttributeDescriptor.create(List.class, "units_all_desc");
		List<ERoboEntity<Long>> allUnitsList = (List<ERoboEntity<Long>>) sqlUnit.onGetAttribute(descriptorAllUnits);


		Assert.assertNotNull(directBySQLUnit);
		Assert.assertNotNull(allPointsList);
		Assert.assertNotNull(allUnitsList);

		Assert.assertTrue(allUnitsList.size() == 1);
		Assert.assertTrue(((ERoboUnit)allUnitsList.get(DEFAULT_INDEX)).getUid().equals(targetUnit));
		Assert.assertTrue(directBySQLUnit.size() == allPointsList.size());

		Assert.assertTrue(allPointsList.size() == maxPoints);
		IntStream.range(DEFAULT_INDEX, directBySQLUnit.size())
				.forEach(i -> Assert.assertTrue(directBySQLUnit.get(i).equals(allPointsList.get(i))));

		system.shutdown();
		System.out.println("System: State after shutdown:");
		System.out.println(SystemUtil.printStateReport(system));

	}

}
