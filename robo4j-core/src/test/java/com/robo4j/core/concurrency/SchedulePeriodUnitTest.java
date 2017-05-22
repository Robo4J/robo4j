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

package com.robo4j.core.concurrency;

import com.robo4j.core.ConfigurationException;
import com.robo4j.core.LifecycleState;
import com.robo4j.core.RoboSystem;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.configuration.ConfigurationFactory;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class SchedulePeriodUnitTest {

    @Test
    public void basicScheudlePeriodUnitTest() throws ConfigurationException{

        RoboSystem system = new RoboSystem();
        Configuration config = ConfigurationFactory.createEmptyConfiguration();

        SchedulePeriodUnit unit = new SchedulePeriodUnit(system, "schedulePeriodUnit");
        config.setString("unit", "test");
        config.setInteger("delay", 1);
        config.setInteger("period", 1);
        config.setString("timeUnit", "SECONDS");

        unit.initialize(config);

        Assert.assertTrue(unit.getState().equals(LifecycleState.INITIALIZED));
    }
}
