/*
 * Copyright (c) 2014, 2017, Marcus Hirt, Miroslav Wengner
 *
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.socket.http.request;

import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.enums.StatusCode;

/**
 *  wrapper for http request result
 *  @see RoboRequestCallable
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public final class HttpResponseProcess implements ChannelResponseProcess<String> {
    private String path;
    private String target;
    private HttpMethod method;
    private StatusCode code;
    private Object result;

    HttpResponseProcess() {
    }

    @Override
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public HttpMethod getMethod() {
        return method;
    }

    @Override
    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public void setCode(StatusCode code){
        this.code = code;
    }

    public StatusCode getCode() {
        return code;
    }

    @Override
    public String toString() {
        return "HttpResponseProcess{" +
                "target='" + target + '\'' +
                ", method=" + method +
                ", code=" + code +
                ", result=" + result +
                '}';
    }
}
