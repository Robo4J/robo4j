/*
 * Copyright (c) 2014, 2017, Miroslav Wengner, Marcus Hirt
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
package com.robo4j.core;

/**
 * Represents the life cycle of a RoboUnit.
 * 
 * <p>
 * UNINITIALIZED -> [INITIALIZED] -> STARTING -> STARTED -> STOPPING -> STOPPED
 * -> SHUTDOWN
 * <p>
 * Components which do not have any initialization parameters can skip entering
 * initialized. It is considered good form to self report when initialized.
 * <p>
 * Other valid transitions:
 * <p>
 * STOPPED -> STARTING
 * <p>
 * any state to FAILED is also OK.
 * <p>
 * any state change from SHUTDOWN and FAILED is invalid.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public enum LifecycleState {
	// @formatter:off
	UNINITIALIZED(0, Messages.getString("LifecycleState.STATE_NAME_UNINITIALIZED"), Messages.getString("LifecycleState.STATE_DESCRIPTION_UNINITIALIZED")),  //$NON-NLS-1$ //$NON-NLS-2$
	INITIALIZED(1, Messages.getString("LifecycleState.STATE_NAME_INITIALIZED"), Messages.getString("LifecycleState.STATE_DESCRIPTION_INITIALIZED")),  //$NON-NLS-1$ //$NON-NLS-2$
	STARTING(2, Messages.getString("LifecycleState.STATE_NAME_STARTING"), Messages.getString("LifecycleState.STATE_DESCRIPTION_STARTING")),  //$NON-NLS-1$ //$NON-NLS-2$
	STARTED(3, Messages.getString("LifecycleState.STATE_NAME_STARTED"), Messages.getString("LifecycleState.STATE_DESCRIPTION_STARTED")),  //$NON-NLS-1$ //$NON-NLS-2$
	STOPPING(4, Messages.getString("LifecycleState.STATE_NAME_STOPPING"), Messages.getString("LifecycleState.STATE_DESCRIPTION_STOPPING")),  //$NON-NLS-1$ //$NON-NLS-2$
	STOPPED(5, Messages.getString("LifecycleState.STATE_NAME_STOPPED"), Messages.getString("LifecycleState.STATE_DESCRIPTION_STOPPED")),  //$NON-NLS-1$ //$NON-NLS-2$
	SHUTTING_DOWN(6, Messages.getString("LifecycleState.STATE_NAME_SHUTTING_DOWN"), Messages.getString("LifecycleState.STATE_DESCRIPTION_SHUTTING_DOWN")),  //$NON-NLS-1$ //$NON-NLS-2$
	SHUTDOWN(7, Messages.getString("LifecycleState.STATE_NAME_SHUTDOWN"), Messages.getString("LifecycleState.STATE_DESCRIPTION_STOPPED")),  //$NON-NLS-1$ //$NON-NLS-2$
	FAILED(8, Messages.getString("LifecycleState.STATE_NAME_FAILED"), Messages.getString("LifecycleState.STATE_DESCRIPTION_FAILED")), 
	; //$NON-NLS-1$ //$NON-NLS-2$
	// @formatter:on

	private final Integer stateId;
	private final String localizedName;
	private final String description;

	LifecycleState(Integer stateID, String localizedName, String description) {
		this.stateId = stateID;
		this.localizedName = localizedName;
		this.description = description;
	}

	/**
	 * Returns the numeric id of the state.
	 * 
	 * @return the numeric id of the state.
	 */
	public Integer getStateId() {
		return stateId;
	}

	/**
	 * @return the localized name of the
	 */
	public String getLocalizedName() {
		return localizedName;
	}

	public String getDescription() {
		return description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
		return localizedName;
	}
}
