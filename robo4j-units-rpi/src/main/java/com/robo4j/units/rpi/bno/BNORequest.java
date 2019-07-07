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

package com.robo4j.units.rpi.bno;

import com.robo4j.RoboReference;
import com.robo4j.hw.rpi.imu.bno.DeviceEvent;

/**
 * BNORequest allows to register listener unit as the destination of values
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class BNORequest {
	/**
	 * target represents the receiver of the DeviceEvent
	 */
	private final RoboReference<DeviceEvent> target;
	private final ListenerAction listenerAction;

	public enum ListenerAction {
		/**
		 * Register target destination for {@link DeviceEvent}
		 */
		REGISTER,

		/**
		 * Unregister target destination for {@link DeviceEvent}
		 */
		UNREGISTER
	}

	/**
	 * 
	 * @param target
	 *            receiver of {@link DeviceEvent}
	 * @param listenerAction
	 *            listener action
	 */
	public BNORequest(RoboReference<DeviceEvent> target, ListenerAction listenerAction) {
		this.target = target;
		this.listenerAction = listenerAction;
	}

    public RoboReference<DeviceEvent> getTarget() {
        return target;
    }

    public ListenerAction getListenerAction() {
        return listenerAction;
    }

    @Override
    public String toString() {
        return "BNORequest{" +
                "target=" + target +
                ", listenerAction=" + listenerAction +
                '}';
    }
}
