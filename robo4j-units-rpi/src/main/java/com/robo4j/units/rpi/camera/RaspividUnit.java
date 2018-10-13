/*
 * Copyright (c) 2014, 2018, Marcus Hirt, Miroslav Wengner
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

package com.robo4j.units.rpi.camera;

import com.robo4j.ConfigurationException;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.hw.rpi.camera.RaspiDevice;
import com.robo4j.logging.SimpleLoggingUtil;

/**
 * Unit generates the video stream on desired socket
 *
 * @link https://www.raspberrypi.org/app/uploads/2013/07/RaspiCam-Documentation.pdf
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class RaspividUnit extends RoboUnit<RaspividRequest> {

    public static final String NAME = "raspividUnit";
    public static final String PROPERTY_SERVER_IP = "serverIp";
    public static final String PROPERTY_SERVER_PORT = "serverPort";

    private final RaspiDevice device = new RaspiDevice();
    private String output;
    private String processId;

    public RaspividUnit(RoboContext context, String id) {
        super(RaspividRequest.class, context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        final String serverIp = configuration.getString(PROPERTY_SERVER_IP, null);
        if (serverIp == null) {
            throw ConfigurationException.createMissingConfigNameException(PROPERTY_SERVER_PORT);
        }

        final String serverPort = configuration.getString(PROPERTY_SERVER_PORT, null);
        if (serverPort == null) {
            throw ConfigurationException.createMissingConfigNameException(PROPERTY_SERVER_PORT);
        }

        output = "tcp://".concat(serverIp).concat(":").concat(serverPort);
    }

    @Override
    public void onMessage(RaspividRequest message) {
        switch (message.getType()){
            case CONFIG:
                message.put(RpiCameraProperty.OUTPUT, output);
                processId = String.valueOf(device.executeCommandReturnPID(message.create()));
                break;
            case START:
                SimpleLoggingUtil.info(getClass(), "not necessary start message: " + message);
                break;
            case STOP:
                stop();
                break;
            default:
                SimpleLoggingUtil.error(getClass(), "message: " + message);
        }

    }

    @Override
    public void stop() {
        super.stop();
        device.executeCommand("kill " + processId);
        System.out.println("stop raspivid : " + processId);
    }
}
