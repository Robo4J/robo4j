package com.robo4j.socket.http.units;

import java.util.HashMap;
import java.util.Map;

/**
 * PathRegister is dedicated to the specific instance of HttpServer Unit.
 * It contains the collection of the units accessible over the https socket
 * {@link HttpServerUnit}
 *
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class HttpPathRegister {

    private static final int DEFAULT_VALUE_0 = 0;
    private static final int DEFAULT_VALUE_1 = 1;
    private static final String PATH_UNITS = "units";


    private final Map<String, RoboUriInfo> pathMethodMap = new HashMap<>();

    public HttpPathRegister() {
    }


}
