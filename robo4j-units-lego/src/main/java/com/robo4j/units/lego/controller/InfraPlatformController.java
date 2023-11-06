/*
 * Copyright (c) 2014, 2023, Marcus Hirt, Miroslav Wengner
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
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.units.lego.enums.LegoPlatformMessageTypeEnum;
import com.robo4j.units.lego.infra.InfraSensorMessage;
import com.robo4j.units.lego.utils.LegoUtils;

import java.util.EnumSet;
import java.util.Random;

/**
 * InfraPlatformController platform controlled by infra red sensor
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class InfraPlatformController extends RoboUnit<InfraSensorMessage> {

    public static final String PROP_TARGET = "target";
    public static final String PROP_DISTANCE_MIN = "distanceMin";
    public static final String PROP_DISTANCE_OPTIMAL = "distanceOptimal";
    public static final String PROP_DISTANCE_MAX = "distanceMax";
    private static final float DISTANCE_MIN = 40f;
    private static final float DISTANCE_OPTIMAL = 50f;
    private static final float DISTANCE_MAX = 100f;
    private static final Random random = new Random();

    private static final EnumSet<LegoPlatformMessageTypeEnum> rotationTypes = EnumSet.of(LegoPlatformMessageTypeEnum.LEFT, LegoPlatformMessageTypeEnum.RIGHT);

    private float minDistance;
    private float optDistance;
    private float maxDistance;
    private String target;
    private LegoPlatformMessageTypeEnum currentMessage = LegoPlatformMessageTypeEnum.STOP;

	public InfraPlatformController(RoboContext context, String id) {
		super(InfraSensorMessage.class, context, id);
	}

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        target = configuration.getString(PROP_TARGET, null);
        if (target == null) {
            throw ConfigurationException.createMissingConfigNameException(PROP_TARGET);
        }

        minDistance = configuration.getFloat(PROP_DISTANCE_MIN, DISTANCE_MIN);
        optDistance = configuration.getFloat(PROP_DISTANCE_OPTIMAL, DISTANCE_OPTIMAL);
        maxDistance = configuration.getFloat(PROP_DISTANCE_MAX, DISTANCE_MAX);

    }

    @Override
    public void onMessage(InfraSensorMessage message) {
        final String value = LegoUtils.parseOneElementString(message.getDistance());
        final float distanceValue = LegoUtils.parseFloatStringWithInfinityDefault(value, maxDistance);
        if(rotationTypes.contains(currentMessage) && distanceValue < optDistance){
            SimpleLoggingUtil.debug(getClass(), String.format("%s : adjusting platform distance: %f", getClass(), distanceValue));
        } else if(distanceValue > minDistance){
            sendPlatformMessage(LegoPlatformMessageTypeEnum.MOVE);
        } else {
            int randomValue = random.nextInt(2) + 3;
            LegoPlatformMessageTypeEnum rotationMessage = LegoPlatformMessageTypeEnum.getById(randomValue);
            sendPlatformMessage(rotationMessage);
        }
    }

    @Override
    public void stop() {
        sendPlatformMessage(LegoPlatformMessageTypeEnum.STOP);
        super.stop();
    }


    private void sendPlatformMessage(LegoPlatformMessageTypeEnum type){
        if(!currentMessage.equals(type)){
            currentMessage = type;
            getContext().getReference(target).sendMessage(type);
        }
    }
}
