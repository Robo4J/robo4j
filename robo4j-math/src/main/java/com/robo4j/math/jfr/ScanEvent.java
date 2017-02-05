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
package com.robo4j.math.jfr;

import com.oracle.jrockit.jfr.EventDefinition;
import com.oracle.jrockit.jfr.EventToken;
import com.oracle.jrockit.jfr.TimedEvent;
import com.oracle.jrockit.jfr.ValueDefinition;

/**
 * The JFR event definition for a full scan event.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
@SuppressWarnings("deprecation")
@EventDefinition(path = "robo4j/overview/scanevent", name = "Scan", description = "An event for a full scan.", stacktrace = false, thread = true) 
public class ScanEvent extends TimedEvent {
	public static final String RELATIONAL_KEY_SCAN = "http://se.hirt.tank/scan";
	
    @ValueDefinition(name = "ScanLeftRight", description = "False if left->right, true if right->left.") 
    private boolean scanLeftRight;

    @ValueDefinition(name = "ScanInfo", description = "Textual information about the scan performed.") 
    private String scanInfo;
    
    @ValueDefinition(name = "Scan ID", description = "The numerical identifier, uniquely identifying the scan.", relationKey = RELATIONAL_KEY_SCAN)
    private int scanID;

	private static final EventToken EVENT_TOKEN;
 
    static {
		EVENT_TOKEN = JfrUtils.register(ScanEvent.class);
    }
     
    public ScanEvent(int scanID, String scanMode) {
    	super(EVENT_TOKEN);
    	setScanID(scanID);
    	setScanInfo(scanMode);
    }
    
    public void setScanLeftRight(boolean scanLeftRight) {
    	this.scanLeftRight = scanLeftRight;
    }
    
    // API does not support is
    public boolean getScanLeftRight() {
    	return scanLeftRight;
    }

	public void setScanID(int scanID) {
		this.scanID = scanID;
	}

	public int getScanID() {
		return scanID;
	}

	public void setScanInfo(String scanMode) {
		this.scanInfo = scanMode;
	}
	
	public String getScanInfo() {
		return this.scanInfo;
	}
}
