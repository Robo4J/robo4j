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
package com.robo4j.math.jfr;

import com.oracle.jrockit.jfr.EventDefinition;
import com.oracle.jrockit.jfr.EventToken;
import com.oracle.jrockit.jfr.TimedEvent;

/**
 * The JFR event definition for a feature extraction.
 * This allows us to know how much time such a pass has taken.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
@SuppressWarnings("deprecation")
@EventDefinition(path = "robo4j/math/featureextraction", name = "FeatureExtraction", description = "An event for a extracting features from a 2D scan.", stacktrace = false, thread = true) 
public class FeatureExtractionEvent extends TimedEvent {
	private static final EventToken EVENT_TOKEN;
	private final int noOfPoints;
	private final float angularResolution;
 
    static {
		EVENT_TOKEN = JfrUtils.register(FeatureExtractionEvent.class);
    }
     
    public FeatureExtractionEvent(int noOfPoints, float angularResolution) {
    	super(EVENT_TOKEN);
		this.noOfPoints = noOfPoints;
		this.angularResolution = angularResolution;
    }

	public int getNoOfPoints() {
		return noOfPoints;
	}

	public float getAngularResolution() {
		return angularResolution;
	}    
}
