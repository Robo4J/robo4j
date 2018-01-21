package com.robo4j.socket.http.dto;

import com.robo4j.socket.http.HttpMethod;

import java.util.List;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ServerPathDTO {

    private String roboUnit;
    private HttpMethod method;
    private List<String> filters;

    public ServerPathDTO() {
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
    public String toString() {
        return "ServerPathDTO{" +
                "roboUnit='" + roboUnit + '\'' +
                ", method='" + method + '\'' +
                ", filters=" + filters +
                '}';
    }
}
