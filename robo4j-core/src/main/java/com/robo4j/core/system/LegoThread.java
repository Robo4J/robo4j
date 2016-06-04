/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This LegoThread.java is part of robo4j.
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

package com.robo4j.core.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Created by miroslavkopecky on 26/09/14.
 *
 * Lego Thread stores the creation date, the start date and the finish date of the Thread. It provides a
 * method that calculates the execution time fo the thread. Overrides the toString method to return
 * information about the creationDate and the execution time of the thread
 *
 */
public class LegoThread extends Thread{

    private static final Logger logger = LoggerFactory.getLogger(LegoThread.class);

    /**
     * Creation Date
     */
    private Date creationDate;

    /**
     * Start date fo the thread
     */
    private Date startDate;

    /**
     * Finish date of the thread
     */
    private Date finishDate;

    LegoThread(Runnable target, String name){
        super(target, name);
        setCreationDate();
    }

    @Override
    public void run(){
        setStartDate();
        super.run();
        setFinishDate();
        logger.debug("Thread= " + toString());
    }

    //Private Methods
    private long getExecutionTime(){
        return finishDate.getTime() - startDate.getTime();
    }

    private void setCreationDate() {
        this.creationDate = new Date();
    }

    private void setStartDate() {
        this.startDate = new Date();
    }

    private void setFinishDate() {
        this.finishDate = new Date();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getName());
        builder.append(": ").append(" CreationDate= ").append(creationDate);
        builder.append(" : Running Time= ").append(getExecutionTime()).append(" Milliseconds");
        return builder.toString();
    }
}
