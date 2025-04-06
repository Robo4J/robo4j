/*
 * Copyright (c) 2014, 2025, Marcus Hirt, Miroslav Wengner
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

class DefaultConfigurationTests {

    @Test
    void parentAddChildDefaultConfigurationTest() {
        var parentValueName = "parentDouble";
        var parentValue = 2.0;
        var childConfigName = "childConfig";
        var childStringName = "childString";
        var childStringValue = "child";

        var parentConfig = new DefaultConfiguration();
        parentConfig.setDouble(parentValueName, parentValue);
        var childConfig = (DefaultConfiguration) parentConfig.createChildConfiguration(childConfigName);
        childConfig.setString(childStringName, childStringValue);

        assertTrue(parentConfig.isDefined());
        assertTrue(childConfig.isDefined());
        assertEquals(parentValue, parentConfig.getDouble(parentValueName, null), 0.000000001f);
        assertEquals(childStringValue, parentConfig.getChildConfiguration(childConfigName).getString(childStringName, null));
    }

}
