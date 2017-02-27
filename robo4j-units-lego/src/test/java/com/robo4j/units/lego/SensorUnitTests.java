/*
 * Copyright (C) 2014, 2017. Miroslav Wengner, Marcus Hirt
 * This SensorUnitTests.java  is part of robo4j.
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

import com.robo4j.core.DefaultAttributeDescriptor;
import com.robo4j.units.lego.platform.LegoPlatformMessage;
import org.junit.Assert;
import org.junit.Test;

import com.robo4j.core.RoboSystem;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.configuration.ConfigurationFactory;
import com.robo4j.hw.lego.enums.SensorTypeEnum;
import com.robo4j.units.lego.sensor.LegoSensorMessage;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class SensorUnitTests {

    @Test
    public void simpleTankUnitMockTest() throws Exception {
        RoboSystem system = new RoboSystem();
        BasicSonicSensorUnitMock sonicUnit  = new BasicSonicSensorUnitMock(system, "sonicSensor");
        Configuration config = ConfigurationFactory.createEmptyConfiguration();

        sonicUnit.onInitialization(config);

        DefaultAttributeDescriptor<Boolean> descriptor = DefaultAttributeDescriptor.create(Boolean.class, "getStatus");
        sonicUnit.onMessage(new LegoSensorMessage(SensorTypeEnum.SONIC, "magic"));
        Assert.assertTrue(sonicUnit.getAttribute(descriptor).get());

        sonicUnit.shutdown();

    }

}
