package com.robo4j.core.bridge.command;

import com.robo4j.core.bridge.command.cache.BatchCommand;

/**
 * Contains only basic platform commands
 *
 * Created by miroslavkopecky on 18/04/16.
 */
public class SimpleCommand implements BasicCommand, BatchCommand {

    private String name;
    private String batch;

    public SimpleCommand(String name, String batch) {
        this.name = name;
        this.batch = batch;
    }

    @Override
    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getBatch() {
        return batch;
    }

    @Override
    public void setBatch(String batch) {
        this.batch = batch;
    }
}
