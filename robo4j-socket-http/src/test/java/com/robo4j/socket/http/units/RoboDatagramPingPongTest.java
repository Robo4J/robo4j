package com.robo4j.socket.http.units;

import com.robo4j.RoboBuilder;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.configuration.Configuration;
import com.robo4j.configuration.ConfigurationFactory;
import com.robo4j.socket.http.message.DatagramDecoratedRequest;
import com.robo4j.socket.http.message.DatagramDenominator;
import com.robo4j.socket.http.units.test.StringConsumer;
import com.robo4j.socket.http.util.DatagramBodyType;
import com.robo4j.socket.http.util.RoboHttpUtils;
import com.robo4j.util.SystemUtil;
import org.junit.Test;

import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_HOST;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_CODEC_PACKAGES;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_SOCKET_PORT;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_UNIT_PATHS_CONFIG;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class RoboDatagramPingPongTest {

    private static final String PACKAGE_CODECS = "com.robo4j.socket.http.units.test.codec";
    private static final String UDP_CLIENT = "udp_client";

    @Test
	public void datagramPingPongTest() throws Exception {
        RoboContext pongSystem = configurePongSystem();
        RoboContext pingSystem = configurePingSystem();

        pongSystem.start();
        pingSystem.start();
        System.out.println("UDP pongSystem: State after start:");
        System.out.println(SystemUtil.printStateReport(pongSystem));
        System.out.println("UDP pingSystem: State after start:");
        System.out.println(SystemUtil.printStateReport(pingSystem));

        RoboReference<DatagramDecoratedRequest> udpClient = pingSystem.getReference(UDP_CLIENT);
        for(int i=0; i < 10; i++){
            DatagramDenominator denominator = new DatagramDenominator(DatagramBodyType.JSON.getType(), "/units/stringConsumer");
            DatagramDecoratedRequest request = new DatagramDecoratedRequest(denominator);
            String message = "{\"message\": \"Hello i:"+ i +"\"}";
            request.addMessage(message.getBytes());
            udpClient.sendMessage(request);
        }

        Thread.sleep(5000);
        pingSystem.shutdown();
        pongSystem.shutdown();

        System.out.println("UDP pongSystem: State after shutdown:");
        System.out.println(SystemUtil.printStateReport(pongSystem));
        System.out.println("UDP pingSystem: State after shutdown:");
        System.out.println(SystemUtil.printStateReport(pingSystem));

	}

	private RoboContext configurePingSystem() throws Exception {
		RoboBuilder builder = new RoboBuilder();

		Configuration config = ConfigurationFactory.createEmptyConfiguration();
		config.setString(PROPERTY_CODEC_PACKAGES, PACKAGE_CODECS);
        config.setString(PROPERTY_HOST, "localhost");
        config.setInteger(PROPERTY_SOCKET_PORT, RoboHttpUtils.DEFAULT_UDP_PORT);
        config.setString(PROPERTY_UNIT_PATHS_CONFIG, "[{\"roboUnit\":\"stringConsumer\",\"callbacks\": [\"stringConsumer\"]}]");
        builder.add(DatagramClientUnit.class, config, UDP_CLIENT);

        config = ConfigurationFactory.createEmptyConfiguration();
        builder.add(StringConsumer.class, config, "stringConsumer");

        return builder.build();
	}

    /**
     * create simple UDP server with consumer unit
     *
     *
     * @return roboContext
     * @throws Exception exception
     */
	private RoboContext configurePongSystem() throws Exception {
        RoboBuilder builder = new RoboBuilder();
        Configuration config = ConfigurationFactory.createEmptyConfiguration();
        config.setString(PROPERTY_CODEC_PACKAGES, PACKAGE_CODECS);
        config.setString(PROPERTY_UNIT_PATHS_CONFIG, "[{\"roboUnit\":\"stringConsumer\",\"filters\":[]}]");
        builder.add(DatagramServerUnit.class, config, "udp_server");

        config = ConfigurationFactory.createEmptyConfiguration();
        builder.add(StringConsumer.class, config, "stringConsumer");

        return builder.build();
    }
}
