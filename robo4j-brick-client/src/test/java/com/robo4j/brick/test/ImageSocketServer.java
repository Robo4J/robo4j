/*
 * Copyright (C)  2016. Miroslav Kopecky
 * This ImageSocketServer.java  is part of robo4j.
 *
 *  robo4j is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  robo4j is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.brick.test;

import com.robo4j.brick.client.io.ClientException;
import com.robo4j.brick.client.io.ResourceLoader;
import com.robo4j.brick.logging.SimpleLoggingUtil;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Miro Kopecky (@miragemiko)
 * @since 06.10.2016
 */
public class ImageSocketServer {

    private static final int PORT = 8022;
    private static final String IMAGE_1 = "robo_sample1.jpg";
    private ResourceLoader resourceLoader;
    private boolean active;

    public static void main(String[] args) {
        SimpleLoggingUtil.debug(ImageSocketServer.class, "Start Server PORT: " + PORT);
        ImageSocketServer server = new ImageSocketServer();
        server.start();
    }


    private ImageSocketServer(){
        this.resourceLoader = new ResourceLoader();
        this.active = false;
    }

    /**
     * currently provides only image
     */
    private void start(){
        SimpleLoggingUtil.debug(getClass(), "SocketServer has started");
        try(ServerSocket server = new ServerSocket(PORT)){
            this.active = true;
            while (active){
                try (final Socket conn = server.accept();
                     final OutputStream out =  conn.getOutputStream()) {
                    SimpleLoggingUtil.debug(getClass(), "Image generated");
                    out.write(getImage());
                    out.flush();
                    SimpleLoggingUtil.print(getClass(), "Image sent");

                }
            }

        } catch (IOException e){
            throw new ClientException("SERVER FAILED", e);
        }
    }


    private byte[] getImage(){
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try(final InputStream in = resourceLoader.getInputStream(IMAGE_1).getInputStream()){
            int imageCh;
            while((imageCh = in.read()) != -1){
                baos.write(imageCh);
            }
            return baos.toByteArray();
        } catch (IOException e){
            throw new ClientException("IMAGE READ FAILED", e);
        }

    }
}
