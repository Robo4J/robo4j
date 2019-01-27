package com.robo4j.socket.http.units;

import com.robo4j.RoboBuilder;
import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.dto.HttpPathMethodDTO;
import com.robo4j.socket.http.units.test.StringConsumer;
import com.robo4j.socket.http.util.HttpPathUtils;
import com.robo4j.util.StringConstants;
import com.robo4j.util.Utf8Constant;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class HttpContextTests {

	@Test
	void serverNotInitiatedContextTest() {

		ServerContext context = new ServerContext();
		System.out.println("context:" + context);
		assertNotNull(context);
		assertTrue(context.isEmpty());

	}

	@Test
	void serverDefaultContextTest() {

		ServerContext context = new ServerContext();
		HttpPathUtils.updateHttpServerContextPaths(null, context, Collections.emptyList());

		System.out.println("context:" + context);
		assertNotNull(context);
		assertTrue(!context.isEmpty());
		assertTrue(context.containsPath(new PathHttpMethod(Utf8Constant.UTF8_SOLIDUS, HttpMethod.GET)));

	}

	@Test
	void clientDefaultContextTest() {

		ClientContext context = new ClientContext();

		System.out.println("context: " + context);
		assertNotNull(context);
		assertTrue(context.isEmpty());
	}

	@Test
	void clientSimpleContextTest() throws Exception {

		RoboBuilder builderProducer = new RoboBuilder();
		InputStream contextIS = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("robo_client_context.xml");
		builderProducer.add(contextIS);

		List<HttpPathMethodDTO> paths = Collections.singletonList(new HttpPathMethodDTO(StringConstants.EMPTY,
				HttpMethod.GET, Collections.singletonList(StringConsumer.NAME)));

		ClientContext context = new ClientContext();
		HttpPathUtils.updateHttpClientContextPaths(context, paths);

		PathHttpMethod basicGet = new PathHttpMethod(Utf8Constant.UTF8_SOLIDUS, HttpMethod.GET);

		System.out.println("context: " + context);
		assertNotNull(context);
		assertNotNull(context.getPathConfig(basicGet));
		assertTrue(!context.getPathConfig(basicGet).getCallbacks().isEmpty());
		assertEquals(HttpMethod.GET, context.getPathConfig(basicGet).getMethod());
		assertEquals(1, context.getPathConfig(basicGet).getCallbacks().size());
		assertEquals(StringConsumer.NAME, context.getPathConfig(basicGet).getCallbacks().get(0));
	}
}
