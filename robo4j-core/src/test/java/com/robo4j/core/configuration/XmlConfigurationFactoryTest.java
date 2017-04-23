/*
 * Copyright (c) 2014, 2017, Marcus Hirt, Miroslav Wengner
 * 
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */
package com.robo4j.core.configuration;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.robo4j.core.util.IOUtil;

/**
 * Tests for the configuration.
 * 
 * @author Marcus Hirt
 * @author Miroslav Wengner (@miragemiko)
 */
public class XmlConfigurationFactoryTest {
	@Test
	public void testSerializeToString() throws ConfigurationFactoryException {
		Configuration config = ConfigurationFactory.createEmptyConfiguration();
		config.setString("firstString", "S1");
		config.setString("secondString", "S2");
		config.setBoolean("boolean", true);
		Configuration child1 = config.createChildConfiguration("child1");
		child1.setInteger("int", 1);
		child1.setFloat("float", 1.0f);
		Configuration child2 = config.createChildConfiguration("child2");
		child2.setInteger("int", 2);
		child2.setFloat("float", 2.0f);
		String xml = XmlConfigurationFactory.toXml(config);
		System.out.println(xml);
		Assert.assertNotNull(xml);

		Configuration fromXml = XmlConfigurationFactory.fromXml(xml);
		System.out.println(XmlConfigurationFactory.toXml(fromXml));

		Assert.assertEquals(config, fromXml);
	}

	@Test
	public void testReadResource() throws IOException, ConfigurationFactoryException {
		String configXml = IOUtil.readStringFromUTF8Stream(XmlConfigurationFactoryTest.class.getClassLoader().getResourceAsStream("configurationtest.xml"));
		Configuration config = XmlConfigurationFactory.fromXml(configXml);
		Assert.assertNotNull(config.getChildConfiguration("multipliers"));
		Assert.assertNotNull(config.getChildConfiguration("offsets"));
	}
}
