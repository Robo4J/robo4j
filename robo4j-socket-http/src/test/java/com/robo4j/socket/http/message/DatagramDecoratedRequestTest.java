package com.robo4j.socket.http.message;

import com.robo4j.socket.http.util.DatagramBodyType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @see DatagramDecoratedRequest
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class DatagramDecoratedRequestTest {

	@Test
	void datagramDecoratedRequestTest() {

		DatagramDenominator denominator = new DatagramDenominator(DatagramBodyType.JSON.getType(),
				"/units/stringConsumer");
		DatagramDecoratedRequest request = new DatagramDecoratedRequest(denominator);
		request.addMessage("{\"number\":22}".getBytes());

		byte[] requestBytes = request.toMessage();

		String requestMessage = new String(requestBytes);

		System.out.println("requestMessage: " + requestMessage);

		assertNotNull(requestBytes);
		assertTrue(requestBytes.length > 0);

	}
}
