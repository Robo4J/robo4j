/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This AgentCache.java is part of robo4j.
 *
 *     robo4j is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     robo4j is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.robo4j.core.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;

/**
 *
 * AgentCache keeps order
 *
 * Created by miroslavkopecky on 29/05/16.
 */
public class AgentCache extends ArrayBlockingQueue<String> {

    private static final Logger logger = LoggerFactory.getLogger(AgentCache.class);
    private static final int FIXED_SIZE = 10;

    public AgentCache(){
        super(FIXED_SIZE);
    }


    @Override
    public void put(String o){
        if(size() == FIXED_SIZE){
            final String removed = remove();
            logger.info("REMOVED element = " + removed);
        }
        try {
            super.put(o);
        } catch (InterruptedException e) {
            throw new AgentException("CACHE IS CORRUPTED NO SPACE");
        }
    }

}
