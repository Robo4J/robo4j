package com.robo4j.core.io;

import java.io.InputStream;

/**
 *
 * interface represents resources
 *
 * Created by miroslavkopecky on 23/05/16.
 */
public interface Resource {

    InputStream getInputStream();

    boolean isReading();
}
