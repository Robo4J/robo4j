/*
 * Copyright (C) 2014, 2017. Miroslav Wengner, Marcus Hirt
 * This SimpleTankUnitTests.java  is part of robo4j.
 * module: robo4j-units-lego
 *
 * robo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * robo4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.units.lego;

import org.junit.Assert;
import org.junit.Test;

import com.robo4j.core.DefaultAttributeDescriptor;
import com.robo4j.core.RoboSystem;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.configuration.ConfigurationFactory;
import com.robo4j.units.lego.platform.LegoPlatformMessage;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class SimpleTankUnitTests {

    @Test
    public void simpleTankUnitMockTest() throws Exception {
        RoboSystem system = new RoboSystem();
        SimpleTankUnitMock tank  = new SimpleTankUnitMock(system, "tank");
        Configuration config = ConfigurationFactory.createEmptyConfiguration();

        tank.onInitialization(config);

        DefaultAttributeDescriptor<Boolean> descriptor = DefaultAttributeDescriptor.create(Boolean.class, "getStatus");

        tank.onMessage(new LegoPlatformMessage("right"));
        tank.onMessage(new LegoPlatformMessage("left"));
        tank.onMessage(new LegoPlatformMessage("move"));
        tank.onMessage(new LegoPlatformMessage("back"));
        tank.onMessage(new LegoPlatformMessage("stop"));

        Assert.assertTrue(tank.getAttribute(descriptor).get());

        tank.shutdown();

    }

}
