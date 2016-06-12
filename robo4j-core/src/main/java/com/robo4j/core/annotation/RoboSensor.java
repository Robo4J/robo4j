package com.robo4j.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation is used to configure specific
 * robo sensor connected to the platform
 *
 * Created by miroslavkopecky on 04/05/16.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RoboSystem
public @interface RoboSensor {
    String value() default "";
}
