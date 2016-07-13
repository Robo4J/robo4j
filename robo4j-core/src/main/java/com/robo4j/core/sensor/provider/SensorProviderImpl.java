/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This SensorProviderImpl.java is part of robo4j.
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

package com.robo4j.core.sensor.provider;

import com.robo4j.core.lego.rmi.LegoUnitProviderUtil;
import com.robo4j.core.sensor.state.SensorDefaultState;
import com.robo4j.core.sensor.state.SensorState;
import com.robo4j.lego.control.LegoBrickRemote;
import com.robo4j.lego.enums.LegoSensorEnum;
import com.robo4j.lego.enums.LegoSensorPortEnum;
import lejos.remote.ev3.RMISampleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;

/**
 *
 * Sensor Provider Implementation represent value/values , sensor mode and type
 *
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 15.02.2016
 */
public class SensorProviderImpl implements SensorProvider {

    private static final Logger logger = LoggerFactory.getLogger(SensorProviderImpl.class);
    private static final int DEFAULT_PRIORITY = 1;
    private static final int DEFAULT_POSITION = 0;
    private static final String ERROR_STATE = "ERROR";
    private static final String DELIMITER = ",";
    private volatile LegoBrickRemote legoBrickRemote;

    public SensorProviderImpl(LegoBrickRemote legoBrickRemote) {
        this.legoBrickRemote = legoBrickRemote;
    }

    public SensorState connect(LegoSensorEnum type, LegoSensorPortEnum port){
        final RMISampleProvider sp = LegoUnitProviderUtil.getRMISampleProvider(legoBrickRemote, type, port);
        try {
            final StringBuilder sbFinalValue= new StringBuilder().append(sp.fetchSample()[DEFAULT_POSITION]) ;
            for(int i=1; i<type.getElements();i++){
                sbFinalValue.append(DELIMITER);
                sbFinalValue.append(sp.fetchSample()[i]);
            }
            sp.close();
            return getState(type, sbFinalValue.toString());
        } catch (RemoteException e) {
            logger.error(e.getMessage());
            return getState(type, ERROR_STATE);
        }


    }

    //Private Methods
    private SensorState getState(LegoSensorEnum type, String value){
        return new SensorDefaultState(type, System.currentTimeMillis(), value, DEFAULT_PRIORITY);
    }


}
