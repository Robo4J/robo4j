package com.robo4j.socket.http.units.test.codec;

import com.robo4j.socket.http.units.test.enums.TestCommandEnum;

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
