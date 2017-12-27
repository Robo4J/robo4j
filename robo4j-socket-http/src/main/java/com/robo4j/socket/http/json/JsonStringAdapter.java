package com.robo4j.socket.http.json;

import com.robo4j.socket.http.util.JsonElementStringBuilder;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class JsonStringAdapter extends JsonAdapter<String> {

	@Override
	public String internalAdapt(String obj) {
		return JsonElementStringBuilder.Builder().addQuotation(obj).build();

	}
}
