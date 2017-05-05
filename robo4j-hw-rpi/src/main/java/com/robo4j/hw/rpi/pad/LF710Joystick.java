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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.hw.rpi.pad;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Logitech F710 Gamepad implementation
 *
 * Created by mirowengner on 05.05.17.
 */
public class LF710Joystick {

    private static final String REGISTERED_INPUT = "/dev/input/js0";
    private static final int ANDING_LEFT = 0x00ff;
    private static final int ANDING_LONG_LEFT = 0x00000000000000ff;
    private static final int BUFFER_SIZE = 8;
    private static final int INDEX_START = 0;
    private static final int INDEX_TIME_1 = 1;
    private static final int INDEX_TIME_2 = 2;
    private static final int INDEX_TIME_3 = 3;
    private static final int INDEX_AMOUNT_4 = 4;
    private static final int INDEX_AMOUNT_5 = 5;
    private static final int INDEX_PART = 6;
    private static final int INDEX_ELEMENT = 7;
    private final Path gamepadPath = Paths.get(REGISTERED_INPUT);
    private final byte[] buffer = new byte[BUFFER_SIZE];
    private InputStream inputStream;
    private boolean active;

    public LF710Joystick() {
    }

    public void activate(){
        try {
            inputStream = Files.newInputStream(gamepadPath);
            active = true;
        } catch (IOException e) {
            throw new LF710Exception("gamepad problem", e);
        }
    }

    public LF710Response getState(){
        try {
            int bytes = inputStream.read(buffer);
            if(bytes == BUFFER_SIZE){
                final long time = ((((((buffer[INDEX_TIME_3] & ANDING_LONG_LEFT) << BUFFER_SIZE) | (buffer[INDEX_TIME_2] &
                        ANDING_LEFT)) << BUFFER_SIZE) | (buffer[INDEX_TIME_1] & ANDING_LEFT)) << BUFFER_SIZE) |
                        (buffer[INDEX_START] & ANDING_LEFT);
                final short amount = (short) (((buffer[INDEX_AMOUNT_5] & ANDING_LEFT) << BUFFER_SIZE) | (buffer[INDEX_AMOUNT_4] & ANDING_LEFT));
                final short part = buffer[INDEX_PART];
                final short element = buffer[INDEX_ELEMENT];
                if(part > 0){
                    final LF710Part lf710Part = LF710Part.getByMask(part);
                    switch (lf710Part){
                        case BUTTON:
                            return new LF710Response(time, amount, lf710Part, LF710Button.getByMask(element), getInputState(amount));
                        case JOYSTICK:
                            return new LF710Response(time, amount, lf710Part, LF710JoystickButtons.getByMask(element), getInputState(amount));
                        default:
                            throw new LF710Exception("uknonw pad part:" + lf710Part);
                    }
                }
            }
        } catch (IOException e) {
            throw new LF710Exception("gamepad reading problem", e);
        }
        return null;
    }

    //Private Methods
    private LF710State getInputState(short amount){
        return (amount == 0) ? LF710State.RELEASED : LF710State.PRESSED;
    }
}
