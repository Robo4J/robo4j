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
package com.robo4j.socket.http.json;

import java.util.Objects;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class JsonDocumentWrapper {
    private String name;
    private JsonDocument document;

    JsonDocumentWrapper(String name, JsonDocument document) {
        this.name = name;
        this.document = document;
    }

    String getName() {
        return name;
    }

    JsonDocument getDocument() {
        return document;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonDocumentWrapper that = (JsonDocumentWrapper) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(document, that.document);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, document);
    }

    @Override
    public String toString() {
        return "JsonDocumentWrapper{" +
                "name='" + name + '\'' +
                ", document=" + document +
                '}';
    }
}
