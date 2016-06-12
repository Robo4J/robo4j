package com.robo4j.core.lego;

import java.util.Map;

/**
 * Created by miroslavkopecky on 25/04/16.
 */
public interface LegoBrickProperties {

    Map<String, String> getBricks();
    String getCorePackage();
    String getCommandPackage();
    String getEnginePackage();

}
