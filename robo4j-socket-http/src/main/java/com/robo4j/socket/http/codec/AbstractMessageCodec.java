package com.robo4j.socket.http.codec;

import com.oracle.javafx.jmx.json.JSONDocument;
import com.oracle.javafx.jmx.json.JSONReader;
import com.oracle.javafx.jmx.json.impl.JSONStreamReaderImpl;
import com.robo4j.socket.http.units.HttpDecoder;
import com.robo4j.socket.http.units.HttpEncoder;
import com.robo4j.socket.http.util.ReflectUtils;

import java.io.StringReader;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public abstract class AbstractMessageCodec<T>  implements HttpDecoder<T>, HttpEncoder<T> {
    private final Class<T> clazz;

    AbstractMessageCodec(Class<T> clazz) {
        this.clazz = clazz;
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
        StringReader sr = new StringReader(json);
        JSONReader jsonReader = new JSONStreamReaderImpl(sr);
        JSONDocument document = jsonReader.build();
        return  ReflectUtils.createInstanceSetterByJSONDocument(clazz, document);
    }

    @Override
    public String encode(T message) {
        return ReflectUtils.createJson(message);
    }

}
