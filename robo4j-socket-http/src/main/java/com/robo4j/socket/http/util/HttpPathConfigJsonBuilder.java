package com.robo4j.socket.http.util;


import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.dto.ClassGetSetDTO;
import com.robo4j.socket.http.dto.ServerUnitPathDTO;
import com.robo4j.util.Utf8Constant;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Util class used to build json path configuration by separated paths
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class HttpPathConfigJsonBuilder {

    private final Map<String, ClassGetSetDTO> descriptorMap;
    private final List<ServerUnitPathDTO> paths = new LinkedList<>();

    private HttpPathConfigJsonBuilder() {
        descriptorMap = ReflectUtils.getFieldsTypeMap(ServerUnitPathDTO.class);
    }

    public static HttpPathConfigJsonBuilder Builder(){
        return new HttpPathConfigJsonBuilder();
    }

    public HttpPathConfigJsonBuilder addPath(String roboUnit, HttpMethod method){
        ServerUnitPathDTO document = new ServerUnitPathDTO(roboUnit, method);
        paths.add(document);
        return this;
    }

    public HttpPathConfigJsonBuilder addPath(String roboUnit, HttpMethod method, List<String> filters){
        ServerUnitPathDTO document = new ServerUnitPathDTO(roboUnit, method, filters);
        paths.add(document);
        return this;
    }

    public String build(){
        return new StringBuilder().append(Utf8Constant.UTF8_SQUARE_BRACKET_LEFT)
                .append(paths.stream().map(e -> ReflectUtils.createJson(descriptorMap, e)).collect(Collectors.joining(Utf8Constant.UTF8_COMMA)))
                .append(Utf8Constant.UTF8_SQUARE_BRACKET_RIGHT)
                .toString();
    }
}
