package com.robo4j.socket.http.units;

import com.robo4j.RoboBuilder;
import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.dto.ClientPathDTO;
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
	public void serverDefaultContextBuilderTest() {

		ServerContext context = ServerContextBuilder.Builder().build(null);

		System.out.println("context:" + context);
		Assert.assertNotNull(context);
		Assert.assertTrue(!context.isEmpty());
		Assert.assertTrue(context.containsPath(Utf8Constant.UTF8_SOLIDUS));

	}

	@Test
	public void clientDefaultContextBuilderTest() {

		ClientContext context = ClientContextBuilder.Builder().build(null);

		System.out.println("context: " + context);
		Assert.assertNotNull(context);
		Assert.assertTrue(context.isEmpty());
	}

	@Test
	public void clientSimpleContextBuilderTest() throws Exception {

	    final String consumerName = "stringConsumer";

		RoboBuilder builderProducer = new RoboBuilder();
		InputStream clientConfigInputStream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("robo_client_context.xml");
		builderProducer.add(clientConfigInputStream);

		List<ClientPathDTO> paths = Collections
				.singletonList(new ClientPathDTO(StringConstants.EMPTY, HttpMethod.GET, Collections.singletonList(consumerName)));

		ClientContext context = ClientContextBuilder.Builder().addPaths(paths).build(builderProducer.build());

        System.out.println("context: " + context);
        Assert.assertNotNull(context);
		Assert.assertNotNull(context.getPathConfig(Utf8Constant.UTF8_SOLIDUS));
		Assert.assertTrue(!context.getPathConfig(Utf8Constant.UTF8_SOLIDUS).getCallbacks().isEmpty());
		Assert.assertTrue(context.getPathConfig(Utf8Constant.UTF8_SOLIDUS).getMethod().equals(HttpMethod.GET));
		Assert.assertTrue(context.getPathConfig(Utf8Constant.UTF8_SOLIDUS).getCallbacks().size() == 1);
		Assert.assertTrue(context.getPathConfig(Utf8Constant.UTF8_SOLIDUS).getCallbacks().get(0).equals(consumerName));
	}
}
