package com.robo4j.socket.http.json;

import com.robo4j.util.Utf8Constant;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class JsonStringAdapter extends JsonAdapter<String> {

    @Override
    public String internalAdapt(String obj) {
        return new StringBuilder()
                .append(Utf8Constant.UTF8_QUOTATION_MARK)
                .append(obj)
                .append(Utf8Constant.UTF8_QUOTATION_MARK)
                .toString();
    }
}
