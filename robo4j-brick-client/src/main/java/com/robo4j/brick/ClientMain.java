/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This ClientMain.java is part of robo4j.
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

package com.robo4j.brick;

import com.robo4j.brick.client.AbstractClient;
import com.robo4j.brick.client.io.ClientException;
import com.robo4j.brick.client.request.RequestProcessorCallable;
import com.robo4j.brick.util.ConstantUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 *
 * Robo4j.io program for Lego EV3
 * program uses Lego EV3 internal resources
 *
 * Created by miroslavkopecky on 24/05/16.
 */
public class ClientMain extends AbstractClient {

    private static final int PORT = 8022;

    public static void main(String... args){
        new ClientMain();
    }

    @SuppressWarnings(value = "unchecked")
    private ClientMain() {

        System.out.println("Robo4j.io STARTS...");
        boolean active = true;
        try(ServerSocket server = new ServerSocket(PORT)){
            while(active){
                Socket request = server.accept();
                Future<String> result = submit(new RequestProcessorCallable(request));
                if(result.get().equals(ConstantUtil.EXIT)){
                    active = false;
                }
            }
        } catch (InterruptedException | ExecutionException | IOException e){
            throw new ClientException("SERVER FAILED", e);
        }
        end();
        System.out.println("FINAL END");
        System.exit(0);

    }
}
