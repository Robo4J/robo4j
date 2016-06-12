package com.robo4j.core.bridge;

import java.util.regex.Pattern;

/**
 * Created by miroslavkopecky on 25/04/16.
 */
public final class BridgeUtils {

    protected static final String BUS_GUARDIAN_BUS = "guardianBus";
    protected static final String BUS_CORE_BUS = "coreBus";
    protected static final String BUS_SENSOR_BUS = "sensorBus";

    /* used to pars the commands */
    public static final Pattern commandLinePattern = Pattern.compile("(^[a-z]{4,5})\\(([-]?[0-9]+)\\)");
    public static final String BUS_COMMAND_CONSUMER = "commandConsumerBus";

}

