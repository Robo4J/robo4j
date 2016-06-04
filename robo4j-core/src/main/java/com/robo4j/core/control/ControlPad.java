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

import com.robo4j.core.agent.RoboAgent;
import com.robo4j.core.bridge.BridgeControl;
import com.robo4j.core.bridge.command.AdvancedCommand;
import com.robo4j.core.bridge.command.BasicCommand;
import com.robo4j.core.bridge.command.CommandParsed;
import com.robo4j.core.bridge.command.CommandTypeEnum;
import com.robo4j.core.bridge.command.CommandUtil;
import com.robo4j.core.bridge.command.ComplexCommand;
import com.robo4j.core.bridge.command.SimpleCommand;
import com.robo4j.core.bridge.command.cache.BatchCommand;
import com.robo4j.core.bridge.task.BrickCommandProviderFuture;
import com.robo4j.core.bridge.task.BridgeCommandConsumer;
import com.robo4j.core.bridge.task.BridgeCommandProducer;
import com.robo4j.core.control.utils.CommandCheckerUtil;
import com.robo4j.core.control.utils.ConnectionUtil;
import com.robo4j.core.fronthand.LegoFrontHandProvider;
import com.robo4j.core.fronthand.LegoFrontHandProviderImp;
import com.robo4j.core.fronthand.command.FrontHandCommandEnum;
import com.robo4j.core.guardian.GuardianRunnable;
import com.robo4j.core.io.NetworkUtils;
import com.robo4j.core.lego.LegoBrickPropertiesHolder;
import com.robo4j.core.lego.LegoBrickRemote;
import com.robo4j.core.platform.PlatformProperties;
import com.robo4j.core.platform.command.LegoPlatformCommandEnum;
import com.robo4j.core.platform.provider.LegoBrickCommandsProvider;
import com.robo4j.core.sensor.provider.SensorProvider;
import com.robo4j.core.sensor.provider.SensorProviderImpl;
import org.apache.commons.exec.util.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Control pad is responsible to process command from user
 *
 * Created by miroslavkopecky on 16/04/16.
 */
public class ControlPad extends BridgeControl {

    private static final Logger logger = LoggerFactory.getLogger(ControlPad.class);
    private static final String CONST_EXIT = "exit";
    private static final String COMMAND_SPLITTER = ",";

    private volatile LegoBrickCommandsProvider legoBrickCommandsProvider;
    private volatile LegoFrontHandProvider legoFrontHandProvider;
    private volatile SensorProvider sensorProvider;
    private volatile PlatformProperties properties;     // Engines properties
    private volatile LegoBrickRemote legoBrickRemote;
    private volatile LegoBrickPropertiesHolder legoBrickPropertiesHolder;


    public ControlPad(final String corePackage) {
        super(corePackage);
        properties = new PlatformProperties();
        legoBrickRemote = ((LegoBrickRemote)systemCache.get(ControlUtil.SYSTEM_PROVIDER));
        legoBrickPropertiesHolder = ((RoboSystemProperties)systemCache.get(ControlUtil.SYSTEM_CONFIG)).getProperties();

        initCache(legoBrickPropertiesHolder);
    }

    public Map<String, BatchCommand> getCommandCache(){
        return commandCacheActive() ? MapUtils.copy(commandCache.getCache()) : Collections.EMPTY_MAP;
    }

    public Map<String, LegoEngine> getEngineCache(){
        return engineCacheActive() ? MapUtils.copy(engineCache.getCache()) : Collections.EMPTY_MAP;
    }

    public LegoBrickRemote getLegoBrickRemote(){
        if(active.get()){
            return legoBrickRemote;
        } else {
            throw new ControlException("PLEASE ACTIVATE THE SYSTEM");
        }
    }

    public boolean getConnectionState(){
        try {
            return ConnectionUtil.ping(legoBrickPropertiesHolder.getAddress());
        } catch (IOException e) {
           throw new ControlException("BRICK CONNECTION ERROR= ", e);
        }
    }

    public LegoBrickPropertiesHolder getLegoBrickPropertiesHolder(){
        return legoBrickPropertiesHolder;
    }

    public void addCommand(final CommandTypeEnum type, final String name, final String batch){
        switch (type){
            case COMPLEX:
                addGenericCommand(new ComplexCommand(name, batch));
                break;
            case BATCH:
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
            putCommand(batch);
        } catch (InterruptedException e) {
            throw new ControlException("Conquer COMMAND LINE = ", e);
        }
    }

    void sendHandCommand(final String command){
        try {
            putCommand(command);
        } catch (InterruptedException e) {
            throw  new ControlException("HAND COMMAND LINE = ", e);
        }
    }


    void sendBatchCommand(final String batch){
        String[] commands = batch.trim().split(COMMAND_SPLITTER);
        try {
            putCommand(CONST_EXIT);
            for(String c: commands){
                String seq = c.trim();
                if(seq.equals(CONST_EXIT)){
                    putCommand(CONST_EXIT);
                } else {
                    final BatchCommand batchCommand = getGenericCommand(seq);
                    if(batchCommand != null) {
                        if(batchCommand instanceof BasicCommand){
                            /* only basics commands move(30),back(30),left(180),right(180) */
                            String command = batchCommand.getBatch();
                            if(!command.isEmpty()){
                                putCommand(command);

                            } else {
                                putCommand(CONST_EXIT);
                                throw new ControlException("NO SUCH BATCH EXITING SYSTEM command: " + command);
                            }
                        }
                        if(batchCommand instanceof AdvancedCommand){
                            for (String line: CommandUtil.getCommandsByTypes(batchCommand.getBatch())){

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
        LegoPlatformCommandEnum legoCommand = LegoPlatformCommandEnum.getCommand(command);
        if(legoCommand != null){
            processCommand(legoCommand);
        }
    }

    public void setIpAddress(String ipAddress){
        this.legoBrickPropertiesHolder.setAddress(ipAddress);
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

        try {
            if(!activeThread.get() && isBrickReachable()){

                initBridge();

                legoBrickCommandsProvider = getLegoBrickCommandsProviderWithGuardian(properties);
                sensorProvider = new SensorProviderImpl(legoBrickRemote);
                legoFrontHandProvider = new LegoFrontHandProviderImp(legoBrickRemote, engineCache.getCache());

                activeThread.set(true);
                active.set(true);

                controlCommandsAdapter = new ControlCommandsAdapter(this);

                /* need to be rewritten Agent PRODUCER produces messages ->  consumer needs them*/
                bridgeCommandProducer = new BridgeCommandProducer(emergency, activeThread, commandLineQueue, properties );
                bridgeCommandConsumer = new BridgeCommandConsumer(legoBrickCommandsProvider, legoFrontHandProvider, activeThread);

                final RoboAgent agent = getCoreBridgeAgent(bridgeCommandProducer, bridgeCommandConsumer);

                roboAgents.add(agent);

            } else {
                throw new ControlException("NO SYSTEM AVAILABLE");
            }

            return active.get();
        } catch (Exception e){
            throw new ControlException("ACTIVATE error: ", e);
        }
    }

    //Private Methods
    private void processCommand(final LegoPlatformCommandEnum command){
        try {
            switch (command){
                case INIT:
                case EMERGENCY_STOP:
                    logger.info("NOT IMPLEMENTED YET= " + command);
                    break;
                case STOP:
                    legoBrickCommandsProvider.process(LegoPlatformCommandEnum.STOP);
                    break;
                case CLOSE:
                case EXIT:
                    activeThread.set(false);
                    legoBrickCommandsProvider.process(LegoPlatformCommandEnum.EXIT);
                    legoFrontHandProvider.process(FrontHandCommandEnum.EXIT);
                    coreBusWithSensorDownSequence();
                    active.set(false);
                    break;
                case MOVE:
                    if(emergency.get()){
                        throw new ControlException("MOVE FORWARD CAN'T BE DONE");
                    }
                case BACK:
                case LEFT:
                case RIGHT:
                    legoBrickCommandsProvider.process(command);
                    break;
                case MOVE_CYCLES:
                case MOVE_DESTINACE:
                case BACK_CYCLES:
                case BACK_DESTINACE:
                case LEFT_CYCLES:
                case RIGHT_CYCLES:
                    legoBrickCommandsProvider.processWithProperty(command, properties.getCycleCommandProperty());
                    break;
                default:
                    throw new ControlException("NO SUCH COMMAND");
            }
        } catch (RemoteException | InterruptedException e) {
            throw new ControlException(e.getMessage(), e);
        }
    }

    private void end(){
        try {
            activeThread.set(false);
            legoBrickCommandsProvider.process(LegoPlatformCommandEnum.EXIT);
            legoFrontHandProvider.process(FrontHandCommandEnum.EXIT);
            coreBusWithSensorDownSequence();
            active.set(false);
        } catch (RemoteException | InterruptedException e) {
            throw new ControlException("EXIT FAILURE: ", e);
        }
    }

    private void coreBusWithSensorDownSequence() throws InterruptedException{
        sensorBusDown();
        coreBusDown();
        guardianBusDown();
        TimeUnit.SECONDS.sleep(5);
        coreBusDownNow();
    }

    private void batchCommandTypePrint(final CommandTypeEnum type){
        switch (type){
            case BATCH:
                logger.debug("COMMAND BATCH");
                break;
            case DIRECT:
                logger.debug("COMMAND DIRECT");
                break;
            case HAND:
                logger.debug("COMMAND HAND");
                break;
            case COMPLEX:
                logger.debug("COMMAND COMPLEX");
                break;
            case ACTIVE:
                logger.debug("COMMAND ACTIVE");
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
                legoBrickPropertiesHolder, properties, engineCache.getCache());
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
