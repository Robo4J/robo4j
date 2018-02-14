package com.robo4j.socket.http.dto;

import com.robo4j.socket.http.HttpMethod;

import java.util.List;
import java.util.Objects;

/**
 *
 * roboUnit - roboUnit
 * method - appropriated HTTP method, UDP case is null
 * List - list of filters
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ServerUnitPathDTO {

    private String roboUnit;
    private HttpMethod method;
    private List<String> filters;

    public ServerUnitPathDTO() {
    }

    public ServerUnitPathDTO(String roboUnit, HttpMethod method) {
        this.roboUnit = roboUnit;
        this.method = method;
    }

    public ServerUnitPathDTO(String roboUnit, HttpMethod method, List<String> filters) {
        this.roboUnit = roboUnit;
        this.method = method;
        this.filters = filters;
    }

    public String getRoboUnit() {
        return roboUnit;
    }

    public void setRoboUnit(String roboUnit) {
        this.roboUnit = roboUnit;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public List<String> getFilters() {
        return filters;
    }

    public void setFilters(List<String> filters) {
        this.filters = filters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerUnitPathDTO that = (ServerUnitPathDTO) o;
        return Objects.equals(roboUnit, that.roboUnit) &&
                method == that.method &&
                Objects.equals(filters, that.filters);
    }

    @Override
    public int hashCode() {

        return Objects.hash(roboUnit, method, filters);
    }

    @Override
    public String toString() {
        return "ServerUnitPathDTO{" +
                "roboUnit='" + roboUnit + '\'' +
                ", method=" + method +
                ", filters=" + filters +
                '}';
    }
}
