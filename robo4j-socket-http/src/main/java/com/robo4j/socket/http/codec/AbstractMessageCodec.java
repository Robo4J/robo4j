package com.robo4j.socket.http.codec;

import com.robo4j.socket.http.dto.ClassGetSetDTO;
import com.robo4j.socket.http.units.HttpDecoder;
import com.robo4j.socket.http.units.HttpEncoder;
import com.robo4j.socket.http.util.JsonUtil;
import com.robo4j.socket.http.util.ReflectUtils;

import java.util.Map;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public abstract class AbstractMessageCodec<T>  implements HttpDecoder<T>, HttpEncoder<T> {
    private final Map<String, ClassGetSetDTO> fieldMethodMap;
    private final Class<T> clazz;

    AbstractMessageCodec(Class<T> clazz) {
        this.clazz = clazz;
        this.fieldMethodMap = ReflectUtils.getFieldsTypeMap(clazz);
    }

    @Override
    public Class<T> getDecodedClass() {
        return clazz;
    }

    @Override
    public Class<T> getEncodedClass() {
        return clazz;
    }

    @Override
    public T decode(String json) {
        return  ReflectUtils.createInstanceSetterByFieldMap(clazz, getFieldMethodMap(),
                JsonUtil.getMapByJson(json));
    }

    @Override
    public String encode(T message) {
        return ReflectUtils.createJsonByFieldClassGetter(getFieldMethodMap(), message);
    }

    private Map<String, ClassGetSetDTO> getFieldMethodMap() {
        return fieldMethodMap;
    }
}
