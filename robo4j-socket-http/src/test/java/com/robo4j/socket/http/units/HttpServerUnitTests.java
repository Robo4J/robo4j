package com.robo4j.socket.http.units;

import static com.robo4j.socket.http.units.RoboHttpPingPongTest.PACKAGE_CODECS;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_CODEC_PACKAGES;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_SOCKET_PORT;

import org.junit.Assert;
import org.junit.Test;

import com.robo4j.LifecycleState;
import com.robo4j.RoboBuilder;
import com.robo4j.RoboBuilderException;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.configuration.Configuration;
import com.robo4j.configuration.ConfigurationBuilder;
import com.robo4j.util.SystemUtil;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class HttpServerUnitTests {
	private static final int PORT = 9000;
	private static final String ID_HTTP_SERVER = "empty_server";

	@Test(expected = RoboBuilderException.class)
	public void httpServerUnitNoCodecsPackageTest() throws Exception {
		RoboBuilder builder = new RoboBuilder();

		Configuration config = new ConfigurationBuilder().addInteger(PROPERTY_SOCKET_PORT, PORT).build();
		builder.add(HttpServerUnit.class, config, ID_HTTP_SERVER);
		RoboContext system = builder.build();

		system.start();
		System.out.println("system: State after start:");
		System.out.println(SystemUtil.printStateReport(system));
		RoboReference<HttpServerUnit> systemReference = system.getReference(ID_HTTP_SERVER);
		system.shutdown();
		System.out.println("system: State after shutdown:");
		System.out.println(SystemUtil.printStateReport(system));
		Assert.assertTrue(systemReference.getState().equals(LifecycleState.SHUTDOWN));

	}

	@Test
	public void httpServerUnitNoPathTest() throws Exception {
		RoboBuilder builder = new RoboBuilder();

		Configuration config = new ConfigurationBuilder().addInteger(PROPERTY_SOCKET_PORT, PORT)
				.addString(PROPERTY_CODEC_PACKAGES, PACKAGE_CODECS).build();
		builder.add(HttpServerUnit.class, config, ID_HTTP_SERVER);
		RoboContext system = builder.build();

		system.start();
		System.out.println("system: State after start:");
		System.out.println(SystemUtil.printStateReport(system));
		RoboReference<HttpServerUnit> systemReference = system.getReference(ID_HTTP_SERVER);
		system.shutdown();
		System.out.println("system: State after shutdown:");
		System.out.println(SystemUtil.printStateReport(system));
		Assert.assertTrue(systemReference.getState().equals(LifecycleState.SHUTDOWN));
	}

}
