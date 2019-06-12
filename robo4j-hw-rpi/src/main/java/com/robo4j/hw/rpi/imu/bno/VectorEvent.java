/*
 * Copyright (c) 2014, 2019, Marcus Hirt, Miroslav Wengner
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

package com.robo4j.hw.rpi.imu.bno;

/**
 * VectorEvent used for Rotation, Game vector event
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class VectorEvent {

    public enum Type {
        ROTATION, GAME;
    }

    private final Type type;
    private final float quatI;
    private final float quatJ;
    private final float quatK;
    private final float quatReal;
    private final float radianAccuracy;

    public VectorEvent(Type type, float quatI, float quatJ, float quatK, float quatReal, float radianAccuracy) {
        this.type = type;
        this.quatI = quatI;
        this.quatJ = quatJ;
        this.quatK = quatK;
        this.quatReal = quatReal;
        this.radianAccuracy = radianAccuracy;
    }

    public Type getType() {
        return type;
    }

    public float getQuatI() {
        return quatI;
    }

    public float getQuatJ() {
        return quatJ;
    }

    public float getQuatK() {
        return quatK;
    }

    public float getQuatReal() {
        return quatReal;
    }

    public float getRadianAccuracy() {
        return radianAccuracy;
    }

    @Override
    public String toString() {
        return "VectorEvent{" +
                "type=" + type +
                ", quatI=" + quatI +
                ", quatJ=" + quatJ +
                ", quatK=" + quatK +
                ", quatReal=" + quatReal +
                ", radianAccuracy=" + radianAccuracy +
                '}';
    }
}
