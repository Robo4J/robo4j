/*
 * Copyright (c) 2014, 2017, Marcus Hirt, Miroslav Wengner
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

package com.robo4j.units.rpi.pad;

import com.robo4j.ConfigurationException;
import com.robo4j.LifecycleState;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.hw.rpi.pad.LF710ButtonObserver;
import com.robo4j.hw.rpi.pad.LF710Message;
import com.robo4j.hw.rpi.pad.LF710Pad;
import com.robo4j.hw.rpi.pad.PadInputResponseListener;
import com.robo4j.hw.rpi.pad.RoboControlPad;
import com.robo4j.logging.SimpleLoggingUtil;

import java.nio.file.Paths;

/**
 * Logitech F710 Gamepad unit
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class LF710PadUnit extends RoboUnit<Object>{

    private RoboControlPad pad;
    private LF710ButtonObserver observer;
    private PadInputResponseListener listener;

    private String input;
    private String target;

    public LF710PadUnit(RoboContext context, String id) {
        super(Object.class, context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        input = configuration.getString("input", null);
        target = configuration.getString("target", null);

        if(input == null){
            throw ConfigurationException.createMissingConfigNameException("input");
        }
        if (target == null) {
            throw ConfigurationException.createMissingConfigNameException("target");
        }
        pad = new LF710Pad(Paths.get(input));
        setState(LifecycleState.INITIALIZED);
    }

    @Override
    public void start() {
        final RoboReference<LF710Message> targetRef = getContext().getReference(target);
        setState(LifecycleState.STARTING);
        pad.connect();
        observer = new LF710ButtonObserver(pad);
        listener = (LF710Message response) -> {
            if(getState() == LifecycleState.STARTED){
                if(targetRef == null){
                    SimpleLoggingUtil.info(getClass(), "PAD pressed: " + response);
                } else {
                    targetRef.sendMessage(response);
                }
            }
        };
        observer.addButtonListener(listener);
        setState(LifecycleState.STARTED);
    }

    @Override
    public void stop() {
        setState(LifecycleState.STOPPING);
        observer.removeButtonListener(listener);
        observer = null;
        listener = null;
        setState(LifecycleState.STOPPED);
    }

}
