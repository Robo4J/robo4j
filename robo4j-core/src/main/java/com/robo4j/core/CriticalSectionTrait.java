package com.robo4j.core;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotate the  with this annotation to declare that the unit will get messages delivered 
 * one at a time.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface CriticalSectionTrait {

}
