package com.robo4j.core.system.util;

import com.robo4j.commons.enums.LegoSystemEnum;

/**
 * Created by miroslavkopecky on 04/05/16.
 */
public final class SystemUtil {

    public static <Source extends LegoSystemEnum<Type>, Type> Type getType(Source sourceEnum){
        return sourceEnum.getType();
    }

}
