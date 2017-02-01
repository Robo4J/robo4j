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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import com.robo4j.core.ConfigurationException;
import com.robo4j.core.LifecycleState;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboReference;
import com.robo4j.core.RoboUnit;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.units.lego.brick.ButtonListener;
import com.robo4j.units.lego.brick.PlateButtonEnum;

import com.robo4j.units.lego.brick.PlateButtonI;
import com.robo4j.units.lego.util.BrickUtils;
import lejos.hardware.Button;
import lejos.hardware.Key;

/**
 * Lego Mindstorm Brick button plate
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 * @since 31.01.2017
 */
public class BrickButtonsUnit extends RoboUnit<String> {
	private static final int COLOR_GREEN = 1;
	private String target;
	private List<Key> availablePlateButtons = Arrays.asList(Button.LEFT, Button.RIGHT, Button.UP, Button.DOWN,
			Button.ENTER);
	private Map<PlateButtonEnum, Key> buttons;

	public BrickButtonsUnit(RoboContext context, String id) {
		super(context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		setState(LifecycleState.UNINITIALIZED);
		target = configuration.getString("target", null);
		if (target == null) {
			throw ConfigurationException.createMissingConfigNameException("target");
		}

		//@formatter:off
        /* initiate button plate */
        buttons = availablePlateButtons.stream()
                .map(lb -> {
                    String bPropName = configuration.getString(BrickUtils.PREFIX_BUTTON.concat(lb.getName().toLowerCase()), null);
                    PlateButtonEnum plateB = PlateButtonEnum.getByName(bPropName);
                    return (bPropName == null || plateB == null) ? null : mapButton().apply(plateB, lb);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(PlateButtonI::getKey, PlateButtonI::getValue));
        //@formatter:on
		setState(LifecycleState.INITIALIZED);
	}

	@Override
	public void start() {
		final RoboReference<String> targetRef = getContext().getReference(target);
		setState(LifecycleState.STARTING);

		//@formatter:off
        buttons.entrySet().stream()
                .forEach(e ->
                    e.getValue().addKeyListener(new ButtonListener(targetRef, e.getKey(), COLOR_GREEN))
                );
        //@formatter:on
		setState(LifecycleState.STARTED);
	}

    //Private Methods
    private BiFunction<PlateButtonEnum, Key, PlateButtonI> mapButton(){
        return (PlateButtonEnum plateButton, Key legoButton) -> new PlateButtonI() {
            @Override
            public PlateButtonEnum getKey() {
                return plateButton;
            }

            @Override
            public Key getValue() {
                return legoButton;
            }
        };
    }
}
