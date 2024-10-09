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
package com.robo4j.configuration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testing supported variables types potentially used for roboUnit configuration
 *
 * @author Marcus Hirt
 * @author Miroslav Wengner (@miragemiko)
 */
class ConfigurationBuilderTest {
    @Test
    void testBasicConfiguration() {
        ConfigurationBuilder builder = new ConfigurationBuilder().addInteger("MyInt", 1).addLong("MyLong", 2L).addFloat("MyFloat", 1.0f)
                .addDouble("MyDouble", 2.0).addString("MyString", "toodiloo").addCharacter("MyCharacter", 'C')
                .addBoolean("MyBoolean", true);
        Configuration config = builder.build();

        assertEquals(1, (int) config.getInteger("MyInt", -1));
        assertEquals(2L, (long) config.getLong("MyLong", -1L));
        assertEquals(1.0f, config.getFloat("MyFloat", -1f), 0.000000001f);
        assertEquals(2.0, config.getDouble("MyDouble", -1.0), 0.000000001f);
        assertEquals("toodiloo", config.getString("MyString", "nope"));
        assertEquals(Character.valueOf('C'), config.getCharacter("MyCharacter", 'A'));
        assertEquals(true, config.getBoolean("MyBoolean", false));
    }

    @Test
    void testSubConfigurations() {
        // Testing that we can also have the same name for an entry and a sub
        // tree.
        ConfigurationBuilder builder = new ConfigurationBuilder().addBuilder("sub", new ConfigurationBuilder().addString("c", "child"))
                .addDouble("sub", 2.0);
        Configuration config = builder.build();
        assertEquals(2.0, config.getDouble("sub", null), 0.000000001f);
        assertEquals("child", config.getChildConfiguration("sub").getString("c", null));
    }
}
