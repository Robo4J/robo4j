package com.robo4j.socket.http.units;

import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.dto.ServerPathDTO;
import com.robo4j.socket.http.util.HttpPathUtils;
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
    public void serverConfigurationNullTest(){
        ServerPathDTO serverPathDTO = HttpPathUtils.readServerPathDTO(null);
        Assert.assertNull(serverPathDTO);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void serverConfigurationEmptyTest(){
        ServerPathDTO serverPathDTO = HttpPathUtils.readServerPathDTO(StringConstants.EMPTY);
        Assert.assertNull(serverPathDTO);
    }

    @Test
    public void serverConfigurationWithoutPropertiesDTOTest(){

        String configurationJson = "{\"roboUnit\":\"roboUnit1\",\"method\":\"GET\"}";
        ServerPathDTO serverPathDTO = HttpPathUtils.readServerPathDTO(configurationJson);

        Assert.assertTrue(serverPathDTO.getRoboUnit().equals("roboUnit1"));
        Assert.assertTrue(serverPathDTO.getMethod().equals(HttpMethod.GET));
        Assert.assertTrue(serverPathDTO.getFilters() == null);

        System.out.println("serverPathDTO: " + serverPathDTO);
    }

	@Test
	public void serverConfigurationWithPropertiesParsingDTOTest() {

		String configurationJson = "{\"roboUnit\":\"roboUnit1\",\"method\":\"GET\",\"filters\":[\"filter1\",\"filter2\"]}";
		ServerPathDTO serverPathDTO = HttpPathUtils.readServerPathDTO(configurationJson);

		Assert.assertTrue(serverPathDTO.getRoboUnit().equals("roboUnit1"));
		Assert.assertTrue(serverPathDTO.getMethod().equals(HttpMethod.GET));
		Assert.assertTrue(Arrays.equals(serverPathDTO.getFilters().toArray(),
				Arrays.asList("filter1", "filter2").toArray()));

		System.out.println("serverPathDTO: " + serverPathDTO);

	}

    @Test
    public void serverConfigurationNullPathTest(){
        ServerPathConfig serverPathConfig = HttpPathUtils.readHttpServerPathConfig(null);
        Assert.assertNotNull(serverPathConfig);
        Assert.assertTrue(serverPathConfig.asList().isEmpty());

    }

	@Test
    public void serverConfigurationEmptyPathTest(){
        ServerPathConfig serverPathConfig = HttpPathUtils.readHttpServerPathConfig(StringConstants.EMPTY);
        Assert.assertNotNull(serverPathConfig);
        Assert.assertTrue(serverPathConfig.asList().isEmpty());

    }

	@Test
    public void serverConfigurationWithMultiplePathsWithoutPropertiesTest(){
        String configurationJson = "[{\"roboUnit\":\"roboUnit1\",\"method\":\"GET\"}," +
                "{\"roboUnit\":\"roboUnit2\",\"method\":\"POST\"}]";

        List<ServerPathMethod> expectedPathList = Arrays.asList(new ServerPathMethod("roboUnit1", HttpMethod.GET),
                new ServerPathMethod("roboUnit2", HttpMethod.POST));

        ServerPathConfig serverPathConfig = HttpPathUtils.readHttpServerPathConfig(configurationJson);
        List<ServerPathMethod> pathList = serverPathConfig.asList();

        System.out.println("serverPathConfig: " + serverPathConfig);

        Assert.assertNotNull(serverPathConfig);
        Assert.assertTrue(pathList.size() == expectedPathList.size());
        Assert.assertTrue(Arrays.equals(pathList.toArray(), expectedPathList.toArray()));
    }

    @Test
    public void serverConfigurationWithMultiplePathsWithPropertiesTest(){
        String configurationJson = "[{\"roboUnit\":\"roboUnit1\",\"method\":\"GET\" , \"filters\":[\"filter1\",\"filter2\"]}," +
                "{\"roboUnit\":\"roboUnit2\",\"method\":\"POST\"}, {\"roboUnit\":\"roboUnit3\",\"method\":\"GET\",\"filters\":[]}]";

        List<ServerPathMethod> expectedPathList = Arrays.asList(new ServerPathMethod("roboUnit1", HttpMethod.GET,
                        Arrays.asList("filter1", "filter2")),
                new ServerPathMethod("roboUnit2", HttpMethod.POST),
                new ServerPathMethod("roboUnit3", HttpMethod.GET, Collections.emptyList()));

        ServerPathConfig serverPathConfig = HttpPathUtils.readHttpServerPathConfig(configurationJson);
        List<ServerPathMethod> pathList = serverPathConfig.asList();
        System.out.println("serverPathConfig: " + serverPathConfig);

        Assert.assertNotNull(serverPathConfig);
        Assert.assertTrue(pathList.size() == expectedPathList.size());
        Assert.assertTrue(Arrays.equals(pathList.toArray(), expectedPathList.toArray()));
    }

}
