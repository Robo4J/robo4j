/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This AbstractClient.java is part of robo4j.
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

package com.robo4j.brick.client;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Created by miroslavkopecky on 09/06/16.
 */
public abstract class AbstractClient<FutureType> {

    private static ExecutorService executors;

    protected AbstractClient() {
        executors = new ClientHTTPExecutor();
    }

    protected Future<FutureType> submit(Callable<FutureType> task){
        return executors.submit(task);
    }

    protected void end(){
        executors.shutdown();
    }
}
