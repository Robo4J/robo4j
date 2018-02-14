package com.robo4j.socket.http.units;

import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.dto.ServerUnitPathDTO;
import com.robo4j.socket.http.util.HttpPathUtils;
import com.robo4j.socket.http.util.JsonUtil;
import com.robo4j.util.StringConstants;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * test for Http Server Unit configuration
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class HttpServerConfigTests {

	@Test(expected = NullPointerException.class)
	public void serverConfigurationNullTest() {
		ServerUnitPathDTO serverUnitPathDTO = HttpPathUtils.readServerPathDTO(null);
		Assert.assertNull(serverUnitPathDTO);
	}

	@Test(expected = ArrayIndexOutOfBoundsException.class)
	public void serverConfigurationEmptyTest() {
		ServerUnitPathDTO serverUnitPathDTO = HttpPathUtils.readServerPathDTO(StringConstants.EMPTY);
		Assert.assertNull(serverUnitPathDTO);
	}

	@Test
	public void serverConfigurationWithoutPropertiesDTOTest() {

		String configurationJson = "{\"roboUnit\":\"roboUnit1\",\"method\":\"GET\"}";
		ServerUnitPathDTO serverUnitPathDTO = HttpPathUtils.readServerPathDTO(configurationJson);

		Assert.assertTrue(serverUnitPathDTO.getRoboUnit().equals("roboUnit1"));
		Assert.assertTrue(serverUnitPathDTO.getMethod().equals(HttpMethod.GET));
		Assert.assertTrue(serverUnitPathDTO.getFilters() == null);

		System.out.println("serverUnitPathDTO: " + serverUnitPathDTO);
	}

	@Test
	public void serverConfigurationWithPropertiesParsingDTOTest() {

		String configurationJson = "{\"roboUnit\":\"roboUnit1\",\"method\":\"GET\",\"filters\":[\"filter1\",\"filter2\"]}";
		ServerUnitPathDTO serverUnitPathDTO = HttpPathUtils.readServerPathDTO(configurationJson);

		Assert.assertTrue(serverUnitPathDTO.getRoboUnit().equals("roboUnit1"));
		Assert.assertTrue(serverUnitPathDTO.getMethod().equals(HttpMethod.GET));
		Assert.assertTrue(
				Arrays.equals(serverUnitPathDTO.getFilters().toArray(), Arrays.asList("filter1", "filter2").toArray()));

		System.out.println("serverUnitPathDTO: " + serverUnitPathDTO);

	}

	@Test
	public void serverConfigurationNullPathTest() {
		List<ServerUnitPathDTO> paths = JsonUtil.readPathConfig(ServerUnitPathDTO.class, null);
		Assert.assertNotNull(paths);
		Assert.assertTrue(paths.isEmpty());

	}

	@Test
	public void serverConfigurationEmptyPathTest() {
		List<ServerUnitPathDTO> paths = JsonUtil.readPathConfig(ServerUnitPathDTO.class, StringConstants.EMPTY);
		Assert.assertNotNull(paths);
		Assert.assertTrue(paths.isEmpty());

	}

	@Test
	public void serverConfigurationWithMultiplePathsWithoutPropertiesTest() {
		String configurationJson = "[{\"roboUnit\":\"roboUnit1\",\"method\":\"GET\"},"
				+ "{\"roboUnit\":\"roboUnit2\",\"method\":\"POST\"}]";

		List<ServerUnitPathDTO> expectedPathList = Arrays.asList(new ServerUnitPathDTO("roboUnit1", HttpMethod.GET),
				new ServerUnitPathDTO("roboUnit2", HttpMethod.POST));

		List<ServerUnitPathDTO> paths = JsonUtil.readPathConfig(ServerUnitPathDTO.class, configurationJson);

		Assert.assertTrue(paths.size() == expectedPathList.size());
		Assert.assertTrue(Arrays.equals(paths.toArray(), expectedPathList.toArray()));
	}

	@Test
	public void serverConfigurationWithMultiplePathsWithPropertiesTest() {
		String configurationJson = "[{\"roboUnit\":\"roboUnit1\",\"method\":\"GET\" , \"filters\":[\"filter1\",\"filter2\"]},"
				+ "{\"roboUnit\":\"roboUnit2\",\"method\":\"POST\"}, {\"roboUnit\":\"roboUnit3\",\"method\":\"GET\",\"filters\":[]}]";

		List<ServerUnitPathDTO> expectedPathList = Arrays.asList(
				new ServerUnitPathDTO("roboUnit1", HttpMethod.GET, Arrays.asList("filter1", "filter2")),
				new ServerUnitPathDTO("roboUnit2", HttpMethod.POST),
				new ServerUnitPathDTO("roboUnit3", HttpMethod.GET, Collections.emptyList()));

		List<ServerUnitPathDTO> paths = JsonUtil.readPathConfig(ServerUnitPathDTO.class, configurationJson);
		System.out.println("paths: " + paths);

		Assert.assertNotNull(paths);
		Assert.assertTrue(paths.size() == expectedPathList.size());
		Assert.assertTrue(Arrays.equals(paths.toArray(), expectedPathList.toArray()));
	}

}
