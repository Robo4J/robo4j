package com.robo4j.socket.http.json;

import com.robo4j.socket.http.dto.ClassGetSetDTO;
import com.robo4j.socket.http.util.JsonElementStringBuilder;
import com.robo4j.socket.http.util.ReflectUtils;
import com.robo4j.socket.http.util.RoboReflectException;
import com.robo4j.socket.http.util.TypeMapper;
import com.robo4j.util.Utf8Constant;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
        JsonElementStringBuilder builder = JsonElementStringBuilder.Builder()
                .add(Utf8Constant.UTF8_CURLY_BRACKET_LEFT);

        builder.add(descriptorMap.entrySet().stream()
                .map(entry -> {
                    StringBuilder sb = new StringBuilder();
                    try {
                        Object val = entry.getValue().getGetMethod().invoke(obj);
                        if(val == null){
                            return null;
                        } else {
                            TypeMapper typeMapper = TypeMapper.getBySource(val.getClass());
                            JsonTypeAdapter adapter = typeMapper == null ?
                                    ReflectUtils.getJsonTypeAdapter(val.getClass()) : typeMapper.getAdapter();
                            return sb.append(Utf8Constant.UTF8_QUOTATION_MARK)
                                    .append(entry.getKey())
                                    .append(Utf8Constant.UTF8_QUOTATION_MARK)
                                    .append(Utf8Constant.UTF8_COLON)
                                    .append(adapter.adapt(val))
                                    .toString();
                        }
                    } catch (Exception e){
                        throw new RoboReflectException("adapter: " + descriptorMap + " sb: " + sb.toString(), e);
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.joining(Utf8Constant.UTF8_COMMA)));
        builder.add(Utf8Constant.UTF8_CURLY_BRACKET_RIGHT);
        return builder.build();
    }
}
