/*
 * Copyright (c) 2014, 2017, Miroslav Wengner, Marcus Hirt
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

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the configuration.
 * 
 * @author Marcus Hirt
 */
public class ConfigurationTest {
	@Test
	public void testBasicConfiguration() {
		Configuration config = ConfigurationFactory.createEmptyConfiguration();
		config.setInt("MyInt", 1);
		config.setLong("MyLong", 2L);
		config.setFloat("MyFloat", 1.0f);
		config.setDouble("MyDouble", 2.0);
		config.setString("MyString", "toodiloo");
		
		Assert.assertEquals(1, config.getInt("MyInt"));
		Assert.assertEquals(2, config.getLong("MyLong"));
		Assert.assertEquals(1.0f, config.getFloat("MyFloat"), 0.000000001f);
		Assert.assertEquals(2.0, config.getDouble("MyDouble"), 0.000000001f);
		Assert.assertEquals("toodiloo", config.getString("MyString"));
	}
	
	@Test
	public void testSubConfigurations() {
		Configuration config = ConfigurationFactory.createEmptyConfiguration();
		Configuration child = config.createChildConfiguration("sub");
		child.setString("c", "child");
		// Children have their own namespace
		config.setDouble("sub", 2.0);

		Assert.assertEquals(2.0, config.getDouble("sub"), 0.000000001f);
		Assert.assertEquals("child", config.getChildConfiguration("sub").getString("c"));
	}
}
