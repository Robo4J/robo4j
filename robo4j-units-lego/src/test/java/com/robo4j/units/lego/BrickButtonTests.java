/*
 * Copyright (C) 2014, 2017. Miroslav Wengner, Marcus Hirt
 * This BrickButtonTests.java  is part of robo4j.
 * module: robo4j-units-lego
 *
 * robo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * robo4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.units.lego;

import com.robo4j.units.lego.brick.PlateButtonEnum;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

/**
 * Basics Lego Mindstorm Brick button plate tests
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class BrickButtonTests {

    @Test
    public void basicButtonPlateTest(){
        Set<String> buttonNames = PlateButtonEnum.getButtonNames();
        System.out.println("buttonNames: " + buttonNames);
        Assert.assertNotNull(buttonNames);

    }

}
