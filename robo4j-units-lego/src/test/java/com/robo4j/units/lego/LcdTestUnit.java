/*
 * Copyright (c) 2014-2019, Marcus Hirt, Miroslav Wengner
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

import com.robo4j.ConfigurationException;
import com.robo4j.LifecycleState;
import com.robo4j.RoboContext;
import com.robo4j.configuration.Configuration;
import com.robo4j.hw.lego.wrapper.LcdTestWrapper;

/**
 * Lego MindStorm LCD Unit mock
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
//TODO: FIXME (miro, marcus) -> discuss testing possibilities
public class LcdTestUnit extends LcdUnit {

    public LcdTestUnit(RoboContext context, String id) {
        super(context, id);
    }

    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        setState(LifecycleState.UNINITIALIZED);
        lcd = new LcdTestWrapper();
        setState(LifecycleState.INITIALIZED);
    }
}
