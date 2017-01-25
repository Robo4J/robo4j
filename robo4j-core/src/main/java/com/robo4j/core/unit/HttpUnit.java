package com.robo4j.core.unit;

import com.robo4j.core.LifecycleState;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboUnit;
import com.robo4j.core.client.enums.RequestStatusEnum;
import com.robo4j.core.client.request.RequestProcessorCallable;
import com.robo4j.core.logging.SimpleLoggingUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author Miro Wengner (@miragemiko)
 * @author Marcus Hirt (@hirt)
 * @since 24.01.2017
 */
public class HttpUnit extends RoboUnit<Object>{

    private Set<LifecycleState> activeStates = EnumSet.of(LifecycleState.STARTED, LifecycleState.STARTING);
    private int port;
    private String target;
    private ExecutorService executor;

    public HttpUnit(RoboContext context, String id){
        super(context, id);
    }

    @Override
    public void initialize(Map<String, String> properties) throws Exception {
        target = properties.get("target");
        port = Integer.valueOf(properties.get("port"));
        executor = Executors.newSingleThreadExecutor();
        setState(LifecycleState.INITIALIZED);

    }

    @Override
    public void start(){
        setState(LifecycleState.STARTING);
        try (ServerSocket server = new ServerSocket(port)) {
            setState(LifecycleState.STARTED);
            while (activeStates.contains(getState())) {
                Socket request = server.accept();
                Future<RequestStatusEnum> result = executor.submit(new RequestProcessorCallable(request));
                SimpleLoggingUtil.debug(getClass(), "RESULT result: " + result.get());
                switch (result.get()) {
                    case ACTIVE:
                        break;
                    case NONE:
                        break;
                    case EXIT:
                        SimpleLoggingUtil.debug(getClass(), "IS EXIT: " + result.get());
                        setState(LifecycleState.STOPPING);
                        break;
                    default:
                        break;
                }
            }
            setState(LifecycleState.STOPPED);
        } catch (InterruptedException | ExecutionException | IOException e) {
            SimpleLoggingUtil.print(getClass(), "SERVER CLOSED");
        }



    }
}
