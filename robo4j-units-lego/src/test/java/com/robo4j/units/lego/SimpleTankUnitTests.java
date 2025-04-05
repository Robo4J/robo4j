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
package com.robo4j.units.lego;

import com.robo4j.DefaultAttributeDescriptor;
import com.robo4j.RoboBuilder;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.units.lego.platform.LegoPlatformMessage;
import org.junit.jupiter.api.Test;

import static com.robo4j.configuration.Configuration.EMPTY_CONFIGURATION;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
class SimpleTankUnitTests {

    @Test
    void simpleTankUnitMockTest() throws Exception {
        RoboBuilder builder = new RoboBuilder();

        builder.add(SimpleTankUnitMock.class, EMPTY_CONFIGURATION, "tank");

        DefaultAttributeDescriptor<Boolean> descriptor = DefaultAttributeDescriptor.create(Boolean.class, "getStatus");

        RoboContext context = builder.build();
        RoboReference<LegoPlatformMessage> tank = context.getReference("tank");

        tank.sendMessage(new LegoPlatformMessage("right"));
        tank.sendMessage(new LegoPlatformMessage("left"));
        tank.sendMessage(new LegoPlatformMessage("move"));
        tank.sendMessage(new LegoPlatformMessage("back"));
        tank.sendMessage(new LegoPlatformMessage("stop"));

        assertTrue(tank.getAttribute(descriptor).get());

        context.shutdown();
    }

}
