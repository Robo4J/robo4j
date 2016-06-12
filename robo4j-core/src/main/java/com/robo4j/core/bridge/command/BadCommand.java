package com.robo4j.core.bridge.command;

import com.robo4j.core.annotation.BatchAnnotation;
import com.robo4j.core.bridge.command.cache.BatchCommand;

/**
 * Created by miroslavkopecky on 18/04/16.
 */
@BatchAnnotation(name = "bad")
public class BadCommand implements BatchCommand {

    private String name;
    private String batch;

    public BadCommand() {
        this.name = "bad";
        this.batch = "move(30),back(30)";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getBatch() {
        return batch;
    }

    @Override
    public void setBatch(String batch) {
        this.batch = batch;
    }

    @Override
    public String toString() {
        return "BadCommand{" +
                "batch='" + batch + '\'' +
                '}';
    }
}
