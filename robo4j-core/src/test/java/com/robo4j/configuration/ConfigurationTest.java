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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */
package com.robo4j.configuration;

import org.junit.Assert;
import org.junit.Test;

import com.robo4j.configuration.Configuration;
import com.robo4j.configuration.ConfigurationFactory;

/**
 * Testing supported variables types potentially used for roboUnit configuration
 * 
 * @author Marcus Hirt
 * @author Miroslav Wengner (@miragemiko)
 */
public class ConfigurationTest {
	@Test
	public void testBasicConfiguration() {
		Configuration config = ConfigurationFactory.createEmptyConfiguration();
		config.setInteger("MyInt", 1);
		config.setLong("MyLong", 2L);
		config.setFloat("MyFloat", 1.0f);
		config.setDouble("MyDouble", 2.0);
		config.setString("MyString", "toodiloo");
		config.setCharacter("MyCharacter", 'C');
		config.setBoolean("MyBoolean", true);

		Assert.assertEquals(1, (int) config.getInteger("MyInt", -1));
		Assert.assertEquals(2L, (long) config.getLong("MyLong", -1L));
		Assert.assertEquals(1.0f, config.getFloat("MyFloat", -1f), 0.000000001f);
		Assert.assertEquals(2.0, config.getDouble("MyDouble", -1.0), 0.000000001f);
		Assert.assertEquals("toodiloo", config.getString("MyString", "nope"));
		Assert.assertEquals(Character.valueOf('C'), config.getCharacter("MyCharacter", 'A'));
		Assert.assertEquals(true, config.getBoolean("MyBoolean", false));
	}

	@Test
	public void testSubConfigurations() {
		Configuration config = ConfigurationFactory.createEmptyConfiguration();
		Configuration child = config.createChildConfiguration("sub");
		child.setString("c", "child");
		// Children have their own namespace
		config.setDouble("sub", 2.0);
		Assert.assertEquals(2.0, config.getDouble("sub", null), 0.000000001f);
		Assert.assertEquals("child", config.getChildConfiguration("sub").getString("c", null));
	}

}
