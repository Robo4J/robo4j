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
package com.robo4j.hw.rpi.pad;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.IO.*;

/**
 * Java port of Logitech F710 Gamepad
 * optimised of raspberryPi
 * <p>
 * note: set the F710 pad to DirectInput by front button
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class LF710PadExample implements Runnable {
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
    private final Path inputPath = Paths.get(REGISTERED_INPUT);

    public static void main(String[] args) {
        println("start joystick logitech F710");
        LF710PadExample pad = new LF710PadExample();
        new Thread(pad).start();
    }

    public LF710PadExample() {

    }

    @Override
    public void run() {
        byte[] buffer = new byte[BUFFER_SIZE];
        try {
            final InputStream inputStream = Files.newInputStream(inputPath);
            int bytes;
            while ((bytes = inputStream.read(buffer)) > INDEX_START) {
                if (bytes == BUFFER_SIZE) {
                    final long time = ((((((buffer[INDEX_TIME_3] & ANDING_LONG_LEFT) << BUFFER_SIZE) | (buffer[INDEX_TIME_2] &
                            ANDING_LEFT)) << BUFFER_SIZE) | (buffer[INDEX_TIME_1] & ANDING_LEFT)) << BUFFER_SIZE) |
                            (buffer[INDEX_START] & ANDING_LEFT);
                    final short amount = (short) (((buffer[INDEX_AMOUNT_5] & ANDING_LEFT) << BUFFER_SIZE) | (buffer[INDEX_AMOUNT_4] & ANDING_LEFT));
                    final short part = buffer[INDEX_PART];
                    final short element = buffer[INDEX_ELEMENT];
                    if (part > 0) {
                        final LF710Part lf710Part = LF710Part.getByMask(part);
                        switch (lf710Part) {
                            case BUTTON:
                                println("BUTTON: %s state: %s time: %d".formatted(LF710Button.getByMask(element), getInputState(amount), time));
                                break;
                            case JOYSTICK:
                                println("JOYSTICK: %s state: %s time: %d".formatted(LF710JoystickButton.getByMask(element), getInputState(amount), time));
                                break;
                            default:
                                throw new RuntimeException("uknonw pad part:" + lf710Part);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("pad input error: " + e);
        }
    }

    //Private Methods
    private LF710State getInputState(short amount) {
        return (amount == 0) ? LF710State.RELEASED : LF710State.PRESSED;
    }

}
