/*
 * Copyright (c) 2014, 2024, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.socket.http.test.message;

import com.robo4j.socket.http.message.DatagramDecoratedRequest;
import com.robo4j.socket.http.message.DatagramDenominator;
import com.robo4j.socket.http.util.DatagramBodyType;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 * @see DatagramDecoratedRequest
 */
class DatagramDecoratedRequestTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatagramDecoratedRequestTest.class);

    @Test
    void datagramDecoratedRequestTest() {

        DatagramDenominator denominator = new DatagramDenominator(DatagramBodyType.JSON.getType(),
                "/units/stringConsumer");
        DatagramDecoratedRequest request = new DatagramDecoratedRequest(denominator);
        request.addMessage("{\"number\":22}".getBytes());

        byte[] requestBytes = request.toMessage();

        String requestMessage = new String(requestBytes);

        LOGGER.debug("requestMessage:{}", requestMessage);

        assertNotNull(requestBytes);
        assertTrue(requestBytes.length > 0);

    }
}
