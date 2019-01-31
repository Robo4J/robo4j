/*
 * Copyright (c) 2014, 2019, Marcus Hirt, Miroslav Wengner
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

import jdk.jfr.*;
/**
 * The JFR event definition for a feature extraction.
 * This allows us to know how much time such a pass has taken.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
@Name("robo4j.math.FeatureExtraction")
@Category ({"Robo4J", "Math"})
@Label("FeatureExtraction")
@Description("An event for a extracting features from a 2D scan")
@StackTrace(false) 
public class FeatureExtractionEvent extends Event {
	@Label("No of Points")
	private final int noOfPoints;
	@Label("Angular Resolution")
	private final float angularResolution;
 
    static {
		FlightRecorder.register(FeatureExtractionEvent.class);
    }
     
    public FeatureExtractionEvent(int noOfPoints, float angularResolution) {
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
