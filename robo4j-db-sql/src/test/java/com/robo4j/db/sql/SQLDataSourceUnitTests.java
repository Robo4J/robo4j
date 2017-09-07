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

import com.robo4j.core.AttributeDescriptor;
import com.robo4j.core.DefaultAttributeDescriptor;
import com.robo4j.core.RoboBuilder;
import com.robo4j.core.RoboBuilderException;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboReference;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.configuration.ConfigurationFactory;
import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.core.util.SystemUtil;
import com.robo4j.db.sql.dto.ERoboRequest;
import com.robo4j.db.sql.model.ERoboEntity;
import com.robo4j.db.sql.model.ERoboPoint;
import com.robo4j.db.sql.model.ERoboUnit;
import com.robo4j.db.sql.support.RoboRequestType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
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

	private static final int DEFAULT_LIMIT = 2;
	private static final String DEFAULT_SORTED = "asc";
	private static final String UNIT_SYSTEM_1_NAME = "system1";
	private static final String UNIT_SYSTEM_2_NAME = "system2";
	private static final String DB_SQL_UNIT = "dbSQLUnit";
	private static final String DB_SIMPLE_RECEIVER = "simpleReceiver";
	private RoboContext system;


	@Before
	public void setUp(){
		system = prepareSystemWithSQLUnits(DEFAULT_SORTED, DEFAULT_LIMIT);
	}

	@After
	public void clean() throws Exception{
		Thread.sleep(200);
		system.shutdown();
		System.out.println("SQL System: State after shutdown:");
		System.out.println(SystemUtil.printStateReport(system));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getAllStoredERoboUnits() throws Exception {

		final RoboReference<ERoboRequest> sqlDataSourceUnit = system.getReference(DB_SQL_UNIT);

		AttributeDescriptor<List> descriptor1 = DefaultAttributeDescriptor.create(List.class, "all");
		Future<List> list1Future = sqlDataSourceUnit.getAttribute(descriptor1);

		List<ERoboEntity<Long>> list1 = (List<ERoboEntity<Long>>) list1Future.get();

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

	}

	@Test
	public void testRetriveUnitsFromDB() throws Exception {
		final RoboReference<ERoboRequest> sqlDataSourceUnit = system.getReference(DB_SQL_UNIT);
		Map<String, Object> map = new HashMap<>();
		map.put("likeUid", "system");

		final ERoboRequest readRequest = new ERoboRequest(RoboRequestType.READ);
		readRequest.addData(ERoboEntity.class, map);
		sqlDataSourceUnit.sendMessage(readRequest);


	}

	// Private Methods
	private List<ERoboPoint> getRoboPoint(ERoboUnit unit, int number) {
		//@formatter:off
		return IntStream.range(0, number).mapToObj(i -> {
							ERoboPoint point = new ERoboPoint();
							point.setUnit(unit);
							point.setValueType("magicType: " + i);
							point.setValues("value: " + i);
							return point;})
						.collect(Collectors.toList());
		//@formatter:on
	}

	private RoboContext prepareSystemWithSQLUnits(String sorted, int limit) {
		try {

			RoboBuilder builder = new RoboBuilder();
			Configuration config = ConfigurationFactory.createEmptyConfiguration();

			config.setString("sourceType", "h2");
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
			ERoboUnit1.setUid("system1");
			ERoboUnit1.setConfig(DB_SQL_UNIT.concat(",").concat("httpClient"));


			ERoboUnit ERoboUnit2 = new ERoboUnit();
			ERoboUnit2.setUid("system2");
			ERoboUnit2.setConfig("httpServer");

			// store message
			final RoboReference<ERoboRequest> dbSqlReference = system.getReference(DB_SQL_UNIT);
			final ERoboRequest storeRequest = new ERoboRequest(RoboRequestType.SAVE);
			storeRequest.addData(ERoboEntity.class, Arrays.asList(ERoboUnit1, ERoboUnit2));
			dbSqlReference.sendMessage(storeRequest);

			return system;
		} catch (RoboBuilderException e){
			SimpleLoggingUtil.error(getClass(), "problem",e);
			throw new RuntimeException("problem");
		}
	}
}
