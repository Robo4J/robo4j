/*
 * Copyright (c) 2014-2019, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.socket.http.units.test.codec;

import java.util.Objects;

/**
 * possible codec message contains noted types
 *
 * {@link Integer}
 * {@link String}
 * {@link Boolean}
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class NSBTypesTestMessage {

    private Integer number;
    private String message;
    private Boolean active;

    public NSBTypesTestMessage() {
    }

    public NSBTypesTestMessage(Integer number, String message, Boolean active) {
        this.number = number;
        this.message = message;
        this.active = active;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NSBTypesTestMessage message1 = (NSBTypesTestMessage) o;
        return Objects.equals(number, message1.number) &&
                Objects.equals(message, message1.message) &&
                Objects.equals(active, message1.active);
    }

    @Override
    public int hashCode() {

        return Objects.hash(number, message, active);
    }

    @Override
    public String toString() {
        return "NSBTypesTestMessage{" +
                "number=" + number +
                ", message='" + message + '\'' +
                ", active=" + active +
                '}';
    }
}
