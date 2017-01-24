package com.robo4j.core;

import java.util.concurrent.Future;

public interface RoboReference<T> {
	/**
	 * Sends a message to this RoboUnit.
	 * 
	 * @param message
	 *            the message to send.
	 * @return the RoboUnit specific response.
	 */
	<R> Future<RoboResult<T, R>> sendMessage(Object message);
}
