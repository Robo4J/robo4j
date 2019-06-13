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

package com.robo4j.hw.rpi.i2c.adafruitoled;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class BarElement {

    private final int x;
    private final int y;
    private final BiColor color;

    public BarElement(int x, int y, BiColor color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public BarElement(int x, int y) {
        this.x = x;
        this.y = y;
        this.color = BiColor.OFF;
    }

    public BarElement(int x, BiColor color) {
        this.x = x;
        this.y = 0;
        this.color = color;
    }

    public BarElement(int x){
        this.x = x;
        this.y = 0;
        this.color = BiColor.OFF;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public BiColor getColor() {
        return color;
    }

    @Override
    public String toString() {
        return "BarElement{" +
                "x=" + x +
                ", y=" + y +
                ", color=" + color +
                '}';
    }
}
