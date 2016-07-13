/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This GuardianRunnable.java is part of robo4j.
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

package com.robo4j.core.guardian;

import com.robo4j.core.platform.provider.LegoBrickCommandsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Task is holding guarding about the specific sensor values
 * Guardian is capable to send signal to the platform
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 31.03.2016
 */
public class GuardianRunnable implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(GuardianRunnable.class);
    private static final int GUARDIAN_SLEEP = 1;
    private volatile AtomicBoolean active;
    private volatile AtomicBoolean emergency;
    private LegoBrickCommandsProvider legoBrickCommandsProvider;

    public GuardianRunnable(LegoBrickCommandsProvider legoBrickCommandsProvider, AtomicBoolean active,
                            AtomicBoolean emergency) {
        this.legoBrickCommandsProvider = legoBrickCommandsProvider;
        this.active = active;
        this.emergency = emergency;
    }

    public void setEmergency(boolean state){
        this.emergency.set(state);
    }

    @Override
    public void run() {
        while(active.get()){
            if(emergency.get()){
                try {
                    logger.info("EMERGENCY STOP IS HERE");
                    //TODO: enable emergency stop
//                    legoBrickCommandsProvider.process(LegoPlatformCommandType.EMERGENCY_STOP);
                    //time until emergency repeating;
                    TimeUnit.SECONDS.sleep(GUARDIAN_SLEEP);
//                } catch (RemoteException | InterruptedException e) {
                } catch (InterruptedException e) {
                    throw new GerundianException("Guardian problem: ", e);
                }
            }
        }
    }
}
