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

package com.robo4j.units.lego.controller;

import com.robo4j.ConfigurationException;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.units.lego.enums.LegoPlatformMessageTypeEnum;
import com.robo4j.units.lego.platform.LegoPlatformMessage;

/**
 * Simple Lego Tank Example
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class PlatformController extends RoboUnit<LegoPlatformMessageTypeEnum> {

    public static final String PROPERTY_TARGET = "target";
    private String target;

    public PlatformController(RoboContext context, String id) {
        super(LegoPlatformMessageTypeEnum.class, context, id);
    }

    @Override
    public void onMessage(LegoPlatformMessageTypeEnum message) {
        processMessage(message);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        target = configuration.getString(PROPERTY_TARGET, null);
        if (target == null) {
            throw ConfigurationException.createMissingConfigNameException(PROPERTY_TARGET);
        }
    }

    // Private Methods
    private void sendMessage(RoboContext ctx, LegoPlatformMessage message) {
        ctx.getReference(target).sendMessage(message);
    }

    private void processMessage(LegoPlatformMessageTypeEnum myMessage) {
        sendMessage(getContext(), new LegoPlatformMessage(myMessage));
    }
}
