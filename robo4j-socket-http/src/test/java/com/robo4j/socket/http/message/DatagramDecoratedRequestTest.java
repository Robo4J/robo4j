package com.robo4j.socket.http.message;

import com.robo4j.socket.http.util.DatagramBodyType;
import org.junit.Assert;
import org.junit.Test;

/**
 * @see  DatagramDecoratedRequest
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class DatagramDecoratedRequestTest {

    @Test
    public void datagramDecoratedRequestTest(){

        DatagramDenominator denominator = new DatagramDenominator(DatagramBodyType.JSON.getType(), "/units/stringConsumer");
        DatagramDecoratedRequest request = new DatagramDecoratedRequest(denominator);
        request.addMessage("{\"number\":22}".getBytes());

        byte[] requestBytes = request.toMessage();

        String requestMessage = new String(requestBytes);

        System.out.println("requestMessage: " + requestMessage);

        Assert.assertNotNull(requestBytes);
        Assert.assertTrue(requestBytes.length > 0);

    }
}
