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

package com.robo4j.hw.rpi.pad;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Logitech F710 Gamepad implementation
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class LF710Pad implements RoboControlPad {

    private final Path gamepadPath;
    private InputStream inputStream;
    private boolean active = false;

    public LF710Pad(Path gamepadPath) {
        this.gamepadPath = gamepadPath;
    }

    @Override
    public boolean connect(){
        try {
            inputStream = Files.newInputStream(gamepadPath);
            active = true;
            return active;
        } catch (IOException e) {
            throw new LF710Exception("gamepad problem", e);
        }
    }


    @Override
    public void disconnect() {
        try {
            inputStream.close();
        } catch (IOException e) {
            throw new LF710Exception("gamepad disconnecting problem", e);
        }
    }

    @Override
    public InputStream source() {
        return inputStream;
    }

}
