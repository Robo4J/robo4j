package com.robo4j.socket.http.units;

import com.robo4j.RoboBuilder;
import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.dto.HttpPathMethodDTO;
import com.robo4j.socket.http.units.test.StringConsumer;
import com.robo4j.socket.http.util.HttpPathUtils;
import com.robo4j.util.StringConstants;
import com.robo4j.util.Utf8Constant;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 *
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class HttpContextTests {

	@Test
	public void serverNotInitiatedContextTest() {

		ServerContext context = new ServerContext();
		System.out.println("context:" + context);
		Assert.assertNotNull(context);
		Assert.assertTrue(context.isEmpty());

	}


	@Test
	public void serverDefaultContextTest() {

		ServerContext context = new ServerContext();
		HttpPathUtils.updateHttpServerContextPaths(null, context, Collections.emptyList());

		System.out.println("context:" + context);
		Assert.assertNotNull(context);
		Assert.assertTrue(!context.isEmpty());
		Assert.assertTrue(context.containsPath(new PathHttpMethod(Utf8Constant.UTF8_SOLIDUS, HttpMethod.GET)));

	}

	@Test
	public void clientDefaultContextTest() {

		ClientContext context = new ClientContext();

		System.out.println("context: " + context);
		Assert.assertNotNull(context);
		Assert.assertTrue(context.isEmpty());
	}

	@Test
	public void clientSimpleContextTest() throws Exception {


		RoboBuilder builderProducer = new RoboBuilder();
		InputStream contextIS = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("robo_client_context.xml");
		builderProducer.add(contextIS);

		List<HttpPathMethodDTO> paths = Collections
				.singletonList(new HttpPathMethodDTO(StringConstants.EMPTY, HttpMethod.GET, Collections.singletonList(StringConsumer.NAME)));

		ClientContext context = new ClientContext();
		HttpPathUtils.updateHttpClientContextPaths(context, paths);


		PathHttpMethod basicGet = new PathHttpMethod(Utf8Constant.UTF8_SOLIDUS, HttpMethod.GET);

        System.out.println("context: " + context);
        Assert.assertNotNull(context);
		Assert.assertNotNull(context.getPathConfig(basicGet));
		Assert.assertTrue(!context.getPathConfig(basicGet).getCallbacks().isEmpty());
		Assert.assertTrue(context.getPathConfig(basicGet).getMethod().equals(HttpMethod.GET));
		Assert.assertTrue(context.getPathConfig(basicGet).getCallbacks().size() == 1);
		Assert.assertTrue(context.getPathConfig(basicGet).getCallbacks().get(0).equals(StringConsumer.NAME));
	}
}
