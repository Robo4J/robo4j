package com.robo4j.socket.http.util;

import com.robo4j.socket.http.HttpMethod;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class HttpPathConfigJsonBuilderTests {

    @Test
    void simpleConfigurationTest(){

        String expectedJson = "[{\"roboUnit\":\"roboUnit1\",\"method\":\"GET\",\"callbacks\":[\"filter1\",\"filter2\"]}," +
                "{\"roboUnit\":\"roboUnit2\",\"method\":\"POST\",\"callbacks\":[]},{\"roboUnit\":\"roboUnit3\",\"method\":\"GET\",\"callbacks\":[]}]";

        HttpPathConfigJsonBuilder builder = HttpPathConfigJsonBuilder.Builder()
                .addPath("roboUnit1", HttpMethod.GET, Arrays.asList("filter1", "filter2"))
                .addPath("roboUnit2", HttpMethod.POST)
                .addPath("roboUnit3", HttpMethod.GET, Collections.emptyList());

        String resultJson = builder.build();

        System.out.println("resultJson: " + resultJson);
        assertEquals(expectedJson, resultJson);

    }

}
