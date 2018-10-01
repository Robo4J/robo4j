/*
 * Copyright (c) 2014, 2018, Marcus Hirt, Miroslav Wengner
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

package com.robo4j.socket.http.units;

import com.robo4j.RoboBuilder;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.configuration.Configuration;
import com.robo4j.configuration.ConfigurationBuilder;
import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.HttpVersion;
import com.robo4j.socket.http.message.HttpDecoratedRequest;
import com.robo4j.socket.http.message.HttpRequestDenominator;
import com.robo4j.socket.http.units.test.HttpOneAttributeGetController;
import com.robo4j.socket.http.units.test.HttpTwoAttributesGetController;
import com.robo4j.socket.http.units.test.StringConsumer;
import com.robo4j.socket.http.util.HttpPathConfigJsonBuilder;
import com.robo4j.util.SystemUtil;
import org.junit.Ignore;
import org.junit.Test;

import static com.robo4j.socket.http.units.RoboHttpPingPongTest.HOST_SYSTEM;
import static com.robo4j.socket.http.units.RoboHttpPingPongTest.PACKAGE_CODECS;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_HOST;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_SOCKET_PORT;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_UNIT_PATHS_CONFIG;

/**
 * RoboHttpUnitGetTest should test Http get requests
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class RoboHttpUnitGetTest {

	private static final int SERVER_PORT = 8061;
	private static final String UNIT_ID_HTTP_CLIENT = "http_client";


	@Test
//	@Ignore
	public void systemWithHttpServerOnlyTest() throws Exception {
		final String httpServerUnitName = "http_server";
		final HttpPathConfigJsonBuilder pathBuilder = HttpPathConfigJsonBuilder.Builder().addPath(httpServerUnitName, HttpMethod.GET);

		//@formatter:off
		Configuration systemConfiguration = new ConfigurationBuilder()
				.addInteger("poolSizeScheduler", 3)
				.addInteger("poolSizeWorker", 2)
				.addInteger("poolSizeBlocking", 2)
				.build();
		RoboBuilder builder = new RoboBuilder("roboSystem1", systemConfiguration);
		//@formatter:on

		//@formatter:off
		Configuration config = new ConfigurationBuilder()
				.addInteger(PROPERTY_SOCKET_PORT, SERVER_PORT)
				.addString("packages", "com.robo4j.socket.http.codec")
				.addString(PROPERTY_UNIT_PATHS_CONFIG, pathBuilder.build())
				.build();
		//@formatter:on
		builder.add(HttpServerUnit.class, config, httpServerUnitName);
		RoboContext system = builder.build();

		system.start();
		System.out.println("systemPong: State after start:");
		System.out.println(SystemUtil.printStateReport(system));
		System.out.println("Press Key...");
		System.in.read();
		system.shutdown();
	}

	@Test
	@Ignore
	public void oneKnownAttributeTest() throws Exception {
		final HttpPathConfigJsonBuilder pathBuilder = HttpPathConfigJsonBuilder.Builder().addPath("controller", HttpMethod.GET);

		//@formatter:off
		Configuration systemConfiguration = new ConfigurationBuilder()
				.addInteger("poolSizeScheduler", 4)
				.addInteger("poolSizeWorker", 2)
				.addInteger("poolSizeBlocking", 3)
				.build();
		RoboBuilder builder = new RoboBuilder("roboSystem1", systemConfiguration);
		//@formatter:on

		//@formatter:off
		Configuration config = new ConfigurationBuilder()
				.addInteger(PROPERTY_SOCKET_PORT, SERVER_PORT)
				.addString("packages", PACKAGE_CODECS)
				.addString(PROPERTY_UNIT_PATHS_CONFIG, pathBuilder.build())
				.build();
		//@formatter:on
		builder.add(HttpServerUnit.class, config, "http_server");

		config = new ConfigurationBuilder().addInteger(StringConsumer.PROP_TOTAL_NUMBER_MESSAGES, 1).build();
		builder.add(StringConsumer.class, config, "request_consumer");

		config = new ConfigurationBuilder().addString("target", "request_consumer").build();
		builder.add(HttpOneAttributeGetController.class, config, "controller");

		RoboContext system = builder.build();

		system.start();
		System.out.println("systemPong: State after start:");
		System.out.println(SystemUtil.printStateReport(system));
		System.out.println("Press Key...");
		System.in.read();
		system.shutdown();

	}

	@Test
	@Ignore
	public void twoKnownAttributesTest() throws Exception {
		RoboContext systemGetProvider = twoAttributesSystem();
		RoboContext systemGetAccessor = attributeRequestSystem();

		systemGetProvider.start();
		System.out.println("systemGetProvider: State after start:");
		System.out.println(SystemUtil.printStateReport(systemGetProvider));
		systemGetAccessor.start();
		System.out.println("systemGetAccessor: State after start:");
		System.out.println(SystemUtil.printStateReport(systemGetAccessor));

		RoboReference<HttpDecoratedRequest> httpClient = systemGetAccessor.getReference(UNIT_ID_HTTP_CLIENT);

		HttpRequestDenominator denominator = new HttpRequestDenominator(HttpMethod.GET, "/units/controller?attributes=number",
				HttpVersion.HTTP_1_1);
		HttpDecoratedRequest request = new HttpDecoratedRequest(denominator);
		request.addCallback(StringConsumer.NAME);
		httpClient.sendMessage(request);

		System.out.println("Press Key...");
		System.in.read();
		systemGetProvider.shutdown();
	}

	public RoboContext attributeRequestSystem() throws Exception {
		RoboBuilder builder = new RoboBuilder();

		Configuration config = new ConfigurationBuilder().addString(PROPERTY_HOST, HOST_SYSTEM)
				.addInteger(PROPERTY_SOCKET_PORT, SERVER_PORT).build();
		builder.add(HttpClientUnit.class, config, UNIT_ID_HTTP_CLIENT);
		builder.add(StringConsumer.class, StringConsumer.NAME);
		return builder.build();
	}

	private RoboContext twoAttributesSystem() throws Exception {
		Configuration systemConfiguration = new ConfigurationBuilder().addInteger("poolSizeScheduler", 4).addInteger("poolSizeWorker", 2)
				.addInteger("poolSizeBlocking", 3).build();
		RoboBuilder builder = new RoboBuilder(systemConfiguration);

		final HttpPathConfigJsonBuilder pathBuilder = HttpPathConfigJsonBuilder.Builder().addPath("controller", HttpMethod.GET);
		Configuration config = new ConfigurationBuilder().addInteger(PROPERTY_SOCKET_PORT, SERVER_PORT)
				.addString("packages", PACKAGE_CODECS).addString(PROPERTY_UNIT_PATHS_CONFIG, pathBuilder.build()).build();
		builder.add(HttpServerUnit.class, config, "http_server");

		config = new ConfigurationBuilder().addInteger(StringConsumer.PROP_TOTAL_NUMBER_MESSAGES, 1).build();
		builder.add(StringConsumer.class, config, "request_consumer");

		config = new ConfigurationBuilder().addString("target", "request_consumer").build();
		builder.add(HttpTwoAttributesGetController.class, config, "controller");
		return builder.build();
	}
}
