/*
 * Copyright (c) 2014, 2023, Marcus Hirt, Miroslav Wengner
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

/**
 * Logitech F710 message
 * message represents the user action on the pad
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class LF710Message {

    private final Long time;
    private final Short amount;
    private final LF710Part part;
    private final LF710Input input;
    private final LF710State state;

    public LF710Message(Long time, Short amount, LF710Part part, LF710Input input, LF710State state) {
        this.time = time;
        this.amount = amount;
        this.part = part;
        this.input = input;
        this.state = state;
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

    public LF710State getState() {
        return state;
    }

    @Override
    public String toString() {
        return "LF710Message{" +
                "time=" + time +
                ", amount=" + amount +
                ", part=" + part +
                ", input=" + input +
                ", state=" + state +
                '}';
    }
}
