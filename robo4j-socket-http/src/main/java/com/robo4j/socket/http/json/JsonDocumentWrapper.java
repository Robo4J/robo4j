package com.robo4j.socket.http.json;

import java.util.Objects;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class JsonDocumentWrapper {
    private String name;
    private JsonDocument document;

    public JsonDocumentWrapper(String name, JsonDocument document) {
        this.name = name;
        this.document = document;
    }

    public String getName() {
        return name;
    }

    public JsonDocument getDocument() {
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
