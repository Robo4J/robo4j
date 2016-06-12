package com.robo4j.core.control;

import java.util.Map;

/**
 * Created by miroslavkopecky on 23/04/16.
 */
public interface ControlSystem <Configuration extends RoboSystemConfig> extends DefaultSystemConfig{

    String PACKAGE_CORE = "com.robo4j.core";
    String METHOD_CONFIG = "load";
    String METHOD_PROVIDER = "getInstance";
    String METHOD_PROPERTIES_BRICKS = "getBricks";
    String METHOD_PROPERTIES_CORE_PACKAGE = "getCorePackage";
    String METHOD_PROPERTIES_COMMAND_PACKAGE = "getCommandPackage";
    int REQURED_CONFIGURATION = 2;
    Map<String, Configuration> getSystemCache();

}
