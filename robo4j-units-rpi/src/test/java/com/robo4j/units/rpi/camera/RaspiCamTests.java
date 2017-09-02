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

package com.robo4j.units.rpi.camera;

import com.robo4j.core.DefaultAttributeDescriptor;
import com.robo4j.core.RoboSystem;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.configuration.ConfigurationFactory;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class RaspiCamTests {

    private static final String DEFAULT_COMMAND = "-w 320 -h 240 -ex sport -t 1 -tl 100 -e jpg -n  -o -";

    @Test
    public void testCameraCommandBuilderEnd() throws  Exception{
        final RoboSystem system = new RoboSystem();

        RaspistillUnit imageController = new RaspistillUnit(system, "camera");
        Configuration config = ConfigurationFactory.createEmptyConfiguration();
        config.setString("targetOut", "out");
        config.setString("client", "client");
        imageController.onInitialization(config);

        final DefaultAttributeDescriptor<String> commandDecriptor = DefaultAttributeDescriptor.create(String.class,
                "command");
        String command = imageController.onGetAttribute(commandDecriptor);

        Assert.assertTrue(command.contains(DEFAULT_COMMAND));
    }

}
