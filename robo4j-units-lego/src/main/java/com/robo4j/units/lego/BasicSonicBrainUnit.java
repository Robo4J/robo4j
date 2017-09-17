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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.units.lego;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import com.robo4j.ConfigurationException;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
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
	private static final int FLAG_CENTER = 0;
	private static final int FLAG_RIGHT = 1;
	private static final int FLAG_LEFT = 2;
	private static final int POSITION_CENTER = 0;
	private static final int POSITION_MAX_OBSERVED = 30;
	private static final int DECISION_VALUES = 3;
	private static final double POSITION_INFINITY = 1024;
	private static final double SECURE_DISTANCE = (double) 0.25;
	private volatile double[] decisionValues = new double[DECISION_VALUES];
	private volatile LegoPlatformMessageTypeEnum currentPlatformState;
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
		final double distance = data.equals("Infinity") ? POSITION_INFINITY : Float.valueOf(data);
		System.err.println(getClass().getSimpleName() + " data: " + data);
		System.err.println(getClass().getSimpleName() + " distance: " + distance);

		final int position = message.getPosition();
		int flagSet = -1;
		switch (position) {
		case POSITION_CENTER:
			flagSet = FLAG_CENTER;
			System.err.println(getClass().getSimpleName() + " CENTER value distance: " + distance);
			break;
		case POSITION_MAX_OBSERVED:
			flagSet = FLAG_RIGHT;
			System.err.println(getClass().getSimpleName() + " RIGHT value distance: " + distance);
			break;
		case -POSITION_MAX_OBSERVED:
			flagSet = FLAG_LEFT;
			System.err.println(getClass().getSimpleName() + " LEFT value distance: " + distance);
			break;
		default:
			break;
		}

		/* react only when flat is set */
		if (flagSet >= 0) {
			decisionValues[flagSet] = distance;
			/* decision about the direction */
			if (decisionValuesReady()) {
				List<LegoPlatformMessageTypeEnum> decisionList = new ArrayList<>();
				boolean decisionSet = false;
				if (decisionValues[FLAG_CENTER] > SECURE_DISTANCE) {
					decisionList.add(LegoPlatformMessageTypeEnum.MOVE);
					decisionSet = true;
					System.err.println(getClass().getSimpleName() + " CENTER  decision: " + decisionValues[FLAG_CENTER]);
				}
				if (decisionValues[FLAG_RIGHT] > SECURE_DISTANCE && !decisionSet) {
					decisionList.add(LegoPlatformMessageTypeEnum.RIGHT);
					System.err.println(getClass().getSimpleName() + " RIGHT  decision: " + decisionValues[FLAG_RIGHT]);
					decisionSet = true;
				}
				if (decisionValues[FLAG_LEFT] > SECURE_DISTANCE  && !decisionSet) {
					decisionList.add(LegoPlatformMessageTypeEnum.LEFT);
					System.err.println(getClass().getSimpleName() + " LEFT  decision: " + decisionValues[FLAG_LEFT]);
					decisionSet = true;
				}

				if (!decisionSet) {
					decisionList.add(LegoPlatformMessageTypeEnum.BACK);
				}

				/* make final decision */
				if (decisionList.contains(currentPlatformState)) {
					System.err.println(getClass().getSimpleName() + " NO DECISION CHANGE: " + currentPlatformState
							+ " decisionValues:" + Arrays.toString(decisionValues));
				} else if(!decisionList.isEmpty()) {
					/* currently we take first */

					LegoPlatformMessage decisionMessage;
					if(EnumSet.of(LegoPlatformMessageTypeEnum.RIGHT, LegoPlatformMessageTypeEnum.LEFT).contains(currentPlatformState)){
						List<LegoPlatformMessageTypeEnum> filteredList = decisionList.stream()
								.filter(e -> !e.equals(currentPlatformState))
								.collect(Collectors.toList());
						if(filteredList.contains(LegoPlatformMessageTypeEnum.MOVE)){
							decisionMessage = new LegoPlatformMessage(LegoPlatformMessageTypeEnum.MOVE);
						} else {
							decisionMessage = new LegoPlatformMessage(decisionList.get(0));
						}
					} else {
						decisionMessage = new LegoPlatformMessage(decisionList.get(0));
					}
					currentPlatformState = decisionMessage.getType();
					System.err.println(getClass().getSimpleName() +  " DECISION TAKEN: " + decisionMessage
							+ " decisionValues:" + Arrays.toString(decisionValues));
					sendMessage(decisionMessage);
					eraseDecisionValues();

				}


			}

		}

    }

    //Private Methods
	private void eraseDecisionValues() {
		this.decisionValues = new double[DECISION_VALUES];
	}

	private boolean decisionValuesReady() {
		return Arrays.stream(decisionValues).filter(e -> e > 0.0).count() == DECISION_VALUES;
	}

	private void sendMessage(LegoPlatformMessage message) {
		System.err.println(getClass().getSimpleName() + " sendMessage message: " + message + ", target: " + target);
		System.err.println(getClass().getSimpleName() + " sendMessage reference: " + getContext());
		getContext().getReference(target).sendMessage(message);
	}



}
