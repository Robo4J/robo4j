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

package com.robo4j.units.lego;

import com.robo4j.core.ConfigurationException;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboUnit;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.units.lego.sonic.LegoSonicBrainMessage;

/**
 * Obstacle avoiding brain
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class BasicSonicBrainUnit extends RoboUnit<LegoSonicBrainMessage>  {

    private String target;

    public BasicSonicBrainUnit(RoboContext context, String id) {
        super(LegoSonicBrainMessage.class, context, id);
    }

    /**
     * @param configuration
     *            desired configuration
     * @throws ConfigurationException
     */
    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        target = configuration.getString("target", null);
        if (target == null) {
            throw ConfigurationException.createMissingConfigNameException("target");
        }
    }

    /**
     * @param message
     *            accepted message
     * @return
     */
    @Override
    public void onMessage(LegoSonicBrainMessage message) {

        System.err.println(getClass().getSimpleName() + " onMessage: " + message);
    }

    //Private Methods



}
