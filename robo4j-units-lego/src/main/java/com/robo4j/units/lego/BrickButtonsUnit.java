/*
 * Copyright (C) 2014, 2017. Miroslav Wengner, Marcus Hirt
 * This ButtonUnit.java  is part of robo4j.
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

import com.robo4j.core.ConfigurationException;
import com.robo4j.core.LifecycleState;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboReference;
import com.robo4j.core.RoboUnit;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.units.lego.brick.ButtonListener;

import com.robo4j.units.lego.brick.PlateButtonEnum;
import lejos.hardware.Button;

/**
 * Lego Mindstorm Brick button plate
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 * @since 31.01.2017
 */
public class BrickButtonsUnit extends RoboUnit<String> {

    private String target;

    public BrickButtonsUnit(RoboContext context, String id) {
        super(context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        target = configuration.getString("target", null);
        if (target == null) {
            throw ConfigurationException.createMissingConfigNameException("target");
        }
    }

    //TODO, FIXME do it configurable
    @Override
    public void start() {
        final RoboReference<String> targetRef = getContext().getReference(target);
        setState(LifecycleState.STARTING);
        Button.LEFT.addKeyListener(new ButtonListener(targetRef, PlateButtonEnum.RIGHT, 1));
        Button.RIGHT.addKeyListener(new ButtonListener(targetRef, PlateButtonEnum.LEFT, 1));
        Button.UP.addKeyListener(new ButtonListener(targetRef, PlateButtonEnum.DOWN, 1));
        Button.DOWN.addKeyListener(new ButtonListener(targetRef, PlateButtonEnum.UP, 1));
        Button.ENTER.addKeyListener(new ButtonListener(targetRef, PlateButtonEnum.ENTER, 2));
        setState(LifecycleState.STARTED);
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
}
