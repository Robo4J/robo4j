/*
 * Copyright (C) 2014, 2017. Miroslav Wengner, Marcus Hirt
 * This LcdTestUnit.java  is part of robo4j.
 * module: robo4j-units-lego-test
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

import com.robo4j.core.ConfigurationException;
import com.robo4j.core.LifecycleState;
import com.robo4j.core.RoboContext;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.hw.lego.wrapper.LcdTestWrapper;

/**
 * Lego MindStorm LCD Unit mock
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 * @since 02.02.2017
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
