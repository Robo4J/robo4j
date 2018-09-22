/*
 * Copyright (c) 2014, 2018, Marcus Hirt, Miroslav Wengner
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

package com.robo4j.configuration;

import org.junit.Assert;
import org.junit.Test;

import com.robo4j.RoboBuilder;
import com.robo4j.RoboBuilderException;
import com.robo4j.RoboContext;
import com.robo4j.util.SystemUtil;

/**
 * RemoteSystemBuilderTest building remote system configuration by
 * {@link ConfigurationBuilder} using declarative and programmatic approach
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class RemoteSystemBuilderTest {

    /**
     * Building {@link com.robo4j.RoboSystem} by xml declarative and programmatic approach
     *
     * @throws RoboBuilderException configuration exception
     */
	@Test
	public void builderRemoteSystemTest() throws RoboBuilderException {

		final RoboBuilder xmlBuilder = new RoboBuilder(
				SystemUtil.getInputStreamByResourceName("testRemoteMessageReceiverAckSystem.xml"));
		final RoboContext xmlContext = xmlBuilder.build();

		//@formatter:off
        final Configuration systemConf = new ConfigurationBuilder()
                .addInteger(RoboBuilder.KEY_SCHEDULER_POOL_SIZE, 2)
                .addInteger(RoboBuilder.KEY_WORKER_POOL_SIZE, 2)
                .addInteger(RoboBuilder.KEY_BLOCKING_POOL_SIZE, 6)
                .addBuilder(RoboBuilder.KEY_CONFIGURATION_SERVER, new ConfigurationBuilder()
                        .addString("hostname", "localhost")
                        .addInteger("port", 0))
                .addBuilder(RoboBuilder.KEY_CONFIGURATION_EMITTER, new ConfigurationBuilder()
                        .addString("multicastAddress", "238.12.15.254")
                        .addInteger("port", 0x0FFE)
                        .addInteger("heartBeatInterval", 250)
                        .addBoolean("enabled", true)
                        .addBuilder(RoboBuilder.KEY_CONFIGURATION_EMITTER_METADATA, new ConfigurationBuilder()
                        .addString("name", "StringMessageConsumer")
                        .addString("class", "MessageConsumer")))
                .build();
        final RoboContext programmaticContext = new RoboBuilder("9", systemConf).build();
	    //@formatter:on

		Assert.assertEquals(xmlContext.getConfiguration(), systemConf);
		Assert.assertEquals(xmlContext.getId(), programmaticContext.getId());

	}
}
