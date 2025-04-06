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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testing supported variables types potentially used for roboUnit configuration
 *
 * @author Marcus Hirt
 * @author Miroslav Wengner (@miragemiko)
 */
class ConfigurationBuilderTests {
    @Test
    void allowedConfigurationTypesTest() {
        var parentIntValueName = "MyInt";
        var parentIntValue = 1;
        var parentLongValueName = "MyLong";
        var parentLongValue = 2L;
        var parentFloatValueName = "MyFloat";
        var parentFloatValue = 1.0f;
        var parentDoubleValueName = "MyDouble";
        var parentDoubleValue = 2.0;
        var parentStringValueName = "MyString";
        var parentStringValue = "toodiloo";
        var parentCharacterValueName = "MyCharacter";
        var parentCharacterValue = 'C';
        var parentBooleanValueName = "MyBoolean";
        var parentBooleanValue = true;

        ConfigurationBuilder builder = new ConfigurationBuilder()
                .addInteger(parentIntValueName, parentIntValue)
                .addLong(parentLongValueName, parentLongValue)
                .addFloat(parentFloatValueName, parentFloatValue)
                .addDouble(parentDoubleValueName, parentDoubleValue)
                .addString(parentStringValueName, parentStringValue)
                .addCharacter(parentCharacterValueName, parentCharacterValue)
                .addBoolean(parentBooleanValueName, parentBooleanValue);
        Configuration config = builder.build();

        assertTrue(config.isDefined());
        assertEquals(parentIntValue, config.getInteger(parentIntValueName, -1));
        assertEquals(parentLongValue, (long) config.getLong(parentLongValueName, -1L));
        assertEquals(parentFloatValue, config.getFloat(parentFloatValueName, -1f), 0.000000001f);
        assertEquals(parentDoubleValue, config.getDouble(parentDoubleValueName, -1.0), 0.000000001f);
        assertEquals(parentStringValue, config.getString(parentStringValueName, "nope"));
        assertEquals(Character.valueOf(parentCharacterValue), config.getCharacter(parentCharacterValueName, 'A'));
        assertEquals(parentBooleanValue, config.getBoolean(parentBooleanValueName, false));
    }

    @Test
    void parentChildConfigurationsTest() {
        // Testing that we can also have the same name for an entry and a sub
        // tree.
        var parentValueName = "parentDouble";
        var parentValue = 2.0;
        var childConfigName = "childConfig";
        var childStringName = "childString";
        var childStringValue = "child";

        Configuration config = new ConfigurationBuilder()
                .addBuilder(childConfigName,
                        new ConfigurationBuilder()
                                .addString(childStringName, childStringValue)
                )
                .addDouble(parentValueName, parentValue).build();

        assertTrue(config.isDefined());
        assertEquals(parentValue, config.getDouble(parentValueName, null), 0.000000001f);
        assertEquals(childStringValue, config.getChildConfiguration(childConfigName).getString(childStringName, null));
    }

}
