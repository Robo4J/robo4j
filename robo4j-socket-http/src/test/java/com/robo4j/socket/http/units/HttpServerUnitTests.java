package com.robo4j.socket.http.units;

import com.robo4j.LifecycleState;
import com.robo4j.RoboBuilder;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.configuration.Configuration;
import com.robo4j.configuration.ConfigurationFactory;
import com.robo4j.util.SystemUtil;
import org.junit.Assert;
import org.junit.Test;

import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_PROPERTY_PORT;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class HttpServerUnitTests {
    private static final int PORT = 9000;
    private static final String ID_HTTP_SERVER = "empty_server";

	@Test
	public void httpServerUnitNoPathsTest() throws Exception {

	    RoboBuilder builder = new RoboBuilder();

        Configuration config = ConfigurationFactory.createEmptyConfiguration();
        config.setInteger(HTTP_PROPERTY_PORT, PORT);

        builder.add(HttpServerUnit.class, config, ID_HTTP_SERVER);
        RoboContext system = builder.build();


        system.start();
        System.out.println("system: State after start:");
        System.out.println(SystemUtil.printStateReport(system));
        RoboReference<HttpServerUnit> systemReference = system.getReference(ID_HTTP_SERVER);
        system.shutdown();
        System.out.println("system: State after shutdown:");
        System.out.println(SystemUtil.printStateReport(system));
        Assert.assertTrue(systemReference.getState().equals(LifecycleState.SHUTDOWN));


	}

}
