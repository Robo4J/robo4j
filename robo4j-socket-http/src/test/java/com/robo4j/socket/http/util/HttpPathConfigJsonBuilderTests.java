/*
 * Copyright (c) 2014, 2019, Marcus Hirt, Miroslav Wengner
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
