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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.db.sql;

import com.robo4j.core.DefaultAttributeDescriptor;
import com.robo4j.core.RoboBuilder;
import com.robo4j.core.RoboBuilderException;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboReference;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.configuration.ConfigurationFactory;
import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.core.util.SystemUtil;
import com.robo4j.db.sql.dto.ERoboDbContract;
import com.robo4j.db.sql.model.ERoboEntity;
import com.robo4j.db.sql.model.ERoboPoint;
import com.robo4j.db.sql.model.ERoboUnit;
import com.robo4j.db.sql.support.RoboRequestType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * SQL database focused tests
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class SQLDataSourceUnitTests {

	private static final int WAIT_TIME = 400;
	private static final int DEFAULT_LIMIT = 2;
	private static final String DEFAULT_SORTED = "asc";
	private static final String UNIT_SYSTEM_1_NAME = "system1";
	private static final String UNIT_SYSTEM_2_NAME = "system2";
	private static final String DB_SQL_UNIT = "dbSQLUnit";
	private static final String DB_SIMPLE_RECEIVER = "simpleReceiver";
	private RoboContext system;

	@Before
	public void setUp() {
		system = prepareSystemWithERoboUnits(DEFAULT_SORTED, DEFAULT_LIMIT);
	}

	@After
	public void clean() throws Exception {
		Thread.sleep(WAIT_TIME);
		system.shutdown();
		System.out.println("SQL System: State after shutdown:");
		System.out.println(SystemUtil.printStateReport(system));
	}

	@Test
	public void retrieveAllERoboUnitsFromDB() throws Exception {
		final RoboReference<ERoboDbContract> sqlDataSourceUnit = system.getReference(DB_SQL_UNIT);

		final ERoboDbContract readRequest = new ERoboDbContract(RoboRequestType.READ);
		readRequest.addData(ERoboUnit.class, Collections.EMPTY_MAP);
		sqlDataSourceUnit.sendMessage(readRequest);

		Thread.sleep(WAIT_TIME);
		final RoboReference<SQLSimpleReceiverUnit> receiverUnit = system.getReference(DB_SIMPLE_RECEIVER);
		Future<ERoboDbContract> responseFuture = receiverUnit.getAttribute(
				DefaultAttributeDescriptor.create(ERoboDbContract.class, SQLSimpleReceiverUnit.ATTRIBUTE_SQL_RESPONSE));
		ERoboDbContract response = responseFuture.get();

		Map<Class<?>, Object> responseMap = response.getData();
		List<ERoboUnit> responseUnitList = (List<ERoboUnit>) responseMap.get(ERoboUnit.class);
		Assert.assertTrue(response.getType().equals(RoboRequestType.READ));
		Assert.assertNotNull(responseMap.get(ERoboUnit.class));
		Assert.assertNotNull(responseUnitList);
		Assert.assertTrue(responseUnitList.size() == 2);
		Assert.assertTrue(responseUnitList.get(0).getUid().equals(UNIT_SYSTEM_1_NAME));
		Assert.assertTrue(responseUnitList.get(1).getUid().equals(UNIT_SYSTEM_2_NAME));
	}

	@Test
	public void retrieveSpecificUnitsFromDB() throws Exception {
		final RoboReference<ERoboDbContract> sqlDataSourceUnit = system.getReference(DB_SQL_UNIT);
		Map<String, Object> map = new HashMap<>();
		map.put("likeUid", "system1");

		final ERoboDbContract readRequest = new ERoboDbContract(RoboRequestType.READ);
		readRequest.addData(ERoboUnit.class, map);
		sqlDataSourceUnit.sendMessage(readRequest);

		Thread.sleep(WAIT_TIME);
		final RoboReference<SQLSimpleReceiverUnit> receiverUnit = system.getReference(DB_SIMPLE_RECEIVER);
		Future<ERoboDbContract> responseFuture = receiverUnit.getAttribute(
				DefaultAttributeDescriptor.create(ERoboDbContract.class, SQLSimpleReceiverUnit.ATTRIBUTE_SQL_RESPONSE));
		ERoboDbContract response = responseFuture.get();

		Map<Class<?>, Object> responseMap = response.getData();
		List<ERoboUnit> responseUnitList = (List<ERoboUnit>) responseMap.get(ERoboUnit.class);

		Assert.assertTrue(response.getType().equals(RoboRequestType.READ));
		Assert.assertNotNull(responseMap.get(ERoboUnit.class));
		Assert.assertNotNull(responseUnitList);
		Assert.assertTrue(responseUnitList.size() == 1);
		Assert.assertTrue(responseUnitList.get(0).getUid().equals(UNIT_SYSTEM_1_NAME));

	}

	@Test
	public void storePointsForERoboUnits() throws Exception {
		final RoboReference<ERoboDbContract> sqlDataSourceUnit = system.getReference(DB_SQL_UNIT);
		Map<String, Object> map = new HashMap<>();
		map.put("uid", UNIT_SYSTEM_1_NAME);

		final ERoboDbContract readRequest = new ERoboDbContract(RoboRequestType.READ);
		readRequest.addData(ERoboUnit.class, map);
		sqlDataSourceUnit.sendMessage(readRequest);

		Thread.sleep(WAIT_TIME);
		final RoboReference<SQLSimpleReceiverUnit> receiverUnit = system.getReference(DB_SIMPLE_RECEIVER);
		Future<ERoboDbContract> responseFuture = receiverUnit.getAttribute(
				DefaultAttributeDescriptor.create(ERoboDbContract.class, SQLSimpleReceiverUnit.ATTRIBUTE_SQL_RESPONSE));
		ERoboDbContract response = responseFuture.get();

		Map<Class<?>, Object> responseMap = response.getData();
		List<ERoboUnit> responseUnitList = (List<ERoboUnit>) responseMap.get(ERoboUnit.class);

		ERoboUnit systemUnit1 = responseUnitList.get(0);
		systemUnit1.addPoints(getRoboPoint(null, DEFAULT_LIMIT));

		final ERoboDbContract saveRequest = new ERoboDbContract(RoboRequestType.SAVE);
		saveRequest.addData(ERoboUnit.class, systemUnit1);
		sqlDataSourceUnit.sendMessage(saveRequest);
		Thread.sleep(WAIT_TIME);

		responseFuture = receiverUnit.getAttribute(
				DefaultAttributeDescriptor.create(ERoboDbContract.class, SQLSimpleReceiverUnit.ATTRIBUTE_SQL_RESPONSE));
		response = responseFuture.get();
		responseMap = response.getData();
		responseUnitList = (List<ERoboUnit>) responseMap.get(ERoboUnit.class);
		ERoboUnit system1WithPoint = responseUnitList.get(0);

		System.out.println("After Points Storage: " + system1WithPoint);
		Assert.assertTrue(response.getType().equals(RoboRequestType.READ));
		Assert.assertNotNull(responseMap.get(ERoboUnit.class));
		Assert.assertNotNull(responseUnitList);
		Assert.assertTrue(responseUnitList.size() == 1);
		Assert.assertTrue(system1WithPoint.getUid().equals(UNIT_SYSTEM_1_NAME));
		Assert.assertTrue(system1WithPoint.getPoints().size() == DEFAULT_LIMIT);
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

	private RoboContext prepareSystemWithERoboUnits(String sorted, int limit) {
		try {

			RoboBuilder builder = new RoboBuilder();
			Configuration config = ConfigurationFactory.createEmptyConfiguration();

//			config.setString("sourceType", "h2");
			config.setString("sourceType", "postgresql");
			config.setString("hibernate.hbm2ddl.auto","create");
			config.setString("hibernate.connection.url", "jdbc:postgresql://192.168.178.42:5433/robo4j1");
			config.setString("packages", "com.robo4j.db.sql.model");
			config.setInteger("limit", limit);
			config.setString("sorted", sorted);
			config.setString("receiver", DB_SIMPLE_RECEIVER);
			config.setString("targetUnit", UNIT_SYSTEM_2_NAME);

			builder.add(SQLDataSourceUnit.class, config, DB_SQL_UNIT);
			builder.add(SQLSimpleReceiverUnit.class, DB_SIMPLE_RECEIVER);

			RoboContext system = builder.build();

			System.out.println("systemPong: State before start:");
			System.out.println(SystemUtil.printStateReport(system));

			system.start();

			System.out.println("systemPong: State after start:");
			System.out.println(SystemUtil.printStateReport(system));

			ERoboUnit ERoboUnit1 = new ERoboUnit();
			ERoboUnit1.setUid(UNIT_SYSTEM_1_NAME);
			ERoboUnit1.setConfig(DB_SQL_UNIT.concat(",").concat("httpClient"));

			ERoboUnit ERoboUnit2 = new ERoboUnit();
			ERoboUnit2.setUid(UNIT_SYSTEM_2_NAME);
			ERoboUnit2.setConfig("httpServer");

			// store message
			final RoboReference<ERoboDbContract> dbSqlReference = system.getReference(DB_SQL_UNIT);
			final ERoboDbContract storeRequest = new ERoboDbContract(RoboRequestType.SAVE);
			storeRequest.addData(ERoboEntity.class, Arrays.asList(ERoboUnit1, ERoboUnit2));
			dbSqlReference.sendMessage(storeRequest);

			return system;
		} catch (RoboBuilderException e) {
			SimpleLoggingUtil.error(getClass(), "problem", e);
			throw new RuntimeException("problem");
		}
	}
}
