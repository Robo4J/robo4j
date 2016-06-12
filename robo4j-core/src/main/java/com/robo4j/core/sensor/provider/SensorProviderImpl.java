package com.robo4j.core.sensor.provider;

import com.robo4j.core.lego.LegoBrickRemote;
import com.robo4j.core.lego.rmi.LegoUnitProviderUtil;
import com.robo4j.core.sensor.SensorType;
import com.robo4j.core.sensor.state.SensorDefaultState;
import com.robo4j.core.sensor.state.SensorState;
import lejos.remote.ev3.RMISampleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;

/**
 *
 * Sensor Provider Implementation represent value/values , sensor mode and type
 *
 * Created by miroslavkopecky on 15/02/16.
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

    public SensorState connect(SensorType type){
        final RMISampleProvider sp = LegoUnitProviderUtil.getRMISampleProvider(legoBrickRemote, type);
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
    private SensorState getState(SensorType type, String value){
        return new SensorDefaultState(type, System.currentTimeMillis(), value, DEFAULT_PRIORITY);
    }


}
