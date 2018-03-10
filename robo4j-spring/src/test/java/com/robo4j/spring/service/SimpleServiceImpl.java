/*
 * Copyright (c) 2014, 2018, Marcus Hirt, Miroslav Wengner
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

package com.robo4j.spring.service;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Simple Spring service
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class SimpleServiceImpl implements SimpleService {

    private static final ThreadLocalRandom random = ThreadLocalRandom.current();

    @Override
    public Integer getRandom() {
        return random.nextInt(0, 1000);
    }

    @Override
    public String updateMessage(String message) {
        return "SPRING:".concat(message);
    }
}
