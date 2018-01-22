package com.robo4j.socket.http.json;

import com.robo4j.socket.http.util.JsonElementStringBuilder;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class JsonEnumAdapter extends JsonAdapter<Enum<?>> {

    @Override
    protected String internalAdapt(Enum<?> obj) {
        return JsonElementStringBuilder.Builder().addQuotation(obj.name()).build();
    }
}
