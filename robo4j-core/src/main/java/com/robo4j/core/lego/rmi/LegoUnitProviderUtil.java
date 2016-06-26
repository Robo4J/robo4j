/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This LegoUnitProviderUtil.java is part of robo4j.
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

package com.robo4j.core.lego.rmi;

import com.robo4j.core.lego.LegoBrickRemote;
import com.robo4j.lego.enums.LegoSensorEnum;
import com.robo4j.core.system.dto.LegoEngineDTO;
import com.robo4j.lego.enums.LegoSensorPortEnum;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.remote.ev3.RMIRegulatedMotor;
import lejos.remote.ev3.RMISampleProvider;
import lejos.remote.ev3.RemoteEV3;

import java.io.IOException;
import java.rmi.NotBoundException;

import static com.robo4j.core.system.util.SystemUtil.getType;

/**
 *
 * Util to create specific Unit on the platform
 *
 * Created by miroslavkopecky on 04/05/16.
 */
public final class LegoUnitProviderUtil {

    public static RMIRegulatedMotor createRMIEngine(final LegoBrickRemote legoBrickRemote, final LegoEngineDTO engine) {


        return legoBrickRemote.getBrick()
                .createRegulatedMotor(getType(engine.getPort()), getType(engine.getType()));
    }

    public static EV3TouchSensor createTouchSensor(final LegoBrickRemote legoBrickRemote, final LegoSensorPortEnum port){
        final Port portTouchSensor = legoBrickRemote.getBrick().getPort(port.getType());
        return new EV3TouchSensor(portTouchSensor);
    }
    public static RemoteEV3 getBrick(String address) throws IOException,  NotBoundException{
        RemoteEV3 result = new RemoteEV3(address);
        result.setDefault();
        return result;
    }

    public static RMISampleProvider getRMISampleProvider(final LegoBrickRemote legoBrickRemote,
                                                         final LegoSensorEnum type, final LegoSensorPortEnum port){
        return legoBrickRemote.getBrick().createSampleProvider(port.getType(), type.getSource(), type.getMode());
    }
}