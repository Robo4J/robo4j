/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This LegoBrickCommandsProviderImp.java is part of robo4j.
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

package com.robo4j.core.platform.provider;

import com.robo4j.commons.agent.GenericAgent;
import com.robo4j.commons.command.CommandProperties;
import com.robo4j.commons.command.GenericCommand;
import com.robo4j.commons.concurrent.LegoThreadFactory;
import com.robo4j.commons.unit.DefaultUnit;
import com.robo4j.core.platform.PlatformProperties;
import com.robo4j.core.platform.PlatformUtils;
import com.robo4j.core.platform.command.LegoCommandProperty;
import com.robo4j.core.platform.command.LegoPlatformCommandEnum;
import com.robo4j.lego.control.LegoBrickRemote;
import com.robo4j.lego.control.LegoEngine;
import com.robo4j.lego.control.LegoUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * Provider is implemented without closing
 *
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 26.02.2016
 */

public class LegoBrickCommandsProviderImp implements LegoBrickCommandsProvider {

    private static final Logger logger = LoggerFactory.getLogger(LegoBrickCommandsProviderImp.class);
    private static final int UNIT_PLATFORM_CONSUMER_PRODUCER = 3;
    private static final int DEFAULT_0 = 0;
    private volatile ExecutorService executorForCommands;
    private final List<GenericAgent> agents;
    private final List<DefaultUnit> units;

    private volatile AtomicBoolean active;

    private PlatformProperties properties;

    //@formatter:off
    public LegoBrickCommandsProviderImp(final LegoBrickRemote legoBrickRemote,
                                        final PlatformProperties properties,
                                        final Map<String, LegoEngine> engineCache,
                                        final Map<String, LegoUnit> unitCache){
    //@formatter:on
        this.agents = new LinkedList<>();
        this.units = new LinkedList<>();
        this.active = new AtomicBoolean(false);
        this.properties = properties;

        executorForCommands = Executors.newFixedThreadPool(UNIT_PLATFORM_CONSUMER_PRODUCER,
                new LegoThreadFactory(PlatformUtils.BUS_BRICK));

        LegoUnit platformUnit = unitCache.get("platformUnit");
        platformUnit.setExecutor(executorForCommands);
        logger.info("EXECUTOR FOR COMMAND IS SET ");
        platformUnit.init(legoBrickRemote, engineCache, null);
        this.units.add((DefaultUnit) platformUnit);
        this.agents.addAll(((DefaultUnit) platformUnit).getAgents());

        if(!agents.isEmpty()){
            active.set(true);
        }
    }

    @Override
    public boolean process(LegoPlatformCommandEnum direction) {
        final CommandProperties commandProperties = () -> 300;
        GenericCommand<LegoPlatformCommandEnum> command = new GenericCommand<>(commandProperties, direction, "", 1);
        LegoUnit unit = (LegoUnit) units.get(DEFAULT_0);
        return unit.process(command);
    }

    @Override
    public boolean process(LegoPlatformCommandEnum direction, LegoCommandProperty property) {

        logger.info("COMMAND PROCESS property= " + property);
        final int speed = property.getSpeed() != 0 ? property.getSpeed() : 300;
        logger.info("COMMAND PROCESS command SPEED= " + speed);
        final CommandProperties commandProperties = () -> speed;

        GenericCommand<LegoPlatformCommandEnum> command =
                new GenericCommand<>(commandProperties, direction, property.getValue(), 1);
        LegoUnit unit = (LegoUnit) units.get(DEFAULT_0);
        logger.info("PROCESS property command  = " + command);
        return unit.process(command);



    }

    @Override
    public void exit() throws RemoteException, InterruptedException {
        logger.info("MAIN EXIT COMMAND CALLED");

    }

    @Override
    public boolean isActive() {
        return active.get();
    }

    //Private Methods




}
