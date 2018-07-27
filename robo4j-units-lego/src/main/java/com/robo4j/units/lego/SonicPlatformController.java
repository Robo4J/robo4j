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

package com.robo4j.units.lego;

import com.robo4j.ConfigurationException;
import com.robo4j.CriticalSectionTrait;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.units.lego.enums.LegoPlatformMessageTypeEnum;
import com.robo4j.units.lego.gripper.GripperEnum;
import com.robo4j.units.lego.sonic.SonicSensorMessage;
import com.robo4j.util.StringConstants;

import java.util.EnumSet;
import java.util.Random;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
@CriticalSectionTrait
public class SonicPlatformController extends RoboUnit<SonicSensorMessage> {

    public static final String TARGET = "target";
    public static final String TARGET_GRIPPER = "targetGripper";
    private static final String VALUE_INFINITY = "Infinity";
    private static final String VALUE_SEPARATOR = ",";
    private static final float OMEGA = 0.833f;  // rad/second
    private static final float LARGE_MOTOR_RADIUS = 1f;
    private static final float DISTANCE_MIN = 0.4f;
    private static final float DISTANCE_OPTIMAL = 0.9f;
    private static final float DISTANCE_MAX = 2.5f;
    private static final Random random = new Random();
    private static final EnumSet<LegoPlatformMessageTypeEnum> rotationTypes = EnumSet.of(LegoPlatformMessageTypeEnum.LEFT, LegoPlatformMessageTypeEnum.RIGHT);


    private String target;
    private String targetGripper;
    private LegoPlatformMessageTypeEnum currentMessage = LegoPlatformMessageTypeEnum.STOP;

    public SonicPlatformController(RoboContext context, String id) {
        super(SonicSensorMessage.class, context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        target = configuration.getString(TARGET, null);
        targetGripper = configuration.getString(TARGET_GRIPPER, null);
        if (target == null) {
            throw ConfigurationException.createMissingConfigNameException("target");
        }
    }

    @Override
    public void onMessage(SonicSensorMessage message) {
        String value = message.getDistance().replace(VALUE_SEPARATOR, StringConstants.EMPTY);
        float distanceValue = VALUE_INFINITY.equals(value) ? DISTANCE_MAX : Float.parseFloat(value);
        System.out.println(getClass().getSimpleName() + " value: " + distanceValue);


        if(rotationTypes.contains(currentMessage) && distanceValue < DISTANCE_OPTIMAL){
            sendMessageToGripper();
        } else if(distanceValue > DISTANCE_MIN){
            sendPlatformMessage(LegoPlatformMessageTypeEnum.MOVE);
        } else {
            int randomValue = random.nextInt(2) + 3;
            LegoPlatformMessageTypeEnum rotationMessage = LegoPlatformMessageTypeEnum.getById(randomValue);
            sendPlatformMessage(rotationMessage);
        }
    }

    @Override
    public void stop() {
        sendPlatformMessage(LegoPlatformMessageTypeEnum.STOP);
        super.stop();
    }

    private void sendPlatformMessage(LegoPlatformMessageTypeEnum type){
        if(!currentMessage.equals(type)){
            System.out.println("SEND: MESSAGE TO PLATFORM: " + type);
            currentMessage = type;
            getContext().getReference(target).sendMessage(type);
        }
    }

    private void sendMessageToGripper(){
        System.out.println("SEND: MESSAGE TO GRIPPER");
        getContext().getReference(targetGripper).sendMessage(GripperEnum.OPEN_CLOSE);
    }



}
