package com.robo4j.socket.http.units;

import com.robo4j.socket.http.HttpMethod;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 *
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class ServerPathMethod {

    private final String roboUnit;
    private final HttpMethod method;
    private final List<String> filters;

    public ServerPathMethod(String roboUnit, HttpMethod method){
        this.roboUnit = roboUnit;
        this.method = method;
        this.filters = null;
    }

    public ServerPathMethod(String roboUnit, HttpMethod method, List<String> filters) {
        this.roboUnit = roboUnit;
        this.method = method;
        this.filters = filters;
    }

    public String getRoboUnit() {
        return roboUnit;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public List<String> getFilters() {
        return Collections.unmodifiableList(filters);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerPathMethod that = (ServerPathMethod) o;
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
        return "ServerPathMethod{" +
                "roboUnit='" + roboUnit + '\'' +
                ", method=" + method +
                ", filters=" + filters +
                '}';
    }
}
