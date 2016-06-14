/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This BrickCommandProviderFuture.java is part of robo4j.
 *
 *     robo4j is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     robo4j is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.robo4j.core.bridge.task;

import com.robo4j.lego.control.LegoEngine;
import com.robo4j.core.lego.LegoBrickPropertiesHolder;
import com.robo4j.core.lego.LegoBrickRemote;
import com.robo4j.core.platform.PlatformProperties;
import com.robo4j.core.platform.provider.LegoBrickCommandsProvider;
import com.robo4j.core.platform.provider.LegoBrickCommandsProviderImp;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by miroslavkopecky on 30/03/16.
 */
public class BrickCommandProviderFuture implements Callable<LegoBrickCommandsProvider> {

    private LegoBrickRemote legoBrickRemote;
    private LegoBrickPropertiesHolder legoBrickPropertiesHolder;
    private PlatformProperties properties;
    private Map<String, LegoEngine> engineCache;

    public BrickCommandProviderFuture(final LegoBrickRemote brickRemote, final LegoBrickPropertiesHolder holder,
                                      final PlatformProperties properties, final Map<String, LegoEngine> engineCache) {
        this.legoBrickRemote = brickRemote;
        this.legoBrickPropertiesHolder = holder;
        this.properties = properties;
        this.engineCache = engineCache;
    }

    @Override
    public LegoBrickCommandsProvider call() throws Exception {
        return new LegoBrickCommandsProviderImp(legoBrickRemote, properties, engineCache);
    }
}
