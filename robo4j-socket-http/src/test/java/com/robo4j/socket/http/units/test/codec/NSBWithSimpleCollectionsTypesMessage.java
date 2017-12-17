package com.robo4j.socket.http.units.test.codec;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class NSBWithSimpleCollectionsTypesMessage {

    private Integer number;
    private String message;
    private Boolean active;
    private String[] array;
    private List<String> list;
    private Map<String, String> map;

    public NSBWithSimpleCollectionsTypesMessage() {
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

    public String[] getArray() {
        return array;
    }

    public void setArray(String[] array) {
        this.array = array;
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NSBWithSimpleCollectionsTypesMessage that = (NSBWithSimpleCollectionsTypesMessage) o;
        return Objects.equals(number, that.number) &&
                Objects.equals(message, that.message) &&
                Objects.equals(active, that.active) &&
                Arrays.equals(array, that.array) &&
                Objects.equals(list, that.list) &&
                Objects.equals(map, that.map);
    }

    @Override
    public int hashCode() {

        int result = Objects.hash(number, message, active, list, map);
        result = 31 * result + Arrays.hashCode(array);
        return result;
    }

    @Override
    public String toString() {
        return "NSBWithSimpleCollectionsTypesMessage{" +
                "number=" + number +
                ", message='" + message + '\'' +
                ", active=" + active +
                ", array=" + Arrays.toString(array) +
                ", list=" + list +
                ", map=" + map +
                '}';
    }
}
