/*
 * Copyright (c) 2014, 2017, Marcus Hirt, Miroslav Wengner
 *
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.core.async;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class ProcessorStudyTests {

    private static final int SYNC_MESSAGES = 8;

    @Test
    public void testSynchronousProcessor(){
        MessageProcessor processor = new MessageProcessor();
        List<String> result = new ArrayList<>();
        for(int i=0; i<SYNC_MESSAGES; i++){
            String message = "_test_" + i + "_message";
            result.add(processor.processSync(message));
        }

        Assert.assertEquals(SYNC_MESSAGES, result.size());

    }

    @Test
    public void testAsyncMessagesProcessor() throws Exception{
        MessageProcessor processor = new MessageProcessor();

        List<String> tickets = new ArrayList<>();
        for(int i=0; i<SYNC_MESSAGES; i++){
            String message = "_test_" + i + "_message";
            tickets.add(processor.processAsync(message));
        }

        int numberAllAsync = processor.getAllAsync();
        Map<String, String> ticketAndResultMap = processor.getTicketAndResults();
        Assert.assertEquals(SYNC_MESSAGES, numberAllAsync);
    }

    @Test
    public void testAsyncAndSyncMessagesProcessor(){

        MessageProcessor processor = new MessageProcessor();
        List<String> tickets = new ArrayList<>();
        List<String> syncResult = new ArrayList<>();
        for(int i=0; i<SYNC_MESSAGES; i++){
            String message = "_test_" + i + "_message";
            syncResult.add(processor.processSync(message));
            tickets.add(processor.processAsync(message));
        }

        Assert.assertEquals(SYNC_MESSAGES, syncResult.size());
        Assert.assertEquals(SYNC_MESSAGES, processor.getAllAsync());

    }



}
