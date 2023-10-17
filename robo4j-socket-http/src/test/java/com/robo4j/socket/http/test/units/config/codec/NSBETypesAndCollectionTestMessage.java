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
package com.robo4j.socket.http.test.units.config.codec;

import com.robo4j.socket.http.test.units.config.enums.TestCommandEnum;

import java.util.List;
import java.util.Objects;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class NSBETypesAndCollectionTestMessage {

    private Integer number;
    private String message;
    private Boolean active;
    private TestCommandEnum command;
    private List<TestCommandEnum> commands;

    public NSBETypesAndCollectionTestMessage() {
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

    public List<TestCommandEnum> getCommands() {
        return commands;
    }

    public void setCommands(List<TestCommandEnum> commands) {
        this.commands = commands;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NSBETypesAndCollectionTestMessage that = (NSBETypesAndCollectionTestMessage) o;
        return Objects.equals(number, that.number) &&
                Objects.equals(message, that.message) &&
                Objects.equals(active, that.active) &&
                command == that.command &&
                Objects.equals(commands, that.commands);
    }

    @Override
    public int hashCode() {

        return Objects.hash(number, message, active, command, commands);
    }

    @Override
    public String toString() {
        return "NSBETypesAndCollectionTestMessage{" +
                "number=" + number +
                ", message='" + message + '\'' +
                ", active=" + active +
                ", command=" + command +
                ", commands=" + commands +
                '}';
    }
}
