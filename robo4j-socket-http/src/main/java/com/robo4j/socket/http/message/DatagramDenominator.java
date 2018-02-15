package com.robo4j.socket.http.message;

import com.robo4j.socket.http.util.RoboHttpUtils;
import com.robo4j.util.Utf8Constant;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class DatagramDenominator implements MessageDenominator<byte[]> {
	private final StringBuilder sb = new StringBuilder();
	private final int type;
	private final String path;

	/**
	 * Constructor
	 * 
	 * @param type
	 *            denominator type
	 * @param path
	 *            requested path
	 */
	public DatagramDenominator(int type, String path) {
		this.type = type;
		this.path = path;
	}

	@Override
	public byte[] generate() {
        sb.append(type)
            .append(Utf8Constant.UTF8_SPACE)
		    .append(path);
		RoboHttpUtils.decorateByNewLine(sb);
		return sb.toString().getBytes();
	}
}
