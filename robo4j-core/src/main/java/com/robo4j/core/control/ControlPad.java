/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This ControlPad.java is part of robo4j.
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

package com.robo4j.core.control;

import com.robo4j.commons.agent.GenericAgent;
import com.robo4j.commons.command.CommandParsed;
import com.robo4j.commons.command.CommandProperties;
import com.robo4j.commons.command.CommandTypeEnum;
import com.robo4j.commons.command.FrontHandCommandEnum;
import com.robo4j.commons.command.GenericCommand;
import com.robo4j.commons.control.RoboSystemConfig;
import com.robo4j.commons.util.CommandUtil;
import com.robo4j.core.bridge.BridgeControl;
import com.robo4j.core.bridge.BridgeUtils;
import com.robo4j.core.bridge.command.AdvancedCommand;
import com.robo4j.core.bridge.command.BasicCommand;
import com.robo4j.core.bridge.command.ComplexCommand;
import com.robo4j.core.bridge.command.SimpleCommand;
import com.robo4j.core.bridge.command.cache.BatchCommand;
import com.robo4j.core.bridge.task.BrickCommandProviderFuture;
import com.robo4j.core.bridge.task.BridgeCommandConsumer;
import com.robo4j.core.bridge.task.BridgeCommandProducer;
import com.robo4j.core.control.utils.CommandCheckerUtil;
import com.robo4j.core.control.utils.ConnectionUtil;
import com.robo4j.core.guardian.GuardianRunnable;
import com.robo4j.core.io.NetworkUtils;
import com.robo4j.core.lego.LegoBrickPropertiesHolder;
import com.robo4j.core.platform.PlatformProperties;
import com.robo4j.core.platform.command.LegoCommandProperty;
import com.robo4j.core.platform.command.LegoPlatformCommandEnum;
import com.robo4j.core.platform.provider.LegoBrickCommandsProvider;
import com.robo4j.core.sensor.provider.SensorProvider;
import com.robo4j.core.sensor.provider.SensorProviderImpl;
import com.robo4j.lego.control.LegoBrickRemote;
import com.robo4j.lego.control.LegoEngine;
import com.robo4j.lego.control.LegoSensor;
import com.robo4j.lego.control.LegoUnit;
import org.apache.commons.exec.util.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

/**
 * Control pad is responsible to process command from user
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 16.04.2016
 */
public class ControlPad extends BridgeControl {

    private static final Logger logger = LoggerFactory.getLogger(ControlPad.class);
    private static final String CONST_EXIT = "exit";
    private static final String COMMAND_SPLITTER = ",";

    private volatile LegoBrickCommandsProvider legoBrickCommandsProvider;
    private volatile SensorProvider sensorProvider;
    private volatile PlatformProperties properties;     // Engines properties
    private volatile LegoBrickPropertiesHolder legoBrickPropertiesHolder;


    public ControlPad(final String corePackage) {
        super(corePackage);
        properties = new PlatformProperties();
        legoBrickPropertiesHolder = ((RoboSystemProperties)systemCache.get(ControlUtil.SYSTEM_CONFIG)).getProperties();
        initCache(legoBrickPropertiesHolder);
    }

    public Map<String, BatchCommand> getCommandCache(){
        return commandCacheActive() ? MapUtils.copy(commandCache.getCache()) : Collections.EMPTY_MAP;
    }

    public Map<String, LegoEngine> getEngineCache(){
        return engineCacheActive() ? MapUtils.copy(engineCache.getCache()) : Collections.EMPTY_MAP;
    }

    public Map<String, LegoSensor> getSensorCache(){
        return sensorCacheActive() ?  MapUtils.copy(sensorCache.getCache()) : Collections.EMPTY_MAP;
    }

    public Map<String, LegoUnit> getUnitCache(){
        return unitCacheActive() ? MapUtils.copy(unitCache.getCache()) : Collections.EMPTY_MAP;
    }

    public LegoBrickRemote getLegoBrickRemote(){
        if(active.get()){
            return getRemoteBrickFromCache();
        } else {
            throw new ControlException("PLEASE ACTIVATE THE SYSTEM");
        }
    }

    public boolean getConnectionState(){
        try {
            return ConnectionUtil.ping(legoBrickPropertiesHolder.getAddress());
        } catch (IOException e) {
            logger.error("BRICK CONNECTION ERROR= ", e);
            return false;
        }
    }

    public void addCommand(final CommandTypeEnum type, final String name, final String batch){
        switch (type){
            case COMPLEX:
                logger.info("ADD COMMAND type = " + type + " name= " + name + " batch= " + batch);
                addGenericCommand(new ComplexCommand(name, batch));
                break;
            case BATCH:
                logger.info("ADD COMMAND type = " + type + " name= " + name + " batch= " + batch);
                addGenericCommand(new SimpleCommand(name, batch));
                break;
            default:
                throw new ControlException("ADD COMMAND NO SUCH COMMAND  = " + type);
        }
    }

    @SuppressWarnings(value = "unchecked")
    public Map<String, RoboSystemConfig> getSystemCache(){
        return systemCache;
    }

    /* send command to the system */
    public boolean sendCommandLine(final String line){
        for(String commandLine: CommandUtil.getCommandsByTypes(line)){
            if(line.contains(CONST_EXIT)){
                end();
            } else if(CommandCheckerUtil.isCommand(line) == 1) {
                /* to improve */
                final CommandParsed commandParsed = controlCommandsAdapter.properCommandWithPrefix(commandLine);
                batchCommandTypePrint(commandParsed.getType());
            } else {
                logger.info("WRONG COMMAND = " + line);
            }
        }

        return activeThread.get();
    }

    /* basic commands types */
    void sendCommands(final String batch){
        try {
            logger.info("CONQUER COMMANDS: " + batch);
            putCommand(batch);
        } catch (InterruptedException e) {
            throw new ControlException("Conquer COMMAND LINE = ", e);
        }
    }

    void sendHandCommand(final String command){
        try {
            logger.info("FRONT HAND COMMAND : " + command);
            putCommand(command);
        } catch (InterruptedException e) {
            throw  new ControlException("HAND COMMAND LINE = ", e);
        }
    }


    /**
     * Command is taken from the command cache
     * @param batch
     */
    void sendBatchCommand(final String batch){
        String[] commands = batch.trim().split(COMMAND_SPLITTER);
        try {
            putCommand(CONST_EXIT);
            for(String c: commands){
                String seq = c.trim();
                logger.info("BATCH COMMAND ENTER = " + seq);
                if(seq.equals(CONST_EXIT)){
                    putCommand(CONST_EXIT);
                } else {
                    final BatchCommand batchCommand = getGenericCommand(seq);
                    logger.info("FOUND IN CACHE batchCommand= " + batchCommand);
                    if(batchCommand != null) {
                        if(batchCommand instanceof BasicCommand){
                            /* only basics commands move(30),back(30),left(180),right(180) */
                            String command = batchCommand.getBatch();
                            if(!command.isEmpty()){
                                logger.info("BATCH SIMPLE COMMANDS command= " + command);
                                putCommand(command);

                            } else {
                                putCommand(CONST_EXIT);
                                throw new ControlException("NO SUCH BATCH EXITING SYSTEM command: " + command);
                            }
                        }
                        if(batchCommand instanceof AdvancedCommand){
                            for (String line: CommandUtil.getCommandsByTypes(batchCommand.getBatch())){
                                /* advanced command contains sequence basic1;D:move(30),back(20) */
                                //TODO: recursive command detection can be improved
                                if(!line.contains(seq)){
                                    controlCommandsAdapter.properCommandWithPrefix(line);
                                } else {
                                    logger.info("RECURSIVE COMMAND DETECTED = " + seq);
                                }
                            }
                        }
                    }

                }
            }
        } catch (InterruptedException e) {
            throw new ControlException("BATCH COMMAND LINE ERROR = ", e);
        }
    }

    void sendActiveCommand(final String command){
        logger.info("ACTIVE COMMAND = " + command);
        Matcher activeCommand = BridgeUtils.commandActivePattern.matcher(command.trim());

        if(activeCommand.find()){
            LegoPlatformCommandEnum legoCommand = LegoPlatformCommandEnum.getCommand(activeCommand.group(1));
            if(legoCommand != null){
                processActiveCommand(legoCommand, activeCommand.group(2));
            }
        } else {
            logger.info("ACTIVE COMMAND NOT FOUND= " + activeCommand);
        }
    }

    public void setPlatformCycles(String cycles){
        this.properties.setCycles(cycles);
    }

    public String getIpAddress(){
        return legoBrickPropertiesHolder.getAddress();
    }

    public boolean isActive() {
        return active.get();
    }

    public boolean isBrickReachable(){
         return  NetworkUtils.pingBrick(legoBrickPropertiesHolder.getAddress()).get();
    }

    public void setEmergency(boolean emergency){
        this.emergency.set(emergency);
    }

    public SensorProvider getSensorProvider(){
        return sensorProvider;
    }


    @SuppressWarnings(value = "unchecked")
    public boolean activate(){

        //FIXME: need to be mocked isReachable
        try {
            if(!activeThread.get() && isBrickReachable()){

                initBridge();

                legoBrickCommandsProvider = getLegoBrickCommandsProviderWithGuardian(properties);
                sensorProvider = new SensorProviderImpl(getRemoteBrickFromCache());

                activeThread.set(true);
                active.set(true);

                controlCommandsAdapter = new ControlCommandsAdapter(this);

                final GenericAgent agent = getCoreBridgeAgent(
                        new BridgeCommandProducer(activeThread, emergency, commandLineQueue, properties ),
                        new BridgeCommandConsumer(activeThread, legoBrickCommandsProvider,
                                initRoboUnitByName("frontHandUnit")));
                genericAgents.add(agent);

                logger.info("UNIT Cache = " + unitCache);

            } else {
                throw new ControlException("NO SYSTEM AVAILABLE");
            }

            return active.get();
        } catch (Exception e){
            throw new ControlException("ACTIVATE error: ", e);
        }
    }

    //Private Methods
    private LegoBrickRemote getRemoteBrickFromCache(){
        return ((LegoBrickRemote)systemCache.get(ControlUtil.SYSTEM_PROVIDER));
    }

    private LegoUnit initRoboUnitByName(final String name){
        return unitCache.getCache().get(name)
                .init(getRemoteBrickFromCache(), engineCache.getCache(), sensorCache.getCache());
    }

    private void processActiveCommand(final LegoPlatformCommandEnum command, String speed){
        switch (command){
            case MOVE:
                if(emergency.get()){
                    throw new ControlException("MOVE FORWARD CAN'T BE DONE");
                }
            case BACK:
            case LEFT:
            case RIGHT:
                logger.info("ACTIVE COMMAND = " + command + " SPEED = " + speed);
                LegoCommandProperty commandProperty = new LegoCommandProperty("", Objects.isNull(speed) ? 300 : Integer.parseInt(speed));
                legoBrickCommandsProvider.process(command, commandProperty);
                break;
            case STOP:
                legoBrickCommandsProvider.process(LegoPlatformCommandEnum.STOP);
                break;
            case EMERGENCY_STOP:
            case CLOSE:
            case EXIT:
                throw new ControlException("COMMAND IS NOT SUPPORTED HERE= " + command);
            default:
                throw new ControlException("NO SUCH COMMAND");
        }
    }

    private void end(){
        activeThread.set(false);
//        legoBrickCommandsProvider.process(LegoPlatformCommandEnum.EXIT);

        /* turn off all units */
        //TODO: needs to be redesigned
        unitCache.getCache().entrySet().stream()
                .map(Map.Entry::getValue)
                .forEach(e ->  {
                    switch (e.getUnitName()){
                        case  "frontHandUnit":
                            e.process(FrontHandCommandEnum.EXIT);
                            break;
                        case "platformUnit":
                            final CommandProperties properties = () -> 300;
                            final GenericCommand<LegoPlatformCommandEnum> exitCommand =
                                    new GenericCommand<>(properties, LegoPlatformCommandEnum.EXIT, "", 1);
                            e.process(exitCommand);
                            break;
                    }
                });

        coreBusWithSensorDownSequence();
        active.set(false);
    }

    private void coreBusWithSensorDownSequence(){
        sensorBusDown();
        coreBusDown();
        guardianBusDown();
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            throw new ControlException("SHUTDOWN SEQUENCE: ", e);
        }
        coreBusDownNow();
    }

    private void batchCommandTypePrint(final CommandTypeEnum type){
        switch (type){
            case BATCH:
                logger.info("COMMAND BATCH");
                break;
            case DIRECT:
                logger.info("COMMAND DIRECT");
                break;
            case HAND:
                logger.info("COMMAND HAND");
                break;
            case COMPLEX:
                logger.info("COMMAND COMPLEX");
                break;
            case ACTIVE:
                logger.info("COMMAND ACTIVE");
                break;
            default:
                throw new ControlException("NOT SUPPORTED STATE type: " + type);
        }
    }

    /* casches should be initiated  */
    @SuppressWarnings(value = "unchecked")
    private LegoBrickCommandsProvider getLegoBrickCommandsProviderWithGuardian(PlatformProperties properties){

        final BrickCommandProviderFuture future =  new BrickCommandProviderFuture(
                (LegoBrickRemote) getSystemCache().get(ControlUtil.SYSTEM_PROVIDER),
                legoBrickPropertiesHolder, properties, engineCache.getCache(), unitCache.getCache());
        final Future<LegoBrickCommandsProvider> brickFuture = guardianBusSubmit(future);
        try {
            final LegoBrickCommandsProvider result = brickFuture.get();
            guardianBusExecute(new GuardianRunnable(result, activeThread, emergency));
            return result;
        } catch (InterruptedException | ExecutionException e) {
            throw new ControlException("LegoBrick initialisation problem: ", e);
        }
    }

}
