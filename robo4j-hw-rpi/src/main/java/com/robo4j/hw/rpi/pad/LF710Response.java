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

/**
 * Logitech F710 response
 * Created by mirowengner on 05.05.17.
 */
public class LF710Response {

    private Long time;
    private Short amount;
    private LF710Part part;
    private LF710Input input;

    public LF710Response(Long time, Short amount, LF710Part part, LF710Input input) {
        this.time = time;
        this.amount = amount;
        this.part = part;
        this.input = input;
    }

    public Long getTime() {
        return time;
    }

    public Short getAmount() {
        return amount;
    }

    public LF710Part getPart() {
        return part;
    }

    public LF710Input getInput() {
        return input;
    }

    @Override
    public String toString() {
        return "LF710Response{" +
                "time=" + time +
                ", amount=" + amount +
                ", part=" + part +
                ", input=" + input +
                '}';
    }
}
