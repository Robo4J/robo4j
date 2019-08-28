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
 * The JFR event definition for a full scan event.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */

@Name("robo4j.math.Scan")
@Category({ "Robo4J", "Math", "Scan" })
@Label("Scan Event")
@Description("An event for a full scan")
@StackTrace(false)
public class ScanEvent extends Event {

	@Label("Scan Left/Right")
	@Description("False if left->right, true if right->left")
	private boolean scanLeftRight;

	@Label("Scan Info")
	@Description("Textual information about the scan performed")
	private String scanInfo;

	@Label("Scan Id")
	@Description("The numerical identifier, uniquely identifying the scan")
	@ScanId
	private int scanId;

	static {
		FlightRecorder.register(ScanEvent.class);
	}

	public ScanEvent(int scanID, String scanMode) {
		setScanID(scanID);
		setScanInfo(scanMode);
	}

	public void setScanLeftRight(boolean scanLeftRight) {
		this.scanLeftRight = scanLeftRight;
	}

	public boolean getScanLeftRight() {
		return scanLeftRight;
	}

	public void setScanID(int scanID) {
		this.scanId = scanID;
	}

	public int getScanID() {
		return scanId;
	}

	public void setScanInfo(String scanMode) {
		this.scanInfo = scanMode;
	}

	public String getScanInfo() {
		return this.scanInfo;
	}
}
