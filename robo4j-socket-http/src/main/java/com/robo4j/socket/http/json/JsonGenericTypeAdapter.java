package com.robo4j.socket.http.json;

import com.robo4j.socket.http.dto.ClassGetSetDTO;
import com.robo4j.socket.http.util.JsonUtil;
import com.robo4j.socket.http.util.ReflectUtils;

import java.util.Map;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class JsonGenericTypeAdapter<T> extends JsonAdapter<T> {

    private Map<String, ClassGetSetDTO> descriptorMap;


    public JsonGenericTypeAdapter(Class<T> clazz) {
        this.descriptorMap = ReflectUtils.getFieldsTypeMap(clazz);
    }

    @Override
    protected String internalAdapt(T obj) {
       return JsonUtil.toJson(descriptorMap, obj);
    }
}
