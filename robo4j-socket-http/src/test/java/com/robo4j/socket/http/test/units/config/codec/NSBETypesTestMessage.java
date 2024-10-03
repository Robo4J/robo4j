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
package com.robo4j.socket.http.test.units.config.codec;

import com.robo4j.socket.http.test.units.config.enums.TestCommandEnum;

import java.util.Objects;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class NSBETypesTestMessage {

    private Integer number;
    private String message;
    private Boolean active;
    private TestCommandEnum command;

    public NSBETypesTestMessage() {
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public TestCommandEnum getCommand() {
        return command;
    }

    public void setCommand(TestCommandEnum command) {
        this.command = command;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NSBETypesTestMessage that = (NSBETypesTestMessage) o;
        return Objects.equals(number, that.number) &&
                Objects.equals(message, that.message) &&
                Objects.equals(active, that.active) &&
                command == that.command;
    }

    @Override
    public int hashCode() {

        return Objects.hash(number, message, active, command);
    }

    @Override
    public String toString() {
        return "NSBETypesTestMessage{" +
                "number=" + number +
                ", message='" + message + '\'' +
                ", active=" + active +
                ", command=" + command +
                '}';
    }
}
