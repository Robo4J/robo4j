/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This LegoBrickRemoteProvider.java is part of robo4j.
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

package com.robo4j.core.lego;

import com.robo4j.core.annotation.SystemProvider;
import com.robo4j.core.control.utils.ConnectionUtil;
import com.robo4j.core.lego.rmi.LegoUnitProviderUtil;
import lejos.remote.ev3.RemoteEV3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.rmi.NotBoundException;

/**
 * Created by miroslavkopecky on 20/10/14.
 *
 * Provides access only to the legoBrick Simple
 * uses RMI communication
 *
 */

//TODO: needs to be fixed -> remove static
@SystemProvider
public class LegoBrickRemoteProvider implements LegoBrickRemote {

    private static final Logger logger = LoggerFactory.getLogger(LegoBrickRemoteProvider.class);
    private volatile static LegoBrickRemoteProvider INSTANCE;
    private volatile static RemoteEV3 brick;
    private volatile static String addressIp;


    private LegoBrickRemoteProvider(final String address) {

        try{
            /* init cache 1st time */
            brick = LegoUnitProviderUtil.getBrick(address);
            addressIp = address;

        }catch (NotBoundException | IOException  e){
            throw new LegoException("Brick is Kaput in constructor ", e);
        }

    }


    public static LegoBrickRemoteProvider getInstance(String address){
        if(INSTANCE == null || (addressIp != null && !addressIp.equals(address))){
            synchronized (LegoBrickRemoteProvider.class){
                if(INSTANCE == null || (addressIp != null && !addressIp.equals(address))){
                    INSTANCE = new LegoBrickRemoteProvider(address);
                }
            }
        }
        logger.debug("getInstance instance = " + address);
        return INSTANCE;
    }

    @Override
    public RemoteEV3 getBrick(){

        try{
            if(ConnectionUtil.ping(addressIp)){
                return brick;
            } else {
                throw new LegoException("DEFAULT IP= "+ addressIp + " Brick is Kaput");
            }
        } catch (IOException e){
            throw new LegoException("Brick is Kaput in getBrick ", e);
        }
    }

    @Override
    public RemoteEV3 getBrick(String address){
        try {
            if(ConnectionUtil.ping(address) && (addressIp != null && !addressIp.equals(address))){
                brick = LegoUnitProviderUtil.getBrick(address);
            }
        } catch (IOException | NotBoundException e) {
            throw new LegoException("Brick is Kaput in getBrick ", e);
        }
        return brick;
    }

}
