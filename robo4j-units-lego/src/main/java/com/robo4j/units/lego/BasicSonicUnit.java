/*
 * Copyright (C) 2014, 2017. Miroslav Wengner, Marcus Hirt
 * This BasicSonicUnit.java  is part of robo4j.
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
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboUnit;
import com.robo4j.core.concurrency.RoboThreadFactory;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.units.lego.utils.LegoUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 * @since 04.02.2017
 */
public class BasicSonicUnit extends RoboUnit<String> {

    private final LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
    private final ExecutorService executor = new ThreadPoolExecutor(LegoUtils.DEFAULT_THREAD_POOL_SIZE,
            LegoUtils.DEFAULT_THREAD_POOL_SIZE, LegoUtils.KEEP_ALIVE_TIME, TimeUnit.SECONDS, workQueue,
            new RoboThreadFactory("Robo4J Lego BasicSensor ", true));

    public BasicSonicUnit(RoboContext context, String id) {
        super(context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {

    }
}
