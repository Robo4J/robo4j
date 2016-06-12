package com.robo4j.core.control.utils;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by miroslavkopecky on 03/05/16.
 */
public final class ConnectionUtil {
    private static final int TIMEOUT = 2000;
    /* connect IP address */
    public static boolean ping(final String address) throws IOException {
        return InetAddress.getByName(address).isReachable(TIMEOUT);
    }

}
