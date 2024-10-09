/*
 * Copyright (c) 2014, 2024, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.units.lego.controller;

import com.robo4j.ConfigurationException;
import com.robo4j.LifecycleState;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.hw.lego.ILegoMotor;
import com.robo4j.hw.lego.enums.AnalogPortEnum;
import com.robo4j.hw.lego.enums.MotorTypeEnum;
import com.robo4j.hw.lego.provider.MotorProvider;
import com.robo4j.hw.lego.wrapper.MotorWrapper;
import com.robo4j.units.lego.gripper.GripperEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class GripperController extends RoboUnit<GripperEnum> {
    public static final int ROTATION = 40;
    public static final String MOTOR_PORT = "motorPort";
    public static final String MOTOR_TYPE = "motorType";
    private static final Logger LOGGER = LoggerFactory.getLogger(GripperController.class);
    protected volatile ILegoMotor gripperMotor;
    private final Lock processLock = new ReentrantLock();
    private final Condition processCondition = processLock.newCondition();
    private final AtomicBoolean active = new AtomicBoolean();

    public GripperController(RoboContext context, String id) {
        super(GripperEnum.class, context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        setState(LifecycleState.UNINITIALIZED);
        String motorPort = configuration.getString(MOTOR_PORT, AnalogPortEnum.A.getType());
        Character motorType = configuration.getCharacter(MOTOR_TYPE, MotorTypeEnum.NXT.getType());

        MotorProvider motorProvider = new MotorProvider();
        gripperMotor = new MotorWrapper<>(motorProvider, AnalogPortEnum.getByType(motorPort),
                MotorTypeEnum.getByType(motorType));
        setState(LifecycleState.INITIALIZED);
    }

    @Override
    public void onMessage(GripperEnum message) {
        if (!active.get()) {
            processMessage(message);
        }
    }

    private void processMessage(GripperEnum message) {
        active.set(true);
        switch (message) {
            case OPEN_CLOSE:
                getContext().getScheduler().execute(() -> {
                    rotateMotor(ROTATION);
                    rotateMotor(-ROTATION);
                    active.set(false);
                });
                break;
            case OPEN:
                getContext().getScheduler().execute(() -> {
                    rotateMotor(ROTATION);
                    active.set(false);
                });
                break;
            case CLOSE:
                getContext().getScheduler().execute(() -> {
                    rotateMotor(-ROTATION);
                    active.set(false);
                });
                break;
            default:
                LOGGER.error("not implemented option: {}", message);
        }
    }

    private void rotateMotor(int rotation) {
        processLock.lock();
        gripperMotor.rotate(rotation);
        try {
            while (gripperMotor.isMoving()) {
                processCondition.await();
            }
        } catch (InterruptedException e) {
            LOGGER.error("error:{}", e.getMessage(), e);
        } finally {
            processLock.unlock();
        }
    }
}
