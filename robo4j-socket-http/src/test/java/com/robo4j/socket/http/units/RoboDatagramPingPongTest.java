package com.robo4j.socket.http.units;

import com.robo4j.RoboBuilder;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.configuration.Configuration;
import com.robo4j.configuration.ConfigurationFactory;
import com.robo4j.socket.http.message.DatagramDecoratedRequest;
import com.robo4j.socket.http.util.RoboHttpUtils;
import com.robo4j.util.SystemUtil;
import org.junit.Test;

import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_PROPERTY_HOST;
import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_PROPERTY_PORT;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class RoboDatagramPingPongTest {

    private static final String PACKAGE_CODECS = "com.robo4j.socket.http.units.test.codec";
    private static final String UDP_CLIENT = "udp_client";

    @Test
	public void datagramPingPongTest() throws Exception {
	    RoboContext pingSystem = configurePingSystem();
		RoboContext pongSystem = configurePongSystem();

        pongSystem.start();
        pingSystem.start();
        System.out.println("UDP pongSystem: State after start:");
        System.out.println(SystemUtil.printStateReport(pongSystem));
        System.out.println("UDP pingSystem: State after start:");
        System.out.println(SystemUtil.printStateReport(pingSystem));

        RoboReference<DatagramDecoratedRequest<String>> udpClient = pingSystem.getReference(UDP_CLIENT);
        for(int i=0; i < 100; i++){
            DatagramDecoratedRequest<String> request = new DatagramDecoratedRequest<>("Hello robo4j" + i);
            udpClient.sendMessage(request);
        }


        pingSystem.shutdown();
        pongSystem.shutdown();

        System.out.println("UDP pongSystem: State after shutdown:");
        System.out.println(SystemUtil.printStateReport(pongSystem));
        System.out.println("UDP pingSystem: State after shutdown:");
        System.out.println(SystemUtil.printStateReport(pingSystem));

	}

	private RoboContext configurePongSystem() throws Exception {
		RoboBuilder builder = new RoboBuilder();

		Configuration config = ConfigurationFactory.createEmptyConfiguration();
		config.setString("packages", PACKAGE_CODECS);
        builder.add(DatagramServerUnit.class, config, "udp_server");
        return builder.build();
	}

	private RoboContext configurePingSystem() throws Exception {
        RoboBuilder builder = new RoboBuilder();
        Configuration config = ConfigurationFactory.createEmptyConfiguration();
        config.setString(HTTP_PROPERTY_HOST, "localhost");
        config.setInteger(HTTP_PROPERTY_PORT, RoboHttpUtils.DEFAULT_UDP_PORT);
        builder.add(DatagramClientUnit.class, config, UDP_CLIENT);

        return builder.build();
    }
}
