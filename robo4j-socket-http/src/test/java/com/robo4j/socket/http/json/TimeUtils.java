package com.robo4j.socket.http.json;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class TimeUtils {

	static void printTimeDiffNano(String message, long start) {

        System.out.println(String.format("message: %s duration: %d", message, System.nanoTime() - start));
    }
}
