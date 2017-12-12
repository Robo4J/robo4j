package com.robo4j.socket.http.message;

import com.robo4j.socket.http.util.HttpDenominator;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public interface HttpMessage {
    HttpDenominator getDenominator();
}
