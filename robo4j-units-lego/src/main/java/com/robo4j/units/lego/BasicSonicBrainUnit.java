/*
 * Copyright (c) 2014, 2017, Marcus Hirt, Miroslav Wengner
 *
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.units.lego;

import com.robo4j.core.ConfigurationException;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboUnit;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.units.lego.enums.LegoPlatformMessageTypeEnum;
import com.robo4j.units.lego.platform.LegoPlatformMessage;
import com.robo4j.units.lego.sonic.LegoSonicBrainMessage;

/**
 * Obstacle avoiding brain
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class BasicSonicBrainUnit extends RoboUnit<LegoSonicBrainMessage>  {

	/* center value is the 0 -> setup by the hand at the begining */
	private static final int POSITION_CENTER = 0;
	private static final int POSITION_INFINITY = 1024;
	private volatile int VALUE_CENTER = 0;
	private volatile LegoPlatformMessageTypeEnum currentPlatformState;
	private final double VALUE_SECURE_DISTANCE = 0.15;
    private String target;

    public BasicSonicBrainUnit(RoboContext context, String id) {
        super(LegoSonicBrainMessage.class, context, id);
    }

    /**
     * @param configuration
     *            desired configuration
     * @throws ConfigurationException
     */
    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        target = configuration.getString("target", null);
        if (target == null) {
            throw ConfigurationException.createMissingConfigNameException("target");
        }
		currentPlatformState = LegoPlatformMessageTypeEnum.STOP;
		System.err.println(getClass().getSimpleName() + " target: " + target);
    }

    /**
     * @param message
     *            accepted message
     * @return
     */
    @Override
    public void onMessage(LegoSonicBrainMessage message) {

		System.err.println(getClass().getSimpleName() + " target: " + target);
		System.err.println(getClass().getSimpleName() + " onMessage: " + message.getData());
		final String data = message.getData().replace(",", "").trim();
		final double distance = data.equals("Infinity") ? POSITION_INFINITY : Double.valueOf(data);
		System.err.println(getClass().getSimpleName() + " data: " + data);
		System.err.println(getClass().getSimpleName() + " distance: " + distance);

		if (message.getPosition() == POSITION_CENTER && distance > VALUE_SECURE_DISTANCE) {
			System.err.println(getClass().getSimpleName() + " RUN onMessage: " + message);
			if (!currentPlatformState.equals(LegoPlatformMessageTypeEnum.MOVE)) {
				sendMessage(new LegoPlatformMessage(LegoPlatformMessageTypeEnum.MOVE));
				currentPlatformState = LegoPlatformMessageTypeEnum.MOVE;
			}
		} else if (message.getPosition() > 0 && distance > VALUE_SECURE_DISTANCE
				&& !currentPlatformState.equals(LegoPlatformMessageTypeEnum.RIGHT)) {
			System.err.println(getClass().getSimpleName() + " RIGHT onMessage: " + message);
			sendMessage(new LegoPlatformMessage(LegoPlatformMessageTypeEnum.RIGHT));
			currentPlatformState = LegoPlatformMessageTypeEnum.RIGHT;
		} else if (message.getPosition() < 0 && distance > VALUE_SECURE_DISTANCE
				&& !currentPlatformState.equals(LegoPlatformMessageTypeEnum.LEFT)) {
			System.err.println(getClass().getSimpleName() + " LEFT onMessage: " + message);
			sendMessage(new LegoPlatformMessage(LegoPlatformMessageTypeEnum.LEFT));
			currentPlatformState = LegoPlatformMessageTypeEnum.LEFT;
		} else {
			System.err.println(getClass().getSimpleName() + " STOP onMessage: " + message);
			sendMessage(new LegoPlatformMessage(LegoPlatformMessageTypeEnum.STOP));
			currentPlatformState = LegoPlatformMessageTypeEnum.STOP;
		}
    }

    //Private Methods
	// LegoPlatformMessageTypeEnum
	private void sendMessage(LegoPlatformMessage message) {
		System.err.println(getClass().getSimpleName() + " sendMessage message: " + message + ", target: " + target);
		System.err.println(getClass().getSimpleName() + " sendMessage reference: " + getContext());
		getContext().getReference(target).sendMessage(message);
	}



}
