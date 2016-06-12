package com.robo4j.core.util;

import java.io.InputStream;

/**
 *
 * Class related utils
 *
 * Created by miroslavkopecky on 23/05/16.
 */
public class RoboClassUtils {

    /**
     * Return current Robo4j ClassLoader
     * preparetion for multiple classloader usage
     * @return System ClassLoader
     */
    public static ClassLoader getClassLoader(){
        return RoboClassLoader.getInstance().getClassLoader();
    }

    public static InputStream getResource(String name){
        return RoboClassLoader.getInstance().getResource(name);
    }

}
