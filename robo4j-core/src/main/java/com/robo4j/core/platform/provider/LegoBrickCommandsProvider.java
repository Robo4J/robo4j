/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This LegoBrickCommandsProvider.java is part of robo4j.
 *
 *     robo4j is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     robo4j is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.robo4j.core.platform.provider;


import com.robo4j.core.platform.command.LegoCommandProperty;
import com.robo4j.core.platform.command.LegoPlatformCommandEnum;

import java.rmi.RemoteException;

/**
 *
 * Interface that provides methods with implemented logic
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 27.09.2014
 */
public interface LegoBrickCommandsProvider {

    boolean process(LegoPlatformCommandEnum direction);

    boolean process(LegoPlatformCommandEnum direction, LegoCommandProperty property);

    void exit() throws RemoteException, InterruptedException;

    boolean isActive();

}
