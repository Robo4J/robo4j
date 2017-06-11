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

	@Test
	public void createSystem() throws Exception {
		final RoboSystem system = new RoboSystem();
		Configuration sqlConfig = ConfigurationFactory.createEmptyConfiguration();

		SQLDataSourceUnit sqlUnit = new SQLDataSourceUnit(system, "dbSQLUnit");
		sqlConfig.setString("sourceType", "h2");
		sqlConfig.setString("packages", "com.robo4j.db.sql.model");
		sqlConfig.setInteger("limit", 3);
		sqlConfig.setString("sorted", "asc");
		sqlConfig.setString("targetUnit", "testSystem");

		TestPersistUnit testRoboUnitOne = new TestPersistUnit(system, "testSystem");
		Configuration testConfig = ConfigurationFactory.createEmptyConfiguration();
		testConfig.setString("persistenceUnit", "dbSQLUnit");
		testConfig.setString("config", "magic config");

		/* specific configuration */
		system.addUnits(sqlUnit, testRoboUnitOne);
		sqlUnit.initialize(sqlConfig);
		testRoboUnitOne.initialize(testConfig);

		system.start();


		System.out.println("System: State after start:");
		System.out.println(SystemUtil.printStateReport(system));


		testRoboUnitOne.onMessage(new TestPersistPointDTO("testType1", "testValue1"));
		testRoboUnitOne.onMessage(new TestPersistPointDTO("testType2", "testValue2"));
		testRoboUnitOne.onMessage(new TestPersistPointDTO("testType3", "testValue3"));

		AttributeDescriptor<List> descriptorAllPoints = DefaultAttributeDescriptor.create(List.class, "unit_points");
		List<ERoboPoint> allPointsList = (List<ERoboPoint>) sqlUnit.onGetAttribute(descriptorAllPoints);
		System.out.println("allPointsList: " + allPointsList);

		Map<String, Object> tmpMap= new HashMap<>();
		tmpMap.put("unit", "testSystem");
		List<ERoboPoint> points = sqlUnit.getByMap(ERoboPoint.class, tmpMap);
		System.out.println("DIRECT points= " + points);

		AttributeDescriptor<List> descriptorAllUnits = DefaultAttributeDescriptor.create(List.class, "units_all_desc");
		List<ERoboEntity<Long>> allUnitsList = (List<ERoboEntity<Long>>) sqlUnit.onGetAttribute(descriptorAllUnits);
		System.out.println("allUnitsList: " + allUnitsList);

		system.shutdown();
		System.out.println("System: State after shutdown:");
		System.out.println(SystemUtil.printStateReport(system));

	}

}
